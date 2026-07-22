package auction.model;

/**
 * [woojin 담당] 입찰 기록 모델 클래스 (CSV 5개 열)
 */
public class Bid {

    private int bidId; // 각 입찰 기록마다 부여되는 고유 입찰 식별 번호

    private int productId; // 입찰을 진행한 경매 대상 상품의 고유 ID 번호

    private String bidderName; // 입찰을 수행한 사용자의 이름 문자열

    private int bidPrice; // 입찰자가 제시한 입찰 가격

    private String bidTime; // 입찰이 발생한 시각 문자열

    public Bid(int bidId, int productId, String bidderName, int bidPrice, String bidTime) {
        this.bidId = bidId;
        this.productId = productId;
        this.bidderName = bidderName;
        this.bidPrice = bidPrice;
        this.bidTime = bidTime;
    }

    public int getBidId() {
        return bidId;
    }

    public void setBidId(int bidId) {
        this.bidId = bidId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public int getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(int bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getBidTime() {
        return bidTime;
    }

    public void setBidTime(String bidTime) {
        this.bidTime = bidTime;
    }
}
