package ru.grapheditor;

import java.awt.Color;
import java.io.Serializable;

public class Settings implements Serializable {

    private Color backgroundColor = Color.WHITE;
    private float defaultBorderWidth = 2f;
    private int defaultShadowOffsetX = 6;
    private int defaultShadowOffsetY = 6;

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public float getDefaultBorderWidth() {
        return defaultBorderWidth;
    }

    public void setDefaultBorderWidth(float defaultBorderWidth) {
        this.defaultBorderWidth = defaultBorderWidth;
    }

    public int getDefaultShadowOffsetX() {
        return defaultShadowOffsetX;
    }

    public void setDefaultShadowOffsetX(int defaultShadowOffsetX) {
        this.defaultShadowOffsetX = defaultShadowOffsetX;
    }

    public int getDefaultShadowOffsetY() {
        return defaultShadowOffsetY;
    }

    public void setDefaultShadowOffsetY(int defaultShadowOffsetY) {
        this.defaultShadowOffsetY = defaultShadowOffsetY;
    }
}
