package auction.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class AuctionPanel extends JPanel {

    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;

    private JLabel imageLabel;
    private JTextArea descArea;
    private JLabel valStartPriceBox, valStatusBox;
    private JLabel valSeller, valPrice, valTopBidder, valPeriod, valRemain, valStatus;
    private DefaultTableModel tableModel;

    public AuctionPanel() {
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
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"전체", "진행 중", "대기 중", "낙찰", "유찰"});
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setLightWeightPopupEnabled(false);
        
        statusCombo.addActionListener(e -> {
            String selected = (String) statusCombo.getSelectedItem();
            // 선택된 상태에 따른 필터링 로직 구현부
        });
        
        filterPanel.add(statusCombo);

        // 요구사항 2: 새로고침 버튼 기능 구현
        JButton refreshBtn = createFlatButton("새로고침", new Color(240, 240, 240), Color.BLACK);
        refreshBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "목록이 최신 데이터로 새로고침 되었습니다.", "새로고침", JOptionPane.INFORMATION_MESSAGE);
            // 여기에 실제 데이터 로드 및 갱신 로직 추가 가능
        });
        
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
        
        JTable table = new JTable(tableModel);
        table.setRowHeight(80); 
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                
                if (selectedRow != -1) { 
                    if (splitPane.getRightComponent() == null) {
                        splitPane.setRightComponent(rightPanel);
                        splitPane.setDividerSize(10); 
                    }
                    
                    splitPane.setDividerLocation(splitPane.getWidth() / 2);
                    
                    String itemName = (String) tableModel.getValueAt(selectedRow, 1);
                    String price = (String) tableModel.getValueAt(selectedRow, 2);
                    String state = (String) tableModel.getValueAt(selectedRow, 3);
                    
                    imageLabel.setText(itemName + " (이미지 표시 영역)");
                    descArea.setText(itemName + "에 대한 상세 설명입니다."); 
                    valStartPriceBox.setText("시작가: 10,000원"); 
                    valStatusBox.setText("상태: " + state);
                    
                    valSeller.setText("지소은");
                    valPrice.setText(price + " 원");
                    valTopBidder.setText("미정"); 
                    valPeriod.setText("2026-07-22 ~ 2026-07-29"); 
                    valRemain.setText("7일 0시간 남음"); 
                    valStatus.setText(state);
                    
                } else { 
                    splitPane.setRightComponent(null);
                    splitPane.setDividerSize(0);
                }
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

        Icon dummyIcon = UIManager.getIcon("FileView.fileIcon"); 
        tableModel.addRow(new Object[]{dummyIcon, "전공 서적 (Java)", "15,000", "진행 중"});
        tableModel.addRow(new Object[]{dummyIcon, "아이패드 프로", "500,000", "대기 중"});

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
        
        JTextField bidPriceField = new JTextField(10);
        bidPriceField.setPreferredSize(new Dimension(80, 35));
        priceInputPanel.add(bidPriceField, BorderLayout.CENTER);
        
        JLabel wonLabel = new JLabel("원");
        wonLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        priceInputPanel.add(wonLabel, BorderLayout.EAST);
        
        bottomBar.add(priceInputPanel);
        
        JButton historyBtn = createFlatButton("입찰 기록", new Color(240, 240, 240), Color.BLACK);
        historyBtn.addActionListener(e -> showBidHistoryModal());
        bottomBar.add(historyBtn);
        
        JButton bidBtn = createFlatButton("입찰하기", new Color(0, 122, 255), Color.WHITE);
        bidBtn.addActionListener(e -> {
            String bidText = bidPriceField.getText().trim();
            if(bidText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "입찰 금액을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, bidText + "원으로 입찰이 완료되었습니다.", "입찰 성공", JOptionPane.INFORMATION_MESSAGE);
                bidPriceField.setText("");
            }
        });
        bottomBar.add(bidBtn);
        
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void showBidHistoryModal() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parentWindow, "입찰 기록", true);
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
        
        model.addRow(new Object[]{"김철수", "16,000", "2026-07-22 14:30:12"});
        model.addRow(new Object[]{"이영희", "15,500", "2026-07-22 14:25:00"});
        model.addRow(new Object[]{"박지성", "15,000", "2026-07-22 14:20:05"});
        
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