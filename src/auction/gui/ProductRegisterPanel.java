package auction.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProductRegisterPanel extends JPanel {

    private JLabel imagePreview;
    private File selectedImageFile;
    
    private JTextField txtStartTime;
    private JTextField txtEndTime;

    public ProductRegisterPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(30, 0));
        centerPanel.setOpaque(false);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        
        // [요구사항 3] 하단(bottom) 여백을 30 -> 60으로 늘려 이미지 버튼 아래 여유 공간 확보
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(30, 30, 60, 30) 
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 10, 12, 10);

        Font labelFont = new Font("맑은 고딕", Font.BOLD, 13);
        Color labelColor = new Color(80, 80, 80);

        addFormRow(formPanel, gbc, 0, "판매자", new JLabel("지소은"), labelFont, labelColor);
        addFormRow(formPanel, gbc, 1, "상품명", createStyledTextField(), labelFont, labelColor);
        
        JTextArea txtDesc = new JTextArea(6, 20);
        txtDesc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        addFormRow(formPanel, gbc, 2, "상품 설명", new JScrollPane(txtDesc), labelFont, labelColor);
        
        JPanel pricePanel = new JPanel(new BorderLayout(10, 0));
        pricePanel.setOpaque(false);
        pricePanel.add(createStyledTextField(), BorderLayout.CENTER);
        JLabel wonLabel = new JLabel("원");
        wonLabel.setFont(labelFont);
        wonLabel.setForeground(labelColor);
        pricePanel.add(wonLabel, BorderLayout.EAST);
        
        addFormRow(formPanel, gbc, 3, "시작 가격", pricePanel, labelFont, labelColor);

        txtStartTime = createStyledTextField();
        addFormRow(formPanel, gbc, 4, "경매 시작", createDateTimeSelectorPanel(txtStartTime), labelFont, labelColor);

        txtEndTime = createStyledTextField();
        addFormRow(formPanel, gbc, 5, "경매 종료", createDateTimeSelectorPanel(txtEndTime), labelFont, labelColor);

        JButton imgBtn = new JButton("이미지 파일 선택");
        imgBtn.setBackground(new Color(240, 240, 240));
        imgBtn.setFocusPainted(false);
        imgBtn.setOpaque(true);
        imgBtn.setBorderPainted(false);
        
        imagePreview = new JLabel("이미지를 선택해 주세요", SwingConstants.CENTER);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(new Color(248, 249, 250));
        imagePreview.setForeground(new Color(150, 150, 150));
        imagePreview.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        imagePreview.setPreferredSize(new Dimension(380, 0));

        imgBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("이미지 파일 (JPG, PNG, GIF)", "jpg", "png", "gif", "jpeg");
            fileChooser.setFileFilter(filter);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fileChooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
                Image scaledImage = icon.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
                imagePreview.setIcon(new ImageIcon(scaledImage));
                imagePreview.setText(""); 
            }
        });

        addFormRow(formPanel, gbc, 6, "상품 이미지", imgBtn, labelFont, labelColor);

        formContainer.add(formPanel, BorderLayout.NORTH);

        centerPanel.add(formContainer, BorderLayout.CENTER);
        centerPanel.add(imagePreview, BorderLayout.EAST);

        return centerPanel;
    }

    private JPanel createDateTimeSelectorPanel(JTextField targetField) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        
        targetField.setEditable(false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        targetField.setText(sdf.format(new Date()));
        
        panel.add(targetField, BorderLayout.CENTER);
        
        JButton selectBtn = new JButton("날짜 선택");
        selectBtn.setBackground(new Color(240, 240, 240));
        selectBtn.setFocusPainted(false);
        selectBtn.setOpaque(true);
        selectBtn.setBorderPainted(false);
        selectBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        
        selectBtn.addActionListener(e -> {
            DateTimePickerDialog dialog = new DateTimePickerDialog((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                SimpleDateFormat sdfModal = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                targetField.setText(sdfModal.format(dialog.getSelectedDate()));
            }
        });
        
        panel.add(selectBtn, BorderLayout.EAST);
        return panel;
    }

    // --- 시각화된 달력 및 오전/오후, 시/분 선택 전용 모달 클래스 ---
    private class DateTimePickerDialog extends JDialog {
        private Calendar selectedCalendar = Calendar.getInstance();
        private Calendar viewCalendar = Calendar.getInstance();
        private JLabel lblMonthYear = new JLabel("", JLabel.CENTER);
        private JPanel dayGridPanel;
        
        private JButton btnAmPm;
        private JSpinner hourSpinner;
        private JSpinner minuteSpinner;
        private boolean confirmed = false;

        public DateTimePickerDialog(Frame parent) {
            super(parent, "경매 일시 선택", true);
            // [요구사항 1] 가로 길이를 400에서 460으로 늘려서 레이아웃 밀림 방지
            setSize(460, 470);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(Color.WHITE);

            // 상단: 월/년 이동 네비게이션
            JPanel monthNavPanel = new JPanel(new BorderLayout());
            monthNavPanel.setBackground(Color.WHITE);
            monthNavPanel.setBorder(new EmptyBorder(15, 20, 0, 20));

            JButton btnPrev = new JButton("<");
            JButton btnNext = new JButton(">");
            styleNavButton(btnPrev);
            styleNavButton(btnNext);

            lblMonthYear.setFont(new Font("맑은 고딕", Font.BOLD, 15));
            lblMonthYear.setForeground(new Color(51, 51, 51));

            btnPrev.addActionListener(e -> {
                viewCalendar.add(Calendar.MONTH, -1);
                refreshCalendarGrid();
            });
            btnNext.addActionListener(e -> {
                viewCalendar.add(Calendar.MONTH, 1);
                refreshCalendarGrid();
            });

            monthNavPanel.add(btnPrev, BorderLayout.WEST);
            monthNavPanel.add(lblMonthYear, BorderLayout.CENTER);
            monthNavPanel.add(btnNext, BorderLayout.EAST);
            add(monthNavPanel, BorderLayout.NORTH);

            // 중앙: 시각화된 달력 그리드 패널
            JPanel centerContainer = new JPanel(new BorderLayout(0, 5));
            centerContainer.setBackground(Color.WHITE);
            centerContainer.setBorder(new EmptyBorder(10, 20, 0, 20));

            JPanel daysHeader = new JPanel(new GridLayout(1, 7, 2, 2));
            daysHeader.setBackground(Color.WHITE);
            String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
            for (int i = 0; i < 7; i++) {
                JLabel lbl = new JLabel(dayNames[i], JLabel.CENTER);
                lbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                if (i == 0) lbl.setForeground(new Color(220, 50, 50));
                else if (i == 6) lbl.setForeground(new Color(50, 50, 220));
                else lbl.setForeground(new Color(100, 100, 100));
                daysHeader.add(lbl);
            }
            centerContainer.add(daysHeader, BorderLayout.NORTH);

            dayGridPanel = new JPanel(new GridLayout(6, 7, 3, 3));
            dayGridPanel.setBackground(Color.WHITE);
            centerContainer.add(dayGridPanel, BorderLayout.CENTER);

            // 하단: 시각 설정 + 확인/취소 버튼
            JPanel bottomWrapper = new JPanel(new BorderLayout(0, 10));
            bottomWrapper.setBackground(Color.WHITE);
            bottomWrapper.setBorder(new EmptyBorder(10, 20, 15, 20));

            JPanel timeSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            timeSelectPanel.setBackground(new Color(248, 249, 250));
            timeSelectPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(8, 10, 8, 10)
            ));

            JLabel timeTitle = new JLabel("시각 선택: ");
            timeTitle.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            timeSelectPanel.add(timeTitle);

            int hourOfDay = selectedCalendar.get(Calendar.HOUR_OF_DAY);
            boolean isPm = hourOfDay >= 12;
            btnAmPm = new JButton(isPm ? "오후" : "오전");
            btnAmPm.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            btnAmPm.setBackground(Color.WHITE);
            btnAmPm.setFocusPainted(false);
            btnAmPm.setPreferredSize(new Dimension(75, 32));
            btnAmPm.addActionListener(e -> {
                if (btnAmPm.getText().equals("오전")) {
                    btnAmPm.setText("오후");
                } else {
                    btnAmPm.setText("오전");
                }
            });
            timeSelectPanel.add(btnAmPm);

            int initHour12 = hourOfDay % 12;
            if (initHour12 == 0) initHour12 = 12;
            int initMinute = selectedCalendar.get(Calendar.MINUTE);

            hourSpinner = new JSpinner(new SpinnerNumberModel(initHour12, 1, 12, 1));
            hourSpinner.setPreferredSize(new Dimension(55, 32));
            JSpinner.DefaultEditor hourEditor = (JSpinner.DefaultEditor) hourSpinner.getEditor();
            hourEditor.getTextField().setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            JLabel colonLabel = new JLabel("시");
            colonLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));

            minuteSpinner = new JSpinner(new SpinnerNumberModel(initMinute, 0, 59, 1));
            minuteSpinner.setPreferredSize(new Dimension(55, 32));
            JSpinner.DefaultEditor minuteEditor = (JSpinner.DefaultEditor) minuteSpinner.getEditor();
            minuteEditor.getTextField().setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            timeSelectPanel.add(hourSpinner);
            timeSelectPanel.add(colonLabel);
            timeSelectPanel.add(minuteSpinner);
            
            JLabel minLabel = new JLabel("분");
            minLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            timeSelectPanel.add(minLabel);

            bottomWrapper.add(timeSelectPanel, BorderLayout.NORTH);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setBackground(Color.WHITE);

            JButton okBtn = new JButton("확인");
            okBtn.setBackground(new Color(0, 122, 255));
            okBtn.setForeground(Color.WHITE);
            okBtn.setFocusPainted(false);
            okBtn.setOpaque(true);
            okBtn.setBorderPainted(false);
            okBtn.setPreferredSize(new Dimension(90, 35));
            okBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            okBtn.addActionListener(e -> {
                confirmed = true;
                int h = (Integer) hourSpinner.getValue();
                boolean pm = btnAmPm.getText().equals("오후");
                if (pm) {
                    if (h < 12) h += 12;
                } else {
                    if (h == 12) h = 0;
                }
                int m = (Integer) minuteSpinner.getValue();

                selectedCalendar.set(Calendar.HOUR_OF_DAY, h);
                selectedCalendar.set(Calendar.MINUTE, m);
                selectedCalendar.set(Calendar.SECOND, 0);
                dispose();
            });

            JButton cancelBtn = new JButton("취소");
            cancelBtn.setBackground(new Color(240, 240, 240));
            cancelBtn.setForeground(Color.BLACK);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setOpaque(true);
            cancelBtn.setBorderPainted(false);
            cancelBtn.setPreferredSize(new Dimension(90, 35));
            cancelBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            cancelBtn.addActionListener(e -> dispose());

            btnPanel.add(okBtn);
            btnPanel.add(cancelBtn);
            bottomWrapper.add(btnPanel, BorderLayout.SOUTH);

            add(centerContainer, BorderLayout.CENTER);
            add(bottomWrapper, BorderLayout.SOUTH);

            refreshCalendarGrid();
        }

        private void styleNavButton(JButton btn) {
            btn.setBackground(Color.WHITE);
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            btn.setPreferredSize(new Dimension(35, 30));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        private void refreshCalendarGrid() {
            dayGridPanel.removeAll();

            SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy년 M월");
            lblMonthYear.setText(sdfMonth.format(viewCalendar.getTime()));

            Calendar cal = (Calendar) viewCalendar.clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);

            int startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); 
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            int offset = startDayOfWeek - 1;
            for (int i = 0; i < offset; i++) {
                dayGridPanel.add(new JLabel(""));
            }

            for (int day = 1; day <= maxDay; day++) {
                final int d = day;
                JButton dayBtn = new JButton(String.valueOf(day));
                dayBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                dayBtn.setFocusPainted(false);
                dayBtn.setBorderPainted(false);

                boolean isSelected = (selectedCalendar.get(Calendar.YEAR) == viewCalendar.get(Calendar.YEAR) &&
                                      selectedCalendar.get(Calendar.MONTH) == viewCalendar.get(Calendar.MONTH) &&
                                      selectedCalendar.get(Calendar.DAY_OF_MONTH) == d);

                if (isSelected) {
                    dayBtn.setBackground(new Color(0, 122, 255));
                    dayBtn.setForeground(Color.WHITE);
                } else {
                    dayBtn.setBackground(new Color(248, 249, 250));
                    dayBtn.setForeground(new Color(51, 51, 51));
                }

                dayBtn.addActionListener(e -> {
                    selectedCalendar.set(viewCalendar.get(Calendar.YEAR),
                                         viewCalendar.get(Calendar.MONTH),
                                         d);
                    refreshCalendarGrid();
                });

                dayGridPanel.add(dayBtn);
            }

            int totalCells = offset + maxDay;
            int remainingCells = (totalCells <= 35) ? (35 - totalCells) : (42 - totalCells);
            for (int i = 0; i < remainingCells; i++) {
                dayGridPanel.add(new JLabel(""));
            }

            dayGridPanel.revalidate();
            dayGridPanel.repaint();
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Date getSelectedDate() {
            return selectedCalendar.getTime();
        }
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setOpaque(false);
        
        JButton cancelBtn = new JButton("취소");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setOpaque(true);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "등록이 취소되었습니다."));
        
        JButton submitBtn = new JButton("상품 등록");
        submitBtn.setPreferredSize(new Dimension(120, 40));
        submitBtn.setBackground(new Color(0, 122, 255));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setOpaque(true);
        submitBtn.setBorderPainted(false);
        submitBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "상품이 성공적으로 등록되었습니다!", "등록 완료", JOptionPane.INFORMATION_MESSAGE));

        bottomPanel.add(cancelBtn);
        bottomPanel.add(submitBtn);
        return bottomPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component comp, Font font, Color color) {
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.1;
        gbc.anchor = (comp instanceof JScrollPane) ? GridBagConstraints.NORTH : GridBagConstraints.CENTER;
        
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        label.setForeground(color);
        panel.add(label, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.9;
        panel.add(comp, gbc);
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }
}