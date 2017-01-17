# Installation

## Required Software and Packages

### Java SE Development Kit 8

See page [Installing Java](InstallJava.md) for full instructions.

### Play Framework

Intalling the Play Framework standalone distribution is optional. It is not required since
all commands can be done through SBT.

See [Installing Play](https://www.playframework.com/documentation/2.5.x/Installing).

### SBT

Follow these instructions for Ubuntu: http://www.scala-sbt.org/0.13/tutorial/Installing-sbt-on-Linux.html

### MySQL

MySQL can be installed on Ubuntu with the following command:

```sh
sudo apt-get update
sudo apt-get install mysql-server
```

A database with 3 tables needs to be created on the MySQL server. See or change `akka-persistence-sql-async`
settings in `conf/application.conf`. These settings are:

| Setting                                         | Description                                                                                                                                |
|-------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `akka-persistence-sql-async.user`               | the database user name.                                                                                                                    |
| `akka-persistence-sql-async.password`           | the database user's password.                                                                                                              |
| `akka-persistence-sql-async.url`                | the database name. This is usually a string that starts with `jdbc:mysql://localhost/`. The database name is then appended to this prefix. |
| `akka-persistence-sql-async.metadata-table-name`| the database table used to store persistence metadata.                                                                                     |
| `akka-persistence-sql-async.journal-table-name` | the database table used to store persistence journal.                                                                                      |
| `akka-persistence-sql-async.snapshot-table-name`| the database table used to store persistence snapshots.                                                                                    |

Here is an example of the MySQL commands to create a database and user using the default values.

```mysql
create database bbweb;
create user 'bbweb_user'@'localhost' identified by 'bbweb_PWDD';
grant all privileges on bbweb.* to 'bbweb_user'@'localhost' with grant option;
```

See file `akka_persistence_schema.sql` in the root directory of this project for a sample script on how to
create the required database tables. Modify the table names used in this script to match the settings you
chose for the settings listed above.

### Administrator Email

Assign value `admin.email` in `confg/application.conf` to your administrator's email address. This will be the
email address to use to log into the application for the first time. The password is `testuser`. Change the
pasword for this user after the application is started.

### To Start the Application

First, create the application and generate an *application secret*.

```bash
sbt clean stage
sbt playGenerateSecret
```

Use `APPLICATION_SECRET` environment variable to start the server:

```bash
APPLICATION_SECRET="__value_generated_above__" target/universal/stage/bin/bbweb
```

Replace `__value_generated_above__` with the value generated from the `sbt playGenerateSecret` command.

To use an HTTPS:

```bash
APPLICATION_SECRET="__value_generated_above__" target/universal/stage/bin/bbweb -Dhttps.port=9443 -Dhttp.port=disabled
```

---

[Back to top](../README.md)
