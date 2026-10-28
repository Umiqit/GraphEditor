package ru.grapheditor;

import ru.grapheditor.pixel.Circle;
import ru.grapheditor.pixel.Pixel;
import ru.grapheditor.pixel.PixelFactory;
import ru.grapheditor.pixel.PixelType;
import ru.grapheditor.pixel.Point;
import ru.grapheditor.pixel.Square;
import ru.grapheditor.shape.BorderDecorator;
import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.FillDecorator;
import ru.grapheditor.shape.Polygon;
import ru.grapheditor.shape.ShadowDecorator;
import ru.grapheditor.shape.Shape;
import ru.grapheditor.shape.ShapeDecorator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Тестовые наборы инкрементов 1 и 2 (T1–T15, T2-01–T2-13). */
public final class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Инкремент 1 ===");
        increment1();
        System.out.println("=== Инкремент 2 ===");
        increment2();
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

    private static void increment2() {
        Polygon tri = new Polygon(0, 0, 10, 0, 5, 10);
        check("T2-01: полигон создан, вершин=3", tri.getVertexCount() == 3
                && tri.getVertices().get(2)[0] == 5 && tri.getVertices().get(2)[1] == 10);
        check("T2-01a: меньше 3 вершин -> исключение",
                expectIllegalArg(() -> new Polygon(0, 0, 10, 10)));

        check("T2-02: полигон отрисовывается", drawsCleanly(tri));

        ru.grapheditor.ui.ToolSelector selector = new ru.grapheditor.ui.ToolSelector();
        selector.setCurrent(ru.grapheditor.ui.Tool.POLYGON);
        check("T2-03: инструмент полигона", selector.getCurrent() == ru.grapheditor.ui.Tool.POLYGON);

        Shape filled = new FillDecorator(tri, Color.YELLOW);
        check("T2-04: заливка", ((FillDecorator) filled).getFillColor() == Color.YELLOW
                && drawsCleanly(filled));

        Shape bordered = new BorderDecorator(tri, Color.RED, 3f);
        check("T2-05: контур", ((BorderDecorator) bordered).getWidth() == 3f
                && drawsCleanly(bordered));

        Shape shadowed = new ShadowDecorator(tri, 5, 5, Color.GRAY);
        check("T2-06: тень", ((ShadowDecorator) shadowed).getOffsetX() == 5
                && drawsCleanly(shadowed));

        Shape combo = new ShadowDecorator(
                new BorderDecorator(
                        new FillDecorator(tri, Color.YELLOW), Color.BLACK, 2f),
                6, 6, Color.DARK_GRAY);
        check("T2-07: все три свойства одновременно", drawsCleanly(combo));

        boolean combosOk = drawsCleanly(new FillDecorator(new BorderDecorator(tri, Color.RED, 1f), Color.BLUE))
                && drawsCleanly(new BorderDecorator(new ShadowDecorator(tri, 2, 2, Color.GRAY), Color.GREEN, 4f))
                && drawsCleanly(new FillDecorator(new ShadowDecorator(tri, 3, 3, Color.BLACK), Color.WHITE))
                && drawsCleanly(new FillDecorator(new BorderDecorator(new ShadowDecorator(tri, 1, 1, Color.GRAY), Color.BLACK, 1f), Color.CYAN));
        check("T2-08: допустимые комбинации корректны", combosOk);

        Shape chain = new ShadowDecorator(
                new BorderDecorator(
                        new FillDecorator(tri, Color.ORANGE), Color.BLACK, 1f),
                4, 4, Color.GRAY);
        check("T2-09: цепочка декораторов",
                ((ShadowDecorator) chain).getInner() instanceof BorderDecorator
                        && ((BorderDecorator) ((ShadowDecorator) chain).getInner()).getInner() instanceof FillDecorator
                        && ((ShapeDecorator) ((BorderDecorator) ((ShadowDecorator) chain).getInner()).getInner()).getInner() == tri);

        Shape transparent = new TransparencyDecorator(tri, 0.5f);
        check("T2-10: новый декоратор без изменения Polygon",
                drawsCleanly(transparent) && tri.getVertexCount() == 3);

        Drawing model = new Drawing();
        int[] pts = {0, 0, 50, 0, 25, 40};
        model.add(new Polygon(pts));
        check("T2-11: полигон в модели", model.size() == 1
                && model.findAt(25, 10) != null);

        model.add(new BorderDecorator(new FillDecorator(new Polygon(100, 100, 150, 100, 125, 160), Color.PINK), Color.BLACK, 2f));
        check("T2-12: оформленная фигура отображается", model.size() == 2);

        Shape altOrder = new FillDecorator(new ShadowDecorator(tri, 5, 5, Color.GRAY), Color.YELLOW);
        check("T2-13: порядок декораторов меняется", drawsCleanly(altOrder));

        Drawing outline = new Drawing();
        outline.add(new BorderDecorator(new Polygon(10, 10, 110, 10, 60, 90), Color.BLACK, 2f));
        check("Хит-тест: клик по линии контура", outline.findAt(60, 10) != null);
        check("Хит-тест: клик внутри полигона", outline.findAt(60, 40) != null);
        check("Хит-тест: клик мимо полигона", outline.findAt(200, 200) == null);
        Shape toErase = outline.findAt(60, 40);
        outline.remove(toErase);
        check("Ластик: фигура удалена", outline.isEmpty());

        Polygon mv = new Polygon(0, 0, 10, 0, 5, 10);
        mv.move(5, 7);
        check("Move: полигон сдвинут",
                mv.getVertices().get(0)[0] == 5 && mv.getVertices().get(0)[1] == 7);
        Shape mvDecorated = new FillDecorator(mv, Color.YELLOW);
        mvDecorated.move(1, 1);
        check("Move: декоратор передаёт сдвиг внутрь",
                mv.getVertices().get(0)[0] == 6 && mv.getVertices().get(0)[1] == 8);

        Drawing mixed = new Drawing();
        mixed.add(PixelFactory.create(PixelType.POINT, 1, 1, Color.BLACK));
        mixed.add(new Polygon(0, 0, 20, 0, 10, 20));
        check("Регрессия: растровый + векторный в одном рисунке", mixed.size() == 2);
        mixed.clear();
        check("Регрессия: очистка удаляет всё (T2-18)", mixed.isEmpty());
    }

    private static class TransparencyDecorator extends ShapeDecorator {
        private final float alpha;

        TransparencyDecorator(Shape inner, float alpha) {
            super(inner);
            this.alpha = alpha;
        }

        @Override
        public void draw(java.awt.Graphics2D g) {
            java.awt.Composite old = g.getComposite();
            g.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, alpha));
            inner.draw(g);
            g.setComposite(old);
        }
    }

    private static boolean drawsCleanly(Shape shape) {
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(200, 200, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        try {
            shape.draw(img.createGraphics());
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
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
