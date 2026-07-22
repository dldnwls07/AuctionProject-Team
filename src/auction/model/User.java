package auction.model;

/**
 * [woojin 담당] 교내 경매 프로그램 사용자의 공통 정보 추상 클래스
 */
public abstract class User {
    protected int userId;
    protected String userName;

    /**
     * User 객체를 생성하는 생성자 함수다.
     * 
     * @param userId   사용자 고유 식별 번호 (this.userId 필드에 저장됨)
     * @param userName 사용자 이름 문자열 (this.userName 필드에 저장됨)
     */
    public User(int userId, String userName) {
        // 전달받은 userId 매개변수 값을 클래스의 protected userId 필드에 할당한다.
        this.userId = userId;
        // 전달받은 userName 매개변수 값을 클래스의 protected userName 필드에 할당한다.
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public abstract void showRole(); // 추상화 클래스
}