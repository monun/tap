#!/bin/sh

download() {
    download_result=$(wget -c --content-disposition -P "$2" -N "$1" 2>&1 | tail -2 | head -1)
    echo "$download_result"
}

build=true
debug=true
backup=false
restart=false

version=1.16.5
heap_max=10G
heap_min=10G
debug_port=5005
jar_url="https://papermc.io/api/v1/paper/$version/latest/download"
download_plugins=(
    'https://github.com/monun/kotlin-plugin/releases/latest/download/Kotlin-1.4.31.jar'
    'https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar'
)

project_folder=$(pwd)
script=$(basename "$0")
server_folder="$project_folder/.${script%.*}"
mkdir -p "$server_folder"
mkdir -p "$server_folder/plugins"
cd "$server_folder" || exit

jar_result=$(download $jar_url .)
jar=$(grep -oG "‘.*’" <<< $jar_result)
jar="${jar:1:-1}"

echo $jar_result

for i in "${download_plugins[@]}"
do
    echo $(download $i ./plugins)
done

jvm_arguments=(
    "-Xmx$heap_max"
    "-Xms$heap_min"
    "-XX:+ParallelRefProcEnabled"
    "-XX:MaxGCPauseMillis=200"
    "-XX:+UnlockExperimentalVMOptions"
    "-XX:+DisableExplicitGC"
    "-XX:+AlwaysPreTouch"
    "-XX:G1NewSizePercent=30"
    "-XX:G1MaxNewSizePercent=40"
    "-XX:G1HeapRegionSize=8M"
    "-XX:G1ReservePercent=20"
    "-XX:G1HeapWastePercent=5"
    "-XX:G1MixedGCCountTarget=4"
    "-XX:InitiatingHeapOccupancyPercent=15"
    "-XX:G1MixedGCLiveThresholdPercent=90"
    "-XX:G1RSetUpdatingPauseTimePercent=5"
    "-XX:SurvivorRatio=32"
    "-XX:+PerfDisableSharedMem"
    "-XX:MaxTenuringThreshold=1"
    "-Dusing.aikars.flags=https://mcflags.emc.gs"
    "-Daikars.new.flags=true"
    "-Dcom.mojang.eula.agree=true"
)

if ($debug)
then
    jvm_arguments+=("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$debug_port")
fi

jvm_arguments+=(
    "-jar"
    "$jar"
    "--nogui"
)

while :
do
    if ($build)
    then
        cd "$project_folder" || exit
        ./gradlew clean copyToServer
        cd "$server_folder" || exit
    fi

    java "${jvm_arguments[@]}"

    if ($backup)
    then
        echo 'Start the backup.'
        backup_file_name=$(date +"%y%m%d-%H%M%S")
        mkdir -p 'backup'
        tar --exclude='*.jar' --exclude='*.gz' --exclude='./cache' --exclude='./backup' -zcf "./backup/$backup_file_name.tar.gz" .
        echo 'The backup is complete.'
    fi

    if (! ($restart))
    then
        break
    fi

    read -r -t 5 -p "The server restarts. Press Enter to start immediately or Ctrl+C to cancel `echo $'\n> '`"
done
