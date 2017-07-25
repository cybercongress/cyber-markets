package fund.cyber.markets

import fund.cyber.markets.configuration.SCHEDULER_POOL_SIZE
import fund.cyber.markets.configuration.WS_CONNECTION_IDLE_TIMEOUT
import fund.cyber.markets.model.Trade
import fund.cyber.markets.storage.RethinkDbService
import io.deepstream.DeepstreamClient
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.actor
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
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

        //add support for wss
        val httpClient = HttpClient(SslContextFactory())
        val jettyNativeClient = org.eclipse.jetty.websocket.client.WebSocketClient(httpClient).apply {
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

    @Bean
    open fun deepStreamClient(): DeepstreamClient {
        val deepstreamUrl = System.getenv("deepstreamUrl") ?: "localhost:6020"
        val client = DeepstreamClient(deepstreamUrl)
        val result = client.login()
        if (result.loggedIn()) {
            println("Log in success!")
        }
        return client
    }

    @Bean
    open fun deepStreamActor(deepstreamClient: DeepstreamClient): ActorJob<Trade> {
        return actor(CommonPool) {
            for (trade in channel) {
                deepstreamClient.event.emit("cyber.markets.trades", trade)
            }
        }
    }

    @Bean
    @Profile("console")
    open fun consolePrinterActor(rethinkDbService: RethinkDbService): ActorJob<Trade> {
        return actor(CommonPool) {
            for (trade in channel) {
                println(trade)
            }
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args).apply {
        getBean(RethinkDbService::class.java)
    }
}
