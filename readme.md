# Tap

[![Build Status](https://travis-ci.org/noonmaru/tap.svg?branch=master)](https://travis-ci.org/noonmaru/tap)
[![JitPack](https://jitpack.io/v/noonmaru/tap.svg)](https://jitpack.io/#noonmaru/tap)
[![GitHub](https://img.shields.io/github/license/noonmaru/tap)](https://github.com/noonmaru/tap/blob/master/LICENSE)
---
> Kotlin으로 작성된 Paper 라이브러리
---
> * ##### Features
>  * Java agent support (asm)
>  * Packets
>  * Mojang user profile
>  * Entity specificEntity events listener
>  * Sub commands
>  * FakeEntity
---
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
>    implementation 'com.github.noonmaru:tap:Tag'
>}
>```
---
>**net.minecraft.server의 구현체가 필요하다면 다음 지시사항을 따르세요.**
>* 먼저 다음 튜토리얼을 따라하세요(Maven local repo에 nms라이브러리 설치) -> https://www.spigotmc.org/wiki/spigot-gradle/
>* Tap 깃 저장소를 복제하세요 (git clone)
>* 다음 명령을 프로젝트내에서 실행하세요 -> `gradlew publishToMavenLocal -PwithNMS`
>* 다음 코드를 build.gradle에 입력하세요
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
>    implementation 'com.github.noonmaru:tap:Tag'
>    implementation 'com.github.noonmaru:tap-v1_15_R1:Tag'
>}
>```
---
> ##### 다음 버전의 net.minecraft.server API는 테스트되지 않았습니다.
> * v1_13_R1 (1.13)
> * v1_13_R2 (1.13.x)
> * v1_14_R1 (1.14, 1.14.x)
> * v1_16_R1 (1.16, 1.16.1)