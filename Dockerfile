FROM eclipse-temurin:17-jdk-jammy AS builder

ARG SBT_VERSION=1.12.3
ENV SBT_HOME=/opt/sbt
ENV PATH="${SBT_HOME}/bin:${PATH}"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p "${SBT_HOME}" \
    && curl -fsSL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" \
    | tar -xz -C "${SBT_HOME}" --strip-components=1

WORKDIR /app

COPY build.sbt ./
COPY project ./project
RUN sbt -batch update

COPY app ./app
COPY conf ./conf
COPY test ./test

RUN sbt -batch clean stage

FROM eclipse-temurin:17-jre-jammy

WORKDIR /opt/app

RUN useradd --system --create-home --home-dir /opt/app --shell /usr/sbin/nologin appuser

COPY --from=builder /app/target/universal/stage/ ./

RUN chown -R appuser:appuser /opt/app

USER appuser

EXPOSE 9000

CMD ["./bin/scala-parking-payment-service"]