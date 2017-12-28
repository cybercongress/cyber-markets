CREATE TABLE markets.ticker (

  tokensPair frozen<tokenpair>,
  exchange text,
  timestampFrom timestamp,
  timestampTo timestamp,
  windowDuration bigint,
  baseAmount decimal,
  quoteAmount decimal,
  price decimal,
  minPrice decimal,
  maxPrice decimal,
  tradeCount bigint,

  PRIMARY KEY ((tokensPair, windowDuration), exchange, timestampTo)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  timestampTo DESC
);