\# 학교 경매 프로그램 코드 읽는 순서



프로그램 코드는 아래 순서대로 읽는 것을 추천합니다.



1\. 데이터를 담는 클래스

2\. CSV 파일 관리

3\. 실제 경매 규칙

4\. 화면 구성

5\. 프로그램 시작과 전체 연결



\---



\# 1단계: 데이터를 담는 클래스



먼저 프로그램에서 사용하는 데이터가 어떤 형태로 저장되는지 확인합니다.



\## 1.1 `Product.java`



상품 하나가 어떤 정보를 가지는지 확인합니다.



다음 순서로 코드를 읽어보세요.



1\. 필드

2\. 생성자

3\. Getter / Setter

4\. CSV 변환 메서드



특히 아래 메서드를 집중해서 확인합니다.



```java

toCsvString()

fromCsvString()

isOpen()

```



\### 확인할 내용



\* 상품 번호

\* 상품명

\* 상품 설명

\* 시작 가격

\* 현재 가격

\* 판매자

\* 최고 입찰자

\* 이미지 경로

\* 경매 시작 시간

\* 경매 종료 시간

\* 경매 상태



\### 주요 메서드의 역할



\#### `toCsvString()`



`Product` 객체의 정보를 CSV 파일에 저장할 수 있는 문자열로 변환합니다.



```text

Product 객체

→ CSV 한 줄

```



\#### `fromCsvString()`



CSV 파일의 한 줄을 읽어서 `Product` 객체로 변환합니다.



```text

CSV 한 줄

→ Product 객체

```



\#### `isOpen()`



현재 상품이 입찰 가능한 상태인지 확인합니다.



\---



\## 1.2 `Bid.java`



입찰 기록 하나가 어떤 구조로 저장되는지 확인합니다.



\### 확인할 내용



\* 입찰 번호

\* 상품 번호

\* 입찰자 이름

\* 입찰 가격

\* 입찰 시간



특히 상품 번호와 입찰 기록이 어떻게 연결되는지 확인합니다.



```text

Product의 상품 번호

↕

Bid의 상품 번호

```



예를 들어 상품 번호가 `3`인 상품에 입찰하면, 해당 입찰 기록의 상품 번호도 `3`으로 저장됩니다.



이를 통해 특정 상품의 입찰 기록만 찾을 수 있습니다.



\---



\## 1.3 `User.java`



`User`는 판매자와 입찰자가 공통으로 가지는 정보를 정의하는 추상 클래스입니다.



\### 확인할 내용



\* 공통 필드

\* 생성자

\* Getter / Setter

\* 추상 메서드



```java

public abstract class User {

}

```



판매자와 입찰자는 모두 사용자이기 때문에 이름과 같은 공통 정보를 `User` 클래스에서 관리합니다.



\### `showRole()`이 추상 메서드인 이유



판매자와 입찰자는 역할을 출력하는 방식이 서로 다릅니다.



```java

public abstract void showRole();

```



`User` 클래스에서는 정확한 역할을 결정할 수 없기 때문에 메서드의 형태만 정의합니다.



실제 내용은 자식 클래스에서 작성합니다.



\---



\## 1.4 `Seller.java`



판매자를 나타내는 클래스입니다.



```java

public class Seller extends User {

}

```



다음 문법을 확인합니다.



```java

extends User

super(...)

@Override

```



\### `extends User`



`Seller`가 `User` 클래스를 상속받는다는 의미입니다.



\### `super(...)`



부모 클래스인 `User`의 생성자를 호출합니다.



```java

public Seller(String name) {

&#x20;   super(name);

}

```



\### `@Override`



부모 클래스의 메서드를 판매자에 맞게 다시 작성합니다.



```java

@Override

public void showRole() {

&#x20;   System.out.println("판매자");

}

```



\---



\## 1.5 `Bidder.java`



입찰자를 나타내는 클래스입니다.



```java

public class Bidder extends User {

}

```



`Seller`와 마찬가지로 다음 내용을 확인합니다.



```java

extends User

super(...)

@Override

```



\### 상속 구조



```text

&#x20;          User

&#x20;         /    \\

&#x20;    Seller    Bidder

```



\### 다형성 사용 예시



```java

User user1 = new Seller("판매자");

User user2 = new Bidder("입찰자");



user1.showRole();

user2.showRole();

```



변수의 자료형은 모두 `User`이지만, 실제 객체에 따라 서로 다른 `showRole()`이 실행됩니다.



\---



\# 2단계: CSV 파일 관리



\## `DataManager.java`



이 클래스는 상품, 입찰 기록, 이미지 파일을 관리합니다.



다음 순서로 읽는 것을 추천합니다.



1\. 폴더 및 CSV 파일 생성

2\. `loadProducts()`

3\. `saveProducts()`

4\. `loadBids()`

5\. `saveBids()`

6\. 상품 번호와 입찰 번호 생성

7\. 이미지 복사



\---



\## 2.1 폴더 및 CSV 파일 생성



프로그램 실행에 필요한 폴더와 CSV 파일이 존재하는지 확인합니다.



없다면 새로 생성합니다.



예시 구조는 다음과 같습니다.



```text

project

├─ data

│  ├─ products.csv

│  └─ bids.csv

└─ images

```



\---



\## 2.2 `loadProducts()`



`products.csv` 파일에서 상품 정보를 읽습니다.



전체 흐름은 다음과 같습니다.



```text

CSV 한 줄 읽기

→ 문자열 분리 또는 변환

→ Product 객체 생성

→ ArrayList<Product>에 저장

```



예시:



```java

ArrayList<Product> products = new ArrayList<>();

```



CSV 파일의 각 줄을 `Product` 객체로 변환한 뒤 리스트에 추가합니다.



```java

products.add(product);

```



\---



\## 2.3 `saveProducts()`



`ArrayList<Product>`에 저장된 상품들을 CSV 파일에 저장합니다.



흐름은 불러오기의 반대입니다.



```text

ArrayList<Product>

→ Product 객체 하나씩 확인

→ toCsvString()

→ CSV 파일에 한 줄씩 저장

```



예시:



```java

for (Product product : products) {

&#x20;   writer.write(product.toCsvString());

&#x20;   writer.newLine();

}

```



\---



\## 2.4 `loadBids()`



`bids.csv` 파일에서 입찰 기록을 읽습니다.



```text

CSV 한 줄

→ Bid 객체 생성

→ ArrayList<Bid>에 저장

```



\---



\## 2.5 `saveBids()`



입찰 기록 리스트를 CSV 파일에 저장합니다.



```text

ArrayList<Bid>

→ Bid 객체의 CSV 문자열 변환

→ bids.csv에 저장

```



\---



\## 2.6 상품 번호와 입찰 번호 생성



새로운 상품이나 입찰 기록을 저장할 때 기존 번호와 겹치지 않는 번호를 생성합니다.



예를 들어 현재 가장 큰 상품 번호가 `5`라면 다음 상품 번호는 `6`이 됩니다.



```text

현재 최대 번호 찾기

→ 최대 번호 + 1

→ 새로운 번호로 사용

```



\---



\## 2.7 이미지 복사



사용자가 선택한 이미지를 프로그램의 이미지 폴더로 복사합니다.



```text

사용자가 선택한 원본 이미지

→ 프로그램의 images 폴더로 복사

→ 복사된 이미지 경로를 Product에 저장

```



\---



\## CSV 관리 핵심 흐름



\### CSV 파일을 불러올 때



```text

CSV 한 줄

→ 문자열 분리

→ Product 또는 Bid 객체 생성

→ ArrayList에 저장

```



\### CSV 파일에 저장할 때



```text

ArrayList

→ 객체 하나씩 꺼내기

→ toCsvString()

→ CSV 파일에 한 줄씩 저장

```



\---



\# 3단계: 실제 경매 규칙



\## `AuctionService.java`



이 클래스는 프로그램의 핵심입니다.



상품 등록, 입찰, 경매 종료, 낙찰자 결정 등 실제 경매 규칙을 처리합니다.



한 번에 모든 코드를 읽지 말고 기능별로 나누어 읽는 것이 좋습니다.



\---



\## 3.1 상품 등록



상품 등록에 필요한 정보를 전달받아 새로운 `Product` 객체를 생성합니다.



```text

화면에서 상품 정보 입력

→ AuctionService.registerProduct()

→ Product 객체 생성

→ 상품 번호 생성

→ 이미지 복사

→ CSV 저장

```



확인할 내용:



\* 필수 입력값 검사

\* 가격 검사

\* 경매 시간 검사

\* 상품 번호 생성

\* 이미지 경로 저장

\* `products.csv` 저장



\---



\## 3.2 입찰 조건 검사



입찰 전에 다음 조건들을 확인합니다.



\* 상품이 존재하는가?

\* 경매가 진행 중인가?

\* 경매 시간이 종료되지 않았는가?

\* 입찰 가격이 현재 가격보다 높은가?

\* 판매자가 자신의 상품에 입찰하지 않았는가?

\* 입력한 가격이 올바른 숫자인가?



조건을 만족하지 못하면 입찰을 진행하지 않습니다.



\---



\## 3.3 입찰 처리



입찰 기능은 다음 흐름으로 읽으면 됩니다.



```text

placeBid()

→ 최신 CSV 불러오기

→ 상품 찾기

→ 입찰 조건 검사

→ 상품의 현재 가격 변경

→ 최고 입찰자 변경

→ Bid 객체 생성

→ 상품 CSV 저장

→ 입찰 CSV 저장

```



\### 상세 흐름



\#### 1. 최신 CSV 불러오기



여러 프로그램이 같은 CSV 파일을 사용하기 때문에 입찰 직전에 최신 데이터를 다시 읽습니다.



```text

기존 메모리 데이터 사용 X

→ CSV 파일에서 최신 상품 목록 다시 불러오기

```



\#### 2. 상품 찾기



상품 번호를 이용하여 입찰할 상품을 찾습니다.



\#### 3. 입찰 조건 검사



입찰 가능한 상태인지 확인합니다.



\#### 4. 상품 정보 변경



입찰에 성공하면 상품의 현재 가격과 최고 입찰자를 변경합니다.



```java

product.setCurrentPrice(bidPrice);

product.setHighestBidder(bidderName);

```



\#### 5. `Bid` 객체 생성



새로운 입찰 기록을 만듭니다.



```java

Bid bid = new Bid(...);

```



\#### 6. CSV 저장



변경된 상품 정보와 새로운 입찰 기록을 저장합니다.



\---



\## 3.4 경매 상태 변경



경매 시간에 따라 상품 상태를 변경합니다.



예시 상태:



```text

대기

진행 중

낙찰

유찰

```



현재 시간이 종료 시간을 넘었는지 확인하여 경매 상태를 변경합니다.



\---



\## 3.5 낙찰 및 유찰 처리



경매 종료 시 최고 입찰자가 존재하면 낙찰 처리합니다.



```text

최고 입찰자 있음

→ 낙찰

```



입찰 기록이 하나도 없다면 유찰 처리합니다.



```text

최고 입찰자 없음

→ 유찰

```



\---



\## 3.6 남은 시간 계산



현재 시간과 종료 시간의 차이를 계산합니다.



```text

종료 시간 - 현재 시간 = 남은 시간

```



화면에서는 다음과 같은 형태로 표시할 수 있습니다.



```text

00:01:35

```



\---



\## 3.7 잠금 파일 처리



여러 실행 프로그램이 동시에 CSV 파일을 수정하면 데이터가 꼬일 수 있습니다.



이를 방지하기 위해 잠금 파일을 사용합니다.



```text

입찰 시작

→ 잠금 파일 확인

→ 잠금 획득

→ CSV 읽기 및 수정

→ CSV 저장

→ 잠금 해제

```



잠금이 이미 사용 중이라면 잠시 기다리거나 입찰을 다시 시도합니다.



\---



\# 4단계: 화면 구성



GUI 코드는 처음부터 모든 배치 코드를 이해하려 하지 않아도 됩니다.



먼저 버튼을 눌렀을 때 어떤 메서드가 호출되는지 확인하세요.



\---



\## 4.1 `ProductRegisterPanel.java`



상품을 등록하는 화면입니다.



다음 순서로 읽습니다.



1\. 입력칸과 버튼 생성

2\. 이미지 선택

3\. 등록 버튼 이벤트

4\. `AuctionService.registerProduct()` 호출



\### 확인할 화면 요소



\* 상품명 입력칸

\* 상품 설명 입력칸

\* 시작 가격 입력칸

\* 경매 시간 입력칸

\* 이미지 선택 버튼

\* 상품 등록 버튼



\### 이미지 선택



이미지 선택 버튼을 누르면 파일 선택 창이 열립니다.



```text

이미지 선택 버튼 클릭

→ JFileChooser 실행

→ 이미지 파일 선택

→ 선택한 경로 저장

```



\### 등록 버튼 이벤트



등록 버튼을 누르면 입력값을 읽어 서비스 클래스에 전달합니다.



```text

등록 버튼 클릭

→ 입력값 가져오기

→ AuctionService.registerProduct() 호출

→ 등록 결과 출력

→ 화면 초기화

```



\---



\## 4.2 `AuctionPanel.java`



상품 목록 조회와 입찰을 담당하는 화면입니다.



다음 부분을 중심으로 읽습니다.



1\. 상품 목록 테이블

2\. 상품 선택 시 상세 정보 표시

3\. 상태 필터

4\. 입찰 버튼 이벤트

5\. 입찰 기록 대화상자

6\. `refresh()`를 통한 화면 갱신



\---



\### 상품 목록 테이블



상품 정보를 표 형태로 보여줍니다.



예시 항목:



```text

상품 번호

상품명

현재 가격

최고 입찰자

남은 시간

상태

```



\---



\### 상품 선택 시 상세 정보 표시



테이블에서 상품을 선택하면 해당 상품의 상세 정보를 표시합니다.



```text

테이블 행 선택

→ 선택한 상품 번호 확인

→ 상품 객체 찾기

→ 설명과 이미지 표시

```



\---



\### 상태 필터



상품 상태에 따라 목록을 필터링합니다.



예시:



```text

전체

진행 중

낙찰

유찰

```



\---



\### 입찰 버튼 이벤트



입찰 버튼을 누르면 다음 흐름이 실행됩니다.



```text

상품 선택

→ 입찰 가격 입력

→ AuctionService.placeBid() 호출

→ 성공 또는 실패 메시지 출력

→ refresh() 실행

```



\---



\### 입찰 기록 대화상자



선택한 상품의 입찰 기록을 보여줍니다.



상품 번호를 기준으로 입찰 기록을 찾습니다.



```text

선택한 상품 번호

→ 전체 Bid 목록 확인

→ 같은 상품 번호의 Bid만 출력

```



\---



\### `refresh()`



CSV 파일에서 최신 상품 정보를 다시 불러와 화면을 갱신합니다.



```text

CSV 다시 읽기

→ 상품 테이블 초기화

→ 최신 데이터 추가

→ 남은 시간과 상태 다시 표시

```



여러 프로그램이 같은 CSV 파일을 사용하기 때문에 `refresh()`가 중요합니다.



\---



\# 5단계: 프로그램 시작과 전체 연결



\## `MainFrame.java`



이 클래스는 가장 마지막에 읽는 것을 추천합니다.



앞에서 확인한 클래스들이 어떻게 연결되는지 확인할 수 있습니다.



다음 순서로 읽습니다.



1\. `main()`으로 프로그램 시작

2\. 사용자 이름 입력

3\. `Bidder` 객체 생성

4\. 두 개의 탭 생성

5\. 서비스와 패널 연결

6\. Swing Timer로 자동 갱신



\---



\## 5.1 `main()`으로 프로그램 시작



자바 프로그램은 `main()` 메서드에서 시작합니다.



```java

public static void main(String\[] args) {

}

```



여기서 `MainFrame` 객체를 생성하고 화면을 표시합니다.



\---



\## 5.2 사용자 이름 입력



프로그램 실행 시 사용자 이름을 입력받습니다.



```text

프로그램 실행

→ 사용자 이름 입력창

→ 입력한 이름 저장

```



\---



\## 5.3 `Bidder` 객체 생성



입력받은 이름을 이용해 입찰자 객체를 생성합니다.



```java

Bidder bidder = new Bidder(userName);

```



이 객체는 입찰할 때 현재 사용자가 누구인지 확인하는 데 사용됩니다.



\---



\## 5.4 두 개의 탭 생성



예를 들어 다음 두 개의 탭을 생성합니다.



```text

상품 등록 탭

경매 참여 탭

```



각 탭에는 서로 다른 패널이 들어갑니다.



```java

ProductRegisterPanel

AuctionPanel

```



\---



\## 5.5 서비스와 패널 연결



GUI 패널에서 직접 CSV를 수정하지 않고 `AuctionService`를 호출합니다.



```text

GUI 패널

→ AuctionService 호출

→ DataManager 호출

→ CSV 저장

```



예시:



```text

ProductRegisterPanel

→ registerProduct()



AuctionPanel

→ placeBid()

```



\---



\## 5.6 Swing Timer 자동 갱신



Swing Timer를 사용하여 일정한 시간마다 화면을 갱신합니다.



예를 들어 1초마다 다음 작업을 실행합니다.



```text

경매 상태 확인

→ 남은 시간 계산

→ 낙찰 또는 유찰 처리

→ 상품 목록 새로고침

```



예시:



```java

Timer timer = new Timer(1000, e -> {

&#x20;   auctionPanel.refresh();

});

```



`1000`은 1,000밀리초, 즉 1초를 의미합니다.



\---



\# 전체 프로그램 구조



```text

MainFrame

│

├─ 프로그램 시작

├─ 사용자 생성

├─ 화면 생성

└─ 자동 갱신

&#x20;    │

&#x20;    ▼

GUI 패널

│

├─ ProductRegisterPanel

└─ AuctionPanel

&#x20;    │

&#x20;    ▼

AuctionService

│

├─ 상품 등록

├─ 입찰 조건 검사

├─ 입찰 처리

├─ 경매 종료

└─ 낙찰자 결정

&#x20;    │

&#x20;    ▼

DataManager

│

├─ products.csv 관리

├─ bids.csv 관리

├─ 번호 생성

└─ 이미지 관리

&#x20;    │

&#x20;    ▼

데이터 클래스

│

├─ Product

├─ Bid

└─ User

&#x20;   ├─ Seller

&#x20;   └─ Bidder

```



\---



\# 클래스별 역할 요약



| 클래스                    | 역할                 |

| ---------------------- | ------------------ |

| `Product`              | 상품 정보 저장           |

| `Bid`                  | 입찰 기록 저장           |

| `User`                 | 사용자 공통 정보 정의       |

| `Seller`               | 판매자 역할 구현          |

| `Bidder`               | 입찰자 역할 구현          |

| `DataManager`          | CSV와 이미지 파일 관리     |

| `AuctionService`       | 경매 규칙과 입찰 처리       |

| `ProductRegisterPanel` | 상품 등록 화면           |

| `AuctionPanel`         | 상품 조회 및 입찰 화면      |

| `MainFrame`            | 프로그램 시작과 전체 클래스 연결 |



\---



\# 코드 읽기 핵심 순서



```text

Product, Bid, User

→ 데이터가 무엇인지 이해



DataManager

→ 데이터가 CSV에 저장되는 방법 이해



AuctionService

→ 경매 규칙이 처리되는 방법 이해



GUI 패널

→ 버튼 클릭 시 어떤 기능이 실행되는지 이해



MainFrame

→ 전체 클래스가 연결되는 방법 이해

```



\---



\# 가장 먼저 이해해야 할 핵심 흐름



\## 상품 등록



```text

사용자 입력

→ ProductRegisterPanel

→ AuctionService.registerProduct()

→ Product 객체 생성

→ DataManager.saveProducts()

→ products.csv 저장

```



\## 입찰



```text

입찰 버튼 클릭

→ AuctionPanel

→ AuctionService.placeBid()

→ 최신 CSV 불러오기

→ 조건 검사

→ Product 가격 변경

→ Bid 객체 생성

→ CSV 저장

```



\## 자동 경매 종료



```text

Swing Timer 실행

→ AuctionService에서 시간 확인

→ 경매 종료 여부 판단

→ 낙찰 또는 유찰 처리

→ CSV 저장

→ 화면 갱신

```


