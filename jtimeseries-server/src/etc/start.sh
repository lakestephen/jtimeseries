#!/bin/sh
#
# Start the jtimeseries-server

java -Xmx256M -cp ./;./jtimeseries-server-${version}.jar;./jtimeseries-component-${version}.jar;./jtimeseries-${version}.jar;./jfreechart-1.0.9.jar;./aopalliance-1.0.jar;./jcommon-1.0.12.jar;./commons-logging-1.1.1.jar;./jcommon-1.0.12.jar;./jmxtools-1.2.1.jar;./spring-beans-2.5.6.jar;./spring-context-2.5.6.jar;./spring-core-2.5.6.jar com.od.jtimeseries.server.JTimeSeriesServer
