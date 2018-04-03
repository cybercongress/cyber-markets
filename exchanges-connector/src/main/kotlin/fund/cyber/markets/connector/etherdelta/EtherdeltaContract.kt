package fund.cyber.markets.connector.etherdelta

import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import rx.Observable
import java.math.BigInteger
import java.util.*

/**
 *
 * Auto generated code.
 *
 * **Do not modify!**
 *
 * Please use the [web3j command line tools](https://docs.web3j.io/command_line.html),
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * [codegen module](https://github.com/web3j/web3j/tree/master/codegen) to update.
 *
 *
 * Generated with web3j version 3.3.1.
 */
class EtherdeltaContract : Contract {

    protected constructor(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {}

    protected constructor(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {}

    fun getOrderEvents(transactionReceipt: TransactionReceipt): List<OrderEventResponse> {
        val event = Event("Order",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<OrderEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = OrderEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.expires = eventValues.nonIndexedValues[4].value as BigInteger
            typedResponse.nonce = eventValues.nonIndexedValues[5].value as BigInteger
            typedResponse.user = eventValues.nonIndexedValues[6].value as String
            responses.add(typedResponse)
        }
        return responses
    }

    fun orderEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<OrderEventResponse> {
        val event = Event("Order",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = OrderEventResponse()
            typedResponse.log = log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.expires = eventValues.nonIndexedValues[4].value as BigInteger
            typedResponse.nonce = eventValues.nonIndexedValues[5].value as BigInteger
            typedResponse.user = eventValues.nonIndexedValues[6].value as String
            typedResponse
        }
    }

    fun getCancelEvents(transactionReceipt: TransactionReceipt): List<CancelEventResponse> {
        val event = Event("Cancel",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint8>() {

                }, object : TypeReference<Bytes32>() {

                }, object : TypeReference<Bytes32>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<CancelEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = CancelEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.expires = eventValues.nonIndexedValues[4].value as BigInteger
            typedResponse.nonce = eventValues.nonIndexedValues[5].value as BigInteger
            typedResponse.user = eventValues.nonIndexedValues[6].value as String
            typedResponse.v = eventValues.nonIndexedValues[7].value as BigInteger
            typedResponse.r = eventValues.nonIndexedValues[8].value as ByteArray
            typedResponse.s = eventValues.nonIndexedValues[9].value as ByteArray
            responses.add(typedResponse)
        }
        return responses
    }

    fun cancelEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<CancelEventResponse> {
        val event = Event("Cancel",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint8>() {

                }, object : TypeReference<Bytes32>() {

                }, object : TypeReference<Bytes32>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = CancelEventResponse()
            typedResponse.log = log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.expires = eventValues.nonIndexedValues[4].value as BigInteger
            typedResponse.nonce = eventValues.nonIndexedValues[5].value as BigInteger
            typedResponse.user = eventValues.nonIndexedValues[6].value as String
            typedResponse.v = eventValues.nonIndexedValues[7].value as BigInteger
            typedResponse.r = eventValues.nonIndexedValues[8].value as ByteArray
            typedResponse.s = eventValues.nonIndexedValues[9].value as ByteArray
            typedResponse
        }
    }

    fun getTradeEvents(transactionReceipt: TransactionReceipt): List<TradeEventResponse> {
        val event = Event("Trade",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<TradeEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = TradeEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.get = eventValues.nonIndexedValues[4].value as String
            typedResponse.give = eventValues.nonIndexedValues[5].value as String
            responses.add(typedResponse)
        }
        return responses
    }

    fun tradeEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<TradeEventResponse> {
        val event = Event("Trade",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = TradeEventResponse()
            typedResponse.log = log
            typedResponse.tokenGet = eventValues.nonIndexedValues[0].value as String
            typedResponse.amountGet = eventValues.nonIndexedValues[1].value as BigInteger
            typedResponse.tokenGive = eventValues.nonIndexedValues[2].value as String
            typedResponse.amountGive = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse.get = eventValues.nonIndexedValues[4].value as String
            typedResponse.give = eventValues.nonIndexedValues[5].value as String
            typedResponse
        }
    }

    fun getDepositEvents(transactionReceipt: TransactionReceipt): List<DepositEventResponse> {
        val event = Event("Deposit",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<DepositEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = DepositEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.token = eventValues.nonIndexedValues[0].value as String
            typedResponse.user = eventValues.nonIndexedValues[1].value as String
            typedResponse.amount = eventValues.nonIndexedValues[2].value as BigInteger
            typedResponse.balance = eventValues.nonIndexedValues[3].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun depositEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<DepositEventResponse> {
        val event = Event("Deposit",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = DepositEventResponse()
            typedResponse.log = log
            typedResponse.token = eventValues.nonIndexedValues[0].value as String
            typedResponse.user = eventValues.nonIndexedValues[1].value as String
            typedResponse.amount = eventValues.nonIndexedValues[2].value as BigInteger
            typedResponse.balance = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse
        }
    }

    fun getWithdrawEvents(transactionReceipt: TransactionReceipt): List<WithdrawEventResponse> {
        val event = Event("Withdraw",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<WithdrawEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = WithdrawEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.token = eventValues.nonIndexedValues[0].value as String
            typedResponse.user = eventValues.nonIndexedValues[1].value as String
            typedResponse.amount = eventValues.nonIndexedValues[2].value as BigInteger
            typedResponse.balance = eventValues.nonIndexedValues[3].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun withdrawEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<WithdrawEventResponse> {
        val event = Event("Withdraw",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = WithdrawEventResponse()
            typedResponse.log = log
            typedResponse.token = eventValues.nonIndexedValues[0].value as String
            typedResponse.user = eventValues.nonIndexedValues[1].value as String
            typedResponse.amount = eventValues.nonIndexedValues[2].value as BigInteger
            typedResponse.balance = eventValues.nonIndexedValues[3].value as BigInteger
            typedResponse
        }
    }

    fun trade(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger, user: String, v: BigInteger, r: ByteArray, s: ByteArray, amount: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "trade",
                Arrays.asList<Type<*>>(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce),
                        Address(user),
                        Uint8(v),
                        Bytes32(r),
                        Bytes32(s),
                        Uint256(amount)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun order(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "order",
                Arrays.asList(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun orderFills(param0: String, param1: ByteArray): RemoteCall<BigInteger> {
        val function = Function("orderFills",
                Arrays.asList<Type<*>>(Address(param0),
                        Bytes32(param1)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun cancelOrder(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger, v: BigInteger, r: ByteArray, s: ByteArray): RemoteCall<TransactionReceipt> {
        val function = Function(
                "cancelOrder",
                Arrays.asList<Type<*>>(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce),
                        Uint8(v),
                        Bytes32(r),
                        Bytes32(s)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun withdraw(amount: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "withdraw",
                Arrays.asList<Type<*>>(Uint256(amount)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun depositToken(token: String, amount: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "depositToken",
                Arrays.asList(Address(token),
                        Uint256(amount)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun amountFilled(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger, user: String, v: BigInteger, r: ByteArray, s: ByteArray): RemoteCall<BigInteger> {
        val function = Function("amountFilled",
                Arrays.asList<Type<*>>(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce),
                        Address(user),
                        Uint8(v),
                        Bytes32(r),
                        Bytes32(s)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun tokens(param0: String, param1: String): RemoteCall<BigInteger> {
        val function = Function("tokens",
                Arrays.asList<Type<*>>(Address(param0),
                        Address(param1)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun changeFeeMake(feeMake_: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeFeeMake",
                Arrays.asList<Type<*>>(Uint256(feeMake_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun feeMake(): RemoteCall<BigInteger> {
        val function = Function("feeMake",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun changeFeeRebate(feeRebate_: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeFeeRebate",
                Arrays.asList<Type<*>>(Uint256(feeRebate_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun feeAccount(): RemoteCall<String> {
        val function = Function("feeAccount",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun testTrade(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger, user: String, v: BigInteger, r: ByteArray, s: ByteArray, amount: BigInteger, sender: String): RemoteCall<Boolean> {
        val function = Function("testTrade",
                Arrays.asList<Type<*>>(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce),
                        Address(user),
                        Uint8(v),
                        Bytes32(r),
                        Bytes32(s),
                        Uint256(amount),
                        Address(sender)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bool>() {

                }))
        return executeRemoteCallSingleValueReturn(function, Boolean::class.java)
    }

    fun changeFeeAccount(feeAccount_: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeFeeAccount",
                Arrays.asList<Type<*>>(Address(feeAccount_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun feeRebate(): RemoteCall<BigInteger> {
        val function = Function("feeRebate",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun changeFeeTake(feeTake_: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeFeeTake",
                Arrays.asList<Type<*>>(Uint256(feeTake_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun changeAdmin(admin_: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeAdmin",
                Arrays.asList<Type<*>>(Address(admin_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun withdrawToken(token: String, amount: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "withdrawToken",
                Arrays.asList(Address(token),
                        Uint256(amount)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun orders(param0: String, param1: ByteArray): RemoteCall<Boolean> {
        val function = Function("orders",
                Arrays.asList<Type<*>>(Address(param0),
                        Bytes32(param1)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bool>() {

                }))
        return executeRemoteCallSingleValueReturn(function, Boolean::class.java)
    }

    fun feeTake(): RemoteCall<BigInteger> {
        val function = Function("feeTake",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun deposit(weiValue: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "deposit",
                Arrays.asList(),
                emptyList())
        return executeRemoteCallTransaction(function, weiValue)
    }

    fun changeAccountLevelsAddr(accountLevelsAddr_: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "changeAccountLevelsAddr",
                Arrays.asList<Type<*>>(Address(accountLevelsAddr_)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun accountLevelsAddr(): RemoteCall<String> {
        val function = Function("accountLevelsAddr",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun balanceOf(token: String, user: String): RemoteCall<BigInteger> {
        val function = Function("balanceOf",
                Arrays.asList<Type<*>>(Address(token),
                        Address(user)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun admin(): RemoteCall<String> {
        val function = Function("admin",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun availableVolume(tokenGet: String, amountGet: BigInteger, tokenGive: String, amountGive: BigInteger, expires: BigInteger, nonce: BigInteger, user: String, v: BigInteger, r: ByteArray, s: ByteArray): RemoteCall<BigInteger> {
        val function = Function("availableVolume",
                Arrays.asList<Type<*>>(Address(tokenGet),
                        Uint256(amountGet),
                        Address(tokenGive),
                        Uint256(amountGive),
                        Uint256(expires),
                        Uint256(nonce),
                        Address(user),
                        Uint8(v),
                        Bytes32(r),
                        Bytes32(s)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    class OrderEventResponse {
        var log: Log? = null

        var tokenGet: String? = null

        var amountGet: BigInteger? = null

        var tokenGive: String? = null

        var amountGive: BigInteger? = null

        var expires: BigInteger? = null

        var nonce: BigInteger? = null

        var user: String? = null
    }

    class CancelEventResponse {
        var log: Log? = null

        var tokenGet: String? = null

        var amountGet: BigInteger? = null

        var tokenGive: String? = null

        var amountGive: BigInteger? = null

        var expires: BigInteger? = null

        var nonce: BigInteger? = null

        var user: String? = null

        var v: BigInteger? = null

        var r: ByteArray? = null

        var s: ByteArray? = null
    }

    class TradeEventResponse {
        var log: Log? = null

        var tokenGet: String? = null

        var amountGet: BigInteger? = null

        var tokenGive: String? = null

        var amountGive: BigInteger? = null

        var get: String? = null

        var give: String? = null
    }

    class DepositEventResponse {
        var log: Log? = null

        var token: String? = null

        var user: String? = null

        var amount: BigInteger? = null

        var balance: BigInteger? = null
    }

    class WithdrawEventResponse {
        var log: Log? = null

        var token: String? = null

        var user: String? = null

        var amount: BigInteger? = null

        var balance: BigInteger? = null
    }

    companion object {
        private val BINARY = "6060604052341561000f57600080fd5b60405160c0806119ac83398101604052808051919060200180519190602001805191906020018051919060200180519190602001805160008054600160a060020a0319908116600160a060020a039a8b16178255600180548216998b169990991790985560028054909816969098169590951790955550600391909155600455600555506119099081906100a390396000f30060606040526004361061013a5763ffffffff60e060020a6000350416630a19b14a811461014a5780630b9276661461019957806319774d43146101cb578063278b8c0e146101ff5780632e1a7d4d1461023e578063338b5dea1461025457806346be96c314610276578063508493bc146102bf57806354d03b5c146102e457806357786394146102fa5780635e1d7ae41461030d57806365e17c9d146103235780636c86888b1461035257806371ffcb16146103be578063731c2f81146103dd5780638823a9c0146103f05780638f283970146104065780639e281a9814610425578063bb5f462914610447578063c281309e14610469578063d0e30db01461047c578063e8f6bc2e14610484578063f3412942146104a3578063f7888aec146104b6578063f851a440146104db578063fb6e155f146104ee575b341561014557600080fd5b600080fd5b341561015557600080fd5b610197600160a060020a0360043581169060243590604435811690606435906084359060a4359060c4351660ff60e43516610104356101243561014435610537565b005b34156101a457600080fd5b610197600160a060020a03600435811690602435906044351660643560843560a4356107d0565b34156101d657600080fd5b6101ed600160a060020a0360043516602435610907565b60405190815260200160405180910390f35b341561020a57600080fd5b610197600160a060020a03600435811690602435906044351660643560843560a43560ff60c4351660e43561010435610924565b341561024957600080fd5b610197600435610b61565b341561025f57600080fd5b610197600160a060020a0360043516602435610c92565b341561028157600080fd5b6101ed600160a060020a0360043581169060243590604435811690606435906084359060a4359060c4351660ff60e435166101043561012435610de5565b34156102ca57600080fd5b6101ed600160a060020a0360043581169060243516610e9f565b34156102ef57600080fd5b610197600435610ebc565b341561030557600080fd5b6101ed610eeb565b341561031857600080fd5b610197600435610ef1565b341561032e57600080fd5b610336610f2c565b604051600160a060020a03909116815260200160405180910390f35b341561035d57600080fd5b6103aa600160a060020a0360043581169060243590604435811690606435906084359060a4359060c43581169060ff60e43516906101043590610124359061014435906101643516610f3b565b604051901515815260200160405180910390f35b34156103c957600080fd5b610197600160a060020a0360043516610fa6565b34156103e857600080fd5b6101ed610ff0565b34156103fb57600080fd5b610197600435610ff6565b341561041157600080fd5b610197600160a060020a0360043516611031565b341561043057600080fd5b610197600160a060020a036004351660243561107b565b341561045257600080fd5b6103aa600160a060020a0360043516602435611217565b341561047457600080fd5b6101ed611237565b61019761123d565b341561048f57600080fd5b610197600160a060020a03600435166112f2565b34156104ae57600080fd5b61033661133c565b34156104c157600080fd5b6101ed600160a060020a036004358116906024351661134b565b34156104e657600080fd5b610336611376565b34156104f957600080fd5b6101ed600160a060020a0360043581169060243590604435811690606435906084359060a4359060c4351660ff60e435166101043561012435611385565b60006002308d8d8d8d8d8d6040516c01000000000000000000000000600160a060020a0398891681028252968816870260148201526028810195909552929095169093026048830152605c820192909252607c810192909252609c82015260bc016020604051808303816000865af115156105b157600080fd5b50506040518051600160a060020a038816600090815260076020908152604080832084845290915290205490925060ff16905080610699575085600160a060020a03166001826040517f19457468657265756d205369676e6564204d6573736167653a0a3332000000008152601c810191909152603c0160405180910390208787876040516000815260200160405260405193845260ff9092166020808501919091526040808501929092526060840192909252608090920191516020810390808403906000865af1151561068557600080fd5b505060206040510351600160a060020a0316145b80156106a55750874311155b80156106df5750600160a060020a03861660009081526008602090815260408083208484529091529020548b906106dc908461159b565b11155b15156106ea57600080fd5b6106f88c8c8c8c8a876115bf565b600160a060020a0386166000908152600860209081526040808320848452909152902054610726908361159b565b600160a060020a03871660009081526008602090815260408083208584529091529020557f6effdda786735d5033bfad5f53e5131abcced9e52be6c507b62d639685fbed6d8c838c8e8d830281151561077b57fe5b048a33604051600160a060020a0396871681526020810195909552928516604080860191909152606085019290925284166080840152921660a082015260c001905180910390a1505050505050505050505050565b60006002308888888888886040516c01000000000000000000000000600160a060020a0398891681028252968816870260148201526028810195909552929095169093026048830152605c820192909252607c810192909252609c82015260bc016020604051808303816000865af1151561084a57600080fd5b50506040518051600160a060020a0333908116600090815260076020908152604080832085845290915290819020805460ff191660011790559193507f3f7f2eda73683c21a15f9435af1028c93185b5f1fa38270762dc32be606b3e8592508991899189918991899189919051600160a060020a03978816815260208101969096529386166040808701919091526060860193909352608085019190915260a0840152921660c082015260e001905180910390a150505050505050565b600860209081526000928352604080842090915290825290205481565b60006002308b8b8b8b8b8b6040516c01000000000000000000000000600160a060020a0398891681028252968816870260148201526028810195909552929095169093026048830152605c820192909252607c810192909252609c82015260bc016020604051808303816000865af1151561099e57600080fd5b50506040518051600160a060020a033316600090815260076020908152604080832084845290915290205490925060ff16905080610a86575033600160a060020a03166001826040517f19457468657265756d205369676e6564204d6573736167653a0a3332000000008152601c810191909152603c0160405180910390208686866040516000815260200160405260405193845260ff9092166020808501919091526040808501929092526060840192909252608090920191516020810390808403906000865af11515610a7257600080fd5b505060206040510351600160a060020a0316145b1515610a9157600080fd5b33600160a060020a0381166000908152600860209081526040808320858452909152908190208b90557f1e0b760c386003e9cb9bcf4fcf3997886042859d9b6ed6320e804597fcdb28b0918c918c918c918c918c918c91908c908c908c9051600160a060020a039a8b16815260208101999099529689166040808a01919091526060890196909652608088019490945260a087019290925290951660c085015260ff90941660e084015261010083019390935261012082015261014001905180910390a150505050505050505050565b33600160a060020a031660009081526000805160206118be833981519152602052604090205481901015610b9457600080fd5b33600160a060020a031660009081526000805160206118be8339815191526020526040902054610bc4908261187b565b33600160a060020a031660008181526000805160206118be833981519152602052604090819020929092559082905160006040518083038185875af1925050501515610c0f57600080fd5b33600160a060020a03811660009081526000805160206118be8339815191526020526040808220547ff341246adaac6f497bc2a656f546ab9e182111d630394f0c57c710a59a2cb567939185919051600160a060020a0394851681529290931660208301526040808301919091526060820192909252608001905180910390a150565b600160a060020a0382161515610ca757600080fd5b81600160a060020a03166323b872dd33308460405160e060020a63ffffffff8616028152600160a060020a0393841660048201529190921660248201526044810191909152606401602060405180830381600087803b1515610d0857600080fd5b5af11515610d1557600080fd5b505050604051805190501515610d2a57600080fd5b600160a060020a0380831660009081526006602090815260408083203390941683529290522054610d5b908261159b565b600160a060020a038381166000908152600660209081526040808320339485168452909152908190208390557fdcbc1c05240f31ff3ad067ef1ee35ce4997762752e3a095284754544f4c709d79285929185919051600160a060020a0394851681529290931660208301526040808301919091526060820192909252608001905180910390a15050565b6000806002308d8d8d8d8d8d6040516c01000000000000000000000000600160a060020a0398891681028252968816870260148201526028810195909552929095169093026048830152605c820192909252607c810192909252609c82015260bc016020604051808303816000865af11515610e6057600080fd5b50506040518051600160a060020a03881660009081526008602090815260408083208484529091529020549350915050509a9950505050505050505050565b600660209081526000928352604080842090915290825290205481565b60005433600160a060020a03908116911614610ed757600080fd5b600354811115610ee657600080fd5b600355565b60035481565b60005433600160a060020a03908116911614610f0c57600080fd5b600554811080610f1d575060045481115b15610f2757600080fd5b600555565b600154600160a060020a031681565b600160a060020a03808d166000908152600660209081526040808320938516835292905290812054839010801590610f84575082610f818e8e8e8e8e8e8e8e8e8e611385565b10155b1515610f9257506000610f96565b5060015b9c9b505050505050505050505050565b60005433600160a060020a03908116911614610fc157600080fd5b6001805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b60055481565b60005433600160a060020a0390811691161461101157600080fd5b600454811180611022575060055481105b1561102c57600080fd5b600455565b60005433600160a060020a0390811691161461104c57600080fd5b6000805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b600160a060020a038216151561109057600080fd5b600160a060020a0380831660009081526006602090815260408083203390941683529290522054819010156110c457600080fd5b600160a060020a03808316600090815260066020908152604080832033909416835292905220546110f5908261187b565b600160a060020a03808416600081815260066020908152604080832033958616845290915290819020939093559163a9059cbb919084905160e060020a63ffffffff8516028152600160a060020a0390921660048301526024820152604401602060405180830381600087803b151561116d57600080fd5b5af1151561117a57600080fd5b50505060405180519050151561118f57600080fd5b600160a060020a03808316600090815260066020908152604080832033948516845290915290819020547ff341246adaac6f497bc2a656f546ab9e182111d630394f0c57c710a59a2cb5679285929091859151600160a060020a0394851681529290931660208301526040808301919091526060820192909252608001905180910390a15050565b600760209081526000928352604080842090915290825290205460ff1681565b60045481565b33600160a060020a031660009081526000805160206118be833981519152602052604090205461126d903461159b565b33600160a060020a03811660009081526000805160206118be83398151915260205260408082208490557fdcbc1c05240f31ff3ad067ef1ee35ce4997762752e3a095284754544f4c709d793919291349151600160a060020a0394851681529290931660208301526040808301919091526060820192909252608001905180910390a1565b60005433600160a060020a0390811691161461130d57600080fd5b6002805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a0392909216919091179055565b600254600160a060020a031681565b600160a060020a03918216600090815260066020908152604080832093909416825291909152205490565b600054600160a060020a031681565b6000806000806002308f8f8f8f8f8f6040516c01000000000000000000000000600160a060020a0398891681028252968816870260148201526028810195909552929095169093026048830152605c820192909252607c810192909252609c82015260bc016020604051808303816000865af1151561140357600080fd5b50506040518051600160a060020a038a16600090815260076020908152604080832084845290915290205490945060ff169050806114eb575087600160a060020a03166001846040517f19457468657265756d205369676e6564204d6573736167653a0a3332000000008152601c810191909152603c0160405180910390208989896040516000815260200160405260405193845260ff9092166020808501919091526040808501929092526060840192909252608090920191516020810390808403906000865af115156114d757600080fd5b505060206040510351600160a060020a0316145b80156114f75750894311155b1515611506576000935061158a565b600160a060020a0388166000908152600860209081526040808320868452909152902054611535908e9061187b565b600160a060020a03808e166000908152600660209081526040808320938d16835292905220549092508b9061156a908f61188f565b81151561157357fe5b049050808210156115865781935061158a565b8093505b5050509a9950505050505050505050565b60008282016115b88482108015906115b35750838210155b6118ae565b9392505050565b600080600080670de0b6b3a76400006115da8660035461188f565b8115156115e357fe5b049350670de0b6b3a76400006115fb8660045461188f565b81151561160457fe5b600254919004935060009250600160a060020a0316156116be57600254600160a060020a0316631cbd05198760405160e060020a63ffffffff8416028152600160a060020a039091166004820152602401602060405180830381600087803b151561166e57600080fd5b5af1151561167b57600080fd5b505050604051805191505060018114156116b157670de0b6b3a76400006116a48660055461188f565b8115156116ad57fe5b0491505b80600214156116be578291505b600160a060020a03808b16600090815260066020908152604080832033909416835292905220546116f8906116f3878661159b565b61187b565b600160a060020a038b81166000908152600660209081526040808320338516845290915280822093909355908816815220546117469061174161173b888661159b565b8761187b565b61159b565b600160a060020a038b811660009081526006602090815260408083208b8516845290915280822093909355600154909116815220546117929061174161178c878761159b565b8561187b565b600160a060020a03808c166000908152600660208181526040808420600154861685528252808420959095558c84168352908152838220928a1682529190915220546117f2908a6117e38a8961188f565b8115156117ec57fe5b0461187b565b600160a060020a0389811660009081526006602090815260408083208b85168452909152808220939093553390911681522054611843908a6118348a8961188f565b81151561183d57fe5b0461159b565b600160a060020a03988916600090815260066020908152604080832033909c1683529a90529890982097909755505050505050505050565b6000611889838311156118ae565b50900390565b60008282026115b88415806115b357508385838115156118ab57fe5b04145b8015156118ba57600080fd5b50560054cdd369e4e8a8515e52ca72ec816c2101831ad1f18bf44102ed171459c9b4f8a165627a7a72305820acf8d1ede8f7f796195588df99bb6a29c8d62a1b900ee17a22dfdb40147153c60029"

        fun deploy(web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, admin_: String, feeAccount_: String, accountLevelsAddr_: String, feeMake_: BigInteger, feeTake_: BigInteger, feeRebate_: BigInteger): RemoteCall<EtherdeltaContract> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(org.web3j.abi.datatypes.Address(admin_),
                    org.web3j.abi.datatypes.Address(feeAccount_),
                    org.web3j.abi.datatypes.Address(accountLevelsAddr_),
                    org.web3j.abi.datatypes.generated.Uint256(feeMake_),
                    org.web3j.abi.datatypes.generated.Uint256(feeTake_),
                    org.web3j.abi.datatypes.generated.Uint256(feeRebate_)))
            return Contract.deployRemoteCall(EtherdeltaContract::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun deploy(web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger, admin_: String, feeAccount_: String, accountLevelsAddr_: String, feeMake_: BigInteger, feeTake_: BigInteger, feeRebate_: BigInteger): RemoteCall<EtherdeltaContract> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(org.web3j.abi.datatypes.Address(admin_),
                    org.web3j.abi.datatypes.Address(feeAccount_),
                    org.web3j.abi.datatypes.Address(accountLevelsAddr_),
                    org.web3j.abi.datatypes.generated.Uint256(feeMake_),
                    org.web3j.abi.datatypes.generated.Uint256(feeTake_),
                    org.web3j.abi.datatypes.generated.Uint256(feeRebate_)))
            return Contract.deployRemoteCall(EtherdeltaContract::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun load(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): EtherdeltaContract {
            return EtherdeltaContract(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        fun load(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): EtherdeltaContract {
            return EtherdeltaContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }
    }
}
