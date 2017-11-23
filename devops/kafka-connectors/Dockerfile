# Container with application
FROM confluentinc/cp-kafka-connect:3.3.0
MAINTAINER Name <mgergalov@gmail.com>

ARG STREAM_REACTOR_URL=https://github.com/datamountaineer/stream-reactor/releases/download/0.3.0/kafka-connect-cassandra-0.3.0-3.3.0-all.tar.gz
RUN wget "${STREAM_REACTOR_URL}" -O stream-reactor.tar.gz \
    && tar -xzf stream-reactor.tar.gz -C /etc/kafka-connect/custom-plugins \
    && rm stream-reactor.tar.gz