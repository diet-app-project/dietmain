# 🍱 DietApp · 학생식당 식단 관리 앱

먹은 음식을 사진·섭취량(g)과 함께 기록하고, **식약처 공공 DB** 기준으로 하루·한 주·최근 30일의 열량과 탄수화물·단백질·지방 균형을 보여주는 Android 앱입니다.

- **시연 영상**: [YouTube 링크](https://www.youtube.com/watch?v=jpzAB4mbThw) — 모든 기능 + 단위·UI 테스트 실행 결과 포함
- **저장소**: https://github.com/diet-app-project/dietmain

---

## 1. 앱 소개

### 과제 요구사항별 구현

| 요구사항 | 구현 내용 | 화면 |
|---|---|---|
| **음식 입력** (1) 날짜·시간 | DatePicker·TimePicker로 선택, 끼니(조식·중식·석식·간식/음료) 선택 | 식사 입력 |
| (2) 사진 | Photo Picker로 선택 → 앱 내부 `filesDir/meal_photos/`에 복사 저장 | 식사 입력 |
| (3) 음식·반찬 이름 | 음식 100종 검색 후 여러 개 추가 (중복 추가 방지) | 식사 입력 |
| (4) 섭취량(g) | 음식별 g 입력 (소수점 가능, 50·100·150·200g 빠른 선택) | 식사 입력 |
| (5) 영양소 미리보기 | g를 바꾸는 즉시 음식별·한 끼 합계 kcal·탄·단·지 갱신 | 식사 입력 |
| **식사 보여주기** (1) 사진+날짜 목록 | 날짜를 이동하며 그날 식사를 사진 썸네일과 함께 표시 | 식단 기록 |
| (2)(3) 터치 시 상세 | 입력 내용 + 음식별 영양소·열량 + 한 끼 합계 | 식사 상세 |
| (4) 날짜별 요약 | 선택한 날의 총 kcal·끼니 수·탄단지 요약 카드 | 식단 기록 |
| **식사 분석** (1) 최근 1달 칼로리 | 최근 30일(오늘 포함) 총 kcal | 섭취 통계 |
| (2) 끼니 종류별 영양 분석 | 조식·중식·석식·간식/음료별 탄단지 막대그래프 (+ 일간·주간 통계) | 섭취 통계 |
| **데이터 출처 표시** | 통계 화면 하단 출처 카드 + 공식 DB 링크 | 섭취 통계 |

### 디자인
- 크림·버건디·연분홍·라벤더 팔레트의 Material 3 테마 (다크 모드 대응)
- 직접 만든 캐릭터 **'보리'** 일러스트와 음식 100종 전용 벡터 아이콘
- 큰 글자 설정에서도 깨지지 않도록 세로 배치 전환

### 기술 스택
Kotlin · Jetpack Compose (Material 3) · MVVM (ViewModel + UiState + StateFlow) · Room · Navigation Compose · Coil

```
app/src/main/java/com/example/dietapp/
├─ data/     Room(Entity·DAO·DB), Repository, foods.json 로더, 사진 저장
├─ domain/   NutritionCalculator(영양 계산), MealAnalyzer(집계·분석)  ← 순수 Kotlin
└─ ui/       history(기록·상세) · input(입력) · statistics(통계) · navigation · theme
```

---

## 2. 설치 및 실행 방법

**요구 환경**: Android Studio (AGP 9.3 지원 버전) · Android SDK API 37 · 기기/에뮬레이터 Android 9(API 28) 이상

1. Android Studio → **File → New → Project from Version Control** → 저장소 주소 입력 후 Clone
2. **SDK Manager**에서 Android API 37 설치 → **Gradle Sync**
3. **Device Manager**에서 에뮬레이터 실행(테스트: Pixel 8, Android 17) 또는 실기기 연결
4. 실행 구성 `app` 선택 → **Run ▶**

> 첫 실행 시 `assets/foods.json`의 음식 100종이 Room DB에 자동으로 저장됩니다.

### 테스트 실행

```bash
# 로컬 단위 테스트
./gradlew :app:testDebugUnitTest            # Windows: .\gradlew.bat :app:testDebugUnitTest
# Compose UI 테스트 (에뮬레이터/기기 연결 필요)
./gradlew :app:connectedDebugAndroidTest    # Windows: .\gradlew.bat :app:connectedDebugAndroidTest
```

| 구분 | 개수 | 주요 검증 내용 |
|---|---|---|
| **로컬 단위 테스트** | **28개** (5개 클래스) | 100g 기준값 일치, 250g = 2.5배, 0g, 음수 거부, 여러 음식 합계, 반올림, 30일 경계(포함/제외), 끼니별 집계, 빈 기록, 서울 시간대 날짜 경계, g 입력 검증 |
| **Compose UI 테스트** | **4개** (2개 클래스) | 섭취량 100→180g 변경 시 음식별 120→216kcal·한 끼 160→256kcal 갱신 / 기록 행 클릭 시 해당 식사 상세 표시 |

- 단위 테스트 28개 = **구현진 12개** (`NutritionCalculatorTest` 6 · `MealAnalyzerTest` 6: 영양 계산·30일 분석) + **이우석 16개** (`MealHistorySummaryTest` 7 · `MealInputFoodValidationTest` 6 · `IntakeValueFormatTest` 3: 날짜별 요약·g 입력 검증·표시 형식, 최종 테스트 단계에서 추가)
- UI 테스트: `MealEntryScreenTest`(2) · `MealListScreenTest`(2) — 테스트마다 인메모리 Room 사용
- 실행 결과: Pixel 8(Android 17)에서 계측 테스트 **5/5 통과** (예제 테스트 포함)

---

## 3. 영양 데이터 출처

- **출처**: 식품의약품안전처 [식품영양성분 데이터베이스(K-FIND)](https://various.foodsafetykorea.go.kr/nutrient/) · 음식 DB 19,617건 (데이터기준일자 2026-08-28)
- **선정**: 20대가 자주 먹는 음식 **100종** → [`app/src/main/assets/foods.json`](app/src/main/assets/foods.json)
  - 프랜차이즈 업체 데이터는 제외하고, 같은 음식은 **외식(분석) → 가정식(분석) → 급식(재료량 산출)** 순으로 실측값을 우선 선택
  - 각 항목에 원본 `식품코드`·`식품명`을 함께 저장해 원본과 대조 가능
- **계산식**: `섭취 영양값 = 100g당 영양값 × 섭취량(g) ÷ 100` → 원값으로 합산한 뒤 화면 표시 시에만 소수 첫째 자리 반올림
  - 예) 쌀밥(166kcal/100g) 150.5g → **249.8kcal**
- 짜장면·탕수육은 원본이 `100ml` 기준이라 앱에서 **단위 확인 필요**로 표시하고 g 계산에서 제외

---

## 4. 역할 분담

| 담당 | 이름 | 맡은 부분 |
|---|---|---|
| **A** | 구현진 | 프로젝트 기획·패키지 구조, 음식 100종 데이터 선정(`foods.json`), 데이터 모델, 영양 계산·분석 로직(`domain`), Room DB·DAO·Repository, 핵심 로직 단위 테스트 12개, 초기 화면 프로토타입, AGP 9 빌드 오류 해결, readme.md 작성 |
| **B** | 이우석 | 기록·입력·상세·통계 화면과 화면 이동, 사진 저장, 팀 코드 통합·Room 연결, 화면 로직 단위 테스트 16개, Compose UI 테스트, 테마·캐릭터·아이콘 디자인, 앱 작동 및 테스트 실행 영상 촬영 |

---

## 5. 팀원별 Vibe Coding 사용 목록

🅰 **A 구현진** · 🅱 **B 이우석** — ID 앞 글자가 프롬프트를 입력한 사람입니다.

| 작성자 | 사용 도구 | 전체 프롬프트 |
|:---:|---|:---:|
| 🅰 A 구현진 | Claude (A-00 기획·데이터) · Gemini in Android Studio (A-01~A-10) | 12건 |
| 🅱 B 이우석 | ChatGPT Work (Codex) · 이미지 생성 도구 · Inkscape | 39건 |

### 5-1. 작업 흐름

| 작성자 | 단계 | ID | 작업 내용 |
|:---:|---|---|---|
| 🅰 A | 기획·데이터 | A-00a~b | 프로젝트 계획, 식약처 DB에서 음식 100종 선정 |
| 🅰 A | 구조·로직 | A-01~05 | 패키지 구조, 영양 계산·분석 로직, 단위 테스트 12개, Room·Repository |
| 🅰 A | 초기 화면·빌드 | A-06~10 | 입력·목록·분석 초기 화면, AGP 9 빌드 오류 해결 |
| 🅱 B | 기반 준비 | B-001~005 | 실행 환경, 공통 테마, 하단 탭, 공통 카드 |
| 🅱 B | 기록·상세 | B-006~011 | 날짜 이동, 하루 요약, 식사 목록, 상세 화면 |
| 🅱 B | 식사 입력 | B-012~018 | 날짜·시간·끼니, 사진, 음식 검색, g 입력, 영양 미리보기, 저장 |
| 🅱 B | 섭취 통계 | B-019~022 | 일간·주간·30일 통계, 끼니별 그래프, 데이터 출처 |
| 🅱 B | 통합·검증 | B-023~026 | A·B 코드 통합, 사진 저장, 실제 데이터 검증, UI 테스트 |
| 🅱 B | 최종 테스트 |  | 화면 로직 단위 테스트 16개 추가 — 최종 테스트 중 직접 작성 |
| 🅱 B | 디자인 | B-028~039 | 화면 디자인, 캐릭터 '보리', 음식 아이콘 100종 |

> A-06~09의 초기 화면은 통합 단계(B-023)에서 B의 최종 화면으로 교체했습니다 (**초기 버전 → 최종 버전**).

### 5-2. 주요 프롬프트

과제 요구사항을 구현하거나 오류 수정을 요청한 프롬프트입니다. 긴 프롬프트는 핵심 요청 부분만 발췌했습니다(…).

<details><summary><b>🅰 A-00b</b> · 식약처 DB에서 음식 100종 선정</summary>

```text
(식약처 식품영양성분 DB 엑셀 첨부: 음식 19,617건)
이 앱에 사용할 데이터베이스야 이중에서 100개 골라줘 20대 청년인구가 주로 먹는 것들 위주로 선택해줘
```

→ 프랜차이즈 제외·실측값 우선으로 100종 선정 → `foods.json`, kcal 검산

</details>

<details><summary><b>🅰 A-01</b> · 요구사항 기반 패키지 구조 설계</summary>

```text
[프로젝트 맥락] MVVM + Room + Navigation Compose + Coil, 엔티티 Food·Meal·MealItem, 계산 로직은 순수 Kotlin(NutritionCalculator, MealAnalyzer)으로 분리
아래 요구사항을 만족하는 앱의 패키지 구조를 제안해줘. (<음식 입력하기> 1~5, <식사 보여주기> 6~9, <식사 분석하기> 10~11, <기타 요구사항> 공공 DB·출처 표시, 음식 100개, 단위 테스트 12개·UI 테스트 2개 이상)
각 패키지/파일이 어떤 역할을 하는지 한 줄씩 설명해주고, 구현 순서도 추천해줘.
```

→ `data` / `domain` / `ui` 구조와 구현 순서

</details>

<details><summary><b>🅰 A-03</b> · 데이터 모델과 영양 계산·분석 로직</summary>

```text
1.Food, Meal, MealItem, MealType data class 정의. 2.NutritionCalculator: 100g 기준 영양성분과 섭취량(g)으로 영양소 계산 로직 작성. 3.MealAnalyzer: 식사 리스트에서 1달 데이터 필터링, 총 칼로리 계산, 끼니별 영양소 분류 집계 로직 작성. 위 세가지 요청사항을 반영해주고 foods.json에 들어있는 자료들과 충돌하지 않도록 코딩해줘
```

→ `data/model`, `NutritionCalculator`, `MealAnalyzer`

</details>

<details><summary><b>🅰 A-04</b> · 핵심 로직 단위 테스트 12개</summary>

```text
위 내용을 참고해서 아래 사항들 코딩해줘 핵심 로직 단위 테스트(Unit Test) 작성 해
1.100g 섭취 시 성분값이 DB 값과 같다  2.250g 섭취 시 2.5배가 된다  3.0g이면 모두 0이다
4.음수 g 입력은 거부(예외)한다  5.여러 음식의 합계가 맞다  6.소수점 반올림 규칙이 맞다
7.시간으로 끼니 자동 추천  8.날짜별 끼니 수 집계  9.날짜별 kcal 합계
10.최근 30일 필터 경계값 (30일 전 포함, 31일 전 제외)  11.끼니 종류별 영양소 집계  12.기록이 없을 때 빈 결과 처리
```

→ `NutritionCalculatorTest`·`MealAnalyzerTest` 12개 — 요청 항목과 1:1 대응

</details>

<details><summary><b>🅰 A-05</b> · Room 로컬 데이터 계층</summary>

```text
[현재 요청사항]Asset JSON 구성 및 Room Local Data Layer 구축
1.assets/foods.json에 식약처 DB에서 고른 음식 100개 넣기. 2.FoodEntity, MealEntity, MealItemEntity, MealWithItems 작성. 3.FoodDao, MealDao, AppDatabase, JsonAssetLoader 및 FoodRepository, MealRepository 작성.
```

→ Entity·DAO·AppDatabase, JSON 로더, Repository

</details>

<details><summary><b>🅰 A-07~09</b> · 입력·목록·분석 초기 화면</summary>

```text
<음식 입력하기> MealEntryViewModel(NutritionCalculator로 미리보기 실시간 계산), MealEntryScreen: 날짜/시간 선택, 사진 선택, 음식 추가/섭취g 입력, 미리보기 카드
<식사 보여주기> MealListViewModel: 날짜별 분류 및 일별 열량/끼니 수 요약, MealListScreen, MealDetailDialog: 카드 터치 시 영양소 상세
<식사 분석하기> MealAnalysisViewModel: 최근 30일 분석, MealAnalysisScreen: 1달 칼로리 총량 및 끼니별 영양성분
```

→ 초기 화면 완성 → 통합(B-023)에서 B의 최종 화면으로 교체

</details>

<details><summary><b>🅰 A-10</b> · 실행 오류 수정 요청</summary>

```text
앱을 거의 다 구현했는데 실행 되지 않는다 그 이유를 알려주고 고쳐서 실행되도록 수정해줘 그리고 아래 기능들이 모두 구현되어 있는 지 체크하고 안되어 있다면 빠진 기능들 만들어줘
```

→ AGP 9 `BaseExtension` 빌드 오류 → 빌드 설정을 AGP 9 방식으로 정리해 실행 성공

</details>

<details><summary><b>🅱 B-004-1</b> · 하단 탭 빌드 오류 수정 요청</summary>

```text
오류 발생 고쳐줘
(첨부: :app:compileDebugKotlin에서 DietNavGraph() 선언이 두 후보로 잡혀 빌드 실패한 캡처)
```

→ `DietNavGraph()` 중복 선언으로 컴파일 실패 → 중복 제거

</details>

<details><summary><b>🅱 B-007</b> · 날짜별 열량·끼니 수 요약 카드</summary>

```text
기록 화면의 날짜 이동 영역 바로 아래에 `하루 섭취` 카드 하나를 추가해줘. `1000017906.png`의 01 식단 기록 화면을 참고해줘.
- 기존 `DietCard`와 연분홍 테마 색을 재사용해줘. 위에는 `하루 섭취` 제목과 크게 표시한 총열량(`390 kcal`), 그 옆에는 끼니 수(`1끼`)를 둔다. 아래에는 `탄수화물 / 단백질 / 지방` 순서로 이름과 g 값을 표시해줘. 큰 글자에서도 수치와 단위가 읽히도록 배치해줘.
- B-006의 기존 선택일을 사용해 `Asia/Seoul` 기준 해당 날짜의 요약을 표시해줘. A의 실제 요약 상태가 있으면 필드와 타입을 확인해 연결하고, 카드는 전달받은 총 kcal·끼니 수·탄단지 값을 그리도록 구성해줘. 음식 여러 개가 담긴 식사 한 건은 1끼로 센다. …
```

→ 선택일 총 kcal·끼니 수·탄단지 카드

</details>

<details><summary><b>🅱 B-008</b> · 사진+날짜 식사 목록</summary>

```text
`1000017906.png`의 01 식단 기록 화면 하단을 참고해, 기존 `하루 섭취` 카드 아래에 `식사 목록`을 추가해줘.
- 한 행은 식사 한 건이다. 왼쪽에 둥근 사진 썸네일, 오른쪽에 `11월 5일 · 12:30`처럼 날짜·시간과 끼니, 한 끼 총 kcal를 보여줘. 끼니 표시는 `조식 / 중식 / 석식 / 간식·음료`로 맞춰줘. 사진이 없거나 불러오지 못하면 같은 크기의 대체 이미지를 표시해줘.
- B-006의 선택일을 요약 카드와 목록에서 함께 사용해줘. `Asia/Seoul` 기준 그날의 기록만 시간 내림차순으로 표시하고, 시간이 같으면 ID 내림차순으로 보여줘. 각 행의 고유 키는 기존 식사 ID를 사용해줘.
```

→ 선택일 식사 목록과 썸네일

</details>

<details><summary><b>🅱 B-011</b> · 상세 화면 영양소·열량</summary>

```text
`1000017906.png`의 03 식사 상세 화면을 참고해, 기존 사진과 날짜·시간·끼니 아래에 다음 두 영역을 붙여줘.
- `한 끼 합계` 카드에는 총 kcal와 탄수화물·단백질·지방의 합계를 g 단위로 표시해줘. 기존 `DietCard`와 영양값 표시 형식을 재사용해줘.
- `음식별 영양`에는 음식명, 실제 섭취량(g), kcal, 탄수화물·단백질·지방(g)을 항목별로 표시해줘. 음식명이 길어도 줄바꿈이 되고, 항목이 많으면 기존 상세 화면에서 이어서 스크롤되게 해줘.
```

→ 음식별 영양값과 한 끼 합계

</details>

<details><summary><b>🅱 B-013</b> · 식사 날짜·시간·끼니 입력</summary>

```text
`1000017906.png`의 02 식사 입력 화면 상단을 참고해, 기존 `MealInputScreen.kt`에 날짜·시간·끼니 선택을 구현해줘.
- 제목 아래에 날짜와 시간 버튼을 배치하고, 그 아래에 `조식 / 중식 / 석식 / 간식·음료` 선택 버튼을 보여줘. 선택한 끼니에는 기존 Material 3 테마의 선택 색을 적용해줘.
- 처음에는 기록 탭에서 보던 날짜, 진입 시점의 `Asia/Seoul` 기준 시각, B-012에서 전달받은 끼니를 표시해줘. 기록 날짜가 이미 전달되고 있다면 재사용하고, 추가 전달이 필요하면 기존 입력 목적지에 필요한 인자만 연결해줘.
```

→ DatePicker·TimePicker·끼니 선택

</details>

<details><summary><b>🅱 B-014</b> · 음식 사진 입력</summary>

```text
`1000017906.png`의 02 식사 입력 화면처럼 끼니 선택 아래에 둥근 사진 미리보기와 `사진 선택` 버튼을 추가해줘.
- Compose의 `rememberLauncherForActivityResult`와 `PickVisualMedia`의 `ImageOnly` 옵션으로 시스템 Photo Picker에서 이미지 한 장을 선택하게 해줘.
- 사진이 없을 때는 빈 사진 영역을 보여줘. 선택하면 반환된 `Uri`의 미리보기를 표시하고 버튼을 `사진 변경`으로 바꿔줘. 새 사진을 고르면 미리보기를 교체하고, 선택창을 취소하면 기존 사진을 유지해줘.
```

→ Photo Picker 선택·미리보기

</details>

<details><summary><b>🅱 B-015</b> · 음식·반찬 이름 입력</summary>

```text
`1000017906.png`의 02 식사 입력 화면을 참고해, 기존 사진 영역 아래에 `음식·반찬 검색` 입력란과 선택한 음식 목록을 추가해줘.
- 음식 이름의 일부로 검색하고, 검색어가 비면 전체 후보를 보여줘. 결과를 누르면 선택 목록에 음식명이 추가된다. 검색어를 바꿔 반찬을 추가해도 먼저 고른 음식이 남게 해줘.
- 항목은 `foodCode`로 구분해줘. 같은 코드가 이미 선택됐다면 결과에서 `추가됨`을 표시하고 선택 목록에는 한 줄만 유지해줘. 이번 단계는 검색과 추가까지이며, 음식별 g 입력은 다음 단계에서 연결한다.
```

→ 검색 후 여러 항목 추가, 중복 방지 (한글 입력 문제 해결)

</details>

<details><summary><b>🅱 B-016</b> · 섭취량(g) 입력</summary>

```text
1. 첨부 파일을 B 프로젝트의 `app/src/main/assets/foods.json`에 넣는 방법을 알려줘. 기존 음식 조회 진입점에 이 파일을 연결해 debug·release에서 같은 데이터를 읽게 해줘. `foodCode`를 선택 항목의 키로 유지하고, 한글 부분 검색과 여러 음식 추가, 중복 방지 동작을 재사용해줘. …
2. `basis`가 `100g`인 음식은 선택과 g 입력을 허용해줘. `100ml`인 짜장면·탕수육은 검색 결과에 `단위 확인 필요`를 표시하고 g 입력 대상에서 보류해줘. JSON 원본 값은 보존해줘.
```

→ `foods.json` 연결, 음식별 g 입력·오류 표시

</details>

<details><summary><b>🅱 B-017</b> · 영양소 실시간 미리보기</summary>

```text
- 선택 음식의 `섭취량(g)` 아래에 그 양에 해당하는 kcal와 탄수화물·단백질·지방(g)을 표시해줘. 선택 목록 아래에는 `한 끼 미리보기` 카드를 추가하고, 어느 음식의 g을 바꿔도 해당 음식과 한 끼 합계가 함께 갱신되게 해줘.
- 팀 저장소 `diet-app-project/dietmain`의 `2db7438`을 기준으로 필드를 대조해줘. `foodCode`는 음식과 입력값을 연결하는 키, `basis`는 계산 기준, `kcal`·`carb`·`protein`·`fat`은 기준량의 영양값이다. …
```

→ 음식별·한 끼 합계 즉시 갱신, 계산값 직접 대조

</details>

<details><summary><b>🅱 B-021</b> · 최근 30일 칼로리·끼니별 분석</summary>

```text
# B-021 최근 30일 총 kcal와 끼니별 탄단지 그래프
B-020에서 StatisticsScreen.kt와 DietNavGraph.kt를 적용했다. 일간 265 kcal·1끼, 주간 431 kcal·2끼를 대조했고, debug·release 빌드와 Preview·Pixel 8 검증을 완료했다. 이번에는 `최근 30일` 탭에 총 kcal와 끼니별 탄단지 그래프를 연결해줘.
## 구현 범위
1. B-020에서 통계에 연결한 식사 원본과 B-019의 날짜 범위를 재사용해줘. Asia/Seoul 기준으로 기준일과 직전 29일을 포함한다. 같은 범위에서 총 kcal를 계산하고, 조식·중식·석식·간식/음료별로 탄수화물·단백질·지방의 g을 합산해줘. …
```

→ 30일 총 kcal, 끼니별 탄단지 그래프

</details>

<details><summary><b>🅱 B-022</b> · 데이터 출처 표시</summary>

```text
# B-022 통계 화면 영양 데이터 출처 표시
B-021에서 StatisticsScreen.kt를 적용했고, 고정일 745 kcal와 네 끼니의 공통 g 척도, debug·release 빌드, Preview·Pixel 8 검증을 완료했다. 이번에는 최근 30일 통계 화면에 음식 영양값 출처를 표시해줘.
## 구현 범위
- 최신 StatisticsScreen.kt를 확인하고, 최근 30일 그래프 아래에 기존 카드와 테마를 사용해 `영양 데이터 출처`와 `식품의약품안전처 식품영양성분 데이터베이스(K-FIND)`를 표시해줘. 식사 기록이 없는 기간에도 출처를 볼 수 있게 해줘.
```

→ 통계 화면 출처 카드와 K-FIND 링크

</details>

<details><summary><b>🅱 B-023</b> · 통합과 실제 저장 연결 요청</summary>

```text
# B-023 팀 코드 수합과 실제 Room 저장·목록 연결
B-022까지 B의 기록·입력·상세·통계 화면을 구현했다. B-018에서 임시 저장과 목록 복귀를 확인했고, B-022에서 K-FIND 출처 표시를 검증했다. 이번에는 팀 Room을 B 화면에 연결해 사진 없는 식사 한 건을 실제로 검색·저장하고 앱 재실행 후에도 기록 목록에서 조회할 수 있게 해줘.
## 수합 기준
- 팀 저장소는 https://github.com/diet-app-project/dietmain 이고 확인한 main은 2db74388e9830df3f8447bee7ba2cb0e5a74bf82다. B 원본 프로젝트를 보관하고, 별도 로컬 폴더에 팀 저장소를 복제해 수합해줘. …
```

→ A·B 코드 통합, 실제 Room 저장·목록 연결

</details>

<details><summary><b>🅱 B-024</b> · 사진 저장 수정</summary>

```text
# B-024 식사 사진 내부 저장과 상세 표시
B-023 통합 프로젝트에서 Pixel 8의 실제 Room 저장, 중복 방지, 강제 종료 후 재조회, 사진 선택 시 안내를 확인했다. Gradle 검증 결과는 확인 대기다. 이번에는 사진 선택부터 저장, 재실행 후 목록·상세 표시까지 연결해줘.
## 적용 전 확인
- B-023 통합 프로젝트 루트에서 .\gradlew.bat :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest를 먼저 확인해줘. 실패하면 첫 실패 태스크와 원인 메시지를 기준으로 해결하고 사진 변경을 적용해줘.
```

→ 사진을 앱 내부 저장소에 복사해 상세에 표시

</details>

<details><summary><b>🅱 B-026</b> · Compose UI 테스트 2개</summary>

```text
# B-026 Compose UI 동작 테스트 2개
B-025에서 코드 변경 없이 B-024 Gradle 통과와 실제 Room 통계를 확인했다. 현재 통합 프로젝트에서 아래 Compose UI 테스트 2개를 작성하고 Pixel 8에서 실행할 수 있게 해줘.
## 적용 위치와 테스트 데이터
- 아래 팀 기준 경로가 현재 통합 프로젝트에도 있는지 먼저 확인해줘.
```

→ Pixel 8에서 계측 테스트 5/5 통과

</details>
