package auction.model;

/**
 * [woojin 담당] 경매 상품 모델 클래스 (CSV 12개 열)
 */
public class Product {
    // [상수] STATUS_WAITING: 아직 경매 시작 시간이 되지 않아 시작을 기다리는 상태
    public static final String STATUS_WAITING = "대기 중";

    // [상수] STATUS_OPEN: 경매가 시작되어 입찰을 정상적으로 받고 있는 상태
    public static final String STATUS_OPEN = "진행 중";

    // [상수] STATUS_SOLD: 경매 종료 시 입찰자가 있어 최종 낙찰된 상태
    public static final String STATUS_SOLD = "낙찰";

    // [상수] STATUS_NO_BID: 경매 종료 시 아무도 입찰하지 않아 유찰된 상태
    public static final String STATUS_NO_BID = "유찰";

    private String productId; // 상품의 고유 식별 번호
    private String sellerName; // 상품 등록한 판매자 이름
    private String title; // 경매 상품 제목
    private String description; // 상품 설명
    private int startPrice; // 상품 최소 시작가격
    private int currentPrice; // 현재까지 등록된 최고 입찰가
    private String currentBidder; // 현재까지 등록된 최고 입찰자 이름
    private String imagePath; // 이미지 경로
    private String auctionStartTime; // 경매가 시각 문자열
    private String auctionEndTime; // 경매 종료 시각
    private String lastBidTime; // 가장 최근 등록된 입찰 시각
    private String status; // 경매 현재 상태

    public Product(String productId, String sellerName, String title, String description, int startPrice,
            int currentPrice, String currentBidder,
            String imagePath, String auctionStartTime, String auctionEndTime, String lastBidTime, String status) {
        this.productId = productId;
        this.sellerName = sellerName;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.currentBidder = currentBidder;
        this.imagePath = imagePath;
        this.auctionStartTime = auctionStartTime;
        this.auctionEndTime = auctionEndTime;
        this.lastBidTime = lastBidTime;
        this.status = status;
    }

    public static String getStatusWaiting() {
        return STATUS_WAITING;
    }

    public static String getStatusOpen() {
        return STATUS_OPEN;
    }

    public static String getStatusSold() {
        return STATUS_SOLD;
    }

    public static String getStatusNoBid() {
        return STATUS_NO_BID;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getcurrentBidder() {
        return currentBidder;
    }

    public void setCurrentBidder(String currentBidder) {
        this.currentBidder = currentBidder;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAuctionStartTime() {
        return auctionStartTime;
    }

    public void setAuctionStartTime(String auctionStartTime) {
        this.auctionStartTime = auctionStartTime;
    }

    public String getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(String auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public String getLastBidTime() {
        return lastBidTime;
    }

    public void setLastBidTime(String lastBidTime) {
        this.lastBidTime = lastBidTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private static String clean(String value) {
        if (value == null)
            return ""; // 들어온 값이 null 이면 공백반환
        // 쉼표 및 엔터(줄바꿈) 문자를 띄어쓰기로 교체하여 반환한다.
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String toCsvString() {
        return productId + "," + clean(sellerName) + "," + clean(title) + ","
                + clean(description) + "," + startPrice + "," + currentPrice + ","
                + clean(currentBidder) + "," + clean(imagePath) + ","
                + clean(auctionStartTime) + "," + clean(auctionEndTime) + ","
                + clean(lastBidTime) + "," + clean(status);
    }

    /**
     * CSV 파일의 한 줄(문자열)을 입력받아 Product 객체로 변환하는 정적 팩토리 메서드 (12개 열 규격)
     */
    public static Product fromCsvString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            // 쉼표(,) 기준으로 분할한다. (limit -1로 빈 값 보존)
            String[] values = line.split(",", -1);

            // 열 개수가 12개가 아니면 정상 규격이 아니므로 null 반환
            if (values.length != 12) {
                return null;
            }

            String productId = values[0].trim();
            int startPrice = Integer.parseInt(values[4].trim());
            int currentPrice = Integer.parseInt(values[5].trim());

            // 12개 필드를 인자로 전달해 Product 객체 생성 후 반환
            return new Product(
                    productId, // 0: 상품 ID (String)
                    values[1].trim(), // 1: 판매자 이름
                    values[2].trim(), // 2: 상품 제목
                    values[3].trim(), // 3: 상품 설명
                    startPrice, // 4: 시작 가격
                    currentPrice, // 5: 현재 가격
                    values[6].trim(), // 6: 최고 입찰자
                    values[7].trim(), // 7: 이미지 경로
                    values[8].trim(), // 8: 경매 시작 시각
                    values[9].trim(), // 9: 경매 종료 시각
                    values[10].trim(), // 10: 최근 입찰 시각
                    values[11].trim() // 11: 상태 문자열
            );
        } catch (Exception e) {
            // 정수 파싱 오류 등이 발생한 경우 null 반환
            return null;
        }
    }

}
