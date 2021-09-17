package com.kakaopay.projectreactordemo.scheduler

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


private val logger = KotlinLogging.logger { }


@Configuration
class WebConfig : WebMvcConfigurer {


    fun configureAsyncSupport(configurer: AsyncSupportConfigurer, threadPoolTaskExecutor: ThreadPoolTaskExecutor) {
        configurer.setTaskExecutor(threadPoolTaskExecutor)
        configurer.setDefaultTimeout(30000)

    }

    @Bean
    fun mvcTaskExecutor(): ThreadPoolTaskExecutor? {
        val taskExecutor = ThreadPoolTaskExecutor()
        //taskExecutor.threadNamePrefix = "mvc-task-"
        return taskExecutor
    }
}

@SpringBootApplication
@EnableAsync
class AsyncMain : WebMvcConfigurer {
    @RestController
    class MyController(val myService: MyService) {

        @Autowired
        lateinit var es: ExecutorService

        @GetMapping("/async")
        fun async(): Callable<String> {
            logger.info("start")

            return Callable<String> {
                logger.info("before sleep")
                Thread.sleep(2000)
                logger.info("quit")
                "ping"
            }

        }

        val queue = ConcurrentLinkedQueue<DeferredResult<String>>()

        @GetMapping("/dr")
        fun dr(): DeferredResult<String> {
            logger.info("dr")
            val deferredResult = DeferredResult<String>(300000000L)
            queue.add(deferredResult)
            return deferredResult
        }

        @GetMapping("/dr/count")
        fun drCount(): String {
            return queue.size.toString()
        }


        @GetMapping("/dr/event")
        fun drEvent(msg: String): String {
            for (dr in queue) {
                dr.setResult("Hello ${msg}")
                queue.remove(dr)
            }

            return "OK"
        }

        @GetMapping("/callable")
        fun callable(): Callable<String> {
            logger.info("start")
            return Callable<String> {
                logger.info("before sleep")
                Thread.sleep(2000)
                logger.info("quit")
                "ping"
            }
        }
    }

    @Bean
    fun executorService(): ExecutorService = Executors.newCachedThreadPool()

    @Service
    class MyService {
        @Async
        fun hello(): ListenableFuture<String> {
            logger.info("Async")
            Thread.sleep(2000)
            return AsyncResult("Hello")
        }
    }


}

fun main(args: Array<String>) {
    runApplication<AsyncMain>(*args)
}

