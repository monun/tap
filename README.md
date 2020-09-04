# Tap
[![Build Status](https://travis-ci.org/noonmaru/tap.svg?branch=master)](https://travis-ci.org/noonmaru/tap)
[![JitPack](https://jitpack.io/v/noonmaru/tap.svg)](https://jitpack.io/#noonmaru/tap)

## Kotlin으로 작성된 Paper 라이브러리
#### 지원 기능
 * 개체 패킷
 * 가상 개체
 * 가상 발사체
 * 모장 프로필
 * 개체별 이벤트 리스너
 * YamlConfiguration을 이용한 문자열 템플릿
 * 추가적인 인벤토리 함수
 
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
    implementation 'com.github.noonmaru:tap:Tag'
}
```
### NOTE
* Tap의 Packet 패키지를 이용하기 위해선 [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/releases) 을 필요로 합니다.

* 내부 코드 변경이 잦아 3.0.0 버전 이후로는 독립형을 지향하기 위해 플러그인이 아닌 라이브러리로만 지원할 예정입니다.
[ShadowJar](https://github.com/johnrengelman/shadow) 플러그인을 사용해 FatJar로 빌드하세요.

* Tap의 개발환경 구축을 위해선 spigot 1.13.2-1.16.2 가 필요합니다. [BuildTools](https://www.spigotmc.org/wiki/buildtools/) 를 이용해 로컬 메이븐 저장소에 spigot을 배포하세요.