package org.softlang.megal.playground

import org.apache.jena.graph.Graph
import org.apache.jena.graph.Triple
import org.apache.jena.sparql.util.graph.GraphListenerBase

class ListenerReg(val graph: Graph) {
	operator inline fun plus(crossinline add: (Triple) -> Unit) = apply {
		graph.eventManager.register(object : GraphListenerBase() {
			override fun deleteEvent(t: Triple) = add(t)

			override fun addEvent(t: Triple?) {
			}
		})
	}

	operator inline fun minus(crossinline remove: (Triple) -> Unit) = apply {
		graph.eventManager.register(object : GraphListenerBase() {
			override fun deleteEvent(t: Triple) {
			}

			override fun addEvent(t: Triple) = remove(t)
		})
	}

}

val Graph.listenerReg: ListenerReg get() = ListenerReg(this)