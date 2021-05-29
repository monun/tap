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
    * 빌드 버전이 필요하신 분들이 계셔서 플러그인 아티팩트도 빌드 할 수 있는 개발환경을 구축했습니다.
    * 플러그인 구현체가 필요하신분은 ./gradlew paperJar 태스크를 이용해 빌드하세요.
    * 하지만 가능하다면 [ShadowJar](https://github.com/johnrengelman/shadow) 플러그인을 사용해 FatJar로 빌드하세요.

* Tap의 개발환경 구축을 위해선 spigot 1.13.2-1.16.5 이 필요합니다. [BuildTools](https://www.spigotmc.org/wiki/buildtools/) 를 이용해 로컬 메이븐 저장소에 spigot을 배포하세요.
  * `./gradlew setupWorkspace` 명령으로 간단하게 배포할수있습니다.
  
<br>
<br>


[![Jetbrains](https://i.ibb.co/fp0CyZ7/jetbrains.png)](https://jb.gg/OpenSource)