package auction.model;

/**
 * Product 객체를 받아 CSV 포맷 한 줄(문자열)로 변환해 주는 클래스
 */
public class productToCsvString {

    public static String convert(Product product) {
        if (product == null) {
            return "";
        }
        return clean.cleanValue(product.getProductId()) + ","
                + clean.cleanValue(product.getSellerName()) + ","
                + clean.cleanValue(product.getTitle()) + ","
                + clean.cleanValue(product.getDescription()) + ","
                + product.getStartPrice() + ","
                + product.getCurrentPrice() + ","
                + clean.cleanValue(product.getCurrentBidder()) + ","
                + clean.cleanValue(product.getImagePath()) + ","
                + clean.cleanValue(product.getAuctionStartTime()) + ","
                + clean.cleanValue(product.getAuctionEndTime()) + ","
                + clean.cleanValue(product.getLastBidTime()) + ","
                + clean.cleanValue(product.getStatus());
    }
}
