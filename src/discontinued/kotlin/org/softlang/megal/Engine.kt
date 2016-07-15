package org.softlang.megal.playground

import org.apache.jena.atlas.data.ThresholdPolicyFactory
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner
import org.apache.jena.sparql.graph.GraphFactory
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import kotlin.system.measureNanoTime


fun main(args: Array<String>) {
	Engine().run()
}

class Engine {
	val syn = "http://softlang.org/2016/07/megal-syntax#"

	// Rules, dependent
	val rules = """
@prefix rdf: <${RDF.uri}>.
@prefix rdfs: <${RDFS.uri}>.
@prefix syn: <$syn>.

partOfPlus: (?a rdf:type syn:Artifact)
	(?b rdf:type syn:Artifact)
	(?c rdf:type syn:Artifact)
	(?a syn:partOf ?b)
	(?b syn:partOf ?c)

->  (?a syn:partOf ?c).
""".rules()

	// Query example
	val queryTest = """
prefix rdf: <${RDF.uri}>
prefix rdfs: <${RDFS.uri}>
prefix syn: <$syn>

select ?x ?p ?y
where {
	?t1 rdfs:subClassOf syn:Entity.
	?t2 rdfs:subClassOf syn:Entity.
	?x rdf:type ?t1.
	?y rdf:type ?t2.

	?p rdfs:domain ?d.
	?t1 rdfs:subClassOf ?d.

	?p rdfs:range ?r.
	?t2 rdfs:subClassOf ?r.
}
""".query()

	fun run() {
		val default = ModelFactory.createModelForGraph(GraphFactory.createDataBagGraph(ThresholdPolicyFactory.count(100)))
		default.configurePrelude()
		default.configureInput()

		// Do RDFS transformations
		val rdfs = ModelFactory.createRDFSModel(default)

		// Apply reasoning per rules
		val reasoner = GenericRuleReasoner(rules)
		val inf = ModelFactory.createInfModel(reasoner, rdfs)

		// Process inferred model
		inf.processModel()
	}

	private fun Model.proposeSubject(rel: RDFNode, right: RDFNode) {
		// Exec query
		for (s in select(queryTest, "p" to rel, "y" to right)) {
			val left = s["x"]
			println("<<$left>> $rel $right")
		}
	}

	private fun Model.proposeProperty(left: RDFNode, right: RDFNode) {
		// Exec query
		for (s in select(queryTest, "x" to left, "y" to right)) {
			val rel = s["p"]
			println("$left <<$rel>>  $right")
		}
	}


	private fun Model.proposeObject(left: RDFNode, rel: RDFNode) {
		// Exec query
		for (s in select(queryTest, "x" to left, "p" to rel)) {
			val right = s["y"]
			println("$left $rel <<$right>>")
		}
	}

	private fun InfModel.processModel() {
		// Propose some stuff
		val xmlFile = getResource(syn + "xmlFile")
		val nesting = getResource(syn + "nesting")
		val uses = getProperty(syn + "uses")
		val partOf = getProperty(syn + "partOf")

		for (s in listStatements(select(pred = partOf)))
			println(s)

		measureNanoTime {
			proposeSubject(uses, nesting)
		}.apply { println("Proposal took: ${this / 1e+9} s") }

		measureNanoTime {
			proposeObject(xmlFile, uses)
		}.apply { println("Proposal took: ${this / 1e+9} s") }

		measureNanoTime {
			proposeProperty(xmlFile, nesting)
		}.apply { println("Proposal took: ${this / 1e+9} s") }
	}

	private fun Model.configureInput() {
		// XML Facts
		val fragment = getResource(syn + "Fragment")
		val artifact = getResource(syn + "Artifact")
		val concept = getResource(syn + "Concept")

		val xmlFile = createResource(syn + "xmlFile", artifact)
		val xmlRoot = createResource(syn + "xmlRoot", fragment)
		val xmlNode = createResource(syn + "xmlNode", fragment)
		createResource(syn + "nesting", concept)
		createResource(syn + "reuse", concept)

		val partOf = getProperty(syn + "partOf")

		add(xmlRoot, partOf, xmlFile)
		add(xmlNode, partOf, xmlRoot)

		for (i in 0 until 100)
			createResource(syn + "xmlLeaf$i", fragment).addProperty(partOf, xmlNode)
	}

	private fun Model.configurePrelude() {
		// Fragment is a class, subclass of artifact
		val entity = createResource(syn + "Entity", RDFS.Class)

		// Artifact is a class
		val artifact = createResource(syn + "Artifact", RDFS.Class)
		add(artifact, RDFS.subClassOf, entity)

		// Fragment is a class, subclass of artifact
		val fragment = createResource(syn + "Fragment", RDFS.Class)
		add(fragment, RDFS.subClassOf, artifact)

		// Concept is a class
		val concept = createResource(syn + "Concept", RDFS.Class)
		add(concept, RDFS.subClassOf, entity)

		// Part of is a relationship between artifacts
		val partOf = createProperty(syn + "partOf")
		add(partOf, RDFS.domain, artifact)
		add(partOf, RDFS.range, artifact)

		// Uses is a relationship between artifact and concept
		val uses = createProperty(syn + "uses")
		add(uses, RDFS.domain, artifact)
		add(uses, RDFS.range, concept)

		// Facilitates is a relationship between artifact and concept
		val facilitates = createProperty(syn + "facilitates")
		add(facilitates, RDFS.domain, artifact)
		add(facilitates, RDFS.range, concept)
	}

}
