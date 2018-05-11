CREATE KEYSPACE IF NOT EXISTS markets WITH REPLICATION = {'class' : 'NetworkTopologyStrategy','DEVELOPMENT' : 1};

CREATE TYPE IF NOT EXISTS markets.tokenprice (
  value decimal
);

CREATE TYPE IF NOT EXISTS markets.tokenpair (
  base text,
  quote text
);

CREATE TYPE IF NOT EXISTS markets.orderbook_order (
  type text,
  timestamp timestamp,
  amount decimal,
  price decimal,
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

CREATE TABLE IF NOT EXISTS markets.trade (
  exchange text,
  pair frozen<tokenpair>,
  type text,
  epochMinute bigint,
  timestamp timestamp,
  tradeId text,
  baseAmount decimal,
  quoteAmount decimal,
  price decimal,
  PRIMARY KEY ((exchange, pair), epochMinute)
)
WITH CLUSTERING ORDER BY (
  epochMinute DESC
);

CREATE TABLE IF NOT EXISTS "markets"."order" (
  exchange text,
  pair frozen<tokenpair>,
  type text,
  epochHour bigint,
  timestamp timestamp,
  orderId text,
  amount decimal,
  price decimal,
  PRIMARY KEY ((exchange, pair, epochHour), orderId)
)
WITH CLUSTERING ORDER BY (
  orderId DESC
);

CREATE TABLE IF NOT EXISTS markets.orderbook (
  exchange text,
  pair frozen<tokenpair>,
  epochHour bigint,
  timestamp timestamp,
  bids frozen<list<orderbook_order>>,
  asks frozen<list<orderbook_order>>,
  PRIMARY KEY ((exchange, pair, epochHour), timestamp)
)
WITH CLUSTERING ORDER BY (
  timestamp DESC
);