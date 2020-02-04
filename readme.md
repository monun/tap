# Tap

[![Build Status](https://travis-ci.org/noonmaru/tap.svg?branch=master)](https://travis-ci.org/noonmaru/tap)
[![Maintainability](https://api.codeclimate.com/v1/badges/6a20ddcbcde03208b75e/maintainability)](https://codeclimate.com/github/noonmaru/tap/maintainability)
[![](https://jitpack.io/v/noonmaru/tap.svg)](https://jitpack.io/#noonmaru/tap)
![JitPack - Downloads](https://img.shields.io/jitpack/dm/github/noonmaru/tap)
![GitHub](https://img.shields.io/github/license/noonmaru/tap)
![Twitch Status](https://img.shields.io/twitch/status/hptgrm)

> Paper library written in Kotlin

> * ##### Features
>  * Java agent support (asm)
>  * Packets
>  * Mojang user profile
>  * Entity specificEntity events listener
>  * Sub commands Module
>  * FakeEntity Module

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
>    implementation 'com.github.noonmaru:tap:2.0'
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
>    implementation 'com.github.noonmaru:tap-v1_15_R1:2.0'
>}
>```
