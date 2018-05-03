package fund.cyber.markets.connector.trade

import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType
import fund.cyber.markets.connector.Connector
import fund.cyber.markets.connector.configuration.EXCHANGE_TAG
import fund.cyber.markets.connector.configuration.NINE_HUNDRED_NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.TOKENS_PAIR_TAG
import fund.cyber.markets.connector.configuration.TRADES_TOPIC_PREFIX
import fund.cyber.markets.connector.configuration.TRADE_COUNT_METRIC
import fund.cyber.markets.connector.configuration.TRADE_LATENCY_METRIC
import fund.cyber.markets.connector.etherdelta.EtherdeltaContract
import fund.cyber.markets.connector.etherdelta.EtherdeltaToken
import fund.cyber.markets.connector.etherdelta.EtherdeltaTokenResolver
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.TimeUnit

private const val ETHERDELTA_CONTRACT_ADDRESS = "0x8d12a197cb00d4747a1fe03395095ce2a5cc6819"
private const val ETH_SYMBOL = "ETH"
private const val ETHERDELTA_CONNECTION_TIMEOUT_MS: Long = 5 * 60 * 1000

@Component
class EtherdeltaTradeConnector : Connector {
    private val log = LoggerFactory.getLogger(EtherdeltaTradeConnector::class.java)!!

    private val exchangeName = "ETHERDELTA"
    private val tradesTopicName by lazy { TRADES_TOPIC_PREFIX + exchangeName }
    private lateinit var etherdeltaContract: EtherdeltaContract
    private var lastTradeTimestamp: Long? = null

    @Autowired
    private lateinit var web3j: Web3j

    @Autowired
    private lateinit var etherdeltaTokenResolver: EtherdeltaTokenResolver

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var monitoring: MeterRegistry

    private val gasProvider: ContractGasProvider = DefaultGasProvider()

    /**
     * Connect to etherdelta and parity token registry smart contracts using web3j
     */
    override fun connect() {
        log.info("Connecting to $exchangeName exchange")

        val etherdeltaTransactionManager = ReadonlyTransactionManager(web3j, ETHERDELTA_CONTRACT_ADDRESS)
        etherdeltaContract = EtherdeltaContract.load(ETHERDELTA_CONTRACT_ADDRESS, web3j,
            etherdeltaTransactionManager, gasProvider.getGasPrice(null), gasProvider.getGasLimit(null))

        log.info("Connected to $exchangeName exchange")
        updateTokensPairs()
    }

    override fun disconnect() {
        log.info("Disconected from $exchangeName exchange")
    }

    override fun updateTokensPairs() {
        etherdeltaTokenResolver.updateTokensPairs()
    }

    override fun isAlive(): Boolean {
        val currentTimestamp = Date().time

        return lastTradeTimestamp != null &&
            currentTimestamp - lastTradeTimestamp!! < ETHERDELTA_CONNECTION_TIMEOUT_MS

    }

    /**
     * Subscribe to new trades from etherdelta using smart contract.
     * For first, check if there are tokens with addresses from the tradeEvent in our map of tokens.
     * If both tokens are present in our dictionary, then we convert TradeEvent from smart contract to a Trade object
     * that we send to kafka topic.
     */
    override fun subscribe() {
        log.info("Subscribing for trades from $exchangeName exchange")

        val exchangeTag = Tags.of(EXCHANGE_TAG, exchangeName)
        val tradeLatencyMonitor = Timer.builder(TRADE_LATENCY_METRIC)
                .tags(exchangeTag)
                .publishPercentiles(NINGTHY_FIVE_PERCENT, NINE_HUNDRED_NINGTHY_FIVE_PERCENT)
                .register(monitoring)

        var block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send()

        etherdeltaContract
                .tradeEventObservable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe{ tradeEvent ->

                    if (block.block.hash != tradeEvent.log!!.blockHash) {
                        block = web3j.ethGetBlockByHash(tradeEvent.log!!.blockHash!!, false).send()
                    }

                    val trade = convertTrade(tradeEvent, block)

                    if (trade != null) {
                        val exchangePairTag = exchangeTag.and(Tags.of(TOKENS_PAIR_TAG, trade.pair.base + "_" + trade.pair.quote))
                        val tradeCountMonitor = monitoring.counter(TRADE_COUNT_METRIC, exchangePairTag)
                        tradeLatencyMonitor.record(System.currentTimeMillis() - trade.timestamp.time, TimeUnit.MILLISECONDS)
                        tradeCountMonitor.increment()

                        kafkaTemplate.send(tradesTopicName, trade)
                        log.debug("Trade from $exchangeName: $trade")
                    }
                }
    }

    /**
     * Convert TradeEvent from smart contract to a Trade object
     * For first convert amounts from BigInteger type to BigDecimal using
     * @see EtherdeltaToken.base and @see EtherdeltaToken.decimals
     * Then get a timestamp from ethereum block and create a trade object
     *
     * @param tradeEvent a trade from etherdelta smart contract
     * @param block a ethereum block which @param tradeEvent was mined
     * @return trade object
     * @see Trade
     */
    private fun convertTrade(tradeEvent: EtherdeltaContract.TradeEventResponse, block: EthBlock): Trade? {
        val tokenGet = etherdeltaTokenResolver.resolveToken(tradeEvent.tokenGet)
        val tokenGive = etherdeltaTokenResolver.resolveToken(tradeEvent.tokenGive)

        if (tokenGet == null || tokenGive == null) {
            return null
        }

        val amountGet = if (tokenGet.base > BigInteger.ZERO) {
            BigDecimal(tradeEvent.amountGet).divide(BigDecimal(tokenGet.base))
        } else {
            BigDecimal(tradeEvent.amountGet)
        }

        val amountGive = if (tokenGive.base > BigInteger.ZERO) {
            BigDecimal(tradeEvent.amountGive).divide(BigDecimal(tokenGive.base))
        } else {
            BigDecimal(tradeEvent.amountGive)
        }

        if (amountGet == BigDecimal.ZERO || amountGive == BigDecimal.ZERO) {
            return null
        }

        val timestamp = Numeric.toBigInt(block.block.timestampRaw).multiply(BigInteger.valueOf(1000)).toLong()
        lastTradeTimestamp = timestamp

        if (tokenGive.symbol == ETH_SYMBOL) {
            val price = amountGive.divide(amountGet, tokenGive.decimals, RoundingMode.HALF_EVEN)

            return Trade(exchangeName,
                    TokensPair(tokenGet.symbol, tokenGive.symbol),
                    TradeType.BID,
                    Date(timestamp),
                    timestamp convert MILLIS_TO_HOURS,
                    tradeEvent.log!!.transactionHash,
                    amountGet,
                    amountGive,
                    price
            )
        } else {
            val price = amountGet.divide(amountGive, tokenGet.decimals, RoundingMode.HALF_EVEN)

            return Trade(exchangeName,
                    TokensPair(tokenGive.symbol, tokenGet.symbol),
                    TradeType.ASK,
                    Date(timestamp),
                    timestamp convert MILLIS_TO_HOURS,
                    tradeEvent.log!!.transactionHash,
                    amountGive,
                    amountGet,
                    price
            )
        }
    }

}