# Kafka, Kafka Manager and Zookeeper
FROM java:openjdk-8-jdk

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y zookeeper wget supervisor dnsutils && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

# Build kafka
ENV SCALA_VERSION 2.11
ENV KAFKA_VERSION 0.10.2.1
ENV KAFKA_HOME /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"

RUN wget -q http://apache.mirrors.spacedump.net/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -O /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz && \
    tar xfz /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -C /opt && \
    rm /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz

ADD scripts/start-kafka.sh /usr/bin/start-kafka.sh
ADD supervisor/kafka.conf supervisor/zookeeper.conf /etc/supervisor/conf.d/

# Build kafka-manager
ENV ZK_HOSTS localhost:2181
ENV KM_VERSION 1.3.3.13
ENV KM_HOME /opt/kafka-manager-"$KM_VERSION"

RUN wget -q https://github.com/yahoo/kafka-manager/archive/"$KM_VERSION".tar.gz -O /tmp/kafka-manager-"${KM_VERSION}".tgz && \
    tar xfz /tmp/kafka-manager-"${KM_VERSION}".tgz -C /tmp && \
    cd /tmp/kafka-manager-"${KM_VERSION}" && \
    echo 'scalacOptions ++= Seq("-Xmax-classfile-name", "200")' >> build.sbt && ./sbt clean dist && \
    unzip  -d /opt ./target/universal/kafka-manager-"${KM_VERSION}".zip && \
    rm -fr /tmp/kafka-manager-"${KM_VERSION}" /root/.sbt /root/.ivy2

ADD scripts/start-km.sh /usr/bin/start-km.sh
ADD supervisor/km.conf /etc/supervisor/conf.d/

# 2181 is zookeeper, 9092 is kafka, 9000 is kafka manager
EXPOSE 2181 9092 9000

CMD ["supervisord", "-n"]
