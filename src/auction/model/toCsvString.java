package auction.model;

/**
 * Product 객체를 받아 CSV 포맷 한 줄(문자열)로 변환해 주는 클래스
 */
public class toCsvString {

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public static String convert(Product product) {
        if (product == null) {
            return "";
        }

        return product.getProductId() + "," + clean(product.getSellerName()) + "," + clean(product.getTitle()) + ","
                + clean(product.getDescription()) + "," + product.getStartPrice() + "," + product.getCurrentPrice()
                + ","
                + clean(product.getcurrentBidder()) + "," + clean(product.getImagePath()) + ","
                + clean(product.getAuctionStartTime()) + "," + clean(product.getAuctionEndTime()) + ","
                + clean(product.getLastBidTime()) + "," + clean(product.getStatus());
    }
}
