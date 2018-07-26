package fund.cyber.markets.ticker.common

import fund.cyber.markets.common.model.TokenTicker
import org.assertj.core.api.Assertions
import org.junit.Test
import java.math.BigDecimal


class HopTickerOperationsTest {

    @Test
    fun addHopTest() {
        val ticker = TokenTicker(
            "BTC",
            0L,
            0L,
            0L
        )

        val volume1 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val volumeEntity1 = mutableMapOf(
            "ex1" to BigDecimal(1),
            "ex2" to BigDecimal(2),
            "ex3" to BigDecimal(3)
        )
        volume1["BNB"] = volumeEntity1

        val baseVolume1 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val baseVolumeEntity1 = mutableMapOf(
            "ex1" to BigDecimal(100),
            "ex2" to BigDecimal(200),
            "ex3" to BigDecimal(300)
        )
        baseVolume1["BTC"] = baseVolumeEntity1

        val hopTicker1 = TokenTicker(
            "BTC",
            0L,
            0L,
            0L,
            mutableMapOf(),
            volume1,
            baseVolume1
        )

        val volume2 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val volumeEntity2 = mutableMapOf(
            "ex1" to BigDecimal(1),
            "ex2" to BigDecimal(2),
            "ex3" to BigDecimal(3)
        )
        volume2["BNB"] = volumeEntity2

        val baseVolume2 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val baseVolumeEntity2 = mutableMapOf(
            "ex1" to BigDecimal(100),
            "ex2" to BigDecimal(200),
            "ex3" to BigDecimal(300)
        )
        baseVolume2["BTC"] = baseVolumeEntity2

        val hopTicker2 = TokenTicker(
            "BTC",
            0L,
            0L,
            0L,
            mutableMapOf(),
            volume2,
            baseVolume2
        )

        ticker addHop hopTicker1
        ticker addHop hopTicker2

        Assertions.assertThat(ticker.volume["BNB"]).hasSize(3)
        Assertions.assertThat(ticker.volume["BNB"]!!["ex3"]).isEqualTo(BigDecimal(6))
        Assertions.assertThat(ticker.baseVolume["BTC"]!!["ex3"]).isEqualTo(BigDecimal(600))
    }

    @Test
    fun minusHopTest() {
        val volume1 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val volumeEntity1 = mutableMapOf(
            "ex1" to BigDecimal(1),
            "ex2" to BigDecimal(2),
            "ex3" to BigDecimal(3)
        )
        volume1["BNB"] = volumeEntity1

        val baseVolume1 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val baseVolumeEntity1 = mutableMapOf(
            "ex1" to BigDecimal(100),
            "ex2" to BigDecimal(200),
            "ex3" to BigDecimal(300)
        )
        baseVolume1["BTC"] = baseVolumeEntity1

        val ticker = TokenTicker(
            "BTC",
            0L,
            0L,
            0L,
            mutableMapOf(),
            volume1,
            baseVolume1
        )

        val volume2 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val volumeEntity2 = mutableMapOf(
            "ex2" to BigDecimal(1),
            "ex3" to BigDecimal(1)
        )
        volume2["BNB"] = volumeEntity2

        val baseVolume2 = mutableMapOf<String, MutableMap<String, BigDecimal>>()
        val baseVolumeEntity2 = mutableMapOf(
            "ex2" to BigDecimal(50),
            "ex3" to BigDecimal(60)
        )
        baseVolume2["BTC"] = baseVolumeEntity2

        val hopTicker2 = TokenTicker(
            "BTC",
            0L,
            0L,
            0L,
            mutableMapOf(),
            volume2,
            baseVolume2
        )

        ticker minusHop hopTicker2

        Assertions.assertThat(ticker.volume["BNB"]).hasSize(3)
        Assertions.assertThat(ticker.volume["BNB"]!!["ex3"]).isEqualTo(BigDecimal(2))
        Assertions.assertThat(ticker.baseVolume["BTC"]!!["ex2"]).isEqualTo(BigDecimal(150))
    }

}