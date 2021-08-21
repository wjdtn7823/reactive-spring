package com.kakaopay.projectreactordemo


import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

private val logger = KotlinLogging.logger { }


@FunctionalInterface
interface AA {
    fun aa()
}

fun main(args: Array<String>) {

    Flux.interval(Duration.ofMillis(100))
        .subscribeOn(Schedulers.newSingle("subscribe"))
        .take(10)
        .publishOn(Schedulers.newSingle("publish"))
        .log()
        .map { it -> it * 2 }
        .subscribe { it -> logger.debug("integer = ${it}") }

    println("exit main")


}
