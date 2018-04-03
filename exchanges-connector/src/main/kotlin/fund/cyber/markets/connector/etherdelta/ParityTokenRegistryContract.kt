package fund.cyber.markets.connector.etherdelta

import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tuples.generated.Tuple5
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import rx.Observable
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Callable

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
class ParityTokenRegistryContract : Contract {

    protected constructor(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {}

    protected constructor(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {}

    fun getRegisteredEvents(transactionReceipt: TransactionReceipt): List<RegisteredEventResponse> {
        val event = Event("Registered",
                Arrays.asList(object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Utf8String>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<RegisteredEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = RegisteredEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.tla = eventValues.indexedValues[0].value as ByteArray
            typedResponse.id = eventValues.indexedValues[1].value as BigInteger
            typedResponse.addr = eventValues.nonIndexedValues[0].value as String
            typedResponse.name = eventValues.nonIndexedValues[1].value as String
            responses.add(typedResponse)
        }
        return responses
    }

    fun registeredEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<RegisteredEventResponse> {
        val event = Event("Registered",
                Arrays.asList(object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Utf8String>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = RegisteredEventResponse()
            typedResponse.log = log
            typedResponse.tla = eventValues.indexedValues[0].value as ByteArray
            typedResponse.id = eventValues.indexedValues[1].value as BigInteger
            typedResponse.addr = eventValues.nonIndexedValues[0].value as String
            typedResponse.name = eventValues.nonIndexedValues[1].value as String
            typedResponse
        }
    }

    fun getUnregisteredEvents(transactionReceipt: TransactionReceipt): List<UnregisteredEventResponse> {
        val event = Event("Unregistered",
                Arrays.asList(object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }),
                Arrays.asList())
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<UnregisteredEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = UnregisteredEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.tla = eventValues.indexedValues[0].value as ByteArray
            typedResponse.id = eventValues.indexedValues[1].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun unregisteredEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<UnregisteredEventResponse> {
        val event = Event("Unregistered",
                Arrays.asList(object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }),
                Arrays.asList())
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = UnregisteredEventResponse()
            typedResponse.log = log
            typedResponse.tla = eventValues.indexedValues[0].value as ByteArray
            typedResponse.id = eventValues.indexedValues[1].value as BigInteger
            typedResponse
        }
    }

    fun getMetaChangedEvents(transactionReceipt: TransactionReceipt): List<MetaChangedEventResponse> {
        val event = Event("MetaChanged",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }, object : TypeReference<Bytes32>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                }))
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<MetaChangedEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = MetaChangedEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.id = eventValues.indexedValues[0].value as BigInteger
            typedResponse.key = eventValues.indexedValues[1].value as ByteArray
            typedResponse.value = eventValues.nonIndexedValues[0].value as ByteArray
            responses.add(typedResponse)
        }
        return responses
    }

    fun metaChangedEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<MetaChangedEventResponse> {
        val event = Event("MetaChanged",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }, object : TypeReference<Bytes32>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                }))
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = MetaChangedEventResponse()
            typedResponse.log = log
            typedResponse.id = eventValues.indexedValues[0].value as BigInteger
            typedResponse.key = eventValues.indexedValues[1].value as ByteArray
            typedResponse.value = eventValues.nonIndexedValues[0].value as ByteArray
            typedResponse
        }
    }

    fun getNewOwnerEvents(transactionReceipt: TransactionReceipt): List<NewOwnerEventResponse> {
        val event = Event("NewOwner",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList())
        val valueList = extractEventParametersWithLog(event, transactionReceipt)
        val responses = ArrayList<NewOwnerEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = NewOwnerEventResponse()
            typedResponse.log = eventValues.log
            typedResponse.old = eventValues.indexedValues[0].value as String
            typedResponse.current = eventValues.indexedValues[1].value as String
            responses.add(typedResponse)
        }
        return responses
    }

    fun newOwnerEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<NewOwnerEventResponse> {
        val event = Event("NewOwner",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList())
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParametersWithLog(event, log)
            val typedResponse = NewOwnerEventResponse()
            typedResponse.log = log
            typedResponse.old = eventValues.indexedValues[0].value as String
            typedResponse.current = eventValues.indexedValues[1].value as String
            typedResponse
        }
    }

    fun token(_id: BigInteger): RemoteCall<Tuple5<String, String, BigInteger, String, String>> {
        val function = Function("token",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_id)),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Address>() {

                }))
        return RemoteCall(
                Callable {
                    val results = executeCallMultipleValueReturn(function)
                    Tuple5(
                            results[0].value as String,
                            results[1].value as String,
                            results[2].value as BigInteger,
                            results[3].value as String,
                            results[4].value as String)
                })
    }

    fun setOwner(_new: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "setOwner",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_new)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun register(_addr: String, _tla: String, _base: BigInteger, _name: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "register",
                Arrays.asList(org.web3j.abi.datatypes.Address(_addr),
                        org.web3j.abi.datatypes.Utf8String(_tla),
                        org.web3j.abi.datatypes.generated.Uint256(_base),
                        org.web3j.abi.datatypes.Utf8String(_name)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun setFee(_fee: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "setFee",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_fee)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun meta(_id: BigInteger, _key: ByteArray): RemoteCall<ByteArray> {
        val function = Function("meta",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_id),
                        org.web3j.abi.datatypes.generated.Bytes32(_key)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                }))
        return executeRemoteCallSingleValueReturn(function, ByteArray::class.java)
    }

    fun registerAs(_addr: String, _tla: String, _base: BigInteger, _name: String, _owner: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "registerAs",
                Arrays.asList(org.web3j.abi.datatypes.Address(_addr),
                        org.web3j.abi.datatypes.Utf8String(_tla),
                        org.web3j.abi.datatypes.generated.Uint256(_base),
                        org.web3j.abi.datatypes.Utf8String(_name),
                        org.web3j.abi.datatypes.Address(_owner)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun fromTLA(_tla: String): RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>> {
        val function = Function("fromTLA",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Utf8String(_tla)),
                Arrays.asList(object : TypeReference<Uint256>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Address>() {

                }))
        return RemoteCall(
                Callable {
                    val results = executeCallMultipleValueReturn(function)
                    Tuple5(
                            results[0].value as BigInteger,
                            results[1].value as String,
                            results[2].value as BigInteger,
                            results[3].value as String,
                            results[4].value as String)
                })
    }

    fun owner(): RemoteCall<String> {
        val function = Function("owner",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun drain(): RemoteCall<TransactionReceipt> {
        val function = Function(
                "drain",
                Arrays.asList(),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun tokenCount(): RemoteCall<BigInteger> {
        val function = Function("tokenCount",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun unregister(_id: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "unregister",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_id)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun fromAddress(_addr: String): RemoteCall<Tuple5<BigInteger, String, BigInteger, String, String>> {
        val function = Function("fromAddress",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_addr)),
                Arrays.asList(object : TypeReference<Uint256>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint256>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Address>() {

                }))
        return RemoteCall(
                Callable {
                    val results = executeCallMultipleValueReturn(function)
                    Tuple5(
                            results[0].value as BigInteger,
                            results[1].value as String,
                            results[2].value as BigInteger,
                            results[3].value as String,
                            results[4].value as String)
                })
    }

    fun setMeta(_id: BigInteger, _key: ByteArray, _value: ByteArray): RemoteCall<TransactionReceipt> {
        val function = Function(
                "setMeta",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_id),
                        org.web3j.abi.datatypes.generated.Bytes32(_key),
                        org.web3j.abi.datatypes.generated.Bytes32(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun fee(): RemoteCall<BigInteger> {
        val function = Function("fee",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    class RegisteredEventResponse {
        var log: Log? = null

        var tla: ByteArray? = null

        var id: BigInteger? = null

        var addr: String? = null

        var name: String? = null
    }

    class UnregisteredEventResponse {
        var log: Log? = null

        var tla: ByteArray? = null

        var id: BigInteger? = null
    }

    class MetaChangedEventResponse {
        var log: Log? = null

        var id: BigInteger? = null

        var key: ByteArray? = null

        var value: ByteArray? = null
    }

    class NewOwnerEventResponse {
        var log: Log? = null

        var old: String? = null

        var current: String? = null
    }

    companion object {
        private val BINARY = "606060405260008054600160a060020a03191633600160a060020a0316179055670de0b6b3a7640000600455341561003657600080fd5b61125a806100456000396000f3006060604052600436106100cf5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663044215c681146100d457806313af4035146101ec57806366b42dcb1461020d57806369fe0e2d146102cb5780637958533a146102e15780637b1a547c1461030c578063891de9ed146103c15780638da5cb5b146104b15780639890220b146104e05780639f181b5e146104f3578063a02b161e14610506578063b72e717d1461051c578063dd93890b14610597578063ddca3f43146105b3575b600080fd5b34156100df57600080fd5b6100ea6004356105c6565b604051600160a060020a038087168252604082018590528216608082015260a060208201818152906060830190830187818151815260200191508051906020019080838360005b83811015610149578082015183820152602001610131565b50505050905090810190601f1680156101765780820380516001836020036101000a031916815260200191505b50838103825285818151815260200191508051906020019080838360005b838110156101ac578082015183820152602001610194565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b5097505050505050505060405180910390f35b34156101f757600080fd5b61020b600160a060020a0360043516610779565b005b341561021857600080fd5b6102b760048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f0160208091040260200160405190810160405281815292919060208401838380828437509496506107ef95505050505050565b604051901515815260200160405180910390f35b34156102d657600080fd5b61020b600435610807565b34156102ec57600080fd5b6102fa600435602435610827565b60405190815260200160405180910390f35b341561031757600080fd5b6102b760048035600160a060020a03169060446024803590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284378201915050505050509190803590602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052818152929190602084018383808284375094965050509235600160a060020a0316925061085f915050565b34156103cc57600080fd5b61041260046024813581810190830135806020601f82018190048102016040519081016040528181529291906020840183838082843750949650610b9595505050505050565b604051858152600160a060020a038086166020830152604082018590528216608082015260a06060820181815290820184818151815260200191508051906020019080838360005b8381101561047257808201518382015260200161045a565b50505050905090810190601f16801561049f5780820380516001836020036101000a031916815260200191505b50965050505050505060405180910390f35b34156104bc57600080fd5b6104c4610cbd565b604051600160a060020a03909116815260200160405180910390f35b34156104eb57600080fd5b61020b610ccc565b34156104fe57600080fd5b6102fa610d26565b341561051157600080fd5b61020b600435610d2d565b341561052757600080fd5b61053b600160a060020a0360043516610f35565b60405185815260408101849052600160a060020a038216608082015260a0602082018181529060608301908301878181518152602001915080519060200190808383600083811015610149578082015183820152602001610131565b34156105a257600080fd5b61020b600435602435604435610ff7565b34156105be57600080fd5b6102fa6110ab565b60006105d06110b1565b60006105da6110b1565b6000806003878154811015156105ec57fe5b906000526020600020906006020190508060000160009054906101000a9004600160a060020a03169550806001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106ae5780601f10610683576101008083540402835291602001916106ae565b820191906000526020600020905b81548152906001019060200180831161069157829003601f168201915b5050505050945080600201549350806003018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107545780601f1061072957610100808354040283529160200191610754565b820191906000526020600020905b81548152906001019060200180831161073757829003601f168201915b505050600490930154979996985094969095600160a060020a03909116945092505050565b60005433600160a060020a03908116911614610794576107ec565b600054600160a060020a0380831691167f70aea8d848e8a90fb7661b227dc522eb6395c3dac71b63cb59edd5c9899b236460405160405180910390a360008054600160a060020a031916600160a060020a0383161790555b50565b60006107fe858585853361085f565b95945050505050565b60005433600160a060020a03908116911614610822576107ec565b600455565b600060038381548110151561083857fe5b60009182526020808320948352600691909102909301600501909252506040902054919050565b6000600454341015610870576107fe565b600160a060020a03861660009081526001602052604090205486901561089557610b8b565b8580516003146108a457610b89565b866002816040518082805190602001908083835b602083106108d75780518252601f1990920191602091820191016108b8565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051908190039020541561091657610b87565b600380546001810161092883826110c3565b9160005260206000209060060201600060a06040519081016040908152600160a060020a03808f168352602083018e90529082018c9052606082018b90528916608082015291905081518154600160a060020a031916600160a060020a03919091161781556020820151816001019080516109a79291602001906110f4565b50604082015181600201556060820151816003019080516109cc9291602001906110f4565b5060808201516004919091018054600160a060020a031916600160a060020a03928316179055600354908c1660009081526001602052604090819020829055909250600291508a90518082805190602001908083835b60208310610a415780518252601f199092019160209182019101610a22565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040519081900390205560035460001901886040518082805190602001908083835b60208310610aad5780518252601f199092019160209182019101610a8e565b6001836020036101000a038019825116818451161790925250505091909101925060409150505180910390207f25074d730da65a10e171fe5589d2182313ef00da38d23a9ae3b78923568bdf2d8b89604051600160a060020a038316815260406020820181815290820183818151815260200191508051906020019080838360005b83811015610b47578082015183820152602001610b2f565b50505050905090810190601f168015610b745780820380516001836020036101000a031916815260200191505b50935050505060405180910390a3600193505b505b505b5095945050505050565b6000806000610ba26110b1565b60008060016002886040518082805190602001908083835b60208310610bd95780518252601f199092019160209182019101610bba565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902054039550600386815481101515610c1f57fe5b906000526020600020906006020190508060000160009054906101000a9004600160a060020a0316945080600201549350806003018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156107545780601f1061072957610100808354040283529160200191610754565b600054600160a060020a031681565b60005433600160a060020a03908116911614610ce757610d24565b33600160a060020a03166108fc30600160a060020a0316319081150290604051600060405180830381858888f193505050501515610d2457600080fd5b565b6003545b90565b60005433600160a060020a03908116911614610d48576107ec565b80600382815481101515610d5857fe5b90600052602060002090600602016001016040518082805460018160011615610100020316600290048015610dc45780601f10610da2576101008083540402835291820191610dc4565b820191906000526020600020905b815481529060010190602001808311610db0575b505091505060405180910390207f96e76fa77fea85d8abeb7533fdb8288c214bb1dcf1f867c8f36a95f1f509c17560405160405180910390a360016000600383815481101515610e1057fe5b60009182526020808320600690920290910154600160a060020a03168352820192909252604001812055600380546002919083908110610e4c57fe5b90600052602060002090600602016001016040518082805460018160011615610100020316600290048015610eb85780601f10610e96576101008083540402835291820191610eb8565b820191906000526020600020905b815481529060010190602001808311610ea4575b50509283525050602001604051809103902060009055600381815481101515610edd57fe5b6000918252602082206006909102018054600160a060020a031916815590610f086001830182611172565b6002820160009055600382016000610f209190611172565b506004018054600160a060020a031916905550565b6000610f3f6110b1565b6000610f496110b1565b600160a060020a038516600090815260016020526040812054600380546000199092019650829187908110610f7a57fe5b90600052602060002090600602019050806001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106ae5780601f10610683576101008083540402835291602001916106ae565b8233600160a060020a031660038281548110151561101157fe5b6000918252602090912060046006909202010154600160a060020a031614611038576110a5565b8160038581548110151561104857fe5b60009182526020808320878452600560069093020191909101905260409081902091909155839085907f7991c63a749706fd298fc2387764d640be6e714307b6357b1d3c2ce35cba3b529085905190815260200160405180910390a35b50505050565b60045481565b60206040519081016040526000815290565b8154818355818115116110ef576006028160060283600052602060002091820191016110ef91906111b6565b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061113557805160ff1916838001178555611162565b82800160010185558215611162579182015b82811115611162578251825591602001919060010190611147565b5061116e929150611214565b5090565b50805460018160011615610100020316600290046000825580601f1061119857506107ec565b601f0160209004906000526020600020908101906107ec9190611214565b610d2a91905b8082111561116e578054600160a060020a031916815560006111e16001830182611172565b60028201600090556003820160006111f99190611172565b50600481018054600160a060020a03191690556006016111bc565b610d2a91905b8082111561116e576000815560010161121a5600a165627a7a723058207d521e91242f4e3ce1fbbbd364bf5b404e318879cabde31a319f7e21e85fec700029"

        fun deploy(web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): RemoteCall<ParityTokenRegistryContract> {
            return Contract.deployRemoteCall(ParityTokenRegistryContract::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, "")
        }

        fun deploy(web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): RemoteCall<ParityTokenRegistryContract> {
            return Contract.deployRemoteCall(ParityTokenRegistryContract::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, "")
        }

        fun load(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): ParityTokenRegistryContract {
            return ParityTokenRegistryContract(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        fun load(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): ParityTokenRegistryContract {
            return ParityTokenRegistryContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }
    }
}

