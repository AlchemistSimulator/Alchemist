#!/bin/bash
openssl aes-256-cbc -K $encrypted_9993846e3f84_key -iv $encrypted_9993846e3f84_iv -in prepare_environment.sh.enc -out prepare_environment.sh -d
sh prepare_environment.sh
cd alchemist
./gradlew check projectReport uploadArchives
./gradlew fatJar
mkdir report
cp --parent */build/reports build/reports report -R
