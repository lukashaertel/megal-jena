package org.softlang.megal.playground

import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask


fun fork(block: Runnable) =
		ForkJoinPool.commonPool().submit(block)


fun <V> fork(block: Callable<V>) =
		ForkJoinPool.commonPool().submit(block)


fun <V> fork(block: () -> V) =
		ForkJoinPool.commonPool().submit(block)


infix fun <T, U> ForkJoinTask<T>.and(other: ForkJoinTask<U>) =
		ForkJoinPair(this, other)


/**
 * Composed forks
 */
data class ForkJoinPair<X1, X2>(
		val first: ForkJoinTask<X1>,
		val second: ForkJoinTask<X2>) {

	infix fun <T> join(block: (X1, X2) -> T) = block(first.join(), second.join())

	infix fun <X3> and(other: ForkJoinTask<X3>) = ForkJoinTriple(first, second, other)
}

/**
 * Composed forks
 */
data class ForkJoinTriple<X1, X2, X3>(
		val first: ForkJoinTask<X1>,
		val second: ForkJoinTask<X2>,
		val third: ForkJoinTask<X3>) {
	infix fun <T> join(block: (X1, X2, X3) -> T) = block(first.join(), second.join(), third.join())

	infix fun <X4> and(other: ForkJoinTask<X4>) = ForkJoinQuad(first, second, third, other)
}

/**
 * Composed forks
 */
data class ForkJoinQuad<X1, X2, X3, X4>(
		val first: ForkJoinTask<X1>,
		val second: ForkJoinTask<X2>,
		val third: ForkJoinTask<X3>,
		val fourth: ForkJoinTask<X4>) {

	infix fun <T> join(block: (X1, X2, X3, X4) -> T) = block(first.join(), second.join(), third.join(), fourth.join())
}


