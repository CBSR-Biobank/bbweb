# Eclipse

Useful plugins:

* [Workspace Mechanic](https://code.google.com/a/eclipselabs.org/p/workspacemechanic/)
* [Play2 plug-in](https://github.com/scala-ide/scala-ide-play2/wiki#installing-the-play2-plug-in-recommended)

SBT command to create Eclipse IDE project files:

    sbt 'eclipse with-source=true'

# Bootstrap

* [Bootstrap 3.0.0 themes](http://bootswatch.com/)

# Useful Commands

Drops the Evensourced Journal databse:

    mongo biobank-web --eval "db.dropDatabase()"

# Debug

To prevent users being logged out when the application is restarted, EHCACHE is configured to cache
to disk. This must be disabled for the production server (see
`conf/ehcache.xml/defaultCache/diskPersistent`).

# Git

* Delete remote branch: `git branch -d -r origin/__branch_name__`

