# byteman-wildfly-log
This project aims to provide extended logging capabilities for Wildfly application
 server without need to change the source code of the server. It uses Byteman (http://byteman.jboss.org/) to inject new log events into server bytecode.

Current build is used with wildfly-8.2.0.Final and Byteman 3.0.3. However, the rules should work in later releases of Wildfly if 
there are no significant changes in source code.

The latest version of this project uses a new feature implemented in Byteman 3.0.3, the JBoss Modules plugin. This plugin allows a user to pass
classes from JBoss Modules to Byteman log helper class and manipulate with them inside the log helper. In order to make this work, the log helper
 must be deployed as a separate module in JBoss Modules directory.

##Setup
Build project with maven: `mvn clean package`

Unzip the archive created in `target/zip` folder to `$WILDFLY_HOME/modules/system/layers/base`.

Directory structure of the unzipped archive:

```
	org
    └── wildfly
        └── byteman
            └── log
                └── main
                    ├── byteman-wildfly-log-2.0.0.jar
                    └── module.xml

```

To run byteman-wildfly-log:

* download Wildfly
* download and install Byteman (https://community.jboss.org/wiki/ABytemanTutorial#how_do_i_download_and_install_byteman)

Set env variables, for example in UBUNTU, edit the ${HOME}/.bashrc:<br />
export BYTEMAN_HOME=$HOME/Byteman/byteman-download-3.0.3<br />
export MODPLUGINJAR=${BYTEMAN_HOME}/contrib/jboss-modules-system/byteman-jboss-modules-plugin.jar<br />
export MODPLUGIN=org.jboss.byteman.modules.jbossmodules.JBossModulesSystem<br />
export PATH=${PATH}:${BYTEMAN_HOME}/bin

Set variable JAVA_OPTS in standalone.conf or domain.conf file in Wildfly directory like this:<br />
JAVA_OPTS="$JAVA_OPTS -Dorg.jboss.byteman.transform.all -javaagent:${BYTEMAN_HOME}/lib/byteman.jar=script:${HOME}/byteman-wildfly-log/rules/jms.btm,script:${HOME}/byteman-wildfly-log/rules/servlets.btm,script:${HOME}/byteman-wildfly-log/rules/ejb.btm,script:${HOME}/byteman-wildfly-log/rules/rest.btm,script:${HOME}/byteman-wildfly-log/rules/ws.btm,listener:true,modules:${MODPLUGIN},sys:${MODPLUGINJAR}"

Run Wildfly.

##Description
The logging mechanism consists of two parts:

* log helper class
* byteman rule

###Log helper class
The helper class (`org.byteman.wildfly.log.LogHelper`) uses `java.util.logging.LogManager` to obtain proper logger instance from logger name.
Logger instance is used to create a log record with the following parameters: level, message and exception associated with the log record.
The full package qualified logger name and parameters are passed by a byteman rule which uses the log helper class.

The Helper class contains two methods:

`public void log(final String loggerName, final String level, final String message)` <br>
`public void log(final String loggerName, final String level, final String message, final Throwable thrown)`

Format of a message within a log is defined as:
[ThreadId-'id of the current thread'] + (Method 'method name') + message

###Byteman rule
A byteman rule which is triggered at specific location in server bytecode.

Rules are defined in directory ./rules and add logs to the following parts of Wildfly:

* ejb.btm (EJB3 subsystem)
* jms.btm (JMS subsystem)
* rest.btm (rest services)
* servlets.btm (servlet container)
* ws.btm (web services)

The following byteman rule is taken from `servlets.btm` script from this project.

```
#Logging of undertow handlers chain
RULE logServletHandler.handleRequest
CLASS io.undertow.servlet.handlers.ServletHandler
METHOD handleRequest(HttpServerExchange)
AT ENTRY
IF true
DO log($CLASS, "DEBUG", "(Method handleRequest) Request info: " + 
		" URL = " + $1.getRequestURL() + ", protocol = " + $1.getProtocol() + 
		", method = " + $1.getRequestMethod() + ", query string: " + $1.getQueryString())
ENDRULE
```

produces log:
`19:08:44,507 DEBUG [io.undertow.servlet.handlers.ServletHandler] (default task-12) [ThreadId-300] (Method handleRequest) Request info:  URL = http://localhost:8080/wildfly-helloworld-ws, protocol = HTTP/1.1, method = GET, query string: wsdl`

###Example of the JBoss Modules plugin
The JBoss Modules plugin is used in one rule within the servlet.btm file to demonstrate its functionality. The rule employs the `org.byteman.wildfly.log.servlets.ServletHandlerLog` class as a rule helper.
The rule helper manipulate with the `io.undertow.server.HttpServerExchange` class which is passed by the rule as an argument of the helper method "logCookie":

```
public void logCookie(final String clazz, final HttpServerExchange exchange)
```

The following code snippet is taken from the servlet.btm, it shows a rule which passes the HttpServerExchange to the helper method:

```
IMPORT org.wildfly.byteman.log  
IMPORT io.undertow.core  
HELPER org.jboss.byteman.koubsky.LogHelper
 
#Log cookies - does not work with JBoss Modules plugin
RULE logServletHandler.handleRequest.cookies
CLASS io.undertow.servlet.handlers.ServletHandler
METHOD handleRequest(HttpServerExchange)
HELPER org.jboss.byteman.koubsky.servlets.ServletHandlerLog
AT ENTRY
IF true
DO logCookie($CLASS, $1) 
ENDRULE

... the rest of the rules ...

```