package auction.model;

/**
 * 마이페이지에서 사용자의 입찰 참여 상품 및 내 상태를 표현하는 DTO 클래스
 */
public class MyBidSummary {

    private final Product product;
    private final int myHighestBid;
    private final String myStatus; // 예: " 내가 1등 유지중", "최고가 갱신당함", " 최종 낙찰", "낙찰 실패", "유찰"

    public MyBidSummary(Product product, int myHighestBid, String myStatus) {
        this.product = product;
        this.myHighestBid = myHighestBid;
        this.myStatus = myStatus;
    }

    public Product getProduct() {
        return product;
    }

    public int getMyHighestBid() {
        return myHighestBid;
    }

    public String getMyStatus() {
        return myStatus;
    }
}
