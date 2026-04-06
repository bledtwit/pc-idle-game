package ui;

import logic.GameEngine;
import model.ComponentType;
import model.GameState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ShopPanel extends JPanel {

    private final GameEngine engine;
    private final GameState  state;
    private final Runnable   onAction;

    public ShopPanel(GameEngine engine, GameState state, Runnable onAction) {
        this.engine   = engine;
        this.state    = state;
        this.onAction = onAction;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 8, 10, 8));

        add(makeTitle("🛒  Магазин"));
        add(Box.createVerticalStrut(8));

        JButton buyAll = makeBuyAllButton();
        buyAll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        add(buyAll);
        add(Box.createVerticalStrut(10));

        for (ComponentType type : ComponentType.values()) {
            add(new ComponentCard(type));
            add(Box.createVerticalStrut(6));
        }
    }

    private JButton makeBuyAllButton() {
        JButton btn = makeRoundedButton(
                "⚡  Купить комплект (все детали, базовый тир)",
                new Color(35, 110, 65), Color.WHITE
        );
        btn.addActionListener(e -> {
            for (ComponentType t : ComponentType.values()) engine.buyPart(t, 1);
            onAction.run();
        });
        return btn;
    }

    private class ComponentCard extends JPanel {
        private final ComponentType type;

        ComponentCard(ComponentType type) {
            this.type = type;
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            buildContent();
        }

        private void buildContent() {
            // ── Верхняя строка: иконка + имя ─────────────────────
            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);
            top.setBorder(new EmptyBorder(8, 10, 4, 10));

            // Иконка компонента
            JLabel iconLbl = new JLabel(type.icon);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            iconLbl.setPreferredSize(new Dimension(28, 28));

            JPanel nameBlock = new JPanel();
            nameBlock.setOpaque(false);
            nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));

            JLabel nameLbl = new JLabel(type.displayName);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLbl.setForeground(UIColors.TEXT_MAIN);

            JLabel modelLbl = new JLabel(type.model);
            modelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            modelLbl.setForeground(UIColors.TEXT_MUTED);

            nameBlock.add(nameLbl);
            nameBlock.add(modelLbl);

            top.add(iconLbl,   BorderLayout.WEST);
            top.add(nameBlock, BorderLayout.CENTER);

            // ── Нижняя строка: кнопки покупки + продажа ──────────
            JPanel btnsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            btnsRow.setOpaque(false);

            // ИСПРАВЛЕНО: читаемые подписи с ценой на новой строке через HTML
            Color[] bgColors   = {UIColors.BTN_T1, UIColors.BTN_T2, UIColors.BTN_T3};
            String[] tierNames = {"Базовый", "Средний", "Топ"};

            for (int i = 0; i < 3; i++) {
                final int tier  = i + 1;
                long price      = type.priceForTier(tier);
                String label    = "<html><center>" + tierNames[i]
                        + "<br><b>" + price + " м</b></center></html>";

                JButton btn = makeRoundedButton(label, bgColors[i], Color.WHITE);
                btn.setPreferredSize(new Dimension(95, 38));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btn.setToolTipText(type.displayName + " тир " + tier + " — цена: " + price + " монет");
                btn.addActionListener(e -> {
                    engine.buyPart(type, tier);
                    onAction.run();
                });
                btnsRow.add(btn);
            }

            // Кнопка продажи
            JButton sellBtn = makeRoundedButton("Продать\n50%", new Color(90, 28, 28), UIColors.RED);
            sellBtn.setPreferredSize(new Dimension(70, 38));
            sellBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            sellBtn.setToolTipText("Продать одну деталь со склада (возврат 50% цены)");
            sellBtn.addActionListener(e -> {
                engine.sellPart(type);
                onAction.run();
            });
            btnsRow.add(sellBtn);

            add(top,     BorderLayout.NORTH);
            add(btnsRow, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UIColors.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.setColor(UIColors.SEPARATOR);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Универсальная скруглённая кнопка ─────────────────────────
    public static JButton makeRoundedButton(String label, Color bg, Color textColor) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()
                        ? UIColors.darken(bg, 0.15f)
                        : getModel().isRollover()
                        ? UIColors.lighten(bg, 0.12f)
                        : bg;
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(textColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton styledButton(String text, Color bg) {
        return makeRoundedButton(text, bg, Color.WHITE);
    }

    public static JButton makeTierButton(String label, Color bg, String tooltip) {
        JButton btn = makeRoundedButton(label, bg, Color.WHITE);
        btn.setToolTipText(tooltip);
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