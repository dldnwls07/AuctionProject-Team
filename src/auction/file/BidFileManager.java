package auction.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import auction.model.Bid;
import auction.model.bidFromCsvString;
import auction.model.bidToCsvString;

/**
 * bids.csv 파일 I/O 및 메모리 내 Bid 데이터 목록을 전담 관리하는 클래스
 */
public class BidFileManager {

    public static final String BID_HEADER = "bidId,productId,bidderName,bidPrice,bidTime";

    private final File bidFile;
    private final ArrayList<Bid> bids;

    public BidFileManager() {
        this.bidFile = new File("data/bids.csv");
        this.bids = new ArrayList<>();
    }

    public BidFileManager(File bidFile) {
        this.bidFile = bidFile;
        this.bids = new ArrayList<>();
    }

    public boolean initializeFile() {
        return FileUtil.createCsvFile(bidFile, BID_HEADER);
    }

    /**
     * CSV 파일에서 입찰 목록을 읽어와 메모리 리스트에 저장합니다.
     */
    public boolean loadBids() {
        bids.clear();
        if (!bidFile.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(bidFile, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // 헤더 건너뛰기
            while ((line = reader.readLine()) != null) {
                Bid bid = bidFromCsvString.parse(line);
                if (bid != null) {
                    bids.add(bid);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 메모리의 입찰 목록을 CSV 파일에 저장합니다.
     */
    public boolean saveBids() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(bidFile, StandardCharsets.UTF_8))) {
            writer.write(BID_HEADER);
            writer.newLine();

            for (Bid bid : bids) {
                String csvLine = bidToCsvString.convert(bid);
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
     * 다음 입찰 ID(int)를 생성합니다 (기존 최대 bidId + 1).
     */
    public int nextBidId() {
        int maxId = 0;
        for (Bid b : bids) {
            if (b.getBidId() > maxId) {
                maxId = b.getBidId();
            }
        }
        return maxId + 1;
    }

    /**
     * 특정 상품 ID(String)에 해당하는 모든 입찰 기록 목록을 반환합니다.
     */
    public ArrayList<Bid> findBids(String productId) {
        ArrayList<Bid> result = new ArrayList<>();
        if (productId == null) {
            return result;
        }
        for (Bid b : bids) {
            if (productId.equals(b.getProductId())) {
                result.add(b);
            }
        }
        return result;
    }

    /**
     * 특정 상품 ID(int)에 해당하는 모든 입찰 기록 목록을 반환합니다 (오버로딩).
     */
    public ArrayList<Bid> findBids(int productId) {
        return findBids(String.valueOf(productId));
    }

    /**
     * 새로운 입찰 기록을 추가하고 CSV 파일에 저장합니다.
     */
    public void addBid(Bid bid) {
        if (bid != null) {
            bids.add(bid);
            saveBids();
        }
    }

    /**
     * 전체 입찰 목록을 반환합니다.
     */
    public ArrayList<Bid> getBids() {
        return bids;
    }
}
