Web3 = require("web3");
request = require('request');
const uuidV1 = require('uuid/v1');
const parityClientUrl = "http://127.0.0.1:8545";
console.log(parityClientUrl)
const zooClientUrl = "http://localhost:2181";
const debugMode = 1;
web3 = new Web3(new Web3.providers.HttpProvider(parityClientUrl));
var exchangeName = 'EtherDelta';

var ABIString = '[{\"constant\":false,\"inputs\":[{\"name\":\"tokenGet\",\"type\":\"address\"},{\"name\":\"amountGet\",' +
    '\"type\":\"uint256\"},{\"name\":\"tokenGive\",\"type\":\"address\"},{\"name\":\"amountGive\",\"type\":\"uint256\"},' +
    '{\"name\":\"expires\",\"type\":\"uint256\"},{\"name\":\"nonce\",\"type\":\"uint256\"},{\"name\":\"user\",\"type\":' +
    '\"address\"},{\"name\":\"v\",\"type\":\"uint8\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":\"s\",\"type\":' +
    '\"bytes32\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"trade\",\"outputs\":[],\"payable\":false,\"type' +
    '\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenGet\",\"type\":\"address\"},{\"name\":\"amountGet' +
    '\",\"type\":\"uint256\"},{\"name\":\"tokenGive\",\"type\":\"address\"},{\"name\":\"amountGive\",\"type\":\"uint256' +
    '\"},{\"name\":\"expires\",\"type\":\"uint256\"},{\"name\":\"nonce\",\"type\":\"uint256\"}],\"name\":\"order\",' +
    '\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":' +
    '\"address\"},{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"orderFills\",\"outputs\":[{\"name\":\"\",\"type\":' +
    '\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenGet\",' +
    '\"type\":\"address\"},{\"name\":\"amountGet\",\"type\":\"uint256\"},{\"name\":\"tokenGive\",\"type\":\"address' +
    '\"},{\"name\":\"amountGive\",\"type\":\"uint256\"},{\"name\":\"expires\",\"type\":\"uint256\"},{\"name\":\"nonce' +
    '\",\"type\":\"uint256\"},{\"name\":\"v\",\"type\":\"uint8\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":\"s' +
    '\",\"type\":\"bytes32\"}],\"name\":\"cancelOrder\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{' +
    '\"constant\":false,\"inputs\":[{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"withdraw\",\"outputs\":[],' +
    '\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"token\",\"type\":\"address' +
    '\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"depositToken\",\"outputs\":[],\"payable\":false,\"type' +
    '\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"tokenGet\",\"type\":\"address\"},{\"name\":\"amountGet' +
    '\",\"type\":\"uint256\"},{\"name\":\"tokenGive\",\"type\":\"address\"},{\"name\":\"amountGive\",\"type\":\"uint256' +
    '\"},{\"name\":\"expires\",\"type\":\"uint256\"},{\"name\":\"nonce\",\"type\":\"uint256\"},{\"name\":\"user\",' +
    '\"type\":\"address\"},{\"name\":\"v\",\"type\":\"uint8\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":' +
    '\"s\",\"type\":\"bytes32\"}],\"name\":\"amountFilled\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],' +
    '\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"address\"},{' +
    '\"name\":\"\",\"type\":\"address\"}],\"name\":\"tokens\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],' +
    '\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"feeMake_\",\"type\":' +
    '\"uint256\"}],\"name\":\"changeFeeMake\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant' +
    '\":true,\"inputs\":[],\"name\":\"feeMake\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,' +
    '\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"feeRebate_\",\"type\":\"uint256\"}],\"name\":' +
    '\"changeFeeRebate\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],' +
    '\"name\":\"feeAccount\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function' +
    '\"},{\"constant\":true,\"inputs\":[{\"name\":\"tokenGet\",\"type\":\"address\"},{\"name\":\"amountGet\",\"type' +
    '\":\"uint256\"},{\"name\":\"tokenGive\",\"type\":\"address\"},{\"name\":\"amountGive\",\"type\":\"uint256\"},{' +
    '\"name\":\"expires\",\"type\":\"uint256\"},{\"name\":\"nonce\",\"type\":\"uint256\"},{\"name\":\"user\",\"type' +
    '\":\"address\"},{\"name\":\"v\",\"type\":\"uint8\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":\"s\",' +
    '\"type\":\"bytes32\"},{\"name\":\"amount\",\"type\":\"uint256\"},{\"name\":\"sender\",\"type\":\"address\"}],' +
    '\"name\":\"testTrade\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function' +
    '\"},{\"constant\":false,\"inputs\":[{\"name\":\"feeAccount_\",\"type\":\"address\"}],\"name\":\"changeFeeAccount' +
    '\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"feeRebate' +
    '\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function\"},{\"constant' +
    '\":false,\"inputs\":[{\"name\":\"feeTake_\",\"type\":\"uint256\"}],\"name\":\"changeFeeTake\",\"outputs\":[],' +
    '\"payable\":false,\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"admin_\",\"type\":\"address' +
    '\"}],\"name\":\"changeAdmin\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":false,' +
    '\"inputs\":[{\"name\":\"token\",\"type\":\"address\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":' +
    '\"withdrawToken\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{' +
    '\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"orders\",\"outputs\":[{' +
    '\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],' +
    '\"name\":\"feeTake\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function' +
    '\"},{\"constant\":false,\"inputs\":[],\"name\":\"deposit\",\"outputs\":[],\"payable\":true,\"type\":\"function' +
    '\"},{\"constant\":false,\"inputs\":[{\"name\":\"accountLevelsAddr_\",\"type\":\"address\"}],\"name\":' +
    '\"changeAccountLevelsAddr\",\"outputs\":[],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs' +
    '\":[],\"name\":\"accountLevelsAddr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,' +
    '\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"token\",\"type\":\"address\"},{\"name\":' +
    '\"user\",\"type\":\"address\"}],\"name\":\"balanceOf\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],' +
    '\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"admin\",\"outputs\":[{' +
    '\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"type\":\"function\"},{\"constant\":true,\"inputs\":[{' +
    '\"name\":\"tokenGet\",\"type\":\"address\"},{\"name\":\"amountGet\",\"type\":\"uint256\"},{\"name\":\"tokenGive' +
    '\",\"type\":\"address\"},{\"name\":\"amountGive\",\"type\":\"uint256\"},{\"name\":\"expires\",\"type\":\"uint256' +
    '\"},{\"name\":\"nonce\",\"type\":\"uint256\"},{\"name\":\"user\",\"type\":\"address\"},{\"name\":\"v\",\"type\":' +
    '\"uint8\"},{\"name\":\"r\",\"type\":\"bytes32\"},{\"name\":\"s\",\"type\":\"bytes32\"}],\"name\":' +
    '\"availableVolume\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"type\":\"function' +
    '\"},{\"inputs\":[{\"name\":\"admin_\",\"type\":\"address\"},{\"name\":\"feeAccount_\",\"type\":\"address\"},{' +
    '\"name\":\"accountLevelsAddr_\",\"type\":\"address\"},{\"name\":\"feeMake_\",\"type\":\"uint256\"},{\"name\":' +
    '\"feeTake_\",\"type\":\"uint256\"},{\"name\":\"feeRebate_\",\"type\":\"uint256\"}],\"payable\":false,\"type\":' +
    '\"constructor\"},{\"payable\":false,\"type\":\"fallback\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,' +
    '\"name\":\"tokenGet\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amountGet\",\"type\":\"uint256\"},{' +
    '\"indexed\":false,\"name\":\"tokenGive\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amountGive\",\"type' +
    '\":\"uint256\"},{\"indexed\":false,\"name\":\"expires\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"nonce' +
    '\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"user\",\"type\":\"address\"}],\"name\":\"Order\",\"type\":' +
    '\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"tokenGet\",\"type\":\"address\"},{' +
    '\"indexed\":false,\"name\":\"amountGet\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"tokenGive\",\"type' +
    '\":\"address\"},{\"indexed\":false,\"name\":\"amountGive\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":' +
    '\"expires\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"nonce\",\"type\":\"uint256\"},{\"indexed\":false,' +
    '\"name\":\"user\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"v\",\"type\":\"uint8\"},{\"indexed\":false,' +
    '\"name\":\"r\",\"type\":\"bytes32\"},{\"indexed\":false,\"name\":\"s\",\"type\":\"bytes32\"}],\"name\":\"Cancel\",' +
    '\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"tokenGet\",\"type\":\"address' +
    '\"},{\"indexed\":false,\"name\":\"amountGet\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"tokenGive\",' +
    '\"type\":\"address\"},{\"indexed\":false,\"name\":\"amountGive\",\"type\":\"uint256\"},{\"indexed\":false,\"name' +
    '\":\"get\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"give\",\"type\":\"address\"}],\"name\":\"Trade\",' +
    '\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"token\",\"type\":\"address' +
    '\"},{\"indexed\":false,\"name\":\"user\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amount\",\"type\":' +
    '\"uint256\"},{\"indexed\":false,\"name\":\"balance\",\"type\":\"uint256\"}],\"name\":\"Deposit\",\"type\":\"event' +
    '\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"token\",\"type\":\"address\"},{\"indexed' +
    '\":false,\"name\":\"user\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"},{' +
    '\"indexed\":false,\"name\":\"balance\",\"type\":\"uint256\"}],\"name\":\"Withdraw\",\"type\":\"event\"}]';

var ABI = JSON.parse(ABIString);
var ContractAddress = '0x8d12A197cB00D4747a1fe03395095ce2A5CC6819';
var contract = web3.eth.contract(ABI).at(ContractAddress);

var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    KeyedMessage = kafka.KeyedMessage,
    Client = kafka.Client,
    // client = new Client(zooClientUrl),
    client = new Client('localhost:2181'),
    producer = new Producer(client),
    topicName = 'TRADES-'+exchangeName;
var payloads = [
    { topic: topicName, messages: '', key: null, partition: 0  },
];

var finalContract = initContractTokensArray();
var createKafkaTopic = initKafkaTopic();

function initKafkaTopic() {
    producer.createTopics([topicName], false, function (err, data) {
        console.log(data);
        console.log(err);
    });
}

function initContractTokensArray() {
    request('https://raw.githubusercontent.com/etherdelta/etherdelta.github.io/master/config/main.json', function (error, response, body) {
        if (!error && response.statusCode == 200) {
            var etherdeltaContract = JSON.parse(body);
            finalContract = new Contract(etherdeltaContract);
            return finalContract
        } else {
            console.log('!!! Invalid contract !!!\n' + error)
        }
    })
};

producer.on('ready', function(){
});
producer.on('error', function(err){});

var myEvent = contract.Trade({}, {fromBlock: 'latest', toBlock: 'latest'});
myEvent.watch(function(error, result){

    var tokenGet = result.args.tokenGet;
    var amountGet = +result.args.amountGet.c.join('.');
    var tokenGive = result.args.tokenGive;
    var amountGive = +result.args.amountGive.c.join('.');
    var spotPrice = amountGive/amountGet;

    if(getTokenByAddr(tokenGet) && getTokenByAddr(tokenGive)){
        var trade = new Trade(new TradeResponse(tokenGet, tokenGive, amountGet, amountGive))
        var kafkaMessage = JSON.stringify(trade);
        if(debugMode){
            console.log(kafkaMessage)
        } else {
            producer.send(
                [{ topic: topicName, messages: kafkaMessage, key: null, partition: 0  },],
                function(err, data){
                    console.log(data)
            });
        }
    } else {
        console.log('could not init tokens array')
    }
});

var TradeResponse = function (tokenGet, tokenGive, amountGet, amountGive) {
    this.tokenGet = tokenGet;
    this.tokenGive = tokenGive;
    this.amountGet = amountGet;
    this.amountGive = amountGive;
}

var Contract = function (contract) {
    this.contractEtherDeltaAddrs = getArrayOfContractAddresses(contract.contractEtherDeltaAddrs),
        this.tokens = getArrayOfContractTokens(contract.tokens)
}

var ContractAddr = function (contractAddr) {
    this.addr = contractAddr.addr,
        this.info = contractAddr.info

}

var Token = function (tokenFromContract) {
    this.addr = tokenFromContract.addr,
        this.name = tokenFromContract.name,
        this.decimals = tokenFromContract.decimals
}

var TokensPairInitializer = function (firstCurrency, secondCurrency) {
    firstCurrency = getTokenByAddr(firstCurrency).name
    secondCurrency = getTokenByAddr(secondCurrency).name
    this.reverted = false;

    if(isTokenEthereum(firstCurrency)){
        this.pair = new TokensPair(secondCurrency, firstCurrency);
    } else if (isTokenEthereum(secondCurrency)){
        this.pair = new TokensPair(firstCurrency, secondCurrency);
        this.reverted = true;
    } else {
        var compareResult = compareTokens(firstCurrency, secondCurrency);
        if(compareResult == -1 || compareResult == 0){
            this.pair = new TokensPair(firstCurrency, secondCurrency);
        } else {
            this.pair = new TokensPair(secondCurrency, firstCurrency);
            this.reverted = true;
        }
    }
}

var TokensPair = function(base, quote) {
    this.base = base;
    this.quote = quote;
}

var Trade = function (trade) {
    this.tradeId = uuidV1();
    this.exchange = exchangeName;
    var currentDate = new Date();
    this.timestamp = currentDate.toISOString();
    this.type = "BUY";
    var pairInitializer = new TokensPairInitializer(trade.tokenGet, trade.tokenGive);
    this.pair = pairInitializer.pair
    if(pairInitializer.reverted){
        this.baseAmount = trade.amountGive;
        this.quoteAmount = trade.amountGet;
        this.type = "SELL"
    } else {
        this.baseAmount = trade.amountGet;
        this.quoteAmount = trade.amountGive;
    }
    debugger;
    this.spotPrice = this.quoteAmount/this.baseAmount;
    this.epoch_h = Math.floor(Math.floor(currentDate/1000/60)/60);
}

function getArrayOfContractAddresses(contractAddrs) {
    var addresses = [];
    for (var i = 0; i < contractAddrs.length; i++) {
        addresses.push(new ContractAddr(contractAddrs[i]));
    }
    return addresses;
}

function getArrayOfContractTokens(contractTokens) {
    var tokens = [];
    for (var i = 0; i < contractTokens.length; i++) {
        tokens.push(new Token(contractTokens[i]));
    }
    return tokens;
}

function getTokenByAddr(value) {
    if(finalContract){
        var arr = finalContract.tokens

        for (var i = 0; i < arr.length; i++) {
            if (arr[i].addr + '' == value + '') {
                return arr[i];
            }
        }
    }
}

function isTokenEthereum(token) {
    return token == 'ETH'
}

function compareTokens(first, second) {
    if(first<second){
        return -1
    } else if(first>second){
        return 1
    } else {
        return 0
    }
}