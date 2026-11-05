# 학생식당 식단 관리 앱 (DietApp)

Android Studio, Kotlin, Jetpack Compose(Material 3) 기반의 학생식당 식단 및 영양성분 관리 애플리케이션입니다.

---

## 📱 주요 기능 소개

### 1. 음식 입력하기 (`<음식 입력하기>`)
- **식사 날짜 및 시간 선택**: 네이티브 DatePicker/TimePicker로 간편하게 지정
- **시간 기반 끼니 자동 추천**: 입력 시간에 맞춰 조식/중식/석식/간식 자동 분류
- **음식 사진 입력**: Coil 기반 갤러리 이미지 선택/미리보기/삭제 지원
- **음식 및 반찬 검색/추가**: 공공 DB 기반 100가지 음식 데이터 실시간 검색 및 추가
- **섭취량(g) 입력**: 음식 항목별 섭취 수치(g) 지정
- **실시간 영양성분 미리보기**: 섭취량 비례식 계산 기반 탄수화물, 단백질, 지방(g) 및 열량(kcal) 실시간 미리보기 카드 제공

### 2. 식사 보여주기 (`<식사 보여주기>`)
- **일별/날짜별 그룹화 카드 목록**: 일시, 대표 사진, 끼니 종류, 포함 음식 요약 및 총 칼로리 표시
- **날짜별 요약 바**: 날짜별 총 칼로리 섭취량 및 끼니 수 요약 제공
- **상세 다이얼로그 (`MealDetailDialog`)**: 목록 클릭 시 섭취 음식 개별 항목별 섭취량(g)과 칼로리/탄단지 상세 보기 및 식사 기록 삭제 지원

### 3. 식사 분석하기 (`<식사 분석하기>`)
- **최근 1달(30일) 칼로리 총량**: 최근 30일간 섭취한 칼로리 합계 및 식사 횟수 요약
- **끼니 종류별 영양 분석**: 조식, 중식, 석식, 간식/음료별 식사 횟수, 칼로리 비율(%), 프로그레스 바 및 탄수화물/단백질/지방 수치 분석

### 4. 기타 안내 및 출처 (`<정보/출처>`)
- **영양 데이터 출처 안내**: 식약처 식품영양성분DB 공공데이터 출처 표기

---

## 🛠 설치 및 실행 방법

1. **프로젝트 클론 및 안드로이드 스튜디오에서 열기**
   ```bash
   git clone <repository_url>
   ```
2. **Gradle 빌드 및 동기화**
   - Android Studio에서 프로젝트를 열고 `Gradle Sync`를 실행합니다.
3. **앱 실행**
   - 에뮬레이터 또는 Android 기기(Android 9.0 / API 28 이상)를 연결한 후 `Run 'app'` (Shift + F10) 실행.

---

## 📊 영양 데이터 출처

- **데이터 출처**: 식품의약품안전처 (식약처) 식품영양성분DB (공공데이터포털)
- **활용 범위**: 대표 음식 약 100가지 100g당 열량, 탄수화물, 단백질, 지방 데이터 (`assets/foods.json`)

---

## 👥 역할 분담 및 Vibe Coding (프롬프트 목록)

### 1. 역할 분담
- **Android / UI / Architecture**: MVVM, Room DB, Navigation Compose, Material 3, Coil 연동
- **Domain & Unit Test**: Pure Kotlin 비즈니스 로직 분리 (`NutritionCalculator`, `MealAnalyzer`), JUnit 로컬 단위 테스트 (13개) 작성
- **UI Test**: Compose UI Test (`MealEntryScreenTest`, `MealListScreenTest`) 작성

### 2. Vibe Coding 프롬프트 로그
- **Prompt 1**: "앱 패키지 구조 및 추천 구현 순서 제안"
- **Prompt 2**: "순수 Kotlin Domain 로직 (`NutritionCalculator`, `MealAnalyzer`) 및 Data Model 구축"
- **Prompt 3**: "핵심 로직 단위 테스트(Unit Test) 12개 이상 작성 및 검증"
- **Prompt 4**: "Room Local Database, DAO, Entity, Repository 및 Asset Json Loader 구축"
- **Prompt 5**: "Navigation Compose 및 BottomNavigation 뼈대 구성"
- **Prompt 6**: "<음식 입력하기> 실시간 영양성분 미리보기 및 PhotoPicker 구현"
- **Prompt 7**: "<식사 보여주기> 날짜별 요약 목록 및 MealDetailDialog 구현"
- **Prompt 8**: "<식사 분석하기> 최근 1달 칼로리 및 끼니별 영양성분 분석 구현"
- **Prompt 9**: "AGP 9 호환성 및 Gradle 빌드 이슈 해결"
