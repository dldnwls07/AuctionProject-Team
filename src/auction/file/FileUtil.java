package auction.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

/**
 * 디렉터리 생성 및 CSV 헤더 파일 생성 등 파일 관련 공통 처리 유틸리티 클래스
 */
public class FileUtil {

    /**
     * 지정한 디렉터리가 존재하지 않는 경우 디렉터리를 생성합니다.
     */
    public static boolean ensureDirectory(File dir) {
        if (dir == null) {
            return false;
        }
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * CSV 파일이 존재하지 않을 때, UTF-8 인코딩으로 헤더 문자열을 작성하여 새 파일을 생성합니다.
     */
    public static boolean createCsvFile(File file, String header) {
        if (file == null) {
            return false;
        }
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (parent != null) {
                    ensureDirectory(parent);
                }
                try (BufferedWriter writer = new BufferedWriter(
                        new FileWriter(file, StandardCharsets.UTF_8))) {
                    writer.write(header);
                    writer.newLine();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
