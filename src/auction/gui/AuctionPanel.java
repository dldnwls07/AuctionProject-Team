package auction.gui;

import auction.model.Bid;
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
import java.util.List;
import java.util.Map;

public class AuctionPanel extends JPanel {

    private final AuctionService auctionService;
    private final User currentUser;

    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;

    private JLabel imageLabel;
    private JTextArea descArea;
    private JLabel valStartPriceBox, valStatusBox;
    private JLabel valSeller, valPrice, valTopBidder, valPeriod, valRemain, valStatus;
    private JTextField bidPriceField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusCombo;

    private final List<Product> displayedProducts = new ArrayList<>();
    private Product selectedProduct;

    // 매 초 Timer가 refresh()를 호출하므로 같은 이미지를 반복 디코딩/스케일링하지 않도록 캐시한다.
    private final Map<String, Icon> thumbnailCache = new HashMap<>();
    private String lastDetailImagePath;

    public AuctionPanel(AuctionService auctionService, User currentUser) {
        this.auctionService = auctionService;
        this.currentUser = currentUser;

        setLayout(new BorderLayout());
        setOpaque(false);

        rightPanel = createRightPanel();
        leftPanel = createLeftPanel();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(leftPanel);

        splitPane.setRightComponent(null);

        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        panel.setMinimumSize(new Dimension(400, 0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("상태 필터:"));

        // 요구사항 1: '전체, 진행 중, 대기 중, 낙찰, 유찰' 5가지 상태 반영
        statusCombo = new JComboBox<>(new String[]{"전체", "진행 중", "대기 중", "낙찰", "유찰"});
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setLightWeightPopupEnabled(false);

        statusCombo.addActionListener(e -> refresh());

        filterPanel.add(statusCombo);

        // 요구사항 2: 새로고침 버튼 기능 구현
        JButton refreshBtn = createFlatButton("새로고침", new Color(240, 240, 240), Color.BLACK);
        refreshBtn.addActionListener(e -> refresh());

        topBar.add(filterPanel, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);

        String[] columns = {"이미지", "상품명", "현재가", "상태"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Icon.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(80);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelectionChanged();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 40));
        header.setBackground(Color.WHITE);
        header.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        panel.setMinimumSize(new Dimension(350, 0));

        imageLabel = new JLabel("상품을 선택해 주세요", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(248, 249, 250));
        imageLabel.setForeground(new Color(150, 150, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        imageLabel.setPreferredSize(new Dimension(0, 250));
        panel.add(imageLabel, BorderLayout.NORTH);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 15));
        detailPanel.setBackground(Color.WHITE);

        JPanel descWrapper = new JPanel(new BorderLayout());
        descWrapper.setBackground(new Color(250, 250, 250));
        descWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 10, 5, 10)
        ));

        descArea = new JTextArea(3, 20);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(250, 250, 250));
        descArea.setBorder(null);
        descWrapper.add(descArea, BorderLayout.CENTER);

        JPanel boxAlignPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        boxAlignPanel.setOpaque(false);

        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        statusBox.setBackground(Color.WHITE);
        statusBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        valStartPriceBox = new JLabel("시작가: -");
        valStartPriceBox.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        valStartPriceBox.setForeground(new Color(100, 100, 100));

        valStatusBox = new JLabel("상태: -");
        valStatusBox.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        valStatusBox.setForeground(new Color(0, 122, 255));

        statusBox.add(valStartPriceBox);
        statusBox.add(new JLabel("|"));
        statusBox.add(valStatusBox);

        boxAlignPanel.add(statusBox);
        descWrapper.add(boxAlignPanel, BorderLayout.SOUTH);

        detailPanel.add(descWrapper, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(6, 2, 10, 15));
        infoGrid.setBackground(Color.WHITE);

        valSeller = new JLabel("-");
        valPrice = new JLabel("-");
        valTopBidder = new JLabel("-");
        valPeriod = new JLabel("-");
        valRemain = new JLabel("-");
        valStatus = new JLabel("-");

        String[] titles = {"판매자", "현재 가격", "최고 입찰자", "경매 기간", "남은 시간", "상태"};
        JLabel[] values = {valSeller, valPrice, valTopBidder, valPeriod, valRemain, valStatus};

        for (int i = 0; i < titles.length; i++) {
            JLabel titleLabel = new JLabel(titles[i]);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            titleLabel.setForeground(new Color(100, 100, 100));
            infoGrid.add(titleLabel);

            values[i].setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            infoGrid.add(values[i]);
        }

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(Color.WHITE);
        gridWrapper.add(infoGrid, BorderLayout.NORTH);

        detailPanel.add(gridWrapper, BorderLayout.CENTER);
        panel.add(detailPanel, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.add(new JLabel("입찰 금액:"));

        JPanel priceInputPanel = new JPanel(new BorderLayout(5, 0));
        priceInputPanel.setBackground(Color.WHITE);

        bidPriceField = new JTextField(10);
        bidPriceField.setPreferredSize(new Dimension(80, 35));
        priceInputPanel.add(bidPriceField, BorderLayout.CENTER);

        JLabel wonLabel = new JLabel("원");
        wonLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        priceInputPanel.add(wonLabel, BorderLayout.EAST);

        bottomBar.add(priceInputPanel);

        JButton historyBtn = createFlatButton("입찰 기록", new Color(240, 240, 240), Color.BLACK);
        historyBtn.addActionListener(e -> {
            if (selectedProduct != null) {
                showBidHistoryModal(selectedProduct);
            }
        });
        bottomBar.add(historyBtn);

        JButton bidBtn = createFlatButton("입찰하기", new Color(0, 122, 255), Color.WHITE);
        bidBtn.addActionListener(e -> placeBid());
        bottomBar.add(bidBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void onRowSelectionChanged() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow != -1 && selectedRow < displayedProducts.size()) {
            if (splitPane.getRightComponent() == null) {
                splitPane.setRightComponent(rightPanel);
                splitPane.setDividerSize(10);
            }
            splitPane.setDividerLocation(splitPane.getWidth() / 2);

            selectedProduct = displayedProducts.get(selectedRow);
            updateDetailPanel(selectedProduct);
        } else {
            selectedProduct = null;
            splitPane.setRightComponent(null);
            splitPane.setDividerSize(0);
        }
    }

    private void updateDetailPanel(Product product) {
        String imagePath = product.getImagePath();
        if (imagePath != null && !imagePath.equals(lastDetailImagePath)) {
            File imgFile = new File(imagePath);
            if (imgFile.isFile()) {
                ImageIcon icon = new ImageIcon(imagePath);
                Image scaled = icon.getImage().getScaledInstance(-1, 250, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText("");
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("이미지를 불러올 수 없습니다");
            }
            lastDetailImagePath = imagePath;
        }

        descArea.setText(product.getDescription());
        valStartPriceBox.setText("시작가: " + formatPrice(product.getStartPrice()) + "원");
        valStatusBox.setText("상태: " + product.getStatus());

        valSeller.setText(product.getSellerName());
        valPrice.setText(formatPrice(product.getCurrentPrice()) + " 원");
        String topBidder = product.getCurrentBidder();
        valTopBidder.setText((topBidder == null || topBidder.trim().isEmpty()) ? "없음" : topBidder);
        valPeriod.setText(product.getAuctionStartTime() + " ~ " + product.getAuctionEndTime());
        valRemain.setText(auctionService.getAuctionTimeMessage(product));
        valStatus.setText(product.getStatus());
    }

    private void placeBid() {
        if (selectedProduct == null) {
            return;
        }

        String bidText = bidPriceField.getText().trim();
        if (bidText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "입찰 금액을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product result = auctionService.placeBid(selectedProduct.getProductId(), currentUser, bidText);

        if (result == null) {
            JOptionPane.showMessageDialog(this, auctionService.getLastErrorMessage(), "입찰 실패", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, bidText + "원으로 입찰이 완료되었습니다.", "입찰 성공", JOptionPane.INFORMATION_MESSAGE);
        bidPriceField.setText("");
        refresh();
    }

    /**
     * CSV를 다시 읽어 상품 목록과 상세 정보(남은 시간 포함)를 최신 상태로 갱신한다.
     * MainFrame의 1초 Timer에서 호출된다.
     */
    public void refresh() {
        String previousSelectedId = (selectedProduct != null) ? selectedProduct.getProductId() : null;

        ArrayList<Product> all = auctionService.loadProducts();
        String filter = (String) statusCombo.getSelectedItem();

        displayedProducts.clear();
        for (Product p : all) {
            if ("전체".equals(filter) || filter.equals(p.getStatus())) {
                displayedProducts.add(p);
            }
        }

        Icon dummyIcon = UIManager.getIcon("FileView.fileIcon");
        tableModel.setRowCount(0);
        for (Product p : displayedProducts) {
            Icon icon = loadThumbnail(p.getImagePath(), dummyIcon);
            tableModel.addRow(new Object[]{icon, p.getTitle(), formatPrice(p.getCurrentPrice()), p.getStatus()});
        }

        selectedProduct = null;
        int restoreRow = -1;
        if (previousSelectedId != null) {
            for (int i = 0; i < displayedProducts.size(); i++) {
                if (previousSelectedId.equals(displayedProducts.get(i).getProductId())) {
                    restoreRow = i;
                    break;
                }
            }
        }

        if (restoreRow >= 0) {
            table.setRowSelectionInterval(restoreRow, restoreRow);
            selectedProduct = displayedProducts.get(restoreRow);
            updateDetailPanel(selectedProduct);
        } else {
            splitPane.setRightComponent(null);
            splitPane.setDividerSize(0);
        }
    }

    /**
     * 상품 등록 성공 직후, 등록된 상품을 목록에서 자동으로 찾아 선택한다.
     * MainFrame.showAuctionProduct()에서 호출된다.
     */
    public void selectProduct(String productId) {
        if (!"전체".equals(statusCombo.getSelectedItem())) {
            statusCombo.setSelectedItem("전체"); // 액션 리스너가 refresh()를 호출한다.
        } else {
            refresh();
        }

        for (int i = 0; i < displayedProducts.size(); i++) {
            if (productId.equals(displayedProducts.get(i).getProductId())) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                break;
            }
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
        Image scaled = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        Icon result = new ImageIcon(scaled);
        thumbnailCache.put(imagePath, result);
        return result;
    }

    private String formatPrice(int price) {
        return String.format("%,d", price);
    }

    private void showBidHistoryModal(Product product) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parentWindow, "입찰 기록 - " + product.getTitle(), true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        String[] columns = {"입찰자", "입찰 금액", "입찰 시간"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(model);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(248, 249, 250));
        historyTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        historyTable.setGridColor(new Color(240, 240, 240));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 3; i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ArrayList<Bid> bids = auctionService.loadBids(product.getProductId());
        for (int i = bids.size() - 1; i >= 0; i--) {
            Bid bid = bids.get(i);
            model.addRow(new Object[]{bid.getBidderName(), formatPrice(bid.getBidPrice()), bid.getBidTime()});
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(new EmptyBorder(15, 15, 10, 15));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JButton closeBtn = createFlatButton("닫기", new Color(240, 240, 240), Color.BLACK);
        closeBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(closeBtn);

        dialog.add(tableWrapper, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(90, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
