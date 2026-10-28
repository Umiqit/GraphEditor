package ru.grapheditor.pixel;

import java.awt.Color;

public final class PixelFactory {

    public static final int DEFAULT_SIZE = 12;

    private PixelFactory() {
    }

    public static Pixel create(PixelType type, int x, int y, Color color) {
        if (type == null) {
            throw new IllegalArgumentException("Тип элемента не задан");
        }
        return switch (type) {
            case POINT -> new Point(x, y, color);
            case CIRCLE -> new Circle(x, y, DEFAULT_SIZE, color);
            case SQUARE -> new Square(x, y, DEFAULT_SIZE, color);
        };
    }
}
