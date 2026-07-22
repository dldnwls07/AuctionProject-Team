package auction.model;

/**
 * CSV 파싱 및 생성 시 쉼표(,) 및 줄바꿈 문자를 공백으로 치환해 주는 유틸리티 클래스
 */
public class clean {

    public static String cleanValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(',', ' ').replace('\n', ' ');
    }
}
