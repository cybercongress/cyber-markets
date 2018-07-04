package fund.cyber.markets.api.rest

import fund.cyber.markets.api.rest.service.TickerService
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.repository.TickerRepository
import fund.cyber.markets.common.Intervals
import fund.cyber.markets.common.model.TokenTicker
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import reactor.core.publisher.Flux
import java.util.*


class TickerServiceTest {

    private lateinit var tickerService: TickerService

    @Before
    fun before() {
        val repository = Mockito.mock(TickerRepository::class.java)
        tickerService = TickerService(repository)

        //1m tickers
        Mockito
            .`when`(
                repository.find("BTC", 0, Date(0), Intervals.MINUTE, Intervals.DAY / Intervals.MINUTE)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(0L, Intervals.MINUTE, Intervals.DAY / Intervals.MINUTE)
            ))

        //24h tickers
        Mockito
            .`when`(
                repository.find("BTC", 0, Date(0), Intervals.DAY, 1)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(0L, Intervals.DAY, 1)
            ))
        Mockito
            .`when`(
                repository.find("BTC", 1, Date(0), Intervals.DAY, 1)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(Intervals.DAY, Intervals.DAY, 1)
            ))
        Mockito
            .`when`(
                repository.find("BTC", 2, Date(0), Intervals.DAY, 1)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(Intervals.DAY * 2, Intervals.DAY, 1)
            ))

        //15m tickers
        Mockito
            .`when`(
                repository.find("BTC", 0, Date(94 * 15 * 60 * 1000), 15 * 60 * 1000, 2)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(94 * 15 * 60 * 1000, 15 * 60 * 1000, 2)
            ))
        Mockito
            .`when`(
                repository.find("BTC", 1, Date(94 * 15 * 60 * 1000), 15 * 60 * 1000, 96)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(Intervals.DAY, 15 * 60 * 1000, 96)
            ))
        Mockito
            .`when`(
                repository.find("BTC", 2, Date(94 * 15 * 60 * 1000), 15 * 60 * 1000, 2)
            )
            .thenReturn(Flux.fromIterable(
                generateTestData(Intervals.DAY * 2, 15 * 60 * 1000, 2)
            ))
    }

    //1 min interval
    @Test
    fun test1() {

        val tickers = tickerService
            .getTickers("BTC", 0L, Intervals.MINUTE, Intervals.DAY / Intervals.MINUTE)
            .collectList()
            .block()

        Assertions.assertThat(tickers).hasSize((Intervals.DAY / Intervals.MINUTE).toInt())
    }

    //24h interval
    @Test
    fun test2() {

        val tickers = tickerService
            .getTickers("BTC", 0L, Intervals.DAY, 3)
            .collectList()
            .block()

        Assertions.assertThat(tickers).hasSize(3)
    }

    //15 min interval
    @Test
    fun test3() {

        val tickers = tickerService
            .getTickers("BTC", 94 * 15 * 60 * 1000, 15 * 60 * 1000, 100)
            .collectList()
            .block()

        Assertions.assertThat(tickers).hasSize(100)
    }

    private fun generateTestData(timestampFrom: Long, interval: Long, count: Long): MutableList<CqlTokenTicker> {
        val tickers = mutableListOf<CqlTokenTicker>()

        for (index in 0 until count) {
            val timestamp = timestampFrom + interval * index

            tickers.add(
                CqlTokenTicker(TokenTicker("BTC", timestamp, timestamp + interval, interval))
            )
        }

        return tickers
    }

}