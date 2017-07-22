#!/bin/bash

if [[ ( "$#" < 1 ) ]] ; then
    echo "You must provide a qualifier for the environment to build into"
    echo "This changes the folder within the home directory that the"
    echo "project is built in. Try \e[31mtest\e[0m or \e[31mprod\e[0m"
    exit;
fi

#Cleanup old server/build files
rm -r "~/server_$1"
rm "~/server_$1.tar"
./gradlew clean

#Rebuild the project and extract it to home
./gradlew build
cp server/build/distributions/server.tar "~/server_$1.tar"
tar xvf "~/server_$1.tar" -C ~
sleep 1

mv ~/server "~/server_$1"

#Maintain the configuration and authentication files
AUTH_USERS_PATH="`pwd`/server/authorized.users"
DEPLOYMENT_CONFIG_PATH="`pwd`/server/config.properties.deployment"

ln -s "$AUTH_USERS_PATH" "~/server_$1/authorized.users"
ln -s "$DEPLOYMENT_CONFIG_PATH" "~/server_$1/config.properties"

#Run the server
cd "~/server_$1"
"bin/server_$1"
