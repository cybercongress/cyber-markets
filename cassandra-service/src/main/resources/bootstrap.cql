CREATE KEYSPACE IF NOT EXISTS markets WITH REPLICATION = {'class' : 'NetworkTopologyStrategy','DEVELOPMENT' : 1};

CREATE TYPE IF NOT EXISTS markets.tokenprice (
  value decimal
);

CREATE TABLE IF NOT EXISTS markets.ticker (
  tokenSymbol text,

  timestampFrom timestamp,
  timestampTo timestamp,
  interval bigint,

  volume frozen<map<text, map<text, decimal>>>,
  baseVolume frozen<map<text, map<text, decimal>>>,
  price frozen<map<text, map<text, tokenprice>>>,

  PRIMARY KEY (tokenSymbol, timestampFrom, interval)
);

CREATE TABLE IF NOT EXISTS markets.supply (
  tokenSymbol text,
  value decimal,
  totalValue decimal,

  PRIMARY KEY (tokenSymbol)
);

CREATE TYPE IF NOT EXISTS markets.tokenpair (
  base text,
  quote text
);

CREATE TABLE IF NOT EXISTS markets.trade (
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

  PRIMARY KEY (epoch_h, exchange, pairInitializer, tradeId)
)
WITH CLUSTERING ORDER BY (
  exchange ASC,
  pairInitializer ASC
);