package auction.model;

/**
 * Product 객체를 받아 CSV 포맷 한 줄(문자열)로 변환해 주는 클래스
 */
public class productToCsvString {

    private static String clean(String value) { // 공백처리하는 클래스
        if (value == null) {
            return "";
        }
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public static String convert(Product product) {
        if (product == null) {
            return "";
        }
        // 공백처리한거끼리 이어 붙이기
        return product.getProductId() + "," + clean(product.getSellerName()) + "," + clean(product.getTitle()) + ","
                + clean(product.getDescription()) + "," + product.getStartPrice() + "," + product.getCurrentPrice()
                + ","
                + clean(product.getcurrentBidder()) + "," + clean(product.getImagePath()) + ","
                + clean(product.getAuctionStartTime()) + "," + clean(product.getAuctionEndTime()) + ","
                + clean(product.getLastBidTime()) + "," + clean(product.getStatus());
    }
}
