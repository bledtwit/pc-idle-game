package ui;

import model.Computer;
import model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class PcListPanel extends JPanel {

    private final GameState state;
    private final JPanel    listArea;
    private final JLabel    totalLabel;
    private final JPanel    billPanel;
    private final JLabel    billLabel;
    private final JLabel    timerLabel;
    private final JButton   payBtn;

    public PcListPanel(GameState state) {
        this.state = state;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 8, 10, 8));

        // ── Заголовок ─────────────────────────────────────────────
        add(makeTitle("🖥️  Работающие ПК"));
        add(Box.createVerticalStrut(6));

        // ── Суммарный доход ───────────────────────────────────────
        totalLabel = new JLabel("Суммарный доход: 0 к/с");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        totalLabel.setForeground(UIColors.TEXT_MUTED);
        totalLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(totalLabel);
        add(Box.createVerticalStrut(10));

        // ── Панель счёта за электричество ─────────────────────────
        billPanel = buildBillPanel();
        billPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(billPanel);
        add(Box.createVerticalStrut(10));

        // ── Список ПК ─────────────────────────────────────────────
        listArea = new JPanel();
        listArea.setOpaque(false);
        listArea.setLayout(new BoxLayout(listArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listArea);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        add(scroll);

        // Получаем ссылки на нужные компоненты внутри billPanel
        billLabel  = (JLabel)  billPanel.getClientProperty("billLabel");
        timerLabel = (JLabel)  billPanel.getClientProperty("timerLabel");
        payBtn     = (JButton) billPanel.getClientProperty("payBtn");

        refresh();
    }

    // ── Обновление ────────────────────────────────────────────────
    public void refresh() {
        // суммарный доход
        long total = state.getTotalIncome();
        totalLabel.setText("Суммарный доход: "
                + (total >= 0 ? "+" : "") + HeaderPanel.formatNum(total) + " к/с");
        totalLabel.setForeground(total >= 0 ? UIColors.GREEN : UIColors.RED);

        // счёт за электричество
        refreshBillPanel();

        // список ПК
        listArea.removeAll();
        List<Computer> pcs = state.getRunningPCs();

        if (pcs.isEmpty()) {
            JLabel empty = new JLabel("Нет собранных ПК");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(UIColors.TEXT_MUTED);
            empty.setAlignmentX(LEFT_ALIGNMENT);
            listArea.add(empty);
        } else {
            for (Computer pc : pcs) {
                listArea.add(buildPcCard(pc));
                listArea.add(Box.createVerticalStrut(6));
            }
        }

        listArea.revalidate();
        listArea.repaint();
    }

    // ── Панель счёта за электричество ─────────────────────────────
    private JPanel buildBillPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = state.isPowerDebt()
                        ? UIColors.RED_DIM
                        : new Color(30, 25, 15);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                Color border = state.isPowerDebt() ? UIColors.RED : UIColors.ORANGE;
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // строка 1: иконка + заголовок
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);

        JLabel elIcon = new JLabel("⚡  Электричество");
        elIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        elIcon.setForeground(UIColors.ORANGE);

        JLabel billLbl = new JLabel("Счёт: 0 монет");
        billLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        billLbl.setForeground(UIColors.YELLOW);
        billLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row1.add(elIcon,  BorderLayout.WEST);
        row1.add(billLbl, BorderLayout.EAST);

        // строка 2: таймер
        JLabel timerLbl = new JLabel("Следующий счёт через: --");
        timerLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timerLbl.setForeground(UIColors.TEXT_MUTED);
        timerLbl.setAlignmentX(LEFT_ALIGNMENT);

        // строка 3: прогресс-бар таймера
        JProgressBar timerBar = new JProgressBar(0, GameState.BILL_INTERVAL) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 50));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                float ratio = (float) getValue() / getMaximum();
                int   fw    = (int)(getWidth() * ratio);
                if (fw > 0) {
                    Color c = ratio > 0.75f ? UIColors.RED
                            : ratio > 0.4f  ? UIColors.ORANGE
                            : UIColors.GREEN;
                    g2.setColor(c);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fw, getHeight(), 6, 6));
                }
                g2.dispose();
            }
        };
        timerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        timerBar.setPreferredSize(new Dimension(0, 6));

        // строка 4: кнопка оплаты (видна только при долге)
        JButton payButton = new JButton("💳  Оплатить долг") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed()
                        ? new Color(100, 20, 20)
                        : getModel().isRollover()
                        ? new Color(160, 40, 40)
                        : new Color(130, 30, 30);
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        payButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        payButton.setForeground(UIColors.RED);
        payButton.setContentAreaFilled(false);
        payButton.setBorderPainted(false);
        payButton.setFocusPainted(false);
        payButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        payButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        payButton.setVisible(false);
        payButton.addActionListener(e -> {
            state.payDebt();
            refresh();
        });

        // сохраняем ссылки через clientProperty
        panel.putClientProperty("billLabel",  billLbl);
        panel.putClientProperty("timerLabel", timerLbl);
        panel.putClientProperty("timerBar",   timerBar);
        panel.putClientProperty("payBtn",     payButton);

        panel.add(row1);
        panel.add(Box.createVerticalStrut(6));
        panel.add(timerLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(timerBar);
        panel.add(Box.createVerticalStrut(6));
        panel.add(payButton);

        return panel;
    }

    private void refreshBillPanel() {
        if (billLabel == null) return;

        long bill      = state.getLastBillAmount();
        int  ticks     = state.getTicksSinceBill();
        int  remaining = GameState.BILL_INTERVAL - ticks;
        boolean debt   = state.isPowerDebt();

        billLabel.setText("Счёт: 💰 " + HeaderPanel.formatNum(bill));
        billLabel.setForeground(debt ? UIColors.RED : UIColors.YELLOW);

        if (debt) {
            timerLabel.setText("⚠️  Долг не оплачен — ПК остановлены!");
            timerLabel.setForeground(UIColors.RED);
        } else {
            timerLabel.setText("Следующий счёт через: " + remaining + " сек");
            timerLabel.setForeground(UIColors.TEXT_MUTED);
        }

        JProgressBar bar = (JProgressBar) billPanel.getClientProperty("timerBar");
        if (bar != null) {
            bar.setValue(ticks);
            bar.repaint();
        }

        payBtn.setVisible(debt);
        billPanel.repaint();
    }

    // ── Карточка ПК ───────────────────────────────────────────────
    private JPanel buildPcCard(Computer pc) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean paused = state.isPowerDebt();
                Color bg = paused ? new Color(40, 25, 25) : UIColors.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                Color border = paused ? UIColors.RED_DIM : UIColors.SEPARATOR;
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // ── Левый блок: иконка ────────────────────────────────────
        boolean paused = state.isPowerDebt();
        JLabel iconLbl = new JLabel(paused ? "⛔" : "🖥️");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setPreferredSize(new Dimension(40, 40));

        // ── Центр: название + доход ───────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("ПК #" + pc.getId()
                + (paused ? "  [СТОП]" : "  [РАБОТАЕТ]"));
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(paused ? UIColors.RED : UIColors.TEXT_MAIN);

        JLabel incomeLbl = new JLabel(
                "▲ +" + pc.getCoinsPerSec() + " к/с   "
                        + "⚡ -" + pc.getPowerCost() + " к/с");
        incomeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        incomeLbl.setForeground(UIColors.TEXT_MUTED);

        long net = pc.getNetIncome();
        JLabel netLbl = new JLabel("Чистый: " + (net >= 0 ? "+" : "") + net + " к/с");
        netLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        netLbl.setForeground(net >= 0 ? UIColors.GREEN : UIColors.RED);

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(incomeLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(netLbl);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(info,    BorderLayout.CENTER);
        return card;
    }

    // ── Утилиты ───────────────────────────────────────────────────
    private JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(UIColors.ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}