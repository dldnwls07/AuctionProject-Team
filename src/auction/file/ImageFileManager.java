package auction.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 상품 이미지 저장 폴더(images/) 관리 및 이미지 파일 복사를 전담하는 클래스
 */
public class ImageFileManager {

    private final File imageFolder;

    public ImageFileManager() {
        this.imageFolder = new File("images");
    }

    public ImageFileManager(File imageFolder) {
        this.imageFolder = imageFolder;
    }

    public boolean initializeFolder() {
        return FileUtil.ensureDirectory(imageFolder);
    }

    /**
     * 원본 이미지 파일을 이미지 저장 폴더로 복사하고, 저장된 상대 경로를 반환합니다.
     */
    public String copyImage(File sourceFile, String productId) {
        if (sourceFile == null || !sourceFile.exists() || productId == null) {
            return "";
        }

        initializeFolder();

        // 확장자 추출 (.png, .jpg 등)
        String fileName = sourceFile.getName();
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }

        // 저장 대상 파일명 생성: prod_<productId>.<extension>
        String targetFileName = "prod_" + productId + extension;
        File targetFile = new File(imageFolder, targetFileName);

        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return targetFile.getPath();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 상품 ID(int) 기반 이미지 복사 오버로딩 메서드
     */
    public String copyImage(File sourceFile, int productId) {
        return copyImage(sourceFile, String.valueOf(productId));
    }

    public File getImageFolder() {
        return imageFolder;
    }
}
