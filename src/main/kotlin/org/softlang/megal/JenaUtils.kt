package org.softlang.megal

import org.apache.jena.graph.*
import org.apache.jena.graph.impl.GraphBase
import org.apache.jena.query.*
import org.apache.jena.rdf.model.*
import org.apache.jena.util.iterator.ExtendedIterator

fun String.asNode(literal: Boolean = false) =
		if (literal)
			NodeFactory.createLiteral(this)
		else
			NodeFactory.createURI(this)

infix fun FrontsNode.via(predicate: FrontsNode) = PendingTriple(asNode(), predicate.asNode())
infix fun Node.via(predicate: FrontsNode) = PendingTriple(this, predicate.asNode())
infix fun String.via(predicate: FrontsNode) = PendingTriple(asNode(), predicate.asNode())

infix fun FrontsNode.via(predicate: Node) = PendingTriple(asNode(), predicate)
infix fun Node.via(predicate: Node) = PendingTriple(this, predicate)
infix fun String.via(predicate: Node) = PendingTriple(asNode(), predicate)

infix fun FrontsNode.via(predicate: String) = PendingTriple(asNode(), predicate.asNode())
infix fun Node.via(predicate: String) = PendingTriple(this, predicate.asNode())
infix fun String.via(predicate: String) = PendingTriple(asNode(), predicate.asNode())

data class PendingTriple(val subject: Node, val predicate: Node) {
	infix fun to(`object`: FrontsNode) = Triple(subject, predicate, `object`.asNode())
	infix fun to(`object`: Node) = Triple(subject, predicate, `object`)
	infix fun to(`object`: String) = Triple(subject, predicate, `object`.asNode())
}

operator fun Graph.plusAssign(triple: Triple) {
	add(triple)
}

operator fun Model.plusAssign(statement: Statement) {
	add(statement)
}


fun Model.use(block: Model.() -> Triple): Statement =
		asStatement(block())

fun Model.res(uri: String) = getResource(uri)

fun Model.prop(uri: String) = getProperty(uri)

operator fun Statement.component1(): Resource = subject
operator fun Statement.component2(): Property = predicate
operator fun Statement.component3(): RDFNode = `object`

val Iterable<Statement>.subjects: Iterable<Resource> get() = map { it.subject }
val Iterable<Statement>.predicates: Iterable<Property> get() = map { it.predicate }
val Iterable<Statement>.objects: Iterable<RDFNode> get() = map { it.`object` }
val ExtendedIterator<Statement>.subjects: ExtendedIterator<Resource> get() = mapWith { it.subject }
val ExtendedIterator<Statement>.predicates: ExtendedIterator<Property> get() = mapWith { it.predicate }
val ExtendedIterator<Statement>.objects: ExtendedIterator<RDFNode> get() = mapWith { it.`object` }

fun Model.list(sub: Resource? = null, pred: Property? = null, obj: RDFNode? = null) =
		listStatements(SimpleSelector(sub, pred, obj))


fun Model.reificationGraph() = object : GraphBase() {
	override fun graphBaseFind(trip: Triple) =
			nonReified(trip).andThen(reified(trip))


	private fun nonReified(trip: Triple) =
			this@reificationGraph.graph.find(trip)

	private fun reified(trip: Triple) =
			this@reificationGraph.listReifiedStatements()
					.mapWith { it.statement }
					.mapWith { Triple(it.subject.asNode(), it.predicate.asNode(), it.`object`.asNode()) }
					.filterKeep { trip.matches(it) }
}


fun Model.openQuery(query: Query, initialBinding: QuerySolution) =
		QueryExecutionFactory.create(query, this, initialBinding)

fun Model.openQuery(query: Query, vararg assignments: Pair<String, RDFNode>) =
		openQuery(query, QuerySolutionMap().apply { for (a in assignments) add(a.first, a.second) })

fun <T> QueryExecution.selectIn(block: QueryExecution.(ResultSet) -> T) = block(execSelect())

fun <T> QueryExecution.askIn(block: QueryExecution.(Boolean) -> T) = block(execAsk())
