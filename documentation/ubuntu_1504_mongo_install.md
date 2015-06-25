# Installation of MongoDB for Ubuntu 15.04

Since Ubuntu 15.04 switched *systemd* the MongoDB team says they won't support Ubuntu 15.04 and plan to
support 16.04 instead (see https://jira.mongodb.org/browse/SERVER-17742).

However, MongoDB can installed from the debian wheeze repository.

First make sure you remove the mongodb-org package and all its dependencies:

```sh
sudo apt-get purge mongodb-org
sudo apt-get autoremove
```

Remove the old mongodb.list you created:

```sh
sudo rm /etc/apt/sources.list.d/mongodb.list
```

Use the Debian repository instead:

```sh
echo "deb http://repo.mongodb.org/apt/debian wheezy/mongodb-org/3.0 main" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
```

Update and install again:

```sh
sudo apt-get update
sudo apt-get install -y mongodb-org
```

After that, you can succesffully start the server:

```sh
sudo service mongod start
```

