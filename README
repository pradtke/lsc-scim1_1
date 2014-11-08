# SCIM Plugin for LSC project

This project contain some rudimentary code to explore a SCIM plugin for the LSC project (http://lsc-project.org)


# Setup a SCIM Server

To improve the plugin you'll need to setup a SCIM server.
UnboundId has a reference implementation available here: https://www.unboundid.com/downloads/scim-ri-1.8.0.zip
and some documentation here: https://code.google.com/p/scimsdk/wiki/Documentation.

Unfortunately the command line examples don't seem to be copy-n-pasteable, I believe because '--' has been converted into a em dash.

Here are the basics for starting your server

     ./tools/in-memory-scim-server --resourceMappingFile config/resources.xml --ldifFile ldif/spec-compat.ldif

    #Search for everyone
    curl --user bjensen:password http://localhost:8080/Users
    #Lookup by guid
    curl --user bjensen:password http://localhost:8080/Users/fecd5b57-0dfb-4663-b038-79953b1cf10d
    #Search by username
    curl --user bjensen:password 'http://localhost:8080/Users?filter=username+eq+"jsmith"'


# Using the SCIM plugin

## Copy it into your lib folder

    cp  lsc-scim1_1/target/lsc-scim-plugin-0.1-SNAPSHOT.jar ~/Downloads/lsc-2.1.1/lib/lsc-scim-plugin-0.1-SNAPSHOT.jar 

## Add other dependent libraries

The scim plugin uses packages some dependent libraries with it (using
the shade plugin). There may be some missing, or that aren't included
right and you'll need to copy them manually.

Jars to copy manually

    javax.ws.rs-api-2.0.1.jar

## Configuration

This is very much a work in progress. Here is an example configuration


```xml
   <pluginDestinationService implementationClass="org.lsc.plugins.connectors.scim1_1.Scim1_1UserDstService">
      <name>scim</name>
      <!-- Update this with a real SCIM connnection class -->
      <connection reference="src-jdbc" />
   </pluginDestinationService>
   <propertiesBasedSyncOptions>
      <mainIdentifier>srcBean.getDatasetFirstValueById("mail")</mainIdentifier>
      <defaultDelimiter>|</defaultDelimiter>
      <defaultPolicy>FORCE</defaultPolicy>
      <!-- TODO: determine a better policy for renaming ldap attributes to scim attributes -->
      <dataset>
         <name>username</name>
         <policy>KEEP</policy>
         <createValues>
            <string>srcBean.getDatasetFirstValueById("uid")</string>
         </createValues>
      </dataset>
      <dataset>
         <name>familyName</name>
         <policy>KEEP</policy>
         <createValues>
            <string>srcBean.getDatasetFirstValueById("sn")</string>
         </createValues>
      </dataset>
   </propertiesBasedSyncOptions>
```

# Developing the Plugin

## JAXB generated classes

To generate the JAXB classes you'll need the lsc.episode file from lsc-core.

    $JAVA_HOME/bin/xjc  -d src/main/java/ -extension -p org.lsc.plugins.connectors.scim1_1.generated -b ../../../lsc-core-2.1/lsc.episode  src/main/resources/schemas/lsc-scim_1_1-plugin-1.0.xsd 

These classes (and the xml) are used to define custom elments in lsc.xml.
Currently we don't use this for anything

This is mostly a long list of TODOs

## Plugin Connection

We can create a connection subclass or use a pluginConnection

## Attribute renaming

The current dataset method for doing attribute renaming (e.g. uid --> username) is pretty tedious.
We could make it easier by define a map in the configuration of the destination

## Source service

We currently are just doing destination writes. We may want to add reads in as well.
