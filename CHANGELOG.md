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