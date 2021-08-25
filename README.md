# Tap

![Maven Central](https://img.shields.io/maven-central/v/io.github.monun/tap)
![GitHub](https://img.shields.io/github/license/monun/tap)
[![YouTube Channel Subscribers](https://img.shields.io/youtube/channel/subscribers/UCDrAR1OWC2MD4s0JLetN0MA?label=%EA%B0%81%EB%B3%84&style=social)](https://www.youtube.com/channel/UCDrAR1OWC2MD4s0JLetN0MA)

### Paper 확장 라이브러리

---

* #### Features
    * 개체 패킷
    * 가상 개체
    * 가상 발사체
    * 개체별 이벤트 리스너
    * YamlConfiguration을 이용한 문자열 템플릿
    * 추가적인 인벤토리 함수
    * GitHub를 통한 업데이트 (BETA)
    * Tick 기반 태스크 스케쥴러 (Ticker)

* #### Environment
    * JDK 16
    * Kotlin 1.5.20
    * Paper & Spigot 1.17, 1.17.1

---

#### Gradle

```kotlin
repositories {
    mavenCentral()
}
```

```kotlin
dependencies {
    implementation("io.github.monun:tap-api:<version>")
}
```

### plugin.yml

```yaml
name: ...
version: ...
main: ...
libraries:
  - io.github.monun:tap:<version>
```

---

### NOTE

* 라이센스는 GPL-3.0이며 변경 혹은 삭제를 금합니다.
* `./gradlew setupWorkspace` 명령을 통해 작업환경을 구축 할 수 있습니다.

---

### Contributors

* [**patrick-choe**](https://github.com/patrick-choe)
    * java 소스 코드 제거 (java -> kotlin)
    * mojang-mapping 빌드환경 구축
    * mavenCentral 배포
    * ProtocolLib 의존성 제거 (nms packet 직접 지원)
* [**dolphin2410**](https://github.com/dolphin2410)
    * FakeFallingBlock 버그 수정
    * Version identifier 추가
    * 일부 코드 향상

[![Jetbrains](https://i.ibb.co/fp0CyZ7/jetbrains.png)](https://jb.gg/OpenSource)
