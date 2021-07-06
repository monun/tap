# Tap

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

#### 환경

* JDK 16
* Kotlin 1.5.20
* Paper 1.17

#### Gradle

```groovy
repositories {
    mavenCentral()
}
...
dependencies {
    implementation 'io.github.monun:tap:4.0.0-RC'
}
```

#### 빌드

> ##### 1.17+
> * plugin.yml에 라이브러리 정보를 아래와 같이 입력하세요.
> ```yaml
> name: ...
> main: ...
> version: ...
> ...
> libraries:
>   - io.github.monun:tap:4.0.0
> ```

### NOTE

* Tap의 개발환경 구축을 위해선 spigot 1.17 이 필요합니다. [BuildTools](https://www.spigotmc.org/wiki/buildtools/) 를 이용해 로컬 메이븐 저장소에
  spigot을 배포하세요.
* `./gradlew setupWorkspace` 명령으로 간단하게 배포할수있습니다.

### 기여자

* [patrick-choe](https://github.com/patrick-choe)
  * java 소스 코드 제거
  * mojang-mapping 빌드환경 구축
  * mavenCentral 배포


[![Jetbrains](https://i.ibb.co/fp0CyZ7/jetbrains.png)](https://jb.gg/OpenSource)