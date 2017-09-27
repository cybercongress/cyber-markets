CREATE TABLE markets.trade (
    epoch_h int,
    tradeId text,
    exchange text,
    timestamp timestamp,
    type text,
    pairInitializer frozen<tokenpair>,
    baseAmount decimal,
    quoteAmount decimal,
    spotPrice decimal,
    reverted boolean,
    PRIMARY KEY ( epoch_h, exchange, pairInitializer, tradeId)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  pairInitializer ASC
);