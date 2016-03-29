# Configuring ORDS

## Overview

ORDS consists of multiple services that are deployed in an application server such as Tomcat. 

Each service requires several pieces of configuration in order to work correctly:

* A *shiro.ini* configuration that defines the service security according for Apache Shiro
* A *hibernate.cfg.xml* configuration that defines the database connection properties for Hibernate
* A *serverConfig.xml* configuration that specifies the database servers used for user databases
* A *servicename.properties* configuration that defines service-specific properties
* Session cache configuration (optional)

Finally, there is a *config.xml* that simply identifies the correct paths for these files.

Optionally, if you are using local usernames and passwords, you'll need an *ehcache.xml* file to cache sessions.

Examples of all of these configuration files can be found in the config-examples folder.

## Where ORDS looks for configuration data

ORDS uses Apache Commons Configuration to load properties. 

The format of *config.xml* is defined by Apache Commons Configuration, and typically will look like this:

```xml

	<?xml version="1.0" encoding="ISO-8859-1" ?>
	<configuration>
		<!-- Allow environment variables to override file-based defaults -->
	   <system/>
	   <env />
   
	 	<!-- Server-level properties. Properties set here will override package properties. -->
		<properties fileName="/etc/ordsconfig/user.properties" config-optional="true"/>
  
	  	<!-- This is the default properties file -->
	  	<properties fileName="user.properties"/>
	</configuration>
```

This identifies that the service should load "user.properties" from the local resource path, and override it with any settings at /etc/ordsconfig/user.properties.

### Using just one properties file for all services

You can specify a single combined properties file for all ORDS service by specifying the ORDS_CONF_DIR environment variable to have all ORDS services load the 
same *config.xml* file. This can then point to a single *ords.properties* configuration file rather than individual service-specific files. In this way you can 
also use a common Shiro and Hibernate configuration as described below.

### Modifying the location of shiro.ini

You can specify the location of *shiro.ini* from within a properties file (either one for the specific service, or in a combined properties file for all services).

The location of *shiro.ini* defaults to the resource path of the service package, but can be specified using the property *ords.shiro.configuration*. Set this
to the full path to your *shiro.ini* file.

### Modifying the location of hibernate.cfg.xml

As with Shiro, this can be set by specifying a path using the *ords.hibernate.configuration* property.

## ServerConfig.xml

This file specifies the available database servers.

The following configuration specifies one database server for "trial" databases, and a different one for "full" databases.

```
	<?xml version="1.0" encoding="UTF-8"?>
	<ordsServerConfig hostName="127.6.10.2:5432">
	    <serverList>
	        <server ip="127.6.10.2:5432" name="127.6.10.2:5432" full="false"/>
	        <server ip="127.6.10.3:5432" name="127.6.10.2:5433" full="true"/>
	    </serverList>
	</ordsServerConfig>
```

## Apache Shiro Configuration

Apache Shiro is configured according to the Apache Shiro documentation; however a typical configuration looks like the one below. This uses form-based authentication using hashed passwords. (This is ORDS' default suggested configuration if you haven't got an organisation-wide SSO)

```
	[main]
	ssl.enabled = false

	#
	# Setup for generating hashed passwords
	#
	hashService = org.apache.shiro.crypto.hash.DefaultHashService
	hashService.hashIterations = 500000
	hashService.hashAlgorithmName = SHA-256
	hashService.generatePublicSalt = true

	passwordService = org.apache.shiro.authc.credential.DefaultPasswordService
	passwordService.hashService = $hashService

	passwordMatcher = org.apache.shiro.authc.credential.PasswordMatcher
	passwordMatcher.passwordService = $passwordService


	#
	# JDBC Realm for using Ords DB for permissions
	#
	ds = org.postgresql.jdbc2.optional.SimpleDataSource
	ds.serverName = localhost
	ds.user = ords
	ds.password = ords
	ds.databaseName = ordstest

	jdbcRealm = org.apache.shiro.realm.jdbc.JdbcRealm
	jdbcRealm.dataSource = $ds
	jdbcRealm.permissionsLookupEnabled = true
	jdbcRealm.authenticationQuery = SELECT token FROM ordsuser WHERE principalname = ?
	jdbcRealm.userRolesQuery = SELECT role FROM userrole WHERE principalname = ?
	jdbcRealm.permissionsQuery = SELECT permission FROM permissions WHERE role = ?
	jdbcRealm.credentialsMatcher = $passwordMatcher

	#
	# In this implementation we want to cache sessions but not authz or authn.
	#
    jdbcRealm.authenticationCachingEnabled = false
    jdbcRealm.authorizationCachingEnabled = false

	#
	# Set up realms and explicit ordering
	#
	securityManager.realms = $jdbcRealm

	#
	# SSO Sessions and cookies. This is iumportant as we need to
	# ensure we can share the same session between our different WARs
	#
	cacheManager = org.apache.shiro.cache.ehcache.EhCacheManager
	#
	# Set this to the actual location of ehcache.xml
	#
	cacheManager.cacheManagerConfigFile = /etc/ords2/ehcache.xml
	securityManager.cacheManager = $cacheManager

	sessionManager = org.apache.shiro.web.session.mgt.DefaultWebSessionManager
	sessionDAO = org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO
	sessionManager.sessionDAO = $sessionDAO

	securityManager.sessionMode = native

	cookie = org.apache.shiro.web.servlet.SimpleCookie
	cookie.name = SSOcookie
	cookie.path = /
	#
	# Set this to the actual domain of your server
	#
	cookie.domain = localhost
	sessionManager.sessionIdCookie = $cookie

	securityManager.sessionManager = $sessionManager

	[urls]
	/** = authc[permissive]

```

Note that in this example we also need to provide an *ehcache.xml* configuration to cache user sessions. For example, we've used this configuration to use a file cache:

```
	<?xml version="1.0" encoding="UTF-8"?>
	<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	         xsi:noNamespaceSchemaLocation="ehcache.xsd" updateCheck="true"
	         monitoring="autodetect" dynamicConfig="true">

	    <diskStore path="java.io.tmpdir" />

	    <cacheManagerPeerProviderFactory
	            class="net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory"
	            properties="peerDiscovery=automatic,
	                        multicastGroupAddress=230.0.0.1,
	                        multicastGroupPort=4446, timeToLive=1"
	            propertySeparator="," />

	    <cacheManagerPeerListenerFactory
	            class="net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory" />

	    <cache name="shiro-activeSessionCache" maxElementsInMemory="600"
	           eternal="true" overflowToDisk="true" memoryStoreEvictionPolicy="LFU">
	        <cacheEventListenerFactory
	                class="net.sf.ehcache.distribution.RMICacheReplicatorFactory" />
	    </cache>

	    <defaultCache maxElementsInMemory="100" eternal="true"
	                  overflowToDisk="true" memoryStoreEvictionPolicy="LFU">
	    </defaultCache>
	</ehcache>
```

The next example uses Oxford's Webauth SSO for security, but can be substituted with another authentication method, such as HTTP BASIC, LDAP or CAS.

*Note: The JDBCRealm is used to read user permissions from the ORDS database; this should be the same in any configuration.*

```

	#
	# SSO Realm using REMOTE_USER principles and shibboleth affiliations
	# as auth tokens
	#
	SSORealm = uk.ac.ox.it.ords.security.SSORealm

	#
	# JDBC Realm for using Ords DB for permissions
	#
	ds = org.postgresql.jdbc2.optional.SimpleDataSource
	ds.serverName = 127.6.10.2:5432
	ds.user = ords
	ds.password = passwordgoeshere
	ds.databaseName = ords
 
	jdbcRealm = org.apache.shiro.realm.jdbc.JdbcRealm
	jdbcRealm.dataSource = $ds
	jdbcRealm.permissionsLookupEnabled = true
	jdbcRealm.userRolesQuery = SELECT role FROM userrole WHERE principalname = ?
	jdbcRealm.permissionsQuery = SELECT permission FROM permissions WHERE role = ?

	#
	# Set up realms and explicit ordering
	#
	securityManager.realms = $SSORealm, $jdbcRealm

	#
	# Filters
	#
	ssoFilter = uk.ac.ox.it.ords.security.SSOFilter

	[urls]
	/** = noSessionCreation, ssoFilter[permissive]
```

The [permissive] directive is important here as we need to allow anonymous access to some GET URLs, while others are protected. 

Not all Shiro realm configurations support this directive, in which case authentication will be required for all methods.

## Hibernate Configuration

The Hibernate configuration is used to define the connection to the common ORDS database used for persisting service-specific data such as users and projects.

Note that for some specific functionality, ORDS services also need direct JDBC connection details, specified in the properties file.

## ORDS combined properties

The following is an example of a combined properties file for a single ORDS installation. It specifies the location for Hibernate, Shiro, and ServerConfig files, and defines the common JDBC and email connection details (the email message configurations have been omitted for brevity).

```

	#
	# Hibernate Config Location - common database settings
	#
	ords.hibernate.configuration=/var/lib/openshift/564358a22d52710b1200006c/app-root/data/hibernate.cfg.xml

	#
	# Shiro config location - common auth settings
	#
	ords.shiro.configuration=file:/var/lib/openshift/564358a22d52710b1200006c/app-root/data/shiro.ini

	#
	# Server Config Location - common server config
	#
	ords.server.configuration=/var/lib/openshift/564358a22d52710b1200006c/app-root/data/serverConfig.xml

	#
	# JDBC Database configuration - used by Statistics and Schema Editor
	#
	ords.database.user=zzzz
	ords.database.password=zzzzz
	ords.database.rootdbuser=xxxx
	ords.database.rootdbpassword=yyyy
	ords.odbc.masterpassword=zzzz
	postgresConnectorFile=postgresql-8.4-702.jdbc4.jar
	ords.database.name=ords
	ords.database.server.host=127.6.10.2:5432

	#
	# The localuser suffix is used to automatically map
	# a principal to the localuser role; a localuser has
	# permission to create new projects.
	#
	ords.localsuffix=ox.ac.uk

	#
	# Common email details
	#
	mail.smtp.auth=true
	mail.smtp.starttls.enable=true
	mail.smtp.host=smtp.myhost.net
	mail.smtp.port=587
	mail.smtp.from=daemons@sysdev.oucs.ox.ac.uk
	mail.smtp.to=
	mail.smtp.subject=Information from ORDS
	mail.smtp.username=zzzzz
	mail.smtp.password=zzzzz

```
