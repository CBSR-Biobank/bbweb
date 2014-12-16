# Development

## MongoDB
                                      | shell command
--------------------------------------|----------------------------------------------
Drop the Evensourced Journal databse: | `mongo bbweb --eval "db.dropDatabase()"`
Dump the Biobank database:            | `mongodump --db bbweb --out ~/tmp`
Restore the Biobank database:         | `mongorestore ~/tmp/bbweb/bbweb.bson`

## Eclipse

IDE:

* [Scala IDE](http://scala-ide.org/)

Useful plugins:

* [Workspace Mechanic](https://code.google.com/a/eclipselabs.org/p/workspacemechanic/)
* [Play2 plug-in](https://github.com/scala-ide/scala-ide-play2/wiki#installing-the-play2-plug-in-recommended)

SBT command to create Eclipse IDE project files:

    sbt 'eclipse with-source=true'

## Bootstrap

* [Bootstrap 3.0.0 themes](http://bootswatch.com/)

## Debug

To prevent users being logged out when the application is restarted, EHCACHE is configured to cache
to disk. This must be disabled for the production server (see [conf/ehcache.xml]
(../conf/ehcache.xml), tags: `defaultCache -> diskPersistent`).

## Git

Delete remote branch:

```bash
git branch -d -r origin/__branch_name__
```

## Squash Commits

Using your own forked GitHub repository for the BBweb project. In this example the forked remote is
named `nelson`, and the topic branch is named `nelson-dev`.

```bash
git rebase -i HEAD~6
git push nelson +nelson-dev
```

## GitHub Markdown

*  [Grip]  (https://github.com/joeyespo/grip) - Preview GitHub Markdown files like Readme.md locally
   before committing them

## Logging

* To enable logging at the Domain or Service layers, edit the file [conf/logger.xml]
  (../conf/logger.xml).

* To enable TEST logging at the Domain or Service layers, edit the file [conf/logback-test.xml]
  (../conf/logback-test.xml).

* The Akka logging configuration for the web application is in [conf/application.conf]
  (../conf/application.conf). It is in [conf/reference.conf] (../conf/reference.conf) for the testing
  environment.

## H2 in Memory Database

Use the `h2-browser` sbt command to create and connect to the H2 database prior to starting the
web application.

Use `run -Dbbweb.query.db.load=true` sbt command to reload an empty query database.

Use `run -Dbbweb.query.db.load=false` sbt command to use an already loaded query database.

Use `run -DapplyEvolutions.default=true` sbt command to automatically apply database evolutions.

## Scalatest

Tag a test by adding the following import:

```scala
import org.scalatest.Tag
```

and the tag declaration to the test(s):

```scala
 "add a user" taggedAs(Tag("MyTag")) in {
```

Use the following sbt command to run tagged tests:

```sbt
test-only *__CLASS_NAME__ -- -n MyTag
```
To tag multiple tests, create a tag object and tag the tests using the object:

```scala
object MyTag extends Tag("MyTag")
...
 "add a user" taggedAs(MyTag) in {
...
```

## Scala code coverage

**sbt-scoverage** is used to determine code coverage. See the
[GitHub page](https://github.com/scoverage/sbt-scoverage)
for instructions on how to use it.

To generate the HTML report use the command:

```sh
sbt clean coverage test coverageReport
```

Or, within the SBT cli:

```sh
; clean; coverage; test; coverageReport
```

### Gtags

Using helm-gtags in Emacs. To generate a tags file the following is required:

1. Install [GNU Global](http://www.gnu.org/software/global/download.html) from source (in `/usr/local/src`).
1. Install exuberant-ctags:

    ```bash
    sudo apt-get install exuberant-ctags
    ```

1. Add the following line to `/usr/local/share/gtags/gtags.conf` in the `exuberant-ctags` section.

    ```
    :langmap=Scala\:.scala:\
    ```

1. Create the tags files (at the project root):

    ```bash
    find app -type f -print > /tmp/bbwebfiles \
    && find test -type f -print >> /tmp/bbwebfiles \
    && find jstest -type f -print >> /tmp/bbwebfiles
    gtags -v -f /tmp/bbwebfiles --gtagslabel ctags
    ```

See: https://gist.github.com/tsdeng/8451067

## NPM

Add dependencies to `package.json` in the root directory. Use command `web-assets:jseNpmNodeModules`
to download the modules.

OR: `web-assets:web-node-modules`

### ng-annotate

ng-annotate adds and removes AngularJS dependency injection annotations.

```sh
cd app/assets/javascripts
find . -name \*.js -exec echo "/usr/local/bin/ng-annotate --add {} -o {}.new && mv {}.new {}" \; | sed 's/\.\///g'  > tmp.sh
sh tmp.sh
```

### Client Tests

Using Krama and Jasmine for client unit tests. Also using Grunt to run the tests.

Client tests are found in the `jstest` directory since they don't pass Jshint inspection. Originally, they
were placed in `tests/assets/javascripts`, but when running SBT, errors were generated. Currently sbt-jshint
does not provide a way to use a `.jshintignore` file, so they had to be moved to the `jstest` directory.


---

[Back to top](../README.md)
