# Tap

[![Build Status](https://travis-ci.org/noonmaru/tap.svg?branch=master)](https://travis-ci.org/noonmaru/tap)
[![Maintainability](https://api.codeclimate.com/v1/badges/6a20ddcbcde03208b75e/maintainability)](https://codeclimate.com/github/noonmaru/tap/maintainability)
[![](https://jitpack.io/v/noonmaru/tap.svg)](https://jitpack.io/#noonmaru/tap)
![JitPack - Downloads](https://img.shields.io/jitpack/dm/github/noonmaru/tap)
![GitHub](https://img.shields.io/github/license/noonmaru/tap)
![Twitch Status](https://img.shields.io/twitch/status/hptgrm)

> Minecraft Spigot NMS Library

> * ##### Features
>  * Java agent support (asm)
>  * Packets
>  * NMS Blocks
>  * NMS Entities
>  * NMS NBT
>  * NMS Inventory
>  * NMS Items
>  * NMS World
>  * Fast math
>  * Mojang user profile
>  * Fake scoreboard
>  * Entity specificEntity events listener
>  * Sub commands Module

> * ##### Gradle
>```groovy
>allprojects {
>    repositories {
>        ...
>        maven { url 'https://jitpack.io' }
>    }
>}
>
>...
>dependencies {
>    implementation 'com.github.noonmaru:tap:1.0'
>}
>```
>
>
>
> **If you want to use net.minecraft.server implementation, Follow the instructions below**
>* First, Follow the tutorial -> https://www.spigotmc.org/wiki/spigot-gradle/
>* Clone this git repository
>* Use the following Gradle command -> `gradlew publishTapPublicationToMavenLocal -PwithNMS`
>* Add the following code to build.gradle
>```groovy
>allprojects {
>    repositories {
>        ...
>        mavenLocal()
>    }
>}
>```
>```groovy
>dependencies {
>    implementation 'com.github.noonmaru:tap-v1_12_R1:1.0'
>}
>```

> * ##### Code sample
>```java
>//send a sound packet
>Player player = Bukkit.getPlayer("Heptagram");
>Location loc = player.getEyeLocation();
>Packet.EFFECT.namedSound(Sounds.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.MASTER, 
>    loc.getX(), loc.getY(), loc.getZ(), 0.75F, 2.0F).sendTo(Bukkit.getOnlinePlayers());
>```