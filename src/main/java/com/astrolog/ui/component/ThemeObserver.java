package com.astrolog.ui.component;

import com.astrolog.config.ThemeConfig.Theme;

public interface ThemeObserver {
    void onThemeChanged(Theme newTheme);
}
