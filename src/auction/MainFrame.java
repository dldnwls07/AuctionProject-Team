package auction;

import auction.gui.AuctionPanel;
import auction.gui.MyPagePanel;
import auction.gui.ProductRegisterPanel;
import auction.model.Bidder;
import auction.model.Product;
import auction.model.User;
import auction.service.AuctionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel; // 사이드바 패널 변수

    private final AuctionService auctionService;
    private final User currentUser;
    private AuctionPanel auctionPanel;
    private MyPagePanel myPagePanel;
    private JLabel userLabel;

    public MainFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        auctionService = new AuctionService();

        if (!auctionService.initializeFiles()) {
            JOptionPane.showMessageDialog(null,
                    auctionService.getLastErrorMessage(),
                    "초기화 실패", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        checkStaleLock();

        currentUser = createCurrentUser();

        setTitle("한국공학대학교 교내 경매");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 249, 250));

        initHeader();
        initSidebar(); // 사이드바 초기화 메서드 호출
        initMainContent();
        initStatusBar();
        startAutoRefreshTimer();
    }

    /**
     * 이전 실행이 비정상 종료되어 auction.lock이 남아있으면 삭제 여부를 물어본다.
     */
    private void checkStaleLock() {
        if (!auctionService.hasStaleLock()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
                "이전 실행에서 남은 잠금 파일(auction.lock)이 있습니다. 삭제하시겠습니까?",
                "잠금 파일 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            auctionService.clearLock();
        }
    }

    /**
     * 실행 시 사용자 이름을 입력받아 Bidder 객체를 생성한다.
     * 공백이면 다시 입력받고, 취소하면 프로그램을 종료한다.
     * 여러 프로세스를 서로 다른 이름으로 띄우면 동시 입찰 잠금(auction.lock) 동작을 테스트할 수 있다.
     */
    private User createCurrentUser() {
        while (true) {
            String inputName = JOptionPane.showInputDialog(
                    null, "사용자 이름을 입력해 주세요.", "로그인", JOptionPane.PLAIN_MESSAGE);

            if (inputName == null) {
                System.exit(0);
                return null; // System.exit() 이후에는 도달하지 않는다.
            }

            String userName = inputName.trim();
            if (!userName.isEmpty()) {
                return new Bidder(Math.abs(userName.hashCode()) % 100000, userName);
            }
        }
    }

    /**
     * 1초마다 경매 상태(대기/진행/낙찰/유찰)를 갱신하고 화면을 새로고침한다.
     */
    private void startAutoRefreshTimer() {
        Timer timer = new Timer(1000, e -> {
            ArrayList<Product> ended = auctionService.updateAuctionStatuses();
            if (auctionPanel != null) {
                auctionPanel.refresh();
            }
            if (myPagePanel != null) {
                myPagePanel.refresh();
            }
            if (!ended.isEmpty()) {
                showAuctionEndedNotice(ended);
            }
        });
        timer.start();
    }

    /**
     * 종료된 경매를 한 번의 알림으로 묶어 보여준다.
     * 상품마다 다이얼로그를 띄우면 여러 건이 동시에 끝났을 때 그 수만큼 확인을 눌러야 한다.
     */
    private void showAuctionEndedNotice(ArrayList<Product> ended) {
        StringBuilder message = new StringBuilder();
        for (Product product : ended) {
            if (message.length() > 0) {
                message.append(System.lineSeparator());
            }
            message.append(Product.STATUS_SOLD.equals(product.getStatus())
                    ? "[" + product.getTitle() + "] 경매가 낙찰되었습니다. (낙찰자: " + product.getCurrentBidder() + ")"
                    : "[" + product.getTitle() + "] 경매가 유찰되었습니다.");
        }

        JOptionPane.showMessageDialog(this, message.toString(), "경매 종료", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 30) 
        ));

        // 좌측 영역 (햄버거 메뉴 버튼 + 타이틀)
        JPanel leftHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeaderPanel.setBackground(Color.WHITE);

        // 사이드바 토글을 위한 햄버거 버튼
        JButton menuToggleBtn = new JButton("▶");
        menuToggleBtn.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        menuToggleBtn.setBackground(Color.WHITE);
        menuToggleBtn.setForeground(new Color(51, 51, 51));
        menuToggleBtn.setFocusPainted(false);
        menuToggleBtn.setBorderPainted(false);
        menuToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuToggleBtn.addActionListener(e -> {
            // 사이드바 보이기/숨기기 토글
            sidebarPanel.setVisible(!sidebarPanel.isVisible());
            revalidate(); // 레이아웃 재계산
            repaint();
        });

        JLabel titleLabel = new JLabel("교내 경매 프로그램");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        titleLabel.setForeground(new Color(51, 51, 51));

        leftHeaderPanel.add(menuToggleBtn);
        leftHeaderPanel.add(titleLabel);

        // 우측 영역 (사용자 정보 박스)
        userLabel = new JLabel(currentUser.getUserName(), SwingConstants.CENTER);
        userLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        userLabel.setForeground(new Color(51, 51, 51));
        userLabel.setBackground(new Color(240, 240, 240));
        userLabel.setOpaque(true); 
        userLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 18, 6, 18) 
        ));

        JPanel userWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        userWrapper.setBackground(Color.WHITE);
        userWrapper.add(userLabel);

        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(userWrapper, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    // 좌측 사이드바 구성 (세로 나열)
    private void initSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)),
                new EmptyBorder(20, 15, 20, 15)
        ));
        sidebarPanel.setPreferredSize(new Dimension(200, 0)); // 사이드바 고정 너비 지정

        JButton btnAuction = createSidebarButton("경매 참여");
        JButton btnRegister = createSidebarButton("상품 등록");
        JButton btnMyPage = createSidebarButton("마이페이지");

        btnAuction.addActionListener(e -> cardLayout.show(mainContentPanel, "AUCTION"));
        btnRegister.addActionListener(e -> cardLayout.show(mainContentPanel, "REGISTER"));
        btnMyPage.addActionListener(e -> {
            cardLayout.show(mainContentPanel, "MYPAGE");
            if (myPagePanel != null) {
                myPagePanel.refresh();
            }
        });

        // 세로 정렬을 위해 버튼 추가 후 약간의 간격(RigidArea) 삽입
        sidebarPanel.add(btnAuction);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnRegister);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(btnMyPage);

        add(sidebarPanel, BorderLayout.WEST);
    }

    // 사이드바 전용 버튼 생성 유틸리티
    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setForeground(new Color(51, 51, 51));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        // 버튼을 꽉 차게 만들기 위한 정렬 및 여백
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 마우스 오버 효과 (선택사항)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        return btn;
    }

    private void initMainContent() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainContentPanel.setOpaque(false);

        auctionPanel = new AuctionPanel(auctionService, currentUser);
        myPagePanel = new MyPagePanel(auctionService, currentUser);

        mainContentPanel.add(auctionPanel, "AUCTION");
        mainContentPanel.add(new ProductRegisterPanel(this, auctionService, currentUser), "REGISTER");
        mainContentPanel.add(myPagePanel, "MYPAGE");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    /**
     * 상품 등록 성공 직후 호출되어 경매 참여 탭으로 전환하고 등록된 상품을 선택한다.
     */
    public void showAuctionProduct(String productId) {
        cardLayout.show(mainContentPanel, "AUCTION");
        auctionPanel.selectProduct(productId);
    }

    private void initStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel statusLabel = new JLabel("CSV 자동 갱신 중 (1초)");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 150, 150));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}