### 4.1.8

* Kotlin 1.5.21로 롤백 (라이브러리 부족)
* buildSrc를 이용한 의존성 관리

---

### 4.1.7

* Kotlin 1.5.30
* 뱃지 추가
* 일부 오타 수정

---

### 4.1.6

* FakeEntity가 이동이 없을때 머리가 회전하지 않던 버그 수정
* `PacketSupport#entityAnimation` 함수 추가
* `FakeEntity#animation` 함수 추가

---

### 4.1.5

* FakeEntity가 플레이어 사망시 클라이언트에서 제거되지 않던 버그 수정

---

### 4.1.2

* kotlin-reflect 의존성에 추가
* `ConfigSupport`가 제대로 동작하지 않던 버그 수정
* 함수 이름 변경 `ConfigSupport#computeConfig` -> `compute`
* 최상위 확장 함수 추가 `ConfigurationSection#compute(Any)`
* `UpstreamReference`를 `Weaky`로 변경
* 활용이 애매한 클래스 제거
    * `Refery`

---

### 4.1.1

* 배포시 tap-api가 빠지던 버그 수정

---

### 4.1.0 (BUG)

* Kotlin 2.1.0
* pom에 dependencies가 빠져있던 버그 수정

---

### 4.0.0

#### Structure

* 멀티 프로젝트 정리
    * :api -> :tap-api
    * :core -> :api 의 internal 패키지 분리
    * nms(:v1_xx_Rx) -> :tap-core:v1_XX_X

#### Environment

* JDK 변경 - **8 -> 16**
* Paper 1.17, 1.17.1 지원
* 1.16.5 이하 하위호환 제거
* 배포 변경 - **jitpack -> mavenCentral**

#### Features

* 개체 패킷
* 가상 개체
* 가상 발사체
* 개체별 이벤트 리스너
* YamlConfiguration을 이용한 문자열 템플릿
* 추가적인 인벤토리 함수
* GitHub를 통한 업데이트 (BETA)
* Tick 기반 태스크 스케쥴러 (Ticker)

#### Changes

* `Any#computeConfig` 확장 함수를 `ConfigSupport#computeConfig` 함수로 변경
* Support 인스턴스를 Companion으로 변경 `PacketSupport.INSTANCE.func()) -> PacketSupport.func()`
* `GitHubSupport#updateFromGitHubMagically` 함수 향상
* 버전 식별자 추가

#### Bug fixes

* FakeFallingBlock 스폰 버그 수정

---
