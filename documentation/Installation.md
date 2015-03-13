# Installation

## Required Software and Packages

### Play Framework

Intalling the Play Framework standalone distribution is optional. It is not required since
all commands can be done through SBT.

See [Installing Play](http://www.playframework.com/documentation/2.2.x/Installing).

### SBT

Install SBT on Ubuntu:

```bash
wget http://apt.typesafe.com/repo-deb-build-0002.deb
sudo dpkg -i repo-deb-build-0002.deb
sudo apt-get update
sudo apt-get install sbt
```

### Protocol Buffers

Using version 2.6.0 and the package for Ubuntu is currently 2.5.0. Have to download source and build locally.

```bash
apt-get install build-essential
wget https://protobuf.googlecode.com/svn/rc/protobuf-2.6.0.tar.gz
tar xvfz protobuf-2.6.0.tar.gz
cd protobuf-2.6.0
./configure && make install
```

### MongoDB

Install on Ubuntu:

```bash
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' \
    | sudo tee /etc/apt/sources.list.d/mongodb.list
sudo apt-get update
sudo apt-get install -y mongodb-org
```

### NodeJS

Install NodeJS following these instructions:

```
https://www.digitalocean.com/community/tutorials/how-to-install-node-js-on-an-ubuntu-14-04-server
```

### To Start the Application

```bash
play clean stage
target/universal/stage/bin/bbweb
```

To use an HTTPS:

```bash
target/universal/stage/bin/bbweb -Dhttps.port=9443 -Dhttp.port=disabled
```

---

[Back to top](../README.md)
