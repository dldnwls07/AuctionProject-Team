package auction.model;

/**
 * [woojin 담당] 입찰자 클래스 (User 상속)
 */
public class Bidder extends User {

    public Bidder(int userId, String userName) {
        super(userId, userName); // 부모생성자 호출하여 필드 설정
    }

    @Override
    public void showRole() {
        System.out.println("입찰자입니다.");
    }
}
