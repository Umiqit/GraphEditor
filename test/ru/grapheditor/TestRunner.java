package ru.grapheditor;

import ru.grapheditor.pixel.Circle;
import ru.grapheditor.pixel.Pixel;
import ru.grapheditor.pixel.PixelFactory;
import ru.grapheditor.pixel.PixelType;
import ru.grapheditor.pixel.Point;
import ru.grapheditor.pixel.Square;
import ru.grapheditor.shape.Drawing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Тестовый набор инкремента 1 (T1–T15). */
public final class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Инкремент 1 ===");
        increment1();
        System.out.println();
        System.out.println("ИТОГО: пройдено " + passed + ", провалено " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void increment1() {
        Point p = new Point(5, 10, Color.RED);
        check("T1: координаты точки", p.getX() == 5 && p.getY() == 10
                && p.getColor() == Color.RED);

        Circle c = new Circle(3, 4, 7, Color.BLUE);
        check("T2: параметры круга", c.getX() == 3 && c.getY() == 4 && c.getRadius() == 7);

        Square s = new Square(1, 2, 9, Color.GREEN);
        check("T3: параметры квадрата", s.getX() == 1 && s.getY() == 2 && s.getSide() == 9);

        List<Pixel> pixels = new ArrayList<>();
        pixels.add(new Point(0, 0, Color.BLACK));
        pixels.add(new Circle(1, 1, 5, Color.BLACK));
        pixels.add(new Square(2, 2, 6, Color.BLACK));
        check("T4: полиморфизм List<Pixel>",
                pixels.stream().allMatch(Pixel.class::isInstance) && pixels.size() == 3);

        check("T5: фабрика POINT -> Point",
                PixelFactory.create(PixelType.POINT, 1, 2, Color.BLACK) instanceof Point);
        check("T6: фабрика CIRCLE -> Circle",
                PixelFactory.create(PixelType.CIRCLE, 1, 2, Color.BLACK) instanceof Circle);
        check("T7: фабрика SQUARE -> Square",
                PixelFactory.create(PixelType.SQUARE, 1, 2, Color.BLACK) instanceof Square);

        check("T8: null-тип -> IllegalArgumentException", expectIllegalArg(() ->
                PixelFactory.create(null, 0, 0, Color.BLACK)));

        Drawing d = new Drawing();
        d.add(new Point(0, 0, Color.BLACK));
        check("T9: элемент присутствует", d.size() == 1);
        d.add(new Circle(5, 5, 3, Color.BLACK));
        d.add(new Square(7, 7, 4, Color.BLACK));
        check("T10: размер корректен", d.size() == 3);
        d.clear();
        check("T11: коллекция пуста", d.isEmpty());

        Point origin = new Point(0, 0, Color.BLACK);
        check("T12: координаты (0,0)", origin.getX() == 0 && origin.getY() == 0);

        Point neg = new Point(-10, -20, Color.BLACK);
        check("T13: отрицательные координаты", neg.getX() == -10 && neg.getY() == -20);

        boolean ok;
        try {
            new Circle(0, 0, Integer.MAX_VALUE / 2, Color.BLACK);
            new Square(Integer.MIN_VALUE, Integer.MIN_VALUE, 1, Color.BLACK);
            ok = true;
        } catch (RuntimeException ex) {
            ok = false;
        }
        check("T14: граничные значения координат", ok);

        PixelType tool = PixelType.CIRCLE;
        Drawing model = new Drawing();
        model.add(PixelFactory.create(tool, 20, 30, Color.RED));
        check("T15: объект появился в модели", model.size() == 1
                && model.findAt(20, 30) != null);
    }

    private static boolean expectIllegalArg(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS " + name);
        } else {
            failed++;
            System.out.println("  FAIL " + name);
        }
    }
}
