# Tap

[![Kotlin](https://img.shields.io/badge/java-17-ED8B00.svg?logo=java)](https://www.azul.com/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.22-585DEF.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-8.2.1-02303A.svg?logo=gradle)](https://gradle.org)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.monun/tap-core)](https://search.maven.org/artifact/io.github.monun/tap-core)
[![GitHub](https://img.shields.io/github/license/monun/tap)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Kotlin](https://img.shields.io/badge/youtube-각별-red.svg?logo=youtube)](https://www.youtube.com/channel/UCDrAR1OWC2MD4s0JLetN0MA)

### Paper 확장 라이브러리

컨텐츠 제작에 자주 사용하는 기능을 제공합니다.

---

* #### Features
    * 개체 패킷
    * 가상 개체
    * 가상 발사체
    * 개체별 이벤트 리스너
    * YamlConfiguration을 이용한 문자열 템플릿
    * 추가적인 인벤토리 함수
    * GitHub를 통한 업데이트
    * Tick 기반 태스크 스케쥴러 (Ticker)
    * PersistentData API 접근성 개선

* #### Supported minecraft versions
    * 1.18
    * 1.18.1
    * 1.18.2
    * 1.19
    * 1.19.1
    * 1.19.2
    * 1.19.3
    * 1.19.4
    * 1.20
    * 1.20.1

---

### Gradle `tap-api`

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

### plugin.yml `tap-core`

```yaml
name: ...
version: ...
main: ...
libraries:
  - io.github.monun:tap-core:<version>
```

#### !!주의!!

* `Gradle`과 `plugin.yml`의 의존성 패키지가 다르므로 주의해주세요.
* 모든 코드는 ShadowJar를 고려하여 작성되지 않았습니다.

---

### NOTE

* 라이센스는 GPL-3.0이며 변경 혹은 삭제를 금합니다.

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
    * FakeEntity에 Player 지원

[![Jetbrains](https://i.ibb.co/fp0CyZ7/jetbrains.png)](https://jb.gg/OpenSource)
