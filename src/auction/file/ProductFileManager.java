package auction.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import auction.model.Product;
import auction.model.productFromCsvString;
import auction.model.productToCsvString;

/**
 * products.csv 파일 I/O 및 메모리 내 Product 데이터 목록을 전담 관리하는 클래스
 */
public class ProductFileManager {

    public static final String PRODUCT_HEADER = "productId,sellerName,title,description,startPrice,currentPrice,currentBidder,imagePath,auctionStartTime,auctionEndTime,lastBidTime,status";

    private final File productFile;
    private final ArrayList<Product> products;

    public ProductFileManager() {
        this.productFile = new File("data/products.csv");
        this.products = new ArrayList<>();
    }

    public ProductFileManager(File productFile) {
        this.productFile = productFile;
        this.products = new ArrayList<>();
    }

    public boolean initializeFile() {
        return FileUtil.createCsvFile(productFile, PRODUCT_HEADER);
    }

    /**
     * CSV 파일에서 상품 목록을 읽어와 메모리 리스트에 저장합니다.
     */
    public boolean loadProducts() {
        products.clear();
        if (!productFile.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(productFile, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 헤더 건너뛰기
            while ((line = reader.readLine()) != null) {
                Product product = productFromCsvString.parse(line);
                if (product != null) {
                    products.add(product);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 메모리의 상품 목록을 CSV 파일에 저장합니다.
     */
    public boolean saveProducts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(productFile, StandardCharsets.UTF_8))) {
            writer.write(PRODUCT_HEADER);
            writer.newLine();

            for (Product product : products) {
                String csvLine = productToCsvString.convert(product);
                if (!csvLine.isEmpty()) {
                    writer.write(csvLine);
                    writer.newLine();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 다음 상품 ID를 생성합니다 (기존 최대 숫자 ID + 1).
     */
    public String nextProductId() {
        int maxId = 0;
        for (Product p : products) {
            try {
                int id = Integer.parseInt(p.getProductId().replaceAll("[^0-9]", ""));
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return String.valueOf(maxId + 1);
    }

    /**
     * 상품 ID(String)로 상품을 찾습니다.
     */
    public Product findProduct(String productId) {
        if (productId == null) {
            return null;
        }
        for (Product p : products) {
            if (productId.equals(p.getProductId())) {
                return p;
            }
        }
        return null;
    }

    /**
     * 상품 ID(int)로 상품을 찾습니다 (오버로딩).
     */
    public Product findProduct(int productId) {
        return findProduct(String.valueOf(productId));
    }

    /**
     * 새로운 상품을 리스트에 추가하고 CSV 파일에 저장합니다.
     */
    public void addProduct(Product product) {
        if (product != null) {
            products.add(product);
            saveProducts();
        }
    }

    /**
     * 전체 상품 목록을 반환합니다.
     */
    public ArrayList<Product> getProducts() {
        return products;
    }
}
