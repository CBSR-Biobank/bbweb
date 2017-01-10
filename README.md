[![Build Status](https://travis-ci.org/cbsrbiobank/bbweb.svg?branch=master)](https://travis-ci.org/cbsrbiobank/bbweb)
[![Coverage Status](https://coveralls.io/repos/github/cbsrbiobank/bbweb/badge.svg?branch=master)](https://coveralls.io/github/cbsrbiobank/bbweb?branch=master)

# Biobank Web Application

Biobank version 4 is a rewrite of the [Biobank] (https://github.com/cbsrbiobank/biobank) application
and meant to provide the majority of its functionality through a web browser based interface. It
uses [Domain Driven Design principles] (http://www.domainlanguage.com/ddd/) and employs a [CQRS]
(https://vaughnvernon.co/?page_id=168]) architecture (Command Query Responsibility Segregation).

In addition, version 4 includes enhancements to the domain model which provides better work flow
and an improved user experience.

Flatbed scanning is supported by having a separate dedicated desktop client, but this client's
functionality is focused only on scanning and decoding tubes etched with 2D DataMatrix barcodes.
Not all users will be required to install the desktop client.

1. [Dependencies](documentation/Dependencies.md)
1. [Architecture](documentation/Architecture.md)
1. [Installation](documentation/Installation.md)
1. [Development](documentation/Development.md)
1. [Notes](NOTES.md)
1. [Todo](Todo.md)
