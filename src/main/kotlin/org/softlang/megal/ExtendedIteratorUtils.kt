package org.softlang.megal

import org.apache.jena.util.iterator.ExtendedIterator


fun <T> ExtendedIterator<T>.forEach(block: (T) -> Unit) {
	while (hasNext())
		block(next())
	close()
}

fun <T> ExtendedIterator<T>.forEachIndexed(block: (Int, T) -> Unit) {
	var i = 0
	while (hasNext())
		block(i++, next())
	close()
}