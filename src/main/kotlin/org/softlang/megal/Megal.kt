package org.softlang.megal

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.sparql.graph.GraphFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import org.softlang.megal.grammar.MegalLexer
import org.softlang.megal.grammar.MegalParser
import org.softlang.megal.grammar.MegalParser.*
import java.io.File

fun parseModel(stream: ANTLRInputStream) =
		MegalParser(CommonTokenStream(MegalLexer(stream))).model()

fun parseModel(file: File) = parseModel(ANTLRInputStream(file.bufferedReader()))

fun parseModel(string: String) = parseModel(ANTLRInputStream(string))

val ParserRuleContext.loc: String get() = "s${sourceInterval.a}e${sourceInterval.b}"

val ModelContext.imports: Set<String> get() = imp().map { it.ID().text }.toSet()

data class Megamodel(val graph: Graph, val imports: Set<String>, val model: ModelContext) {

	companion object {
		val URI = "http://softlang.org/megal/syntax#"

		val to = ResourceFactory.createProperty("${URI}to")!!

		val key = ResourceFactory.createProperty("${URI}key")!!

		val value = ResourceFactory.createProperty("${URI}value")!!

		val argument = ResourceFactory.createProperty("${URI}argument")!!

		val unresolved = ResourceFactory.createResource("${URI}Unresolved")!!
	}


	/**
	 * Constructs an unresolved megamodel from [model], uses the default graph
	 */
	constructor(model: ModelContext) : this(GraphFactory.createDefaultGraph(), model)

	/**
	 * Constructs an unresolved megamodel from [model] stored in [graph]
	 */
	constructor(graph: Graph, model: ModelContext) : this(graph, model.imports, model) {
		for (stm in model.stm())
			stm.process()
	}

	private val localUri: String get() {
		val id = model.ID().text
		val str = model.STR()?.text?.unescape()

		return str?.replace("\$name", id) ?: "http://softlang.org/megal/instances/$id#"
	}

	private fun TermContext.process(): Node {
		// Fix potential values
		val sym = SYM()?.text
		val id = ID()?.text
		val str = STR()?.text?.unescape()

		// Decide on value states
		return when {
		// When ID, make a new unresolved node
			id != null -> {
				val node = id.asNode()
				graph += node via RDF.type to unresolved
				return node
			}
		// When symbol, find the appropriate property
			sym == ":->" -> to
			sym == "|->" -> to
			sym == "->" -> to
			sym == "<" -> RDFS.subClassOf
			sym == ":" -> RDF.type
			sym == "=" -> RDFS.seeAlso

		// For strings, create URI
			str != null -> return str.asNode()
			else -> throw IllegalArgumentException()
		}.asNode()
	}

	/**
	 * TODO Implement
	 */
	private fun String.unescape() = substring(1, length - 1).replace("\\'", "'")

	private fun FuncContext.process(): Node {
		val name = "_:$loc"

		// Connect arguments TODO Sequence
		for (s in node())
			graph += name via argument to s.process()

		graph += name via RDFS.member to term().process()

		return name.asNode()
	}

	private fun StmsContext.process(): Node {
		val name = "_:$loc"

		// Connect contained statements TODO Sequence
		for (stm in stm())
			graph += stm.process() via RDFS.member to name

		return name.asNode()
	}

	private fun NodeContext.process(): Node =
			if (term() != null)
				term().process()
			else if (func() != null)
				func().process()
			else if (stms() != null)
				stms().process()
			else throw IllegalArgumentException()

	private fun AnnContext.process(): Node {
		val name = "_:$loc"

		graph += name via key to ANN().text.asNode(literal = true)

		if (node() != null)
			graph += name via value to node().process()

		return name.asNode()
	}

	private fun StmContext.process(): Node {
		val name = "_:$loc"

		// Connect annotations TODO Sequence
		for (ann in ann())
			graph += name via RDFS.comment to ann.process()

		// Set type of statement and add participants
		graph += name via RDF.type to RDF.Statement
		graph += name via RDF.subject to node(0).process()
		graph += name via RDF.predicate to node(1).process()
		graph += name via RDF.`object` to node(2).process()

		// Interpret as node
		return name.asNode()
	}
}