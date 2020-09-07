FROM arm32v7/maven:3-adoptopenjdk-14 as base
RUN apt update -q
RUN apt install openjdk-11-jdk-headless -y

FROM arm32v7/maven:3-adoptopenjdk-14 as builder
RUN apt update -q
RUN apt install openjdk-11-jdk-headless -y

ARG MAVEN_VERSION=3.6.3
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries
RUN mkdir -p /usr/share/maven
RUN curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz
RUN (cd /usr/share/maven && tar xvfz /tmp/apache-maven.tar.gz)
RUN mkdir -p /app
ADD . /app
RUN (cd /app && /usr/share/maven/bin/mvn install)

FROM arm32v7/maven:3-adoptopenjdk-14
RUN apt update -q
RUN apt install openjdk-11-jdk-headless -y
#ADD src/main/bin go.sh
#RUN chmod 0755 go.sh
COPY --from=builder /app/target/ReportBot-1.0-SNAPSHOT.jar /.
CMD ["java", "-Xmx16m", "-jar", "ReportBot-1.0-SNAPSHOT.jar"]
