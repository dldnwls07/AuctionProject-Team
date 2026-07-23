package auction.service;

/**
 * [myunghun 담당] 경매 검증 규칙, 동시성 Lock 제어, 1초 마감 판정 서비스 클래스
 */

import auction.file.DataManager;
import auction.model.Bid;
import auction.model.MyBidSummary;
import auction.model.Product;
import auction.model.Seller;
import auction.model.User;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 상품 등록, 입찰 검증, 경매 상태 변경을 담당하는 서비스 클래스다.
 * 잠금 파일 제어는 LockManager, 입력값 파싱은 InputParser에 위임한다.
 */
public class AuctionService {

    private final DataManager dataManager;
    private final LockManager lockManager;
    private String lastErrorMessage;

    /**
     * 데이터 관리자와 잠금 관리자를 준비한다.
     */
    public AuctionService() {
        dataManager = new DataManager();
        lockManager = new LockManager(new File("data/auction.lock"));
        lastErrorMessage = "";
    }

    /**
     * 이전 실행이 비정상 종료되어 auction.lock이 남아있는지 확인한다.
     * MainFrame이 시작 시 호출해서 사용자에게 삭제 여부를 물어볼 수 있다.
     */
    public boolean hasStaleLock() {
        return lockManager.exists();
    }

    /**
     * 남아있는 auction.lock 파일을 강제로 삭제한다.
     */
    public void clearLock() {
        lockManager.release();
    }

    /**
     * data, images 폴더와 CSV 파일을 준비한다.
     */
    public boolean initializeFiles() {
        boolean success = dataManager.initializeFiles();

        if (!success) {
            copyDataError();
            return false;
        }

        lastErrorMessage = "";
        return true;
    }

    /**
     * 상품을 등록한다.
     */
    public Product registerProduct(
            User currentUser,
            String title,
            String description,
            String priceText,
            String auctionStartTime,
            String auctionEndTime,
            File imageFile) {

        if (currentUser == null) {
            lastErrorMessage =
                    "현재 사용자 정보가 없습니다.";
            return null;
        }

        String cleanTitle = InputParser.cleanText(title);
        String cleanDescription = InputParser.cleanText(description);

        int startPrice = InputParser.parsePrice(priceText);

        LocalDateTime startTime =
                InputParser.parseDateTime(auctionStartTime);

        LocalDateTime endTime =
                InputParser.parseDateTime(auctionEndTime);

        if (cleanTitle.isEmpty()) {
            lastErrorMessage =
                    "상품명을 입력해 주세요.";
            return null;
        }

        if (cleanDescription.isEmpty()) {
            lastErrorMessage =
                    "상품 설명을 입력해 주세요.";
            return null;
        }

        if (startPrice <= 0) {
            lastErrorMessage =
                    "시작 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        if (imageFile == null || !imageFile.isFile()) {
            lastErrorMessage =
                    "상품 이미지를 선택해 주세요.";
            return null;
        }

        if (startTime == null || endTime == null) {
            lastErrorMessage =
                    "경매 시작 시간과 종료 시간을 확인해 주세요.";
            return null;
        }

        if (!endTime.isAfter(startTime)) {
            lastErrorMessage =
                    "경매 종료 시간은 시작 시간보다 늦어야 합니다.";
            return null;
        }

        if (!endTime.isAfter(LocalDateTime.now())) {
            lastErrorMessage =
                    "경매 종료 시간은 현재 시간보다 늦어야 합니다.";
            return null;
        }

        if (!lockManager.acquire()) {
            lastErrorMessage = lockManager.getLastErrorMessage();
            return null;
        }

        try {
            /*
             * 잠금을 얻은 다음 최신 상품 정보를 다시 읽는다.
             */
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            /*
             * DataManager.nextProductId()는 String을 반환한다.
             */
            String productId =
                    dataManager.nextProductId();

            String imagePath =
                    dataManager.copyImage(
                            imageFile,
                            productId
                    );

            if (imagePath == null || imagePath.isEmpty()) {
                copyDataError();
                return null;
            }

            /*
             * 현재 시간이 시작 시간보다 이르면 대기 중,
             * 그렇지 않으면 진행 중이다.
             */
            String status = Product.STATUS_OPEN;

            if (startTime.isAfter(LocalDateTime.now())) {
                status = Product.STATUS_WAITING;
            }

            /*
             * 현재 사용자를 판매자 역할로 사용한다.
             */
            User seller = new Seller(
                    currentUser.getUserId(),
                    currentUser.getUserName()
            );

            Product product = new Product(
                    productId,
                    seller.getUserName(),
                    cleanTitle,
                    cleanDescription,
                    startPrice,
                    startPrice,
                    "",
                    imagePath,
                    startTime.format(InputParser.TIME_FORMAT),
                    endTime.format(InputParser.TIME_FORMAT),
                    "",
                    status
            );

            dataManager.addProduct(product);

            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            lastErrorMessage = "";
            return product;

        } finally {
            lockManager.release();
        }
    }

    /**
     * int 상품 번호를 사용해 입찰한다.
     */
    public Product placeBid(
            int productId,
            User currentUser,
            String priceText) {

        if (currentUser == null) {
            lastErrorMessage =
                    "현재 사용자 정보가 없습니다.";
            return null;
        }

        if (productId < 1) {
            lastErrorMessage =
                    "상품 번호가 올바르지 않습니다.";
            return null;
        }

        int bidPrice = InputParser.parsePrice(priceText);

        if (bidPrice <= 0) {
            lastErrorMessage =
                    "입찰 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        if (!lockManager.acquire()) {
            lastErrorMessage = lockManager.getLastErrorMessage();
            return null;
        }

        try {
            /*
             * 다른 사용자의 최신 입찰을 확인하기 위해
             * 잠금을 얻은 후 CSV를 다시 읽는다.
             */
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            if (!dataManager.loadBids()) {
                copyDataError();
                return null;
            }

            Product product =
                    dataManager.findProduct(productId);

            if (!checkBid(
                    product,
                    currentUser,
                    bidPrice)) {

                return null;
            }

            String now = InputParser.getCurrentTime();

            /*
             * Product의 현재 최고 입찰 정보를 변경한다.
             */
            product.setCurrentPrice(bidPrice);
            product.setCurrentBidder(
                    currentUser.getUserName()
            );
            product.setLastBidTime(now);
            product.setStatus(Product.STATUS_OPEN);

            /*
             * Bid의 productId는 String 자료형이므로 변환해서 전달한다.
             */
            Bid bid = new Bid(
                    dataManager.nextBidId(),
                    String.valueOf(productId),
                    currentUser.getUserName(),
                    bidPrice,
                    now
            );

            dataManager.addBid(bid);

            /*
             * 변경된 최고 가격을 products.csv에 저장한다.
             */
            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            /*
             * 입찰 기록을 bids.csv에 저장한다.
             */
            if (!dataManager.saveBids()) {
                copyDataError();
                return null;
            }

            lastErrorMessage = "";
            return product;

        } finally {
            lockManager.release();
        }
    }

    /**
     * String 상품 번호를 사용하는 입찰 메서드다.
     *
     * Product의 productId가 String으로 바뀌었기 때문에
     * GUI에서 문자열 상품 번호를 전달할 때 사용할 수 있다.
     */
    public Product placeBid(
            String productId,
            User currentUser,
            String priceText) {

        int numericProductId =
                InputParser.parseProductId(productId);

        if (numericProductId < 1) {
            lastErrorMessage =
                    "상품 번호가 올바르지 않습니다.";
            return null;
        }

        return placeBid(
                numericProductId,
                currentUser,
                priceText
        );
    }

    /**
     * 입찰할 수 있는 상품인지 검사한다.
     */
    private boolean checkBid(
            Product product,
            User currentUser,
            int bidPrice) {

        if (product == null) {
            lastErrorMessage =
                    "상품을 찾을 수 없습니다.";
            return false;
        }

        LocalDateTime startTime =
                InputParser.parseDateTime(
                        product.getAuctionStartTime()
                );

        LocalDateTime endTime =
                InputParser.parseDateTime(
                        product.getAuctionEndTime()
                );

        LocalDateTime now =
                LocalDateTime.now();

        if (startTime == null || endTime == null) {
            lastErrorMessage =
                    "상품의 경매 시간 정보가 올바르지 않습니다.";
            return false;
        }

        if (now.isBefore(startTime)) {
            lastErrorMessage =
                    "아직 경매 시작 전입니다.";
            return false;
        }

        if (!now.isBefore(endTime)) {
            finishProduct(product);

            if (!dataManager.saveProducts()) {
                copyDataError();
                return false;
            }

            lastErrorMessage =
                    "경매가 종료되었습니다.";
            return false;
        }

        if (!Product.STATUS_OPEN.equals(
                product.getStatus())
                && !Product.STATUS_WAITING.equals(
                        product.getStatus())) {

            lastErrorMessage =
                    "이미 종료된 경매입니다.";
            return false;
        }

        if (product.getSellerName() != null
                && product.getSellerName().equals(
                        currentUser.getUserName())) {

            lastErrorMessage =
                    "판매자는 자신의 상품에 입찰할 수 없습니다.";
            return false;
        }

        if (bidPrice <= product.getCurrentPrice()) {
            lastErrorMessage =
                    "현재 최고가 "
                            + String.format(
                                    "%,d원",
                                    product.getCurrentPrice()
                            )
                            + "보다 높은 가격을 입력해 주세요.";

            return false;
        }

        return true;
    }

    /**
     * 모든 상품을 읽어 반환한다.
     */
    public ArrayList<Product> loadProducts() {
        if (!dataManager.loadProducts()) {
            copyDataError();
            return new ArrayList<Product>();
        }

        lastErrorMessage = "";
        return dataManager.getProducts();
    }

    /**
     * int 상품 번호로 상품 한 개를 찾는다.
     */
    public Product loadProduct(int productId) {
        if (!dataManager.loadProducts()) {
            copyDataError();
            return null;
        }

        Product product =
                dataManager.findProduct(productId);

        if (product == null) {
            lastErrorMessage =
                    "상품을 찾을 수 없습니다.";
            return null;
        }

        lastErrorMessage = "";
        return product;
    }

    /**
     * String 상품 번호로 상품 한 개를 찾는다.
     */
    public Product loadProduct(String productId) {
        int numericProductId =
                InputParser.parseProductId(productId);

        if (numericProductId < 1) {
            lastErrorMessage =
                    "상품 번호가 올바르지 않습니다.";
            return null;
        }

        return loadProduct(numericProductId);
    }

    /**
     * int 상품 번호에 해당하는 입찰 기록을 반환한다.
     */
    public ArrayList<Bid> loadBids(int productId) {
        if (!dataManager.loadBids()) {
            copyDataError();
            return new ArrayList<Bid>();
        }

        lastErrorMessage = "";
        return dataManager.findBids(productId);
    }

    /**
     * String 상품 번호에 해당하는 입찰 기록을 반환한다.
     */
    public ArrayList<Bid> loadBids(String productId) {
        int numericProductId =
                InputParser.parseProductId(productId);

        if (numericProductId < 1) {
            lastErrorMessage =
                    "상품 번호가 올바르지 않습니다.";
            return new ArrayList<Bid>();
        }

        return loadBids(numericProductId);
    }

    /**
     * 시작 또는 종료 시간이 된 상품의 상태를 변경한다.
     *
     * MainFrame의 1초 Timer에서 호출할 수 있다.
     */
    public ArrayList<Product> updateAuctionStatuses() {
        ArrayList<Product> endedProducts =
                new ArrayList<Product>();

        if (!dataManager.loadProducts()) {
            copyDataError();
            return endedProducts;
        }

        boolean changeNeeded = false;

        for (Product product :
                dataManager.getProducts()) {

            String newStatus =
                    calculateStatus(product);

            if (!product.getStatus().equals(newStatus)) {
                changeNeeded = true;
            }
        }

        /*
         * 변경할 상태가 없으면 CSV를 다시 저장하지 않는다.
         */
        if (!changeNeeded) {
            lastErrorMessage = "";
            return endedProducts;
        }

        /*
         * Timer는 1초마다 실행되므로 잠금을 얻지 못해도
         * 별도의 오류 메시지를 만들지 않고 다음 실행을 기다린다.
         */
        if (!lockManager.acquireQuietly()) {
            return endedProducts;
        }

        try {
            /*
             * 잠금을 얻은 뒤 최신 상품 정보를 다시 읽는다.
             */
            if (!dataManager.loadProducts()) {
                copyDataError();
                return endedProducts;
            }

            for (Product product :
                    dataManager.getProducts()) {

                String oldStatus =
                        product.getStatus();

                String newStatus =
                        calculateStatus(product);

                product.setStatus(newStatus);

                /*
                 * 이번 검사에서 새롭게 낙찰이나 유찰로
                 * 변경된 상품만 결과 목록에 넣는다.
                 */
                if (!oldStatus.equals(newStatus)
                        && (Product.STATUS_SOLD.equals(
                                newStatus)
                        || Product.STATUS_NO_BID.equals(
                                newStatus))) {

                    endedProducts.add(product);
                }
            }

            if (!dataManager.saveProducts()) {
                copyDataError();
                endedProducts.clear();
                return endedProducts;
            }

            lastErrorMessage = "";
            return endedProducts;

        } finally {
            lockManager.release();
        }
    }

    /**
     * 현재 시간을 기준으로 상품 상태를 계산한다.
     */
    private String calculateStatus(Product product) {
        if (product == null) {
            return "";
        }

        /*
         * 이미 낙찰 또는 유찰된 상품의 상태는
         * 다시 변경하지 않는다.
         */
        if (Product.STATUS_SOLD.equals(
                product.getStatus())
                || Product.STATUS_NO_BID.equals(
                        product.getStatus())) {

            return product.getStatus();
        }

        LocalDateTime startTime =
                InputParser.parseDateTime(
                        product.getAuctionStartTime()
                );

        LocalDateTime endTime =
                InputParser.parseDateTime(
                        product.getAuctionEndTime()
                );

        if (startTime == null || endTime == null) {
            return product.getStatus();
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (now.isBefore(startTime)) {
            return Product.STATUS_WAITING;
        }

        if (now.isBefore(endTime)) {
            return Product.STATUS_OPEN;
        }

        String currentBidder =
                product.getCurrentBidder();

        if (currentBidder == null
                || currentBidder.trim().isEmpty()) {

            return Product.STATUS_NO_BID;
        }

        return Product.STATUS_SOLD;
    }

    /**
     * 경매가 끝난 상품을 낙찰 또는 유찰로 변경한다.
     */
    private void finishProduct(Product product) {
        String currentBidder =
                product.getCurrentBidder();

        if (currentBidder == null
                || currentBidder.trim().isEmpty()) {

            product.setStatus(
                    Product.STATUS_NO_BID
            );

        } else {
            product.setStatus(
                    Product.STATUS_SOLD
            );
        }
    }

    /**
     * 경매 시작 또는 종료까지 남은 시간을 반환한다.
     */
    public String getAuctionTimeMessage(
            Product product) {

        if (product == null) {
            return "-";
        }

        if (Product.STATUS_SOLD.equals(
                product.getStatus())
                || Product.STATUS_NO_BID.equals(
                        product.getStatus())) {

            return "경매 종료";
        }

        LocalDateTime targetTime;

        String prefix;

        if (Product.STATUS_WAITING.equals(
                product.getStatus())) {

            targetTime =
                    InputParser.parseDateTime(
                            product.getAuctionStartTime()
                    );

            prefix = "시작까지 ";

        } else {
            targetTime =
                    InputParser.parseDateTime(
                            product.getAuctionEndTime()
                    );

            prefix = "종료까지 ";
        }

        if (targetTime == null) {
            return "시간 정보 오류";
        }

        long totalSeconds = Duration.between(
                LocalDateTime.now(),
                targetTime
        ).getSeconds();

        if (totalSeconds < 0) {
            totalSeconds = 0;
        }

        long days =
                totalSeconds / 86400;

        long hours =
                (totalSeconds % 86400) / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

        return prefix
                + days + "일 "
                + hours + "시간 "
                + minutes + "분 "
                + seconds + "초";
    }

    /**
     * DataManager의 오류 메시지를 복사한다.
     */
    private void copyDataError() {
        lastErrorMessage =
                dataManager.getLastErrorMessage();
    }

    /**
     * 특정 사용자가 등록한 상품 목록을 반환한다.
     */
    public ArrayList<Product> getMyRegisteredProducts(User currentUser) {
        ArrayList<Product> result = new ArrayList<>();
        if (currentUser == null || currentUser.getUserName() == null) {
            return result;
        }

        ArrayList<Product> allProducts = dataManager.getProducts();
        for (Product p : allProducts) {
            if (currentUser.getUserName().equals(p.getSellerName())) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * 특정 사용자가 입찰에 참여한 상품 목록과 해당 사용자의 입찰 상태를 반환한다.
     */
    public ArrayList<MyBidSummary> getMyBidSummaryList(User currentUser) {
        ArrayList<MyBidSummary> result = new ArrayList<>();
        if (currentUser == null || currentUser.getUserName() == null) {
            return result;
        }

        String userName = currentUser.getUserName();
        ArrayList<Bid> allBids = dataManager.getBids();

        // 1. 해당 사용자가 입찰한 상품 ID와 내 최고 입찰액 수집
        java.util.Map<String, Integer> myHighestBids = new java.util.LinkedHashMap<>();
        for (Bid bid : allBids) {
            if (userName.equals(bid.getBidderName())) {
                String pId = bid.getProductId();
                int price = bid.getBidPrice();
                if (!myHighestBids.containsKey(pId) || price > myHighestBids.get(pId)) {
                    myHighestBids.put(pId, price);
                }
            }
        }

        // 2. 각 상품별 상태 및 내 상태 판정
        for (java.util.Map.Entry<String, Integer> entry : myHighestBids.entrySet()) {
            String productId = entry.getKey();
            int myMaxBid = entry.getValue();

            Product product = dataManager.findProduct(productId);
            if (product == null) {
                continue;
            }

            String myStatus;
            String productStatus = product.getStatus();

            if (Product.STATUS_SOLD.equals(productStatus)) {
                if (userName.equals(product.getCurrentBidder())) {
                    myStatus = "🎉 최종 낙찰";
                } else {
                    myStatus = "❌ 낙찰 실패";
                }
            } else if (Product.STATUS_NO_BID.equals(productStatus)) {
                myStatus = "⚪ 유찰";
            } else if (Product.STATUS_OPEN.equals(productStatus)) {
                if (userName.equals(product.getCurrentBidder())) {
                    myStatus = "🔥 내가 1등 유지중";
                } else {
                    myStatus = "⚡ 최고가 갱신당함";
                }
            } else { // STATUS_WAITING
                myStatus = "대기 중";
            }

            result.add(new MyBidSummary(product, myMaxBid, myStatus));
        }

        return result;
    }

    /**
     * 마지막 오류 메시지를 반환한다.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
