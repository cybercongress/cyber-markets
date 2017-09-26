CREATE TABLE markets.trade (
    epoch_h int,
    tradeId text,
    exchange text,
    timestamp timestamp,
    type text,
    pair frozen<tokenpair>,
    baseAmount decimal,
    quoteAmount decimal,
    spotPrice decimal,
    reverted boolean,
    PRIMARY KEY ( epoch_h, exchange, pair, tradeId)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  pair ASC
);