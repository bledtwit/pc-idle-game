package ui;

import logic.GameEngine;
import model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameWindow extends JFrame {

    private final GameState      state;
    private final WorkbenchPanel workbench;
    private final PcListPanel    pcList;

    public GameWindow(GameEngine engine, GameState state) {
        this.state = state;

        setTitle("Idle PC Builder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1020, 700);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(null);
        setBackground(UIColors.BG_DARK);

        try { setIconImage(makeAppIcon()); } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIColors.BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(true);
        setContentPane(root);

        HeaderPanel header = new HeaderPanel(state);
        root.add(header, BorderLayout.NORTH);

        Runnable refresh = this::refreshAll;

        ShopPanel shop = new ShopPanel(engine, state, refresh);
        workbench      = new WorkbenchPanel(engine, state, refresh);
        pcList         = new PcListPanel(state, engine, refresh);

        JPanel center = new JPanel(new GridLayout(1, 3, 8, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(8, 10, 10, 10));

        // Магазин — с рамкой (стандартная колонка)
        center.add(wrapColumn("🛒  Магазин", wrapScroll(shop), true));

        // ИСПРАВЛЕНО: Верстак и Парк машин — без внешнего прямоугольника
        center.add(wrapColumn("🔧  Верстак", wrapScroll(workbench), false));
        center.add(wrapColumn("🖥️  Парк машин", wrapScroll(pcList), false));

        root.add(center, BorderLayout.CENTER);
        root.add(makeStatusBar(), BorderLayout.SOUTH);

        new Timer(500, e -> refreshAll()).start();

        refreshAll();
        setVisible(true);
    }

    private void refreshAll() {
        workbench.refresh();
        pcList.refresh();
    }

    // ── ИСПРАВЛЕНО: параметр showBackground убирает лишний прямоугольник ──
    private JPanel wrapColumn(String title, JComponent content, boolean showBackground) {
        JPanel col = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                if (showBackground) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UIColors.BG_PANEL);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                    g2.setColor(UIColors.SEPARATOR);
                    g2.draw(new RoundRectangle2D.Float(0, 0,
                            getWidth() - 1, getHeight() - 1, 14, 14));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        col.setOpaque(false);
        col.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Заголовок колонки
        JPanel titleBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIColors.BG_DEEP);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + 14, 14, 14));
                g2.setColor(UIColors.SEPARATOR);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        titleBar.setOpaque(false);
        titleBar.setPreferredSize(new Dimension(0, 38));
        titleBar.setBorder(new EmptyBorder(0, 14, 0, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(UIColors.ACCENT);
        titleBar.add(titleLbl, BorderLayout.CENTER);

        col.add(titleBar, BorderLayout.NORTH);
        col.add(content,  BorderLayout.CENTER);
        return col;
    }

    private JScrollPane wrapScroll(JComponent panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setOpaque(false);

        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = UIColors.SEPARATOR;
                trackColor = UIColors.BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });

        return sp;
    }

    private JPanel makeStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIColors.BG_DEEP);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UIColors.SEPARATOR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        bar.setPreferredSize(new Dimension(0, 26));
        bar.setBorder(new EmptyBorder(0, 14, 0, 14));
        bar.setOpaque(false);

        JLabel left = new JLabel("Idle PC Builder  ·  v0.3");
        left.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        left.setForeground(UIColors.TEXT_MUTED);

        JLabel right = new JLabel("Каждые " + GameState.BILL_INTERVAL + " сек приходит счёт за электричество ⚡");
        right.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        right.setForeground(UIColors.TEXT_MUTED);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private Image makeAppIcon() {
        int size = 32;
        var img = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(28, 32, 55));
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, 8, 8));
        g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        g2.setColor(UIColors.ACCENT);
        g2.drawString("🖥", 4, 24);
        g2.dispose();
        return img;
    }
}