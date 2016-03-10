# Installing ORDS

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

ORDS requires at least one PostgreSQL database server to be set available. 

Server addresses are defined in the serverConfig.xml configuration file.

Connection settings for the database are defined in two places:

* hibernate.cfg.xml
* ords.properties

(We're assuming here a single properties file setup for ORDS - see the Configuration guide - for advanced setups each API can be deployed and configured separately)

## Configuration

First, set the ORDS_CONF_DIR environment variable to the location where you wish to store configuration files

In the ORDS_CONF_DIR directory you will need, at a minimum:

* hibernate.cfg.xml
* ords.properties
* shiro.ini
* config.xml
* serverConfig.xml

Examples of these can be found in this repository; further details are in the configuration guide.



