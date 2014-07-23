How to install
==========

* Download the JAR : telosys-monitoring.jar

* Put it in the application classpath or in the server classpath

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
    	<param-value>/monitor</param-value> <!-- default is "/monitor" -->
    </init-param>
  </filter>
  <filter-mapping>
  	<filter-name>Monitor</filter-name>
  	<url-pattern>/*</url-pattern>
  </filter-mapping>
```

* Do not forget to restart your application server

* Navigate in your web application

### Monitoring Report page

* Go to the monitor report page. For that, add "/monitor" at the end of the base URL of your application

_Example :_

if the base URL of your application is : http://my.application/web

then the URL of the report page is : http://my.application/web/monitor

"/monitor" can be redefined in the web.xml by the init-param "reporting" of the "telosys-monitoring" filter.

Actions
-------

You can change monitoring values with parameters in the "/monitoring" URL :
* ```action=reset``` : reset to the defaults parameters defined in the web.xml and clear all logs
* ```action=clear``` : clear all logs
* ```duration=``` : request duration threshold
* ```log_size=``` : number of requests in the log
* ```by_time_size=``` : number of the longuest requests by time
* ```by_url_size=``` : number of the longuest requests by URL

Contact
---

Contact us at : [telosysteam@gmail.com](telosysteam@gmail.com)