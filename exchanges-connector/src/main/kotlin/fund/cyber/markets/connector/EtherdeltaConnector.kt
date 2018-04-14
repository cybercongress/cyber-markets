package fund.cyber.markets.connector

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fund.cyber.markets.common.MILLIS_TO_HOURS
import fund.cyber.markets.common.convert
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType
import fund.cyber.markets.connector.configuration.ConnectorConfiguration
import fund.cyber.markets.connector.configuration.EXCHANGE_TAG
import fund.cyber.markets.connector.configuration.NINE_HUNDRED_NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.NINGTHY_FIVE_PERCENT
import fund.cyber.markets.connector.configuration.TOKENS_PAIR_TAG
import fund.cyber.markets.connector.configuration.TRADES_TOPIC_PREFIX
import fund.cyber.markets.connector.configuration.TRADE_COUNT_METRIC
import fund.cyber.markets.connector.configuration.TRADE_LATENCY_METRIC
import fund.cyber.markets.connector.etherdelta.EtherdeltaContract
import fund.cyber.markets.connector.etherdelta.EtherdeltaToken
import fund.cyber.markets.connector.etherdelta.ParityTokenRegistryContract
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Contract
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

private const val ETHERDELTA_CONFIG_URL = "https://raw.githubusercontent.com/etherdelta/etherdelta.github.io/master/config/main.json"
private const val ETHERDELTA_CONTRACT_ADDRESS = "0x8d12a197cb00d4747a1fe03395095ce2a5cc6819"
private const val PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS = "0x5F0281910Af44bFb5fC7e86A404d0304B0e042F1"
private const val PARITY_TOKEN_REGISTRY_EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000"
private const val ETH_SYMBOL = "ETH"

@Component
class EtherdeltaConnector : ExchangeConnector {
    private val log = LoggerFactory.getLogger(EtherdeltaConnector::class.java)!!

    private val exchangeName = "ETHERDELTA"
    private val tradesTopicName by lazy { TRADES_TOPIC_PREFIX + exchangeName }

    private lateinit var web3j: Web3j
    private lateinit var exchangeTokensPairs: MutableMap<String, EtherdeltaToken>
    private lateinit var parityTokenRegistryContract: ParityTokenRegistryContract
    private lateinit var etherdeltaContract: EtherdeltaContract

    @Autowired
    private lateinit var configuration: ConnectorConfiguration

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var monitoring: MeterRegistry

    /**
     * Connect to etherdelta and parity token registry smart contracts using web3j
     */
    override fun connect() {
        log.info("Connecting to $exchangeName exchange")

        web3j = Web3j.build(HttpService(configuration.parityUrl))

        val gasPrice = web3j.ethGasPrice().send()
        val tokenRegistryTransactionManager = ReadonlyTransactionManager(web3j, PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS)
        val etherdeltaTransactionManager = ReadonlyTransactionManager(web3j, ETHERDELTA_CONTRACT_ADDRESS)

        parityTokenRegistryContract = ParityTokenRegistryContract.load(PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS, web3j, tokenRegistryTransactionManager, gasPrice.gasPrice, Contract.GAS_LIMIT)
        etherdeltaContract = EtherdeltaContract.load(ETHERDELTA_CONTRACT_ADDRESS, web3j, etherdeltaTransactionManager, gasPrice.gasPrice, Contract.GAS_LIMIT)

        log.info("Connected to $exchangeName exchange")

        if (!this::exchangeTokensPairs.isInitialized) {
            updateTokensPairs()
        }
    }

    override fun disconnect() {
        log.info("Disconected from $exchangeName exchange")
    }

    override fun isAlive(): Boolean {
        val test: String?

        try {
            test = etherdeltaContract.accountLevelsAddr().send()
        } catch (e: Throwable) {
            return false
        }

        return test != null
    }

    /**
     * Subscribe to new trades from etherdelta using smart contract.
     * For first, check if there are tokens with addresses from the tradeEvent in our map of tokens.
     * If both tokens are present in our dictionary, then we convert TradeEvent from smart contract to a Trade object
     * that we send to kafka topic.
     */
    override fun subscribeTrades() {
        log.info("Subscribing for trades from $exchangeName exchange")

        val exchangeTag = Tags.of(EXCHANGE_TAG, exchangeName)
        val tradeLatencyMonitor = Timer.builder(TRADE_LATENCY_METRIC)
                .tags(exchangeTag)
                .publishPercentiles(NINGTHY_FIVE_PERCENT, NINE_HUNDRED_NINGTHY_FIVE_PERCENT)
                .register(monitoring)

        val latestBlockParameter = DefaultBlockParameter.valueOf("latest")
        var block = web3j.ethGetBlockByNumber(latestBlockParameter, false).send()

        etherdeltaContract
                .tradeEventObservable(latestBlockParameter, latestBlockParameter)
                .subscribe{ tradeEvent ->

                    if (block.block.hash != tradeEvent.log!!.blockHash) {
                        block = web3j.ethGetBlockByHash(tradeEvent.log!!.blockHash!!, false).send()
                    }

                    val getToken = exchangeTokensPairs[tradeEvent.tokenGet]
                    val giveToken = exchangeTokensPairs[tradeEvent.tokenGive]

                    if (getToken == null || giveToken == null) {
                        val missedTokenAddress = if (getToken == null) {
                            tradeEvent.tokenGet
                        } else {
                            tradeEvent.tokenGive
                        }
                        log.warn("Token not found in token list: $missedTokenAddress")
                    } else {
                        val trade = convertTrade(tradeEvent, block)

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
    private fun convertTrade(tradeEvent: EtherdeltaContract.TradeEventResponse, block: EthBlock): Trade {
        val tokenGet = exchangeTokensPairs[tradeEvent.tokenGet]
        val tokenGive = exchangeTokensPairs[tradeEvent.tokenGive]

        val amountGet = if (tokenGet!!.base > BigInteger.ZERO) {
            BigDecimal(tradeEvent.amountGet).divide(BigDecimal(tokenGet.base))
        } else {
            BigDecimal(tradeEvent.amountGet)
        }

        val amountGive = if (tokenGive!!.base > BigInteger.ZERO) {
            BigDecimal(tradeEvent.amountGive).divide(BigDecimal(tokenGive.base))
        } else {
            BigDecimal(tradeEvent.amountGive)
        }

        val timestamp = Numeric.toBigInt(block.block.timestampRaw).multiply(BigInteger.valueOf(1000)).toLong()

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

    /**
     * Get ERC20 token definitions from different locations
     */
    override fun updateTokensPairs() {
        log.info("Updating tokens pairs")

        val parityTokenRegistryTokens = getParityTokenRegistryTokens()
        val etherdeltaConfigTokens = getEtherdeltaConfigTokens()
        val myEtherWalletTokens = getMyEtherWalletTokens()
        exchangeTokensPairs = parityTokenRegistryTokens
        exchangeTokensPairs.putAll(etherdeltaConfigTokens)
        exchangeTokensPairs.putAll(myEtherWalletTokens)

        log.info("Tokens pairs updated. Count: ${exchangeTokensPairs.size}")
    }

    /**
     * Get ERC20 token definitions from etherdelta configuration JSON file.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getEtherdeltaConfigTokens(): MutableMap<String, EtherdeltaToken> {
        val mapper = ObjectMapper()
        val tokens = mutableMapOf<String, EtherdeltaToken>()
        val base = BigInteger.TEN

        val configTree =  mapper.readTree(URL(ETHERDELTA_CONFIG_URL))
        configTree.get("tokens").asIterable().forEach { tokenNode ->

            if (tokenNode.get("decimals").asInt() != 0) {
                val tokenContractAddress = tokenNode.get("addr").asText()
                val tokenSymbol = tokenNode.get("name").asText()
                val tokenDecimal = tokenNode.get("decimals").asInt()
                val tokenBase = base.pow(tokenDecimal)

                tokens[tokenContractAddress] = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimal)
            }
        }

        return tokens
    }

    /**
     * Get ERC20 token definitions from MyEtherWallet token list.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getMyEtherWalletTokens(): MutableMap<String, EtherdeltaToken> {
        val mapper = ObjectMapper()
        val tokens = mutableMapOf<String, EtherdeltaToken>()
        val base = BigInteger.TEN

        val tokenListTree =  mapper.readValue<Iterable<JsonNode>>(ClassPathResource("tokens-eth.json").file)
        tokenListTree.asIterable().forEach { tokenNode ->

            if (tokenNode.get("decimals").asInt() != 0) {
                val tokenContractAddress = tokenNode.get("address").asText()
                val tokenSymbol = tokenNode.get("symbol").asText()
                val tokenDecimal = tokenNode.get("decimals").asInt()
                val tokenBase = base.pow(tokenDecimal)

                tokens[tokenContractAddress] = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimal)
            }
        }

        return tokens
    }

    /**
     * Get ERC20 token definitions from parity token registry smart contract.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getParityTokenRegistryTokens(): MutableMap<String, EtherdeltaToken> {
        val tokens = mutableMapOf<String, EtherdeltaToken>()

        val tokensCount = parityTokenRegistryContract.tokenCount().send().toLong()
        for (index in 0 until tokensCount) {
            val token = parityTokenRegistryContract.token(BigInteger.valueOf(index)).send()

            if (token.value1 == PARITY_TOKEN_REGISTRY_EMPTY_ADDRESS || !validBase(token.value3.toString())) {
                continue
            }

            val tokenContractAddress = token.value1
            val tokenSymbol = token.value2
            val tokenBase = token.value3
            val tokenDecimals = tokenBase.toString().length - 1

            tokens[tokenContractAddress] = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimals)
        }

        return tokens
    }

    /**
     * Check that @param tokenBase is valid power of 10.
     * We need check this because parity token registry contains not valid data.
     * @return boolean result
     */
    fun validBase(tokenBase: String): Boolean {
        val pattern = Pattern.compile("10*")
        val matcher = pattern.matcher(tokenBase)

        val result = (matcher.regionEnd() - matcher.regionStart()) == tokenBase.length || tokenBase == "0"

        return result
    }

}