package fund.cyber.markets.connector.etherdelta

import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
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
class Erc20Contract : Contract {

    protected constructor(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {}

    protected constructor(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {}

    fun getTransferEvents(transactionReceipt: TransactionReceipt): List<TransferEventResponse> {
        val event = Event("Transfer",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<TransferEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = TransferEventResponse()
            typedResponse.log = eventValues.log
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._to = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun transferEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<TransferEventResponse> {
        val event = Event("Transfer",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = TransferEventResponse()
            typedResponse.log = log
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._to = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse
        }
    }

    fun getApprovalEvents(transactionReceipt: TransactionReceipt): List<ApprovalEventResponse> {
        val event = Event("Approval",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<ApprovalEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = ApprovalEventResponse()
            typedResponse.log = eventValues.log
            typedResponse._owner = eventValues.indexedValues[0].value as String
            typedResponse._spender = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun approvalEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<ApprovalEventResponse> {
        val event = Event("Approval",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = ApprovalEventResponse()
            typedResponse.log = log
            typedResponse._owner = eventValues.indexedValues[0].value as String
            typedResponse._spender = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse
        }
    }

    fun name(): RemoteCall<String> {
        val function = Function("name",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun approve(_spender: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "approve",
                Arrays.asList(Address(_spender),
                        Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun totalSupply(): RemoteCall<BigInteger> {
        val function = Function("totalSupply",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun transferFrom(_from: String, _to: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "transferFrom",
                Arrays.asList(Address(_from),
                        Address(_to),
                        Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun balances(param0: String): RemoteCall<BigInteger> {
        val function = Function("balances",
                Arrays.asList<Type<*>>(Address(param0)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun decimals(): RemoteCall<BigInteger> {
        val function = Function("decimals",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint8>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun allowed(param0: String, param1: String): RemoteCall<BigInteger> {
        val function = Function("allowed",
                Arrays.asList<Type<*>>(Address(param0),
                        Address(param1)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun balanceOf(_owner: String): RemoteCall<BigInteger> {
        val function = Function("balanceOf",
                Arrays.asList<Type<*>>(Address(_owner)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun symbol(): RemoteCall<String> {
        val function = Function("symbol",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun transfer(_to: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "transfer",
                Arrays.asList(Address(_to),
                        Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun allowance(_owner: String, _spender: String): RemoteCall<BigInteger> {
        val function = Function("allowance",
                Arrays.asList<Type<*>>(Address(_owner),
                        Address(_spender)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    class TransferEventResponse {
        var log: Log? = null

        var _from: String? = null

        var _to: String? = null

        var _value: BigInteger? = null
    }

    class ApprovalEventResponse {
        var log: Log? = null

        var _owner: String? = null

        var _spender: String? = null

        var _value: BigInteger? = null
    }

    companion object {
        private val BINARY = "6060604052341561000f57600080fd5b6040516107ae3803806107ae833981016040528080519190602001805182019190602001805191906020018051600160a060020a03331660009081526001602052604081208790558690559091019050600383805161007292916020019061009f565b506004805460ff191660ff8416179055600581805161009592916020019061009f565b505050505061013a565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e057805160ff191683800117855561010d565b8280016001018555821561010d579182015b8281111561010d5782518255916020019190600101906100f2565b5061011992915061011d565b5090565b61013791905b808211156101195760008155600101610123565b90565b610665806101496000396000f3006060604052600436106100ae5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde0381146100b3578063095ea7b31461013d57806318160ddd1461017357806323b872dd1461019857806327e235e3146101c0578063313ce567146101df5780635c6581651461020857806370a082311461022d57806395d89b411461024c578063a9059cbb1461025f578063dd62ed3e14610281575b600080fd5b34156100be57600080fd5b6100c66102a6565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101025780820151838201526020016100ea565b50505050905090810190601f16801561012f5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561014857600080fd5b61015f600160a060020a0360043516602435610344565b604051901515815260200160405180910390f35b341561017e57600080fd5b6101866103b0565b60405190815260200160405180910390f35b34156101a357600080fd5b61015f600160a060020a03600435811690602435166044356103b6565b34156101cb57600080fd5b610186600160a060020a03600435166104bc565b34156101ea57600080fd5b6101f26104ce565b60405160ff909116815260200160405180910390f35b341561021357600080fd5b610186600160a060020a03600435811690602435166104d7565b341561023857600080fd5b610186600160a060020a03600435166104f4565b341561025757600080fd5b6100c661050f565b341561026a57600080fd5b61015f600160a060020a036004351660243561057a565b341561028c57600080fd5b610186600160a060020a036004358116906024351661060e565b60038054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561033c5780601f106103115761010080835404028352916020019161033c565b820191906000526020600020905b81548152906001019060200180831161031f57829003601f168201915b505050505081565b600160a060020a03338116600081815260026020908152604080832094871680845294909152808220859055909291907f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259085905190815260200160405180910390a350600192915050565b60005481565b600160a060020a0380841660008181526002602090815260408083203390951683529381528382205492825260019052918220548390108015906103fa5750828110155b151561040557600080fd5b600160a060020a038085166000908152600160205260408082208054870190559187168152208054849003905560001981101561046a57600160a060020a03808616600090815260026020908152604080832033909416835292905220805484900390555b83600160a060020a031685600160a060020a03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8560405190815260200160405180910390a3506001949350505050565b60016020526000908152604090205481565b60045460ff1681565b600260209081526000928352604080842090915290825290205481565b600160a060020a031660009081526001602052604090205490565b60058054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561033c5780601f106103115761010080835404028352916020019161033c565b600160a060020a033316600090815260016020526040812054829010156105a057600080fd5b600160a060020a033381166000818152600160205260408082208054879003905592861680825290839020805486019055917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a350600192915050565b600160a060020a039182166000908152600260209081526040808320939094168252919091522054905600a165627a7a7230582014460711919f30fcd89e7b9de8b5f0bbff3c332f171a36f57ebe69eb37412b170029"

        fun deploy(web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, _initialAmount: BigInteger, _tokenName: String, _decimalUnits: BigInteger, _tokenSymbol: String): RemoteCall<Erc20Contract> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(Uint256(_initialAmount),
                    Utf8String(_tokenName),
                    Uint8(_decimalUnits),
                    Utf8String(_tokenSymbol)))
            return Contract.deployRemoteCall(Erc20Contract::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun deploy(web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger, _initialAmount: BigInteger, _tokenName: String, _decimalUnits: BigInteger, _tokenSymbol: String): RemoteCall<Erc20Contract> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(Uint256(_initialAmount),
                    Utf8String(_tokenName),
                    Uint8(_decimalUnits),
                    Utf8String(_tokenSymbol)))
            return Contract.deployRemoteCall(Erc20Contract::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun load(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): Erc20Contract {
            return Erc20Contract(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        fun load(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): Erc20Contract {
            return Erc20Contract(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }
    }
}
