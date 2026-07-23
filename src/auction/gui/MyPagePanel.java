package auction.gui;

import auction.model.MyBidSummary;
import auction.model.Product;
import auction.model.User;
import auction.service.AuctionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPagePanel extends JPanel {

    private final AuctionService auctionService;
    private final User currentUser;

    private JLabel lblUserName;
    private JLabel lblRegCount;
    private JLabel lblBidCount;

    private JTable regTable;
    private DefaultTableModel regTableModel;
    private JTable bidTable;
    private DefaultTableModel bidTableModel;

    private final Map<String, Icon> thumbnailCache = new HashMap<>();

    public MyPagePanel(AuctionService auctionService, User currentUser) {
        this.auctionService = auctionService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(0, 15));
        setOpaque(false);

        add(createProfileHeader(), BorderLayout.NORTH);
        add(createTablesPanel(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createProfileHeader() {
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(new Color(30, 35, 60)); // 짙은 네이비 톤 헤더
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230)),
                new EmptyBorder(20, 25, 20, 25)));

        // 좌측: 프로필 정보
        JPanel leftInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftInfo.setOpaque(false);

        JLabel avatarLabel = new JLabel("🎓");
        avatarLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 36));

        JPanel nameBox = new JPanel();
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.Y_AXIS));
        nameBox.setOpaque(false);

        lblUserName = new JLabel(currentUser.getUserName() + " 님의 마이페이지");
        lblUserName.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        lblUserName.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("학번/ID: " + currentUser.getUserId());
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        subLabel.setForeground(new Color(200, 205, 220));

        nameBox.add(lblUserName);
        nameBox.add(Box.createRigidArea(new Dimension(0, 4)));
        nameBox.add(subLabel);

        leftInfo.add(avatarLabel);
        leftInfo.add(nameBox);

        // 우측: 통계 요약 박스
        JPanel rightStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightStats.setOpaque(false);

        lblRegCount = createStatBadge("총 등록 상품", "0개");
        lblBidCount = createStatBadge("참여 입찰 건수", "0개");

        rightStats.add(lblRegCount.getParent());
        rightStats.add(lblBidCount.getParent());

        headerCard.add(leftInfo, BorderLayout.WEST);
        headerCard.add(rightStats, BorderLayout.EAST);

        return headerCard;
    }

    private JLabel createStatBadge(String title, String initialValue) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(255, 255, 255, 30)); // 반투명 흰색
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
                new EmptyBorder(8, 15, 8, 15)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(220, 225, 240));

        JLabel valLbl = new JLabel(initialValue);
        valLbl.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        valLbl.setForeground(Color.WHITE);

        box.add(titleLbl);
        box.add(Box.createRigidArea(new Dimension(0, 3)));
        box.add(valLbl);

        return valLbl;
    }

    private JPanel createTablesPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        mainPanel.setOpaque(false);

        // 1. 내가 등록한 판매 상품
        mainPanel.add(createSectionPanel("🎁 내가 등록한 판매 상품", createRegisteredTable()));

        // 2. 나의 입찰 참여 내역 및 결과
        mainPanel.add(createSectionPanel("📜 나의 입찰 참여 내역 및 결과", createBidTable()));

        return mainPanel;
    }

    private JPanel createSectionPanel(String sectionTitle, JComponent content) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(12, 15, 12, 15)));

        JLabel titleLbl = new JLabel(sectionTitle);
        titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titleLbl.setForeground(new Color(40, 40, 40));

        section.add(titleLbl, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);

        return section;
    }

    private JScrollPane createRegisteredTable() {
        String[] columns = { "이미지", "상품명", "시작가", "현재가", "최고 입찰자", "경매 상태", "마감 시간" };

        regTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Icon.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        regTable = new JTable(regTableModel);
        setupTableStyle(regTable);

        JScrollPane scrollPane = new JScrollPane(regTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JScrollPane createBidTable() {
        String[] columns = { "이미지", "상품명", "내 최고 입찰가", "현재 최고가", "나의 상태", "마감 시간" };

        bidTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Icon.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bidTable = new JTable(bidTableModel);
        setupTableStyle(bidTable);

        JScrollPane scrollPane = new JScrollPane(bidTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private void setupTableStyle(JTable table) {
        table.setRowHeight(60);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 35));
        header.setBackground(new Color(248, 249, 250));
        header.setFont(new Font("맑은 고딕", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    /**
     * 마이페이지 목록 및 통계 데이터를 실시간 갱신한다.
     */
    public void refresh() {
        Icon dummyIcon = UIManager.getIcon("FileView.fileIcon");

        // 1. 내가 등록한 판매 상품 갱신
        ArrayList<Product> regProducts = auctionService.getMyRegisteredProducts(currentUser);
        lblRegCount.setText(regProducts.size() + "개");

        regTableModel.setRowCount(0);
        for (Product p : regProducts) {
            Icon icon = loadThumbnail(p.getImagePath(), dummyIcon);
            String topBidder = (p.getCurrentBidder() == null || p.getCurrentBidder().isEmpty()) ? "없음"
                    : p.getCurrentBidder();

            regTableModel.addRow(new Object[] {
                    icon,
                    p.getTitle(),
                    formatPrice(p.getStartPrice()) + " 원",
                    formatPrice(p.getCurrentPrice()) + " 원",
                    topBidder,
                    p.getStatus(),
                    p.getAuctionEndTime()
            });
        }

        // 2. 나의 입찰 참여 내역 및 결과 갱신
        ArrayList<MyBidSummary> bidSummaries = auctionService.getMyBidSummaryList(currentUser);
        lblBidCount.setText(bidSummaries.size() + "개");

        bidTableModel.setRowCount(0);
        for (MyBidSummary summary : bidSummaries) {
            Product p = summary.getProduct();
            Icon icon = loadThumbnail(p.getImagePath(), dummyIcon);

            bidTableModel.addRow(new Object[] {
                    icon,
                    p.getTitle(),
                    formatPrice(summary.getMyHighestBid()) + " 원",
                    formatPrice(p.getCurrentPrice()) + " 원",
                    summary.getMyStatus(),
                    p.getAuctionEndTime()
            });
        }
    }

    private Icon loadThumbnail(String imagePath, Icon fallback) {
        if (imagePath == null || imagePath.isEmpty()) {
            return fallback;
        }

        Icon cached = thumbnailCache.get(imagePath);
        if (cached != null) {
            return cached;
        }

        File file = new File(imagePath);
        if (!file.isFile()) {
            return fallback;
        }

        ImageIcon icon = new ImageIcon(imagePath);
        Image scaled = icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
        Icon result = new ImageIcon(scaled);
        thumbnailCache.put(imagePath, result);
        return result;
    }

    private String formatPrice(int price) {
        return String.format("%,d", price);
    }
}
