#!/bin/sh

java -cp ivy-java-1.2.14.jar fr.dgac.ivy.tools.Probe "^(.*)" -b 127.255.255.255:2010
