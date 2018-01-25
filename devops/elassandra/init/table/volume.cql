CREATE TABLE markets.volume (

  tokenSymbol text,
  windowDuration bigint,
  exchange text,
  value decimal,
  timestampTo timestamp,
  timestampFrom timestamp,

  PRIMARY KEY ((tokenSymbol, windowDuration), exchange, timestampTo)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  timestampTo DESC
);