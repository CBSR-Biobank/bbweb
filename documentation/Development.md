# Development

## MongoDB
                                      | shell command
--------------------------------------|----------------------------------------------
Drop the Evensourced Journal databse: | `mongo biobank-web --eval "db.dropDatabase()"`
Dump the Biobank database:            | `mongodump --db biobank-web --out ~/tmp`
Restore the Biobank database:         | `mongorestore ~/tmp/biobank-web/bbweb.bson`

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

## NPM

Add dependencies to `package.json` in the root directory. Use command `web-assets:jseNpmNodeModules`
to download the modules.

OR: `web-assets:web-node-modules`

---

[Back to top](../README.md)
