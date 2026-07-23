package auction.service;

import java.io.File;

/**
 * auction.lock 파일을 이용한 동시성 제어를 전담하는 클래스.
 * 여러 프로세스가 동시에 CSV를 수정하지 못하도록 파일 생성/삭제로 잠금을 구현한다.
 */
public class LockManager {

    private final File lockFile;
    private String lastErrorMessage = "";

    public LockManager(File lockFile) {
        this.lockFile = lockFile;
    }

    /**
     * 잠금 파일을 생성한다. 실패하면 사용자에게 보여줄 오류 메시지를 남긴다.
     */
    public boolean acquire() {
        try {
            if (lockFile.createNewFile()) {
                return true;
            }

            lastErrorMessage =
                    "다른 사용자가 처리 중입니다. "
                            + "잠시 후 다시 시도해 주세요.";

            return false;

        } catch (Exception e) {
            lastErrorMessage =
                    "잠금 파일을 만들지 못했습니다.";

            return false;
        }
    }

    /**
     * 자동 상태 갱신(1초 Timer)에서 조용히 잠금을 시도한다.
     * 실패해도 별도의 오류 메시지를 남기지 않고 다음 실행을 기다린다.
     */
    public boolean acquireQuietly() {
        try {
            return lockFile.createNewFile();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 작업이 끝나면 잠금 파일을 삭제한다.
     */
    public void release() {
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    /**
     * 이전 실행이 비정상 종료되어 잠금 파일이 남아있는지 확인한다.
     */
    public boolean exists() {
        return lockFile.exists();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
