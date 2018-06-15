CREATE KEYSPACE IF NOT EXISTS markets WITH REPLICATION = {'class' : 'NetworkTopologyStrategy','WITHOUT_REPLICATION' : 1};

CREATE TYPE IF NOT EXISTS markets.tickerprice (
  open decimal,
  close decimal,
  min decimal,
  max decimal
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
  epochDay bigint,

  volume blob,
  baseVolume blob,
  price blob,

  PRIMARY KEY ((tokenSymbol, epochDay, interval), timestampFrom)
)
WITH CLUSTERING ORDER BY (
    timestampFrom DESC
);

CREATE TABLE IF NOT EXISTS markets.trade_last_timestamp (
  id text,
  value bigint,

  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS markets.token_price (
  tokenSymbol text,
  method text,

  timestampFrom bigint,
  timestampTo bigint,
  epochHourTo bigint,

  values blob,

  PRIMARY KEY ((tokenSymbol, epochHourTo), timestampTo)
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
  PRIMARY KEY ((exchange, pair, epochMinute), tradeId)
)
WITH CLUSTERING ORDER BY (
  tradeId DESC
);

CREATE TABLE IF NOT EXISTS markets.trade_temporary (
  exchange text,
  pair frozen<tokenpair>,
  type text,
  epochMinute bigint,
  timestamp timestamp,
  tradeId text,
  baseAmount decimal,
  quoteAmount decimal,
  price decimal,
  PRIMARY KEY (epochMinute, exchange, pair, tradeId)
) WITH default_time_to_live = 604800;

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