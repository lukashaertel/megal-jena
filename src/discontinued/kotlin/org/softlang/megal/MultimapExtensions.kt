package org.softlang.megal.playground

import com.google.common.collect.Multimap

operator fun <K, V> Multimap<K, V>.plusAssign(pair: Pair<K, V>) {
	put(pair.first, pair.second)
}

operator fun <K, V> Multimap<K, V>.minusAssign(pair: Pair<K, V>) {
	remove(pair.first, pair.second)
}

operator fun <K, V> Multimap<K, V>.get(key: K) = this.get(key)

operator fun <K, V> Multimap<K, V>.set(key: K, values: Array<out V>) {
	replaceValues(key, values.asList())
}

operator fun <K, V> Multimap<K, V>.set(key: K, values: Iterable<V>) {
	replaceValues(key, values)
}

operator fun <K, V> Multimap<K, V>.set(key: K, values: Sequence<V>) {
	replaceValues(key, values.asIterable())
}