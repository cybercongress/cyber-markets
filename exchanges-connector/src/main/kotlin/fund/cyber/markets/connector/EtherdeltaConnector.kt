package fund.cyber.markets.connector

import com.fasterxml.jackson.databind.ObjectMapper
import fund.cyber.markets.connector.configuration.ConnectorConfiguration
import fund.cyber.markets.connector.configuration.TRADES_TOPIC_PREFIX
import fund.cyber.markets.connector.etherdelta.EtherdeltaContract
import fund.cyber.markets.connector.etherdelta.EtherdeltaToken
import fund.cyber.markets.connector.etherdelta.ParityTokenRegistryContract
import fund.cyber.markets.helpers.MILLIS_TO_HOURS
import fund.cyber.markets.helpers.convert
import fund.cyber.markets.model.TokensPair
import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.TradeType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
import java.util.regex.Pattern

private const val ETHERDELTA_CONFIG_URL = "https://raw.githubusercontent.com/etherdelta/etherdelta.github.io/master/config/main.json"
private const val ETHERDELTA_CONTRACT_ADDRESS = "0x8d12a197cb00d4747a1fe03395095ce2a5cc6819"
private const val PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS = "0x5F0281910Af44bFb5fC7e86A404d0304B0e042F1"
private const val PARITY_TOKEN_REGISTRY_EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000"

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
    private lateinit var tradeKafkaTemplate: KafkaTemplate<String, Trade>

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
        return true
    }

    override fun subscribeTrades() {
        log.info("Subscribing for trades from $exchangeName exchange")
        try {
            var block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf("latest"), false).send()

            etherdeltaContract.tradeEventObservable(DefaultBlockParameter.valueOf("latest"), DefaultBlockParameter.valueOf("latest"))
                    .subscribe { tradeEvent ->

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
                            log.debug("{}", trade)

                            tradeKafkaTemplate.send(tradesTopicName, trade)
                        }

                    }
        } catch (e: Exception) {
            log.error("An error occurred subscribing trades from $exchangeName exchange. Reconnecting...", e)
            reconnect()
        }
    }

    private fun convertTrade(tradeEvent: EtherdeltaContract.TradeEventResponse, block: EthBlock): Trade? {
        val tokenGet = exchangeTokensPairs[tradeEvent.tokenGet]
        val tokenGive = exchangeTokensPairs[tradeEvent.tokenGive]

        val amountGet = if (tokenGet!!.base.compareTo(BigInteger.ZERO) == 1) {
            BigDecimal(tradeEvent.amountGet).divide(BigDecimal(tokenGet.base))
        } else {
            BigDecimal(tradeEvent.amountGet)
        }

        val amountGive = if (tokenGive!!.base.compareTo(BigInteger.ZERO) == 1) {
            BigDecimal(tradeEvent.amountGive).divide(BigDecimal(tokenGive.base))
        } else {
            BigDecimal(tradeEvent.amountGive)
        }

        val timestamp = Numeric.toBigInt(block.block.timestampRaw).multiply(BigInteger.valueOf(1000)).toLong()

        if (tokenGive.symbol == "ETH") {
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

    override fun updateTokensPairs() {
        log.info("Updating tokens pairs")

        val etherdeltaConfigTokens = getEtherdeltaConfigTokens()
        val parityTokenRegistryTokens = getParityTokenRegistryTokens()
        exchangeTokensPairs = parityTokenRegistryTokens
        exchangeTokensPairs.putAll(etherdeltaConfigTokens)

        log.info("Tokens pairs updated. Count: ${exchangeTokensPairs.size}")
    }

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

    fun getParityTokenRegistryTokens(): MutableMap<String, EtherdeltaToken> {
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

    fun validBase(tokenBase: String): Boolean {
        val pattern = Pattern.compile("10*")
        val matcher = pattern.matcher(tokenBase)

        val result = (matcher.regionEnd() - matcher.regionStart()) == tokenBase.length || tokenBase == "0"

        return result
    }

}