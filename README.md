# 아빠의 기록장

![아빠의 기록장 랜딩 이미지](docs/assets/landing-hero.png)

아빠의 기록장은 가족에게 남기고 싶은 말, 아이에게 해주고 싶은 조언, 아내에게 고마운 마음을 글과 음성으로 차곡차곡 저장하는 Android 로컬 감성 일기장입니다. 기록은 서버로 전송하지 않고 기기 안에 보관하는 것을 기본 원칙으로 합니다.

| 항목 | 링크 |
| --- | --- |
| GitHub Pages | <https://jeiel85.github.io/daddy-heart-journal/> |
| 개인정보처리방침 | <https://jeiel85.github.io/daddy-heart-journal/privacy.html> |
| Play Console 그래픽 묶음 | [store-graphics/play-console-current](store-graphics/play-console-current/) |
| 앱 아이콘 512px | [store-graphics/icon-512.png](store-graphics/icon-512.png) |
| 기능 그래픽 1024x500 | [store-graphics/feature-graphic-1024x500.png](store-graphics/feature-graphic-1024x500.png) |

## 주요 기능

- 가족 구성원별로 마음의 기록을 남기는 로컬 일기장
- 글과 함께 아빠의 목소리를 선택적으로 녹음
- 고마움, 미안함, 기쁨, 걱정, 응원, 추억 태그로 기록 정리
- 특정 날짜가 되어야 열 수 있는 타임캡슐 기록
- 4자리 PIN 잠금과 기기 내부 보관 중심의 프라이버시 설계
- 전체 기록을 텍스트로 복사해 별도 백업이나 책 제작에 활용

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Android MediaRecorder
- Robolectric / Roborazzi

## 빌드

Android Studio에서 프로젝트를 열거나 로컬 Gradle 환경에서 다음 작업을 실행합니다.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

릴리즈 번들은 로컬 `.keystore/release-signing.properties` 또는 환경 변수 `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`를 사용해 서명됩니다. `.keystore/`는 Git에서 제외되어 있으며, Play Console 업로드 키 백업용으로 로컬에만 보관합니다.

```powershell
.\gradlew.bat :app:bundleRelease
.\gradlew.bat :app:exportReleaseToDesktop
```

`exportReleaseToDesktop`는 `store-graphics/play-console-current/release-notes.txt`와 최신 AAB를 바탕화면 `Build` 폴더로 함께 내보냅니다.

## 릴리즈 상태

- 패키지명: `com.jeiel.daddyheartjournal`
- 현재 버전: `1.0.0` (`versionCode` 1)
- 공개 랜딩: `docs/index.html`
- Play Console 현재 제출 묶음: `store-graphics/play-console-current/`

## 개인정보

아빠의 기록장은 광고 SDK, 분석 SDK, 서버 동기화, 클라우드 백업을 사용하지 않는 로컬 우선 앱입니다. 마이크 권한은 사용자가 직접 음성 기록을 남길 때만 필요합니다.
