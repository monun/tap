# Tap
[![Build Status](https://travis-ci.com/monun/tap.svg?branch=master)](https://travis-ci.com/monun/tap)
[![JitPack](https://jitpack.io/v/monun/tap.svg)](https://jitpack.io/#monun/tap)

## Kotlin으로 작성된 Paper 라이브러리
#### 지원 기능
 * 개체 패킷
 * 가상 개체
 * 가상 발사체
 * 개체별 이벤트 리스너
 * YamlConfiguration을 이용한 문자열 템플릿
 * 추가적인 인벤토리 함수
 * GitHub를 통한 업데이트 (BETA)
 * Tick 기반 태스크 스케쥴러 (Ticker)

### 환경
* JAVA 16
* Kotlin 1.5.10
* ProtocolLib 4.6.0
* Paper 1.13.2 - 1.16.5
 
### Gradle
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

...
dependencies {
    implementation 'com.github.monun:tap:Tag'
}
``` 

### NOTE
* Tap의 Packet 패키지를 이용하기 위해선 [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/releases) 을 필요로 합니다.
* Tap의 개발환경 구축을 위해선 spigot 1.13.2-1.16.5 이 필요합니다. [BuildTools](https://www.spigotmc.org/wiki/buildtools/) 를 이용해 로컬 메이븐 저장소에 spigot을 배포하세요.
* `./gradlew setupWorkspace` 명령으로 간단하게 배포할수있습니다.
  
<br>
<br>

<p align="center">
 <a href="https://jb.gg/OpenSource" target="_blank"><img src="https://i.ibb.co/fp0CyZ7/jetbrains.png" width="300" alt="JetBrains"></a>
</p>
