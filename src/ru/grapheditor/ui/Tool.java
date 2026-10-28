package ru.grapheditor.ui;

public enum Tool {
    POINT("Точка"),
    CIRCLE("Круг"),
    SQUARE("Квадрат");

    private final String label;

    Tool(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
