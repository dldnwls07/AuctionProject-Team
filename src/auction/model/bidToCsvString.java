package auction.model;

public class bidToCsvString {
    public static String convert(Bid bid) {
        if (bid == null) {
            return "";
        }
        return bid.getBidId() + "," + bid.getProductId() + ","
                + clean.cleanValue(bid.getBidderName()) + ","
                + bid.getBidPrice() + ","
                + clean.cleanValue(bid.getBidTime());
    }
}
