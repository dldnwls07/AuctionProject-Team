package auction.model;

/**
 * [woojin 담당] 판매자 클래스 (User 상속)
 */
public class Seller extends User {

    public Seller(int userId, String userName) {
        super(userId, userName); // 부모생성자 호출하여 필드 설정
    }

    @Override
    public void showRole() {
        System.out.println("판매자입니다.");
    }
}
