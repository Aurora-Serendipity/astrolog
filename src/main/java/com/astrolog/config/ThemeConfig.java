package com.astrolog.config;

import java.awt.Color;
import java.awt.Font;

public final class ThemeConfig {
    private ThemeConfig() {}

    public record Theme(Color bg, Color fg, Color panelBg, Color buttonBg,
                        Color accent, Color tableBg, Font font) {}

    private static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 18);

    public static final Theme LIGHT = new Theme(
        Color.WHITE,
        Color.BLACK,
        new Color(240, 240, 240),
        new Color(220, 220, 220),
        new Color(0, 102, 204),
        new Color(250, 250, 250),
        DEFAULT_FONT
    );

    public static final Theme DARK = new Theme(
        new Color(43, 43, 43),
        new Color(220, 220, 220),
        new Color(60, 63, 65),
        new Color(80, 80, 80),
        new Color(75, 110, 175),
        new Color(50, 50, 50),
        DEFAULT_FONT
    );

    public static final Theme STARRY = new Theme(
        new Color(10, 10, 40),
        new Color(200, 210, 240),
        new Color(20, 20, 60),
        new Color(30, 30, 80),
        new Color(180, 150, 80),
        new Color(15, 15, 50),
        DEFAULT_FONT
    );
}
