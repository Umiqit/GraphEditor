package ru.grapheditor.ui;

import java.awt.Color;

public class ToolSelector {

    private Tool current = Tool.POINT;
    private Color color = Color.BLACK;

    public Tool getCurrent() {
        return current;
    }

    public void setCurrent(Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("Инструмент не задан");
        }
        this.current = tool;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Цвет не задан");
        }
        this.color = color;
    }
}
