package auction.model;

/**
 * CSV 한 줄(문자열)을 입력받아 Bid 객체로 변환해 주는 클래스 (5개 열 규격)
 */
public class bidFromCsvString {

    public static Bid parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            String[] values = line.split(",", -1);
            if (values.length != 5) {
                return null;
            }

            int bidId = Integer.parseInt(values[0].trim());
            int productId = Integer.parseInt(values[1].trim());
            String bidderName = values[2].trim();
            int bidPrice = Integer.parseInt(values[3].trim());
            String bidTime = values[4].trim();

            return new Bid(bidId, productId, bidderName, bidPrice, bidTime);
        } catch (Exception e) {
            return null;
        }
    }
}
