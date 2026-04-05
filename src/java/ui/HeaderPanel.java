package ui;

import model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HeaderPanel extends JPanel {

    private final GameState state;
    private final List<FloatCoin> floatCoins = new ArrayList<>();

    public HeaderPanel(GameState state) {
        this.state = state;
        setPreferredSize(new Dimension(0, 76));
        setOpaque(false);

        // 30 fps — анимация
        new Timer(33, e -> {
            long inc = state.consumeTickIncome();
            if (inc > 0) {
                // только ОДНА строка раз в секунду, без спама
                floatCoins.add(new FloatCoin(
                        getWidth() / 2 + 120,
                        52,
                        "+" + formatNum(inc)
                ));
            }
            Iterator<FloatCoin> it = floatCoins.iterator();
            while (it.hasNext()) {
                FloatCoin fc = it.next();
                fc.y   -= 0.9f;
                fc.life -= 2;
                if (fc.life <= 0) it.remove();
            }
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Фон
        GradientPaint gp = new GradientPaint(0, 0, new Color(12, 14, 28), w, 0, new Color(20, 18, 42));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Нижняя линия
        g2.setColor(UIColors.SEPARATOR);
        g2.fillRect(0, h - 1, w, 1);

        // ── Центр: баланс ─────────────────────────────────────────
        String coinsStr = formatNum(state.getCoins()) + "  монет";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int cx = (w - fm.stringWidth(coinsStr)) / 2;
        int cy = h / 2 + 6;

        // тень
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(coinsStr, cx + 1, cy + 1);

        // текст
        g2.setColor(UIColors.YELLOW);
        g2.drawString(coinsStr, cx, cy);

        // подпись
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(UIColors.TEXT_MUTED);
        String sub = "БАЛАНС";
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, cy + 18);

        // ── Слева: доход/сек ──────────────────────────────────────
        long inc   = state.getTotalIncome();
        String incStr = (inc >= 0 ? "+" : "") + formatNum(inc) + " к/с";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(inc >= 0 ? UIColors.GREEN : UIColors.RED);
        g2.drawString(incStr, 20, h / 2 + 6);

        // ── Справа: статус электричества ──────────────────────────
        if (state.isPowerDebt()) {
            drawDebtBadge(g2, w, h);
        } else if (!state.getRunningPCs().isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(UIColors.ORANGE);
            String bill = "Счёт: " + formatNum(state.getLastBillAmount()) + " монет / "
                    + GameState.BILL_INTERVAL + "с";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(bill, w - fm2.stringWidth(bill) - 20, h / 2 + 6);
        }

        // ── Плавающие +монеты (одна за раз) ──────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        for (FloatCoin fc : floatCoins) {
            int alpha = Math.min(255, fc.life * 4);
            g2.setColor(new Color(180, 255, 150, alpha));
            g2.drawString(fc.text, (int) fc.x, (int) fc.y);
        }

        g2.dispose();
    }

    private void drawDebtBadge(Graphics2D g2, int w, int h) {
        String msg = "ДОЛГ ЗА ЭЛЕКТРИЧЕСТВО  —  ПК ОСТАНОВЛЕНЫ";
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(msg) + 24, bh = 24;
        int bx = w - bw - 16, by = (h - bh) / 2;

        g2.setColor(UIColors.RED_DIM);
        g2.fill(new RoundRectangle2D.Float(bx, by, bw, bh, 8, 8));
        g2.setColor(UIColors.RED);
        g2.draw(new RoundRectangle2D.Float(bx, by, bw - 1, bh - 1, 8, 8));
        g2.setColor(UIColors.RED);
        g2.drawString(msg, bx + 12, by + bh - 7);
    }

    public static String formatNum(long n) {
        if (n < 1_000)     return String.valueOf(n);
        if (n < 1_000_000) return String.format("%.1fк", n / 1_000.0);
        return                    String.format("%.2fМ", n / 1_000_000.0);
    }

    private static class FloatCoin {
        float x, y;
        int   life = 80;
        final String text;
        FloatCoin(float x, float y, String text) {
            this.x = x; this.y = y; this.text = text;
        }
    }
}