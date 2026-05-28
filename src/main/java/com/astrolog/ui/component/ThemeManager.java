package com.astrolog.ui.component;

import com.astrolog.config.ThemeConfig;
import com.astrolog.config.ThemeConfig.Theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme;
    private final List<ThemeObserver> observers = new ArrayList<>();

    private ThemeManager() {
        currentTheme = ThemeConfig.DARK;
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setTheme(Theme theme) {
        if (theme != null && theme != currentTheme) {
            currentTheme = theme;
            notifyObservers();
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void register(ThemeObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregister(ThemeObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (ThemeObserver observer : observers) {
            observer.onThemeChanged(currentTheme);
        }
    }
}
