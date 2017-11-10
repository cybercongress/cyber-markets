package fund.cyber.markets.processing

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.functions.max
import org.apache.spark.sql.functions.min
import org.apache.spark.sql.functions.sum
import org.apache.spark.sql.types.DataTypes.StringType
import org.apache.spark.sql.types.DataTypes.TimestampType
import org.apache.spark.sql.types.DecimalType
import org.apache.spark.sql.types.StructType

/**
 * @author Ibragimov Ruslan
 * @since 0.2.4
 */
object TickerStreaming {
    @JvmStatic
    fun main(args: Array<String>) {
        val spark = SparkSession
            .builder()
            .appName("Simple Application")
            .getOrCreate()

        val dataset = spark
            .readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", "127.0.0.1:9092")
            .option("subscribePattern", "TRADES-.*")
            .load()

        val schema = StructType()
            .add("tradeId", StringType)
            .add("exchange", StringType)
            .add("type", StringType)
            .add("baseToken", StringType)
            .add("quoteToken", StringType)
            .add("timestamp", TimestampType)
            .add("baseAmount", DecimalType())
            .add("quoteAmount", DecimalType())
            .add("spotPrice", DecimalType())

        val trades = dataset
            .select(
                col("key").cast("string"),
                col("topic").cast("string"),
                col("timestamp").cast("timestamp"),
                from_json(col("value").cast("string"), schema).alias("parsed")
            )
            .withColumn("time", col("parsed.timestamp").cast("timestamp"))

        trades.printSchema()

        trades
            .withWatermark(
                "time",
                "10 minutes"
            )
            .groupBy(
                functions.window(
                    trades.col("parsed.timestamp"),
                    "5 seconds",
                    "5 seconds"
                ),
                col("topic")
            )
            .agg(
                sum("parsed.baseAmount").`as`("baseVolume"),
                sum("parsed.quoteAmount").`as`("quoteAmount"),
                min("parsed.spotPrice").`as`("lowestAsk"),
                max("parsed.spotPrice").`as`("highestBid")
            )
            .`as`("value")
            .writeStream()
//            .format("console")
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("topic", "tickers-5")
            .option("checkpointLocation", "/home/yoda/dev/cyberFund/checkpoint")
            .outputMode("complete")
            .start()
            .awaitTermination()

        spark.stop()
    }
}
