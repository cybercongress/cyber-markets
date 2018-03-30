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
  exchange text,
  pair frozen<tokenpair>,
  type text,
  epochHour bigint,
  timestamp timestamp,
  tradeId text,
  baseAmount decimal,
  quoteAmount decimal,
  price decimal,
  PRIMARY KEY ((exchange, pair, epochHour), timestamp)
)
WITH CLUSTERING ORDER BY (
  timestamp DESC
);

CREATE TABLE IF NOT EXISTS markets.order (
  exchange text,
  pair frozen<tokenpair>,
  type text,
  epochHour bigint,
  timestamp timestamp,
  orderId text,
  amount decimal,
  price decimal,
  PRIMARY KEY ((exchange, pair, epochHour), timestamp)
)
WITH CLUSTERING ORDER BY (
  timestamp DESC
);