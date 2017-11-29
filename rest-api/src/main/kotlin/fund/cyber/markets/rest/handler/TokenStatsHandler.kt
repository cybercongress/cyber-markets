package fund.cyber.markets.rest.handler

import com.fasterxml.jackson.databind.ObjectMapper
import graphs.TokenStats
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import java.math.BigDecimal
import java.util.*


class TokenStatsHandler : HttpHandler {

    private val random = Random()
    private val mapper = ObjectMapper()

    override fun handleRequest(exchange: HttpServerExchange) {

        val systems = mutableListOf("Agoras","Agrello","Aidos Kuneen","Anoncoin","Ardor","Asch","AsiaCoin","Auroracoin","Belacoin","BitBay","BitConnect","Bitcoin","Bitcoin Cash","BitcoinDark","Bitcoin Gold","BlackCoin","Blocknet","BoardRoom","Burst","Byteball","Bytecoin","Canada eCoin","CannabisCoin","Casinocoin","Cethereum","Clams","CloakCoin","Cointingency","Crypviser","CureCoin","DNotes","Dash","Decred","Dether","Deutsche eMark","Diamond","DigiByte","DigitalNote","Digitalcoin","DogeCoinDark","Dogecoin","EIX.fund","Edgeless","Electronic Gulden","Elephant","Emercoin","EnergyCoin","EtherEx","Expanse","FairCoin","Feathercoin","FlorinCoin","Freicoin","FuelCoin","Golos Gold","GameCredits","GeoCoin","GoldCoin","Graft","GridCoin","GroestlCoin","Guldencoin","HoboNickels","Horizon","Hyper","HyperStake","IOCoin","InstantDEX","Jumbucks","LBRY","LTBcoin","Ledgys","Lendroid","LEOCoin","Litecoin","Livepeer","Loopring","Lykke","MakerDAO","MazaCoin","Megacoin","Metaverse","Mintcoin","MonaCoin","Monero","Moonstone","Musicoin","Namecoin","NautilusCoin","Navajo","NobleNXT","Novacoin","NuBits","NuShares","Numeraire","Nxttycoin","Okcash","Omega One","OpenAnx","OpenZeppelin","PIVX","Peercoin","Pillar","PotCoin","Primecoin","Prizm","Propy","Qora","Quantum Resistant Ledger","Quark","ReddCoin","Riecoin","Rimbit","Ripple","Rootstock","Rouge Project","Round","Satoshi-Steem","SatoshiFund","SatoshiPie","Scotcoin","ShadowCash","Sia","SmileyCoin","SolarCoin","Startcoin","Steem","SteemDollar","SteemPower","Stellar","Stox","Stratis","SysCoin","TEKcoin","TaaS","TenX","Tendermint","Tether","TrueBit","Truthcoin","Ubiq","Unobtanium","Urbit","Vanillacoin","Vega Fund","Veltor","VeriCoin","Vertcoin","Viacoin","WeiFund","WorldCoin","Xaurum","YbCoin","ZClassic","ZCoin","Zcash","Zeitcoin","Zen Protocol","bitBTC","bitCNY","bitEUR","bitGOLD","bitSILVER","bitUSD","cyberFund")

        val data = mutableListOf<TokenStats>()

        for (system in systems) {

            val historicalData = mutableListOf<BigDecimal>()
            for (index in 1..7) {
                historicalData.add(rand(1, 10000))
            }
            val graphData = TokenStats(
                    system,
                    rand(1, 10000),
                    rand(1, 10000),
                    historicalData,
                    rand(-5, 10)
            )

            data.add(graphData)
        }

        exchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        exchange.responseSender.send(mapper.writeValueAsString(data))
    }

    private fun rand(from: Int, to: Int) : BigDecimal {
        return BigDecimal(random.nextInt(to - from) + from)
    }
}

