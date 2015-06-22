# Build an image using the Dockerfile at current location
# use command: sudo docker build -t bbweb .

FROM ubuntu:14.04

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update
RUN apt-get install -y vim less net-tools inetutils-ping curl git nmap socat dnsutils netcat tree htop unzip sudo software-properties-common

#Install Oracle Java 8 and MongoDB.
RUN add-apt-repository ppa:webupd8team/java -y
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' > /etc/apt/sources.list.d/mongodb.list
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get update
RUN apt-get install -y oracle-java8-installer mongodb-org
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

###----

RUN useradd -ms /bin/bash bbweb

#Add bbweb zip file
USER bbweb
WORKDIR /home/bbweb
ADD bbweb_start.sh /home/bbweb/bbweb_start.sh

RUN mkdir -p /home/bbweb/application
WORKDIR /home/bbweb/application

ADD bbweb-0.1-SNAPSHOT.zip /home/bbweb/application/bbweb-0.1-SNAPSHOT.zip
RUN unzip bbweb-0.1-SNAPSHOT.zip
ADD email.conf /home/bbweb/application/bbweb-0.1-SNAPSHOT/conf/email.conf

# run mongodb in /opt/bbweb_docker
# /usr/bin/mongod --config /opt/bbweb_docker/mongod.conf &

EXPOSE 9000

USER root
WORKDIR /home/bbweb

# to run the image:
#
# docker run -d -p 9000:9000 -v /opt/bbweb_docker/mongodb_data:/data/db bbweb /bin/bash -c "(/usr/bin/mongod &) && su bbweb -c '/home/bbweb/bbweb_start.sh'"

