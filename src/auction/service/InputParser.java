package auction.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 사용자 입력(가격/시간/텍스트)을 도메인 값으로 변환하는 파싱 유틸리티.
 */
public class InputParser {

    /**
     * CSV에 저장할 때 사용하는 시간 형식이다.
     */
    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * ProductRegisterPanel이 사용하는 시간 형식(초 없음)이다.
     */
    private static final DateTimeFormatter MINUTE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private InputParser() {
    }

    /**
     * 상품 번호 문자열을 정수로 변환한다.
     */
    public static int parseProductId(String productId) {
        if (productId == null) {
            return -1;
        }

        try {
            return Integer.parseInt(productId.trim());

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 가격 문자열을 정수로 변환한다.
     */
    public static int parsePrice(String text) {
        if (text == null) {
            return -1;
        }

        try {
            return Integer.parseInt(text.trim().replace(",", ""));

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 시간 문자열을 LocalDateTime으로 변환한다.
     *
     * 다음 두 형식을 모두 지원한다.
     *
     * yyyy-MM-dd HH:mm:ss
     * yyyy-MM-dd HH:mm
     */
    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String value = text.trim();

        try {
            return LocalDateTime.parse(value, TIME_FORMAT);

        } catch (Exception e) {
            /*
             * 초가 없는 새 GUI 시간 형식으로 다시 시도한다.
             */
        }

        try {
            return LocalDateTime.parse(value, MINUTE_TIME_FORMAT);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * CSV 저장에 문제가 될 수 있는 문자를 정리한다.
     */
    public static String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.trim()
                .replace(',', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }

    /**
     * 현재 시간을 초 단위 문자열로 반환한다.
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
