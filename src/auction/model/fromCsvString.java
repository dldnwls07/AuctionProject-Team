package auction.model;

/**
 * CSV 파일의 한 줄(문자열)을 입력받아 Product 객체로 변환해 주는 클래스
 */
public class fromCsvString {

    public static Product parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            String[] values = line.split(",", -1);
            if (values.length != 12) {
                return null;
            }

            String productId = values[0].trim();
            int startPrice = Integer.parseInt(values[4].trim());
            int currentPrice = Integer.parseInt(values[5].trim());

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
            return null;
        }
    }
}
