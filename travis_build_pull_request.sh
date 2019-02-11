#!/bin/bash
set -e
cd alchemist
./gradlew -x dokka -x javadoc check install fatJar > >(egrep -v "(null:-1:-1)|(Can't find node by signature)")
