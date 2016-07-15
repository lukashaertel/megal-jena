package org.softlang.megal.playground

import com.google.common.collect.HashMultimap
import org.apache.jena.graph.Node
import org.apache.jena.graph.Triple
import org.apache.jena.graph.impl.GraphBase
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.NiceIterator
import java.util.*

/**
 * Created by Pazuzu on 07.07.2016.
 */
class Graph1X() : GraphBase() {
	/**
	 * First component fixed in this node
	 */
	val first1x = HashMultimap.create<Node, Triple>()

	/**
	 * Second component fixed in this node
	 */
	val second1x = HashMultimap.create<Node, Triple>()

	/**
	 * Third component fixed in this node
	 */
	val third1x = HashMultimap.create<Node, Triple>()

	override fun performAdd(t: Triple) {
		// Decompose
		val (s, p, o) = t

		// Add respectively
		first1x += s to t
		second1x += p to t
		third1x += o to t
	}

	override fun performDelete(t: Triple) {
		// Decompose
		val (s, p, o) = t

		// Add respectively
		first1x -= s to t
		second1x -= p to t
		third1x -= o to t
	}

	override fun graphBaseFind(triplePattern: Triple): ExtendedIterator<Triple> {
		// Decompose
		val (s, p, o) = triplePattern

		// Query for all elements
		if (s == Node.ANY && p == Node.ANY && o == Node.ANY)
			return WrappedIterator(first1x.values())

		// Join all
		val ss = if (s == Node.ANY) null else first1x[s]
		val ps = if (p == Node.ANY) null else second1x[p]
		val os = if (o == Node.ANY) null else third1x[o]
		val rs = groundedIntersection(ss, ps, os)

		// Return
		return WrappedIterator(rs ?: first1x.values())
	}

	override fun clear() {
		first1x.clear()
		second1x.clear()
		third1x.clear()
	}

	override fun isEmpty() = first1x.isEmpty

	override fun graphBaseSize() = first1x.size()
}

class WrappedIterator<E>(val base: Iterable<E>) : NiceIterator<E>() {
	val baseIterator = base.iterator()

	override fun toList() = base.toList()

	override fun toSet() = base.toSet()

	override fun hasNext() = baseIterator.hasNext()

	override fun next() = baseIterator.next()
}

operator fun Triple.component1() = subject
operator fun Triple.component2() = predicate
operator fun Triple.component3() = `object`

private fun <T> groundedIntersection(vararg ys: Set<T>?): Set<T>? {
	// Grounded if a data set was added, retain will then be performed
	var grounded = false
	val ret = HashSet<T>()

	for (y in ys) {
		if (y != null) {
			if (grounded)
				ret.retainAll(y)
			else {
				ret.addAll(y)
				grounded = true
			}
		}
	}

	return if (!grounded)
		null
	else
		ret
}
