# Cyber Markets [![Travis CI Build Status Badge](https://api.travis-ci.org/cyberFund/cyber-markets.svg?branch=development)](https://travis-ci.org/cyberFund/cyber-markets)
🚀 [KR] Crawler for cryptoasset's markets

## Quick Start

To build and run Cyber Markets from sources run following commands:
```bash
git clone https://github.com/cyberFund/cyber-markets.git
cd cyber-markets/
./gradlew bootRun -Drethink.host="127.0.0.1" -Drethink.port=28015
```
You can skip providing rethink properties, if they are same as above

To run rethink use following command:
```bash
docker run --name cm-rethink \
    -p 28015:28015 \
    -p 29015:29015 \
    -p 8081:8080 \
    -v "$PWD:/data" \
    -d rethinkdb
```

## Wiki

For addition information, please, visit [Wiki pages](https://github.com/cyberFund/cyber-markets/wiki)