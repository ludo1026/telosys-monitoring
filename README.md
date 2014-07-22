telosys-monitoring
==================

Tools for Java web application monitoring.

HOW TO USE
==========

* Download the JAR : telosys-monitoring.jar
* Put it in the application classpath or in the Tomcat server classpath
* Add these lines in the web.xml of your application :
```xml
  <filter>
    <filter-name>Monitor</filter-name>
    <filter-class>org.telosys.webtools.monitoring.RequestsMonitor</filter-class>    
    <init-param>
    	<param-name>duration</param-name>
    	<param-value>1000</param-value> <!-- default is 1000 ( 1 sec )  -->
    </init-param>
    <init-param>
    	<param-name>logsize</param-name>
    	<param-value>100</param-value> <!-- default is 100 -->
    </init-param>
    <init-param>
    	<param-name>toptensize</param-name>
    	<param-value>10</param-value> <!-- default is 10 -->
    </init-param>
    <init-param>
    	<param-name>longestsize</param-name>
    	<param-value>10</param-value> <!-- default is 10 -->
    </init-param>
    <init-param>
    	<param-name>reporting</param-name>
    	<param-value>/monitoring</param-value> <!-- default is "/monitor" -->
    </init-param>
    <init-param>
    	<param-name>trace</param-name>
    	<param-value>false</param-value> <!-- default is false -->
    </init-param>
  </filter>
  <filter-mapping>
  	<filter-name>Monitor</filter-name>
  	<url-pattern>/*</url-pattern>
  </filter-mapping>
```
* Do not forget to restart your application server
* Navigate in your web application
* After some times, go the monitoring report page by the root URL of your application ending by "/monitoring"
  For example, if the base URL of your application is ```http://my.application/web```, the URL to acces to the report page will be ````http://my.application/web/monitoring```.
