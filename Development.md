# Development

## MongoDB

Drops the Evensourced Journal databse:

```bash
mongo biobank-web --eval "db.dropDatabase()"
```

Dump the Biobank database:

```bash
mongodump --db biobank-web --out ~/tmp
```

Restores the Biobank database:

```bash
mongorestore ~/tmp/biobank-web/bbweb.bson
```

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
to disk. This must be disabled for the production server (see
`conf/ehcache.xml/defaultCache/diskPersistent`).

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

* [Grip](https://github.com/joeyespo/grip) - Preview GitHub Markdown files like Readme locally before committing them

## Logging

* To enable logging at the Domain or Service layers, edit the file `conf/logger.xml`.

* To enable TEST logging at the Domain or Service layers, edit the file `conf/logback-test.xml`.

## In Memory Database

Use the `h2-browser` sbt command to connect to the H2 in memory database.
