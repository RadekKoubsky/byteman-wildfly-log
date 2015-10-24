# byteman-wildfly-log
This project aims to provide extended logging capabilities for Wildfly application server without need to change the source code of the server. It uses Byteman (http://byteman.jboss.org/) to inject log message to server bytecode.

Build project with maven: `mvn clean package`

Current build is used for wildfly-8.2.0.Final. However, the rules should work in later releases of wildfly if there are no significant changes in the source code.

##Setup


##Description
The logging mechanism consists of two parts:

* log helper class
* byteman rule

###Log helper class
This class uses `java.util.logging.LogManager` to obtain proper logger instance from logger name. Logger instance is used to create a log message with following parameters: level, message and exception associated with log message. The full package qualified logger name and parameters are passed by a byteman rule which uses the log helper class.

###Byteman rule
A byteman rule which is triggered at specific location in server bytecode. Following byteman rule is taken from `ws.btm` script from this project.

```
#Log invoking request handler call
RULE logCXFServletExt.invoke
CLASS org.jboss.wsf.stack.cxf.CXFServletExt
METHOD invoke(HttpServletRequest, HttpServletResponse)
AT ENTRY
IF true
DO log($CLASS, "DEBUG", "(Method invoke) Invoking request handler call...")
ENDRULE
```

