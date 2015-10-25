# byteman-wildfly-log
This project aims to provide extended logging capabilities for Wildfly application
 server without need to change the source code of the server. It uses Byteman (http://byteman.jboss.org/) to inject log messages to server bytecode.

##Setup
Build project with maven: `mvn clean package`

Current build is used with wildfly-8.2.0.Final and Byteman 3.0.2. However, the rules should work in later releases of Wildfly if 
there are no significant changes in source code.

To run byteman-wildfly-log:

* download Wildfly
* download and install Byteman (https://community.jboss.org/wiki/ABytemanTutorial#how_do_i_download_and_install_byteman)

Set variable JAVA_OPTS in standalone.conf or domain.conf file in Wildfly directory like this:
JAVA_OPTS="$JAVA_OPTS -Dorg.jboss.byteman.transform.all -javaagent:${BYTEMAN_HOME}/lib/byteman.jar=script:${HOME}/byteman-wildfly-log/rules/jms.btm,script:${HOME}/byteman-wildfly-log/rules/servlets.btm,script:${HOME}/byteman-wildfly-log/rules/ejb.btm,script:${HOME}/byteman-wildfly-log/rules/rest.btm,script:${HOME}/byteman-wildfly-log/rules/ws.btm,sys:${HOME}/byteman-wildfly-log/target/byteman-wildfly-log-0.0.1-SNAPSHOT.jar,listener:true"

Run Wildfly.

##Description
The logging mechanism consists of two parts:

* log helper class
* byteman rule

###Log helper class
This class uses `java.util.logging.LogManager` to obtain proper logger instance from logger name.
Logger instance is used to create a log message with following parameters: level, message and exception associated with the log message.
The full package qualified logger name and parameters are passed by a byteman rule which uses the log helper class.

Helper class contains two methods:

`public void log(final String loggerName, final String level, final String message)` <br>
`public void log(final String loggerName, final String level, final String message, final Throwable thrown)`

Format of a message within a log is defined as:
[ThreadId-'id of the current thread'] + (Method 'method name') + message

###Byteman rule
A byteman rule which is triggered at specific location in server bytecode.

Rules are defined in directory ./rules and add logs to following parts of Wildfly:

* ejb.btm (EJB3 subsystem)
* jms.btm (JMS subsystem)
* rest.btm (rest services)
* servlets.btm (servlet container)
* ws.btm (web services)

Following byteman rule is taken from `servlets.btm` script from this project.

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

## Byteman vs JBoss module class loading
When a byteman rule passes a class from JBoss modules system to the log helper class, the ClassNotFoundException is thrown because the log helper class is loaded by different classloader.
As a consequence of this, rules use dollar expression to build log message and passes it to log helper class as java.lang.String.

After discussion with the author of Byteman, an experimental implementation of the classloading problem was made and is considered in the next version of Byteman.
Full discussion can be viewed at https://developer.jboss.org/thread/261314
