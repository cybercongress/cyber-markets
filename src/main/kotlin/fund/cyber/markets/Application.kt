package fund.cyber.markets

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.jetty.JettyWebSocketClient


/**
 * Application entry point.
 *
 * @author hleb.albau@gmail.com
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan("fund.cyber.markets")
open class Application {

    @Bean
    open fun webSocketClient(): WebSocketClient {
        val jettyNativeClient = org.eclipse.jetty.websocket.client.WebSocketClient()
        jettyNativeClient.policy.maxTextMessageSize = Int.MAX_VALUE
        jettyNativeClient.policy.idleTimeout = 10 * 1000
        jettyNativeClient.start()
        return JettyWebSocketClient(jettyNativeClient)
    }


    @Bean
    open fun taskExecutor(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = 5
        scheduler.threadNamePrefix = "Cyber.Markets Task Executor"
        return scheduler
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
