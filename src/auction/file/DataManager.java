package auction.file;

import java.io.File;
import java.util.ArrayList;

import auction.model.Bid;
import auction.model.Product;

/**
 * [woojin 담당] CSV 파일 읽기/쓰기 및 이미지 저장 데이터 관리 파사드(Facade) 클래스
 * 
 * 단일 책임 원칙(SRP)에 따라 분리된 서브 매니저(ProductFileManager, BidFileManager,
 * ImageFileManager)들에게
 * 실제 처리를 위임하며, 외부(GUI 및 Service)와의 통일된 통로 역할을 수행합니다.
 */
public class DataManager {

    public static final String PRODUCT_HEADER = ProductFileManager.PRODUCT_HEADER;
    public static final String BID_HEADER = BidFileManager.BID_HEADER;

    private final ProductFileManager productFileManager;
    private final BidFileManager bidFileManager;
    private final ImageFileManager imageFileManager;

    private String lastErrorMessage;

    public DataManager() {
        this.productFileManager = new ProductFileManager();
        this.bidFileManager = new BidFileManager();
        this.imageFileManager = new ImageFileManager();
        this.lastErrorMessage = "";
    }

    /**
     * 파일 및 디렉터리 구조(data/, images/, products.csv, bids.csv)를 초기화합니다.
     */
    public boolean initializeFiles() {
        File dataFolder = new File("data");
        if (!FileUtil.ensureDirectory(dataFolder)) {
            lastErrorMessage = "data 디렉터리를 생성하지 못했습니다.";
            return false;
        }

        if (!imageFileManager.initializeFolder()) {
            lastErrorMessage = "images 디렉터리를 생성하지 못했습니다.";
            return false;
        }

        if (!productFileManager.initializeFile()) {
            lastErrorMessage = "products.csv 파일을 생성하지 못했습니다.";
            return false;
        }

        if (!bidFileManager.initializeFile()) {
            lastErrorMessage = "bids.csv 파일을 생성하지 못했습니다.";
            return false;
        }

        lastErrorMessage = "";
        return true;
    }

    public boolean loadProducts() {
        boolean result = productFileManager.loadProducts();
        if (!result) {
            lastErrorMessage = "상품 목록을 불러오지 못했습니다.";
        }
        return result;
    }

    public boolean saveProducts() {
        boolean result = productFileManager.saveProducts();
        if (!result) {
            lastErrorMessage = "상품 목록을 저장하지 못했습니다.";
        }
        return result;
    }

    public boolean loadBids() {
        boolean result = bidFileManager.loadBids();
        if (!result) {
            lastErrorMessage = "입찰 목록을 불러오지 못했습니다.";
        }
        return result;
    }

    public boolean saveBids() {
        boolean result = bidFileManager.saveBids();
        if (!result) {
            lastErrorMessage = "입찰 목록을 저장하지 못했습니다.";
        }
        return result;
    }

    public String copyImage(File sourceFile, String productId) {
        return imageFileManager.copyImage(sourceFile, productId);
    }

    public String copyImage(File sourceFile, int productId) {
        return imageFileManager.copyImage(sourceFile, productId);
    }

    public String nextProductId() {
        return productFileManager.nextProductId();
    }

    public int nextBidId() {
        return bidFileManager.nextBidId();
    }

    public Product findProduct(String productId) {
        return productFileManager.findProduct(productId);
    }

    public Product findProduct(int productId) {
        return productFileManager.findProduct(productId);
    }

    public ArrayList<Bid> findBids(String productId) {
        return bidFileManager.findBids(productId);
    }

    public ArrayList<Bid> findBids(int productId) {
        return bidFileManager.findBids(productId);
    }

    public void addProduct(Product product) {
        productFileManager.addProduct(product);
    }

    public void addBid(Bid bid) {
        bidFileManager.addBid(bid);
    }

    public ArrayList<Product> getProducts() {
        return productFileManager.getProducts();
    }

    public ArrayList<Bid> getBids() {
        return bidFileManager.getBids();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
