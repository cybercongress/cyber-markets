package fund.cyber.markets.connector.etherdelta

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.GenericApplicationContext
import org.springframework.stereotype.Component
import org.web3j.protocol.Web3j
import org.web3j.tx.ReadonlyTransactionManager
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger
import java.net.URL
import java.nio.charset.Charset
import java.util.regex.Pattern

private const val ETHERDELTA_CONFIG_URL = "https://raw.githubusercontent.com/etherdelta/etherdelta.github.io/master/config/main.json"
private const val PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS = "0x5F0281910Af44bFb5fC7e86A404d0304B0e042F1"
private const val PARITY_TOKEN_REGISTRY_EMPTY_ADDRESS = "0x0000000000000000000000000000000000000000"

@Component
class EtherdeltaTokenResolver {
    private val log = LoggerFactory.getLogger(EtherdeltaTokenResolver::class.java)!!

    lateinit var exchangeTokensPairs: MutableMap<String, EtherdeltaToken>

    @Autowired
    private lateinit var web3j: Web3j

    @Autowired
    private lateinit var resourceLoader: GenericApplicationContext

    private val gasProvider: ContractGasProvider = DefaultGasProvider()

    /**
     * Get ERC20 token definitions from different sources
     */
    fun updateTokensPairs() {
        log.info("Updating Etherdelta tokens pairs")

        val parityTokenRegistryTokens = getParityTokenRegistryTokens()
        val etherdeltaConfigTokens = getEtherdeltaConfigTokens()
        val myEtherWalletTokens = getMyEtherWalletTokens()
        exchangeTokensPairs = parityTokenRegistryTokens
        exchangeTokensPairs.putAll(etherdeltaConfigTokens)
        exchangeTokensPairs.putAll(myEtherWalletTokens)

        log.info("Etherdelta tokens pairs updated. Count: ${exchangeTokensPairs.size}")
    }

    /**
     * Get ERC20 token definitions from etherdelta configuration JSON file.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getEtherdeltaConfigTokens(): MutableMap<String, EtherdeltaToken> {
        val mapper = ObjectMapper()
        val tokens = mutableMapOf<String, EtherdeltaToken>()
        val base = BigInteger.TEN

        try {
            val configTree = mapper.readTree(URL(ETHERDELTA_CONFIG_URL))
            configTree.get("tokens").asIterable().forEach { tokenNode ->

                if (tokenNode.get("decimals").asInt() != 0) {
                    val tokenContractAddress = tokenNode.get("addr").asText()
                    val tokenSymbol = tokenNode.get("name").asText()
                    val tokenDecimal = tokenNode.get("decimals").asInt()
                    val tokenBase = base.pow(tokenDecimal)

                    tokens[tokenContractAddress] = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimal)
                }
            }
        } catch (e: Exception) {
            log.warn("Cant get tokens definitions from Etherdelta config")
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

        try {
            val tokenDefinitionInputStream = resourceLoader.getResource("classpath:tokens-eth.json").inputStream
            val tokenDefinitionString = IOUtils.toString(tokenDefinitionInputStream, Charset.forName("UTF-8"))

            val tokenListTree = mapper.readValue<Iterable<JsonNode>>(tokenDefinitionString)
            tokenListTree.asIterable().forEach { tokenNode ->

                if (tokenNode.get("decimals").asInt() != 0) {
                    val tokenContractAddress = tokenNode.get("address").asText()
                    val tokenSymbol = tokenNode.get("symbol").asText()
                    val tokenDecimal = tokenNode.get("decimals").asInt()
                    val tokenBase = base.pow(tokenDecimal)

                    tokens[tokenContractAddress] = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimal)
                }
            }
        } catch (e: Exception) {
            log.warn("Cant get tokens definitions from MyEtherWallet config")
        }

        return tokens
    }

    /**
     * Get ERC20 token definitions from parity token registry smart contract.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getParityTokenRegistryTokens(): MutableMap<String, EtherdeltaToken> {
        val tokens = mutableMapOf<String, EtherdeltaToken>()

        try {
            val transactionManager = ReadonlyTransactionManager(web3j, PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS)
            val parityTokenRegistryContract = ParityTokenRegistryContract.load(PARITY_TOKEN_REGISTRY_CONTRACT_ADDRESS,
                web3j, transactionManager, gasProvider.getGasPrice(null), gasProvider.getGasLimit(null))

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
        } catch (e: Exception) {
            log.warn("Cant get parity token registry tokens")
        }

        return tokens
    }

    /**
     * Get ERC20 token definitions from its own smart contract.
     * @return a map of ERC20 token address and token definition.
     */
    private fun getTokenDefinitionByAddress(address: String): EtherdeltaToken? {
        var tokenDefinition: EtherdeltaToken? = null

        try {
            val transactionManager = ReadonlyTransactionManager(web3j, address)
            val erc20Contract = Erc20Contract.load(address, web3j, transactionManager, gasProvider.getGasPrice(null), gasProvider.getGasLimit(null))

            val tokenSymbol = erc20Contract.symbol().send().trim()
            val tokenDecimals = erc20Contract.decimals().send().intValueExact()
            val tokenBase = BigInteger.TEN.pow(tokenDecimals)

            tokenDefinition = EtherdeltaToken(tokenSymbol, tokenBase, tokenDecimals)

            log.info("Resolve new token: symbol=$tokenSymbol, decimals=$tokenDecimals")
            exchangeTokensPairs[address] = tokenDefinition
        } catch (e: Exception) {
            log.info("Cant get token definition from address: $address")
        }

        return tokenDefinition
    }

    fun resolveToken(address: String?): EtherdeltaToken? {
        val tokenDefiniton = exchangeTokensPairs[address]

        if (tokenDefiniton == null && address != null) {
            getTokenDefinitionByAddress(address)
        }

        return tokenDefiniton
    }

    /**
     * Check that @param tokenBase is valid power of 10.
     * We need check this because parity token registry contains not valid data.
     * @return boolean result
     */
    private fun validBase(tokenBase: String): Boolean {
        val pattern = Pattern.compile("10*")
        val matcher = pattern.matcher(tokenBase)

        val result = (matcher.regionEnd() - matcher.regionStart()) == tokenBase.length || tokenBase == "0"

        return result
    }

}