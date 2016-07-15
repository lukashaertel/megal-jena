package org.softlang.megal.playground

import org.apache.jena.graph.GraphEvents
import org.apache.jena.query.*
import org.apache.jena.rdf.model.*
import org.apache.jena.reasoner.rulesys.Rule

fun Model.select(query: Query, initialBinding: QuerySolution) =
		QueryExecutionFactory.create(query, this, initialBinding).use { Sequence { it.execSelect() }.toList() }

fun Model.select(query: Query, vararg assignments: Pair<String, RDFNode>) =
		select(query, QuerySolutionMap().apply { for (a in assignments) add(a.first, a.second) })


fun Model.getAllStatements() = Sequence { listStatements() }

fun select(sub: Resource? = null, pred: Property? = null, obj: Any?) = SimpleSelector(sub, pred, obj)

fun select(sub: Resource? = null, pred: Property? = null, obj: RDFNode? = null) = SimpleSelector(sub, pred, obj)

inline fun select(sub: Resource? = null, pred: Property? = null, obj: Any?, crossinline test: (Statement) -> Boolean) = object : SimpleSelector(sub, pred, obj) {
	override fun test(s: Statement) = test(s)
}

inline fun select(sub: Resource? = null, pred: Property? = null, obj: RDFNode? = null, crossinline test: (Statement) -> Boolean) = object : SimpleSelector(sub, pred, obj) {
	override fun test(s: Statement) = test(s)
}

fun String.rules() = reader().buffered().use {
	Rule.parseRules(Rule.rulesParserFromReader(it))
}

fun String.query() = QueryFactory.create(this)

fun String.model(model: Model = ModelFactory.createDefaultModel(), base: String? = null, lang: String = "TRIG") = reader().buffered().use {
	model.apply {
		inBulk {
			val reader = getReader(lang)
			reader.setProperty("WARN_REDEFINITION_OF_ID", "EM_IGNORE")
			reader.read(this, it, base)
		}
	}
}

fun <T> Model.inBulk(action: Model.() -> T): T {
	notifyEvent(GraphEvents.startRead)
	try {
		return action()
	} finally {
		notifyEvent(GraphEvents.finishRead)
	}
}
fun Model.list(sub: Resource? = null, pred: Property? = null, obj: RDFNode? = null, reified: Boolean = true) =
		listStatements(SimpleSelector(sub, pred, obj)).andThen(
				listReifiedStatements()
						.mapWith { it.statement }
						.filterKeep { sub == null || it.subject.asNode().sameValueAs(sub) }
						.filterKeep { pred == null || it.predicate.asNode().sameValueAs(pred) }
						.filterKeep { obj == null || it.`object`.asNode().sameValueAs(obj) }
		)
