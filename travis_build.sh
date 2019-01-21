#!/bin/bash
set -e
openssl aes-256-cbc -K $encrypted_9993846e3f84_key -iv $encrypted_9993846e3f84_iv -in prepare_environment.sh.enc -out prepare_environment.sh -d
sh prepare_environment.sh
cd alchemist
./gradlew --scan > >(egrep -v "(null:-1:-1)|(Can't find node by signature)")
mkdir -p report
cp --parent */build/reports build/reports report -R
mkdir --parents foo
cp dokka/index.html build/dokka/ 
