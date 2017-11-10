## Development

### Build fatjat for spark

```bash
./gradlew :stream-processing:shadowJar -t
```

### Run spark job

```bash
# pwd = spark/bin

./spark-submit --class "fund.cyber.markets.processing.TickerStreaming" --master local[*] local:/home/yoda/dev/IRus/cyber-markets/stream-processing/build/libs/stream-processing-all.jar
```

## Production

TBD



