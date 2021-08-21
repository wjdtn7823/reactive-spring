package com.kakaopay.projectreactordemo.scheduler

import mu.KotlinLogging
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger { }

fun main() {

//    val pub = Publisher<Int> { val es = Executors.newSingleThreadExecutor{ Thread("pubOnSub") } }


    val pub = Publisher<Int> { s ->
        s.onSubscribe(object : Subscription {
            override fun request(n: Long) {
                s.onNext(1)
                s.onNext(2)
                s.onNext(3)
                s.onNext(4)
                s.onNext(5)
                s.onComplete()
            }

            override fun cancel() {

            }

        })

    }


    val pubOnPub = object : Publisher<Int> {
        val es = Executors.newSingleThreadExecutor(CustomizableThreadFactory("pubOnPub-"))

        override fun subscribe(s: Subscriber<in Int>) {

            es.execute {
                s.onNext(1)
                s.onNext(2)
                s.onNext(3)
                s.onNext(4)
                s.onNext(5)
                s.onComplete()

            }
            es.shutdown()
        }

    }

    pubOnPub.subscribe(object : Subscriber<Int> {
        val es = Executors.newSingleThreadExecutor(CustomizableThreadFactory("pubOnSub-"))

        override fun onSubscribe(s: Subscription) {

            es.execute { s.request(Long.MAX_VALUE) }
        }

        override fun onNext(t: Int?) {
            es.execute {
                logger.debug { "onNext ${t}" }

            }
        }

        override fun onError(t: Throwable?) {
            es.execute {
                logger.debug { "onError ${t}" }
            }

            es.shutdown()

        }

        override fun onComplete() {
            es.execute {
                logger.debug { "onComplete " }
            }

            es.shutdown()

        }

    })


}