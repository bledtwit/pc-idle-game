package ui;

import logic.GameEngine;
import model.ComponentType;
import model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class WorkbenchPanel extends JPanel {

    private final GameEngine engine;
    private final GameState  state;
    private final Runnable   onAction;

    private final JLabel       statusLabel;
    private final JPanel       slotsPanel;
    private final JButton      buildBtn;
    private final JProgressBar progressBar;

    public WorkbenchPanel(GameEngine engine, GameState state, Runnable onAction) {
        this.engine   = engine;
        this.state    = state;
        this.onAction = onAction;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 8, 10, 8));

        add(makeTitle("🔧  Верстак"));
        add(Box.createVerticalStrut(8));

        // ── Прогресс-бар ──────────────────────────────────────────
        progressBar = makeProgressBar();
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setAlignmentX(LEFT_ALIGNMENT);
        add(progressBar);
        add(Box.createVerticalStrut(10));

        // ── Слоты ─────────────────────────────────────────────────
        slotsPanel = new JPanel();
        slotsPanel.setOpaque(false);
        slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
        slotsPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(slotsPanel);

        add(Box.createVerticalStrut(12));

        // ── Статус ────────────────────────────────────────────────
        statusLabel = new JLabel("Установите все 7 деталей");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(UIColors.TEXT_MUTED);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(statusLabel);

        add(Box.createVerticalStrut(10));

        // ── Кнопка сборки ─────────────────────────────────────────
        buildBtn = makeBuildButton();
        buildBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        buildBtn.setAlignmentX(LEFT_ALIGNMENT);
        add(buildBtn);

        refresh();
    }

    public void refresh() {
        slotsPanel.removeAll();

        var inv       = state.getInventory();
        var tierInInv = state.getTierInInv();
        var workbench = state.getWorkbench();
        int filled    = 0;

        for (ComponentType type : ComponentType.values()) {
            boolean installed = workbench.hasPart(type);
            int     inStock   = inv.getOrDefault(type, 0);
            int     tier      = tierInInv.getOrDefault(type, 1);
            if (installed) filled++;

            slotsPanel.add(buildSlotCard(type, installed, inStock, tier));
            slotsPanel.add(Box.createVerticalStrut(5));
        }

        progressBar.setValue(filled);
        progressBar.setString(filled + " / 7");

        if (filled == ComponentType.values().length) {
            statusLabel.setText("✅  Всё готово — нажмите «Собрать»!");
            statusLabel.setForeground(UIColors.GREEN);
            buildBtn.setEnabled(true);
        } else {
            statusLabel.setText("Слотов заполнено: " + filled + " из 7");
            statusLabel.setForeground(UIColors.TEXT_MUTED);
            buildBtn.setEnabled(false);
        }

        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    // ── Карточка слота — ИСПРАВЛЕНО: убраны "·", нормальные кнопки ──
    private JPanel buildSlotCard(ComponentType type,
                                 boolean installed,
                                 int inStock,
                                 int tier) {

        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = installed ? UIColors.BG_SLOT_OK : UIColors.BG_SLOT_MT;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                Color border = installed ? UIColors.GREEN_DIM : UIColors.SEPARATOR;
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(6, 10, 6, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        // ── Иконка ────────────────────────────────────────────────
        JLabel iconLbl = new JLabel(installed ? "✅" : type.icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLbl.setPreferredSize(new Dimension(32, 32));

        // ── Инфо ──────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(type.displayName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(installed ? UIColors.GREEN : UIColors.TEXT_MAIN);

        String subText;
        Color  subColor;
        if (installed) {
            subText  = "Тир " + tier + " · установлена";
            subColor = UIColors.GREEN;
        } else if (inStock > 0) {
            subText  = "На складе: " + inStock + "  (Тир " + tier + ")";
            subColor = UIColors.ACCENT;
        } else {
            subText  = "Нет на складе — купите в магазине";
            subColor = UIColors.TEXT_MUTED;
        }

        JLabel subLbl = new JLabel(subText);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(subColor);

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(subLbl);

        // ── ИСПРАВЛЕНО: кнопка установки с нормальным текстом ────
        JButton instBtn;
        if (installed) {
            // Уже установлена — кнопка не нужна, показываем заглушку-метку
            instBtn = new JButton("Уст.") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UIColors.GREEN_DIM);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            instBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            instBtn.setForeground(UIColors.GREEN);
            instBtn.setEnabled(false);
        } else if (inStock > 0) {
            instBtn = ShopPanel.makeRoundedButton("⬆ Уст.", UIColors.BTN_INST, Color.WHITE);
            instBtn.addActionListener(e -> {
                engine.installPart(type);
                onAction.run();
            });
        } else {
            instBtn = new JButton("Нет") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UIColors.BG_CARD);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            instBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            instBtn.setForeground(UIColors.TEXT_MUTED);
            instBtn.setEnabled(false);
        }

        instBtn.setPreferredSize(new Dimension(60, 30));
        instBtn.setContentAreaFilled(false);
        instBtn.setBorderPainted(false);
        instBtn.setFocusPainted(false);
        if (inStock > 0 && !installed)
            instBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(iconLbl, BorderLayout.WEST);
        card.add(info,    BorderLayout.CENTER);
        card.add(instBtn, BorderLayout.EAST);
        return card;
    }

    // ── Прогресс-бар ─────────────────────────────────────────────
    private JProgressBar makeProgressBar() {
        JProgressBar pb = new JProgressBar(0, 7) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIColors.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                float ratio = (float) getValue() / getMaximum();
                int   fw    = (int)(getWidth() * ratio);
                if (fw > 0) {
                    GradientPaint gp = new GradientPaint(
                            0, 0, UIColors.ACCENT,
                            fw, 0, UIColors.GREEN
                    );
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Float(0, 0, fw, getHeight(), 8, 8));
                }
                if (isStringPainted()) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    String s = getString();
                    g2.drawString(s,
                            (getWidth()  - fm.stringWidth(s)) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        pb.setValue(0);
        pb.setStringPainted(true);
        pb.setString("0 / 7");
        pb.setPreferredSize(new Dimension(0, 14));
        return pb;
    }

    // ── Кнопка сборки ─────────────────────────────────────────────
    private JButton makeBuildButton() {
        JButton btn = new JButton("⚡  Собрать ПК") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = !isEnabled()
                        ? new Color(40, 35, 60)
                        : (getModel().isPressed()
                        ? new Color(80, 50, 150)
                        : (getModel().isRollover()
                        ? new Color(120, 85, 210)
                        : new Color(86, 50, 173)));
                g2.setColor(base);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                if (isEnabled()) {
                    g2.setColor(UIColors.PURPLE);
                    g2.draw(new RoundRectangle2D.Float(0, 0,
                            getWidth() - 1, getHeight() - 1, 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            engine.buildPC();
            onAction.run();
        });
        return btn;
    }

    private JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(UIColors.ACCENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}