# 🚀 교내 경매 프로젝트 (AuctionProject-Team) 폴더 & 개발 파이프라인

본 문서는 `/Users/woojin/Developer/AuctionProject-Team` 자바 경매 프로그램 프로젝트의 폴더 구조, 팀원별 역할 분담, Git 브랜치 전략 및 데이터 흐름 파이프라인 정리 문서입니다.

---

## 📁 1. 프로젝트 폴더 및 패키지 구조 파이프라인

```text
AuctionProject-Team/
├── src/                               # 자바 소스 코드 패키지 루트
│   └── auction/                       # 메인 패키지 (MainFrame.java 진입점)
│       ├── model/                     # [개발자 A: woojin] 도메인 데이터 모델
│       │   ├── User.java              # 추상 사용자 클래스
│       │   ├── Bidder.java            # 입찰자 클래스 (User 상속)
│       │   ├── Seller.java            # 판매자 클래스 (User 상속)
│       │   ├── Bid.java               # 입찰 기록 모델 (CSV 5개 열 변환)
│       │   └── Product.java           # 상품 모델 (CSV 12개 열 / 10개 열 호환)
│       ├── file/                      # [개발자 A: woojin] 파일 입출력 및 데이터 관리
│       │   └── DataManager.java       # products.csv, bids.csv, images/ 처리
│       ├── service/                   # [개발자 B: myunghun] 비즈니스 로직 & 동시성
│       │   └── AuctionService.java    # 등록/입찰 검증, lock 파일 제어, 1초 마감 판정
│       ├── gui/                       # [개발자 C: soeun] Swing 프론트엔드 UI
│       │   ├── ProductRegisterPanel.java # 상품 등록 입력 폼 & 이미지 선택기
│       │   └── AuctionPanel.java      # 경매 목록 JTable, 필터, 입찰 & 기록 팝업
│       └── MainFrame.java             # [개발자 C: soeun] 메인 프레임 & 1초 백그라운드 타이머
├── data/                              # [자동 생성] CSV 데이터 보관 폴더
│   ├── products.csv                   # 상품 데이터 저장 파일
│   ├── bids.csv                       # 입찰 데이터 저장 파일
│   └── auction.lock                   # 동시 접근 제어 임시 잠금 파일
├── images/                            # [자동 생성] 등록된 상품 이미지 저장 폴더 (product_X.png)
├── docs/                              # 프로젝트 문서 및 명세서 보관
└── Pipeline.md                        # 본 개발 파이프라인 정리 문서
```

---

## 👥 2. 개발자별 역할 분담 및 담당 브랜치

| 브랜치 명 | 담당 개발자 | 역할 구분 | 주요 담당 클래스 및 패키지 |
| :--- | :--- | :--- | :--- |
| **`main`** | 팀 전체 | 통합 상용 브랜치 | 최종 검증된 안정 버전 통합 코드 |
| **`woojin`** | 개발자 A (우진) | Data/Model 백엔드 | `src/auction/model/*`, `src/auction/file/DataManager.java` |
| **`myunghun`** | 개발자 B (명훈) | Service 로직엔진 | `src/auction/service/AuctionService.java` |
| **`soeun`** | 개발자 C (소은) | GUI/Frontend UI | `src/auction/gui/*`, `src/auction/MainFrame.java` |

---

## 🔀 3. Git 브랜치 전략 및 릴레이 통합 파이프라인

코드 충돌(Git Conflict)을 최소화하기 위해 **A ➔ B ➔ C** 순서로 의존성을 가지고 `main` 브랜치에 순차 통합(Merge)합니다.

```mermaid
gitGraph
   commit id: "Initial Commit"
   branch woojin
   branch myunghun
   branch soeun
   checkout woojin
   commit id: "Model & DataManager 개발"
   checkout main
   merge woojin id: "1차 Merge (A)"
   checkout myunghun
   merge main id: "A코드 pull"
   commit id: "AuctionService 로직 개발"
   checkout main
   merge myunghun id: "2차 Merge (B)"
   checkout soeun
   merge main id: "A,B코드 pull"
   commit id: "Swing UI & Timer 개발"
   checkout main
   merge soeun id: "3차 Merge (C)"
```

### 📌 릴레이 통합 3단계 수칙
1. **1단계 (`woojin`)**: 데이터 모델 및 `DataManager` 구현 완료 후 `main`에 1차 Merge
2. **2단계 (`myunghun`)**: `git pull origin main` 수행 후 `AuctionService` 구현 ➔ `main`에 2차 Merge
3. **3단계 (`soeun`)**: `git pull origin main` 수행 후 Swing UI 패널 & 1초 타이머 연동 ➔ `main`에 최종 3차 Merge

---

## 🔄 4. 데이터 및 실행 파이프라인 (Data Flow Pipeline)

1. **사용자 입력 (GUI Layer - 소은)**: `ProductRegisterPanel` 또는 `AuctionPanel`에서 버튼 클릭 이벤트 발생
2. **검증 및 비즈니스 처리 (Service Layer - 명훈)**: `AuctionService`에서 유효성 검증(시작가, 마감시간, 자가입찰 방지) 및 `auction.lock` 잠금 생성
3. **데이터 파일 영속화 (Data Layer - 우진)**: `DataManager`를 통해 CSV 텍스트 변환 (`toCsvString`) 후 `products.csv`, `bids.csv` 저장 및 이미지 복사
4. **실시간 자동 반영 (Timer Event - 소은)**: `MainFrame`의 `javax.swing.Timer`가 1000ms 마다 `updateAuctionStatuses()`를 호출하여 마감 알림 팝업 및 화면 자동 갱신

---

## 🛠️ 5. 빌드 및 실행 명령어

### 컴파일 (Compile)
```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
```

### 프로그램 실행 (Run)
```bash
java -cp bin auction.MainFrame
```
