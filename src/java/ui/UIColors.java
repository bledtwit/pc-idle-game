package ui;

import java.awt.*;

public class UIColors {

    // ── Фоны ──────────────────────────────────────────────────────
    public static final Color BG_DEEP    = new Color(10,  12,  20);
    public static final Color BG_DARK    = new Color(15,  17,  30);
    public static final Color BG_PANEL   = new Color(20,  23,  40);
    public static final Color BG_CARD    = new Color(28,  32,  55);
    public static final Color BG_CARD_HO = new Color(35,  40,  68);
    public static final Color BG_SLOT_OK = new Color(18,  48,  30);
    public static final Color BG_SLOT_MT = new Color(28,  32,  55);
    public static final Color SEPARATOR  = new Color(40,  45,  72);

    // ── Акценты ───────────────────────────────────────────────────
    public static final Color ACCENT     = new Color(99,  179, 237);
    public static final Color ACCENT2    = new Color(129, 140, 248);
    public static final Color GREEN      = new Color(72,  199, 142);
    public static final Color GREEN_DIM  = new Color(39,  100,  70);
    public static final Color YELLOW     = new Color(251, 211,  80);
    public static final Color YELLOW_DIM = new Color(120,  95,  20);
    public static final Color RED        = new Color(252, 129, 129);
    public static final Color RED_DIM    = new Color(120,  35,  35);
    public static final Color ORANGE     = new Color(251, 160,  75);
    public static final Color PURPLE     = new Color(183, 148, 246);

    // ── Текст ─────────────────────────────────────────────────────
    public static final Color TEXT_MAIN  = new Color(226, 232, 240);
    public static final Color TEXT_SUB   = new Color(160, 174, 192);
    public static final Color TEXT_MUTED = new Color(90,  100, 125);

    // ── Кнопки тиров — ИСПРАВЛЕНО: намного светлее, текст виден ──
    public static final Color BTN_T1     = new Color(34,  170,  95);   // tier 1 — яркий зелёный
    public static final Color BTN_T2     = new Color(37,  130, 210);   // tier 2 — яркий синий
    public static final Color BTN_T3     = new Color(140,  80, 230);   // tier 3 — яркий фиолетовый
    public static final Color BTN_INST   = new Color(49,  130, 206);
    public static final Color BTN_BUILD  = new Color(130,  90, 220);
    public static final Color BTN_HOVER  = new Color(255, 255, 255, 25);

    // ── Утилиты ───────────────────────────────────────────────────
    public static Color darken(Color c, float delta) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hsb[2] = Math.max(0, hsb[2] - delta);
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    public static Color lighten(Color c, float delta) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hsb[2] = Math.min(1, hsb[2] + delta);
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    public static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}