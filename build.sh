#!/bin/bash -ex

# FIXME: this is a workaround for out-of-date transitive dependencies
# See: https://github.com/ISA-tools/linkedISA/issues/11
mvn dependency:get -Dartifact=xalan:xalan:2.4.0

mvn install:install-file -Dfile="$HOME/.m2/repository/xalan/xalan/2.4.0/xalan-2.4.0.jar" -DgroupId=xalan -DartifactId=xalan -Dversion=2.4 -Dpackaging=jar

# FIXME: some tests are failing
# See: https://github.com/ISA-tools/linkedISA/issues/12
mvn clean install -DskipTests=true
