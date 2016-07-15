package org.softlang.megal

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory
import java.io.File

fun resourceFile(string: String) = File(File("src/main/resources"), string)

fun main(args: Array<String>) {
	val megamodel = Megamodel(parseModel(resourceFile("Test.megal")))
	val model = ModelFactory.createModelForGraph(megamodel.graph)
	val reified = ModelFactory.createModelForGraph(model.reificationGraph())

	val reasoner = GenericRuleReasonerFactory.theInstance().create(reified.getResource("reasoner"))
	val inference = ModelFactory.createInfModel(reasoner, reified)
	val inferenceReified = ModelFactory.createModelForGraph(inference.reificationGraph())

	for (x in inferenceReified.listStatements())
		println(x)


}

