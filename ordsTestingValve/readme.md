Tomcat User Valve
================

A quick tomcat valve which sets a remote user on a request based on a user's entry
in a form. It works on Tomcat 5/6.

Installation
------------

Drop the jar file into the server/lib folder at the toplevel of the Tomcat distribution.
As it needs to be in the same classloader as org.apache.catalina.valves.ValveBase which
is normally in catalina.jar

Configuration
-------------

In your tomcat server configuration file (eg server.xml) add a Valve element to a <Engine>,
<Host> or <Context>. The requestURI attribute specifies the request URI which must match
to require a user parameter on the request.
  
    <!-- Simulates RemoteUser login using WebAuth and Shibboleth for testing purposes-->
	<Valve className="uk.ac.ox.it.ords.RemoteUserValve" requestURI="/Shibboleth.sso/Login" />

Notes
-----

See http://stackoverflow.com/questions/7553967/