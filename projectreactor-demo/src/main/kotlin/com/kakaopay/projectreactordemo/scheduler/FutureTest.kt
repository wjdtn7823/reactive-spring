package com.kakaopay.projectreactordemo.scheduler

import mu.KotlinLogging
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class FutureTest {
}

fun interface SuccessCallBack<T> {
    fun onSuccess(sc: T): Unit
}

fun interface ExceptionCallBack {
    fun onError(t: Throwable)
}

class CallbackFutureTask<T>(val callable: Callable<T>, val sc: SuccessCallBack<T>, val th: ExceptionCallBack) : FutureTask<T>(callable) {

    override fun done() {
        logger.info("done: ${get()}")
        try {
            sc.onSuccess(get())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: ExecutionException) {
            logger.info("???")
            th.onError(e)
        }

    }

}


private val logger = KotlinLogging.logger { }

fun main() {
    val es = Executors.newCachedThreadPool()

    val f = CallbackFutureTask(callable = {
        Thread.sleep(2000)
        if (1 == 1) throw RuntimeException("RE")
        logger.info("Async")
        "Hello"
    }, sc = SuccessCallBack { it -> logger.info("Result : $it") },
        th = ExceptionCallBack { it -> logger.info("Exception : $it") }
    )
    logger.info("before execute")
    es.execute(f)
    logger.info("after executer")
    es.shutdown()
    logger.info("after shutdown")
////
////    val futureTask : FutureTask<String> = object : FutureTask<String>() {
////        Thread.sleep(2000)
////        logger.info("Async")
////        "Hello"
////    }
//
//   val str = es.submit(futureTask)
////
////    val future: Future<String> = es.submit(Callable {
////        Thread.sleep(2000)
////        logger.info("Async")
////        "Hello"
////    })
//    logger.info("Exit")
//
//    Thread.sleep(3)
//
//    logger.info(str.get())
//    es.shutdown()
//

}