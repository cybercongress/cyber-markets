package fund.cyber.markets

import fund.cyber.markets.configuration.SCHEDULER_POOL_SIZE
import fund.cyber.markets.configuration.WS_CONNECTION_IDLE_TIMEOUT
import fund.cyber.markets.exchanges.ExchangeManagingService
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.util.ssl.SslContextFactory
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
import org.eclipse.jetty.websocket.client.WebSocketClient as JettyNativeWebSocketClient


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

        //add support for wss
        val httpClient = HttpClient(SslContextFactory())
        val jettyNativeClient = JettyNativeWebSocketClient(httpClient).apply {
            policy.idleTimeout = WS_CONNECTION_IDLE_TIMEOUT * 1000
            policy.maxTextMessageSize = Int.MAX_VALUE
            policy.maxTextMessageBufferSize = Int.MAX_VALUE
            maxIdleTimeout = WS_CONNECTION_IDLE_TIMEOUT * 1000
        }
        //strange jetty code
        jettyNativeClient.addBean(httpClient)
        jettyNativeClient.start()

        return JettyWebSocketClient(jettyNativeClient)
    }


    @Bean
    open fun taskExecutor(): TaskScheduler {
        return ThreadPoolTaskScheduler().apply {
            poolSize = SCHEDULER_POOL_SIZE
            threadNamePrefix = "Cyber.Markets Task Executor"
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args).apply {
        getBean(ExchangeManagingService::class.java).run()
    }
}

val applicationPool = newSingleThreadContext(
    "cmcp"
)