#!/bin/sh

curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 16.0.1-zulu
sdk use java 16.0.1-zulu

./gradlew clean copyToServer -Prelocate=false

server=paper
version=1.16.5
plugins=(
    'https://github.com/monun/kotlin-plugin/releases/latest/download/Kotlin-1.4.32.jar'
    'https://github.com/monun/auto-update/releases/latest/download/AutoUpdate.jar'
    'https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar'
)

script=$(basename "$0")
server_folder=".${script%.*}"
mkdir -p "$server_folder"

cd "$server_folder"

server_script="$server.sh"
server_config="$server_script.conf"
wget -qc -N "https://raw.githubusercontent.com/monun/server-script/master/$server_script"

if [ ! -f "$server_config" ]
then
    cat << EOF > $server_config
jar_url="https://papermc.io/api/v1/paper/$version/latest/download"
debug=true
debug_port=5005
backup=false
restart=false
memory=16
plugins=(
EOF
    for plugin in "${plugins[@]}"
    do
        echo "  \"$plugin\"" >> $server_config
    done
    echo ")" >> $server_config
fi

chmod +x ./$server_script
./$server_script