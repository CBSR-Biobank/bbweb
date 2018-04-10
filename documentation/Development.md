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

* To start the server in production mode, use the following commands:

    ```sh
    npm run dist-build
    APPLICATION_SECRET="abcdefghijklmnopqrstuvwxyz" sbt start
    ```

    In the browser open the following link: http://localhost:9000/#!/

* To start the application in development mode, use the following commands:

    ```sh
    npm run dev-build
    npm run dev-start-server
    ```

    In the browser open the following link: http://localhost:9000/#!/

* When modifying code in the client application, use the following commands to start the server and build the
  client side and also watch for changes in the client files (uses `webpack-dev-server`):

    In one shell:

    ```sh
    npm run dev-start-server
    ```

    In another shell:

    ```sh
    npm run dev
    ```

    In the browser open the following link: http://localhost:8080/#!/. *Note that this link is for a
    different port.*

### Testing

#### Server Tests

Use the command `sbt test` to run the server tests.

##### Scalatest

###### Run one or more tests within a Suite

Use the `-z` flag to run a test with the specified substring:

```sbt
test-only *__CLASS_NAME__ -- -z "sub string"
```

###### Scala code coverage

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

#### Client Tests

Using the [Karma](https://karma-runner.github.io/1.0/index.html) and [Jasmine](https://jasmine.github.io/)
packages for client unit tests.

* Use the following command to start tests on the command line:

    ```sh
    npm run test
    ```

* Use the following command to start tests in Chrome:

    ```sh
    npm run test-in-chrome
    ```

    Press the `Debug` button, open a JavaScript file in DevTools to place a breakpoint, and reload the page.
    Execution stops at the breakpoint.

##### Code Coverage

Uses `istanbul-instrumenter-loader` and `karma-coverage-istanbul-reporter` npm packages to perform code coverage analysis on the client code. After running `npm test` open the `coverage/index.html` file in your browser to inpect the results.

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

# Souce code documentation

## Server

To generate the documentationt from the server source code, run the following command:

```sh
sbt doc
```

The documentation can now be opened in a web broser by opening this link:

```
file:///<_path_to_project_>/target/scala-2.12/api/index.html
```

Where `<_path_to_project_>` is the root directory for the project.



## Client

To generate the documentation from the client source code, run the following command:

```sh
npm run jsdoc
```

The documentation can now be opened in a web broser by opening this link:

```
file:///<_path_to_project_>/dcumentation/js/index.htm
```

Where `<_path_to_project_>` is the root directory for the project.

# Translations

Internationalization is done with [angular-gettext](https://angular-gettext.rocketeer.be/). To include the
latest translations, the command `grunt nggettext_compile` must be run from the command line.
