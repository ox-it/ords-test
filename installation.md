# Installing ORDS

## Deploying binaries

You can find the latest binary releases here:

http://jcenter.bintray.com/uk/ac/ox/it/ords/

You will need binaries for:

* ords-audit-api
* ords-database-api
* ords-database-structure-api
* ords-project-api
* ords-statistics-api
* ords-user-api

Set the ORDS_CONF_DIR environment variable to point to the location of your configuration files, and follow the instructions below for setting up the database server(s) you wish to use.

## Building from source

ORDS consists of 6 REST API modules, and one user interface module. Each is built and packaged as a WAR for deployment to an application server.

The REST API modules are:

* ords-audit-api
* ords-database-api
* ords-database-structure-api
* ords-project-api
* ords-statistics-api
* ords-user-api

Each REST API module depends on a library for common security code:

* ords-security-common

This dependency is defined in the pom.xml for each of the API modules as usual; 
you should install this before trying to install the REST modules using:

    mvn install

The user interface is:

* ords-ui

Each package must be built as a WAR using Maven:

    mvn package

This will create the WAR, e.g. for the User API:

   target/api#1.0#user.war

Each .war file must be deployed to the webapps folder of the application server (e.g. Tomcat).

## Setting up the database

ORDS requires at least one PostgreSQL database server to be set available; you can also use one server for ORDS' internal metadata and project information, and another server for user's databases.

Server addresses and credentials are defined in the databaseservers.xml configuration file.

(We're assuming here a single properties file setup for ORDS - see the Configuration guide - for advanced setups each API can be deployed and configured separately.)

Note that we assume when building and testing that MD5 password checking is in place when connecting to the database - even from the
same server. Otherwise users can overwrite other user's data without being challenged.

This is configured in your pg_hba.conf file with the line:

    host    all             all             127.0.0.1/32            md5
    
By default many PostgreSQL installations will have this set to "Trust", which will not issue a password challenge.

We also assume you are using PostgreSQL versions 9.2 and higher. 

## Configuration

First, set the ORDS_CONF_DIR environment variable to the location where you wish to store configuration files

In the ORDS_CONF_DIR directory you will need, at a minimum:

* hibernate.cfg.xml
* ords.properties
* shiro.ini
* config.xml
* databaseservers.xml

Examples of these can be found in this repository; further details are in the configuration guide.



