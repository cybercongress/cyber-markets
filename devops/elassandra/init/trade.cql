CREATE TYPE IF NOT EXISTS markets.tokenpair (
    baseToken text,
    quoteToken text
);

CREATE TABLE markets.trade (
    epoch_h int,
    tradeId text,
    exchange text,
    date timestamp,
    tradeType text,
    pair frozen<tokenpair>,
    baseAmount decimal,
    quoteAmount decimal,
    spotPrice decimal,
    PRIMARY KEY ( epoch_h, exchange, pair, tradeId)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  pair ASC
);