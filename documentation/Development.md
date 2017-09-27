# Development

# Snapshot Migration

Snapshots can be considered a pure performance optimization and are not important from a functional point of
view for a successful recovery of application state. That is why it might be a valid alternative to do without
custom serializers and backwards compatibility in case of snapshots. Incompatible snapshots could simply be
deleted in case of an upgrade. Of course this results in a longer recovery time for the first restart after an
upgrade.

## Libraries and Frameworks

### Protocol Buffers

Google [protobuf](https://github.com/google/protobuf/) is used to save events to the Akka Persistence journal.
By using protobuf it is easier to modify the events as the system grows. If an event is changed an old
database can still be used with the application.

See the installation page for instructions on how to install on your computer.

#### ScalaPB

[ScalaPB](http://trueaccord.github.io/ScalaPB/generated-code.html) is used to generate the scala files from
the `proto` files.

### Bootstrap

* [Bootstrap 3.0.0 themes](http://bootswatch.com/)

### AngularJS

The code uses the style guide proposed by John Papa: [AngularJS Style Guide](https://github.com/johnpapa/angularjs-styleguide).

### NodeJS

Install NodeJS using NVM following these instructions:

* https://www.digitalocean.com/community/tutorials/how-to-install-node-js-on-an-ubuntu-14-04-server

## Development Environment

### SBT

Project [sbt-updated](https://github.com/rtimush/sbt-updates) is used to determine if any
dependencies can be updated. Use command `dependencyUpdates` to display what can be updated.

#### Squash Commits

Using your own forked GitHub repository for the BBweb project. In this example the forked remote is
named `nelson`, and the topic branch is named `nelson-dev`.

```bash
git rebase -i HEAD~6
git push nelson +nelson-dev
```

### NPM

Add dependencies to `package.json` in the root directory. Use command `npm install` to download the modules.

#### Global NPM packages

Install the following packages globally (`npm install -g <pacakge_name>`):

* `eslint`
* `eslint-plugin-jasmine`
* `npm-check-updates`

### GitHub Markdown

*  [Grip]  (https://github.com/joeyespo/grip) - Preview GitHub Markdown files like Readme.md locally
   before committing them

## Application

### Running

* To start the server in development mode, use the following command:

    ```sh
    npm run dev-start-server
    ```

* To build the client application, use the following command:

     ```sh
     npm run dev-build
     ```

    This must be done before the server is started.

* To modifying code in the client application, use the following command for hot module replacement:

     ```sh
     npm run dev-build
     ```

### Testing

#### Server Tests

Use the command `sbt test` to run the server tests.

#### Client Tests

Using [Karma](https://karma-runner.github.io/1.0/index.html) and [Jasmine](https://jasmine.github.io/) for
client unit tests.

* Use the following command to start tests on the command line:

    ```sh
    npm run test
    ```

* Use the following command to start tests in Chrome:

    ```sh
    npm run test-in-chrome
    ```

##### Code Coverage


*THIS SECTION NEEDS UPDATING AFTER MOVING TO WEBPACK*

##### Run tests in Chrome

Use the following command to start tests in Chrome:

```sh
npm run test
```

Press the `Debug` button, open a JavaScript file in DevTools to place a breakpoint, and reload the page.
Execution stops at the breakpoint.

### Debug

To prevent users being logged out when the application is restarted, EHCACHE is configured to cache
to disk. This must be disabled for the production server (see [conf/ehcache.xml]
(../conf/ehcache.xml), tags: `defaultCache -> diskPersistent`).

### Server Logging

* To enable logging at the Domain or Service layers, edit the file [conf/logger.xml]
  (../conf/logger.xml).

* To enable TEST logging at the Domain or Service layers, edit the file [conf/logback-test.xml]
  (../conf/logback-test.xml).

* The Akka logging configuration for the web application is in [conf/application.conf]
  (../conf/application.conf). It is in [conf/reference.conf] (../conf/reference.conf) for the testing
  environment.

## Scalatest

### Run one or more tests within a Suite

Use the `-z` flag to run a test with the specified substring:

```sbt
test-only *__CLASS_NAME__ -- -z "sub string"
```

## Scala code coverage

**sbt-scoverage** is used to determine code coverage. See the
[GitHub page](https://github.com/scoverage/sbt-scoverage)
for instructions on how to use it.

To generate the HTML report use the command:

```sh
sbt clean coverage test
sbt coverageReport
```

Or, within the SBT cli:

```sh
; clean; coverage; test; coverageReport
```

# Translations

Internationalization is done with [angular-gettext](https://angular-gettext.rocketeer.be/). To include the
latest translations, the command `grunt nggettext_compile` must be run from the command line.

## Docker

In the `tools\docker` folder of the project is a **Docker** file. This file can be used to create a docker
image to run the web application in. You also need:

* the ZIP file that is created when the web application is created with the `sbt dist` command,
* a valid email.conf file
* and the `bbweb_start.sh` script.

This docker image contains instructions to also install Oracle Java 8.

Run the command `docker build -t bbweb .` to build the docker image in the directory containing the
Dockerfile.

To keep data between appliction versions, the image uses a data volume to store the mongo database. On
`aicml-med.cs.ualberta.ca`, the database files are kept in directory `/opt/bbweb_docker/mongodb_data`. To make
backups of this database a mongo server must be started with the configuration pointing at the `mongod.conf`
file. Use the command:

```bash
/usr/bin/mongod --config /opt/bbweb_docker/mongod.conf &
```

The Dockerfile was built by using the following as an example:

* https://github.com/mingfang/docker-play/blob/master/Dockerfile
* http://stackoverflow.com/questions/25364940/how-to-create-docker-image-for-local-application-taking-file-and-value-parameter

Some docker commands:
* remove docker image by id: `docker rmi __image_id__`
* remove all existing containers: `docker rm $(docker ps -a -q)`
* stop a docker image: `docker stop __container_id__`

#### Test server

Once the docker image has been created on the test server, the docker container can be started using this
command:

```bash
sudo docker run -d -p 9000:9000 -v /opt/bbweb_docker/mongodb_data:/data/db bbweb /bin/bash -c "(/usr/bin/mongod &) && su bbweb -c '/home/bbweb/bbweb_start.sh'"
```

---

[Back to top](../README.md)
