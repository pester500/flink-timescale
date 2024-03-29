FROM openjdk:15-slim

ENV FLINK_HOME=/opt/flink
ENV FLINK_VERSION 1.16.0
ENV FLINK_TGZ_URL=https://archive.apache.org/dist/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-scala_2.12.tgz
ENV GOSU_VERSION 1.12
ENV PATH=$FLINK_HOME/bin:/usr/local/bin:/usr/bin:$PATH
ENV START_FILE entrypoint.sh
ENV STOP_JOB_FILE stop_job.sh

WORKDIR $FLINK_HOME

RUN set -ex; \
  apt-get update; \
  apt-get --no-install-recommends -y install wget; \
  wget -nv -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)"; \
  chmod +x /usr/local/bin/gosu; \
  gosu nobody true;\
  groupadd --system --gid=9999 flink && \
  useradd --system --home-dir $FLINK_HOME --uid=9999 --gid=flink flink;\
  set -ex; \
  wget -nv -O flink.tgz "$FLINK_TGZ_URL"; \
  tar -xf flink.tgz --strip-components=1; \
  rm flink.tgz; \
  mkdir -p $FLINK_HOME/logs; \
  chown -R flink:flink .; \
  rm -rf $FLINK_HOME/opt/*; \
  rm -rf /var/lib/apt/lists/* ;    

# Configure container
COPY scripts/$START_FILE /
COPY scripts/$STOP_JOB_FILE /
COPY crimes/build/libs/crimes.jar $FLINK_HOME
COPY logs/build/libs/logs.jar $FLINK_HOME

RUN chmod +x /$START_FILE; \
  chmod +x /$STOP_JOB_FILE; \
  chmod +x $FLINK_HOME/crimes.jar; \
  chmod +x $FLINK_HOME/logs.jar;

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 6121 6122 6123 6125 8081

#sleep 1 year
CMD ["sleep", "31536000"]
