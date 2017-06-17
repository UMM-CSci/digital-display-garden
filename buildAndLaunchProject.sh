#!/bin/bash

#Cleanup old server/build files
rm -r ~/server
rm ~/server.tar
./gradlew clean

#Rebuild the project and extract it to home
./gradlew build
cp server/build/distributions/server.tar ~
tar xvf ~/server.tar -C ~
sleep 1

#Maintain the configuration and authentication files
AUTH_USERS_PATH="`pwd`/server/authorized.users"
DEPLOYMENT_CONFIG_PATH="`pwd`/server/config.properties.deployment"

#If you want authorized.users as a soft-link, the java WatchService
#will consider it as "authorized.users~" and will not automatically update
ln "$AUTH_USERS_PATH" ~/server/authorized.users
ln -s "$DEPLOYMENT_CONFIG_PATH" ~/server/config.properties

#Run the server
cd ~/server
bin/server

