CREATE TABLE markets.ticker (

  pair frozen<tokenpair>,
  windowDuration bigint,
  exchange text,
  timestampTo timestamp,
  timestampFrom timestamp,
  avgPrice decimal,
  open decimal,
  close decimal,
  minPrice decimal,
  maxPrice decimal,
  baseAmount decimal,
  quoteAmount decimal,
  tradeCount bigint,

  PRIMARY KEY ((pair, windowDuration), exchange, timestampTo)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  timestampTo DESC
);