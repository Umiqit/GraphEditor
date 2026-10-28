package ru.grapheditor;

import ru.grapheditor.command.ClearDrawingCommand;
import ru.grapheditor.command.CreateShapeCommand;
import ru.grapheditor.command.DeleteShapeCommand;
import ru.grapheditor.command.History;
import ru.grapheditor.command.LambdaCommand;
import ru.grapheditor.command.MoveShapeCommand;
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
import ru.grapheditor.storage.DrawingStorage;
import ru.grapheditor.storage.Storages;
import ru.grapheditor.ui.Canvas;
import ru.grapheditor.ui.DecorationOptions;
import ru.grapheditor.ui.Tool;
import ru.grapheditor.ui.ToolSelector;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        System.out.println("=== Инкремент 3 ===");
        increment3();
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

    private static void increment3() {
        commandTests();
        settingsTests();
        storageTests();
        interactionTests();
    }

    private static void commandTests() {
        Drawing d = new Drawing();
        History h = new History();
        Square shape = new Square(10, 10, 20, Color.RED);
        h.execute(new CreateShapeCommand(d, shape));
        check("Команды: создание", d.size() == 1 && h.canUndo());
        h.undo();
        check("Команды: отмена создания", d.isEmpty() && h.canRedo());
        h.redo();
        check("Команды: повтор создания", d.size() == 1 && shape == d.iterator().next());
        h.execute(new MoveShapeCommand(shape, 5, -3));
        check("Команды: сдвиг", shape.getX() == 15 && shape.getY() == 7);
        h.undo();
        check("Команды: отмена сдвига", shape.getX() == 10 && shape.getY() == 10);
        h.execute(new DeleteShapeCommand(d, shape));
        check("Команды: удаление", d.isEmpty());
        h.undo();
        check("Команды: отмена удаления", d.size() == 1);
        h.execute(new ClearDrawingCommand(d));
        check("Команды: очистка", d.isEmpty());
        h.undo();
        check("Команды: отмена очистки", d.size() == 1);
        h.execute(new LambdaCommand(() -> shape.move(1, 1), () -> shape.move(-1, -1)));
        check("Команды: lambda", shape.getX() == 11);
        h.undo();
        check("Команды: отмена lambda", shape.getX() == 10);
    }

    private static void settingsTests() {
        Settings s = new Settings();
        s.setBackgroundColor(Color.CYAN);
        s.setDefaultBorderWidth(4f);
        s.setDefaultShadowOffsetX(6);
        s.setDefaultShadowOffsetY(-2);
        DecorationOptions o = new DecorationOptions(s);
        check("Настройки: фон применён", s.getBackgroundColor() == Color.CYAN);
        check("Настройки: контур из настроек", o.getBorderWidth() == 4f);
        check("Настройки: тень из настроек",
                o.getShadowOffsetX() == 6 && o.getShadowOffsetY() == -2);
        s.setBackgroundColor(Color.MAGENTA);
        History h = new History();
        Canvas canvas = new Canvas(new Drawing(), new ToolSelector(), o, s, h);
        canvas.applySettings();
        check("Настройки: canvas применяет фон",
                canvas.getBackground().equals(Color.MAGENTA));
    }

    private static Drawing sampleDrawing() {
        Drawing d = new Drawing();
        d.add(new Point(30, 40, Color.BLACK));
        d.add(new Circle(100, 100, 25, Color.BLUE));
        d.add(new Square(200, 150, 40, Color.GREEN));
        Shape poly = new Polygon(50, 200, 150, 200, 100, 260);
        poly = new BorderDecorator(poly, Color.RED, 2f);
        poly = new FillDecorator(poly, Color.YELLOW);
        d.add(poly);
        return d;
    }

    private static boolean sameContent(Drawing a, Drawing b) {
        List<Shape> la = toList(a);
        List<Shape> lb = toList(b);
        if (la.size() != lb.size()) {
            return false;
        }
        for (int i = 0; i < la.size(); i++) {
            if (!sameShape(la.get(i), lb.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<Shape> toList(Drawing d) {
        List<Shape> list = new ArrayList<>();
        for (Shape s : d) {
            list.add(s);
        }
        return list;
    }

    private static Shape base(Shape s) {
        while (s instanceof ShapeDecorator d) {
            s = d.getInner();
        }
        return s;
    }

    private static boolean sameShape(Shape a, Shape b) {
        Shape ba = base(a);
        Shape bb = base(b);
        if (ba.getClass() != bb.getClass()) {
            return false;
        }
        if (ba instanceof Point pa && bb instanceof Point pb) {
            return pa.getX() == pb.getX() && pa.getY() == pb.getY()
                    && pa.getColor().equals(pb.getColor());
        }
        if (ba instanceof Circle ca && bb instanceof Circle cb) {
            return ca.getX() == cb.getX() && ca.getY() == cb.getY()
                    && ca.getRadius() == cb.getRadius() && ca.getColor().equals(cb.getColor());
        }
        if (ba instanceof Square qa && bb instanceof Square qb) {
            return qa.getX() == qb.getX() && qa.getY() == qb.getY()
                    && qa.getSide() == qb.getSide() && qa.getColor().equals(qb.getColor());
        }
        if (ba instanceof Polygon pa && bb instanceof Polygon pb) {
            if (pa.getVertexCount() != pb.getVertexCount()) {
                return false;
            }
            List<int[]> va = pa.getVertices();
            List<int[]> vb = pb.getVertices();
            for (int i = 0; i < va.size(); i++) {
                if (va.get(i)[0] != vb.get(i)[0] || va.get(i)[1] != vb.get(i)[1]) {
                    return false;
                }
            }
            return decorationsEqual(a, b);
        }
        return false;
    }

    private static boolean decorationsEqual(Shape a, Shape b) {
        List<String> da = decoCodes(a);
        List<String> db = decoCodes(b);
        return da.equals(db);
    }

    private static List<String> decoCodes(Shape s) {
        List<String> codes = new ArrayList<>();
        while (s instanceof ShapeDecorator d) {
            if (s instanceof FillDecorator f) {
                codes.add("F:" + f.getFillColor().getRGB());
            } else if (s instanceof BorderDecorator br) {
                codes.add("B:" + br.getBorderColor().getRGB() + ":" + br.getWidth());
            } else if (s instanceof ShadowDecorator sh) {
                codes.add("S:" + sh.getOffsetX() + ":" + sh.getOffsetY()
                        + ":" + sh.getShadowColor().getRGB());
            }
            s = d.getInner();
        }
        return codes;
    }

    private static void storageTests() {
        Drawing original = sampleDrawing();
        for (DrawingStorage storage : Storages.ALL) {
            File tmp = null;
            try {
                tmp = File.createTempFile("ge-test-", "." + storage.extension());
                storage.save(original, tmp);
                Drawing loaded = storage.load(tmp);
                check("Хранилище " + storage.extension() + ": roundtrip",
                        sameContent(original, loaded));
                byte[] raw = Files.readAllBytes(tmp.toPath());
                check("Хранилище " + storage.extension() + ": файл не пуст",
                        raw.length > 20);
            } catch (IOException ex) {
                check("Хранилище " + storage.extension() + ": roundtrip", false);
            } finally {
                if (tmp != null) {
                    tmp.delete();
                }
            }
        }
        DrawingStorage txt = Storages.byExtension("txt");
        try {
            File bad = File.createTempFile("ge-bad-", ".txt");
            Files.writeString(bad.toPath(), "не рисунок");
            txt.load(bad);
            check("Хранилище txt: порченый файл -> IOException", false);
            bad.delete();
        } catch (IOException ex) {
            check("Хранилище txt: порченый файл -> IOException", true);
        }
        DrawingStorage png = Storages.byExtension("png");
        try {
            File plain = File.createTempFile("ge-plain-", ".png");
            java.awt.image.BufferedImage img =
                    new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_RGB);
            javax.imageio.ImageIO.write(img, "png", plain);
            png.load(plain);
            check("PNG без метаданных -> IOException", false);
            plain.delete();
        } catch (IOException ex) {
            check("PNG без метаданных -> IOException", true);
        }
    }

    private static void interactionTests() {
        ToolSelector tools = new ToolSelector();
        DecorationOptions options = new DecorationOptions();
        History history = new History();
        Canvas canvas = new Canvas(new Drawing(), tools, options, new Settings(), history);

        Circle circle = new Circle(100, 100, 30, Color.RED);
        canvas.getDrawing().add(circle);
        tools.setCurrent(Tool.SELECT);
        press(canvas, 100, 100);
        check("draw.io: круг выбран кликом", canvas.getSelected() == circle);

        dragTo(canvas, 130, 130);
        release(canvas, 130, 130);
        check("draw.io: круг перетащен",
                circle.getX() == 130 && circle.getY() == 130);
        history.undo();
        check("draw.io: отмена перетаскивания",
                circle.getX() == 100 && circle.getY() == 100);

        press(canvas, 100 - 30, 100 - 30);
        dragTo(canvas, 150, 150);
        release(canvas, 150, 150);
        check("draw.io: ресайз круга за уголок", circle.getRadius() == 50);
        history.undo();
        check("draw.io: отмена ресайза", circle.getRadius() == 30);

        Polygon poly = new Polygon(300, 300, 400, 300, 350, 380);
        canvas.getDrawing().add(poly);
        press(canvas, 350, 380);
        check("draw.io: полигон выбран, вершина взята за ручку",
                canvas.getSelected() == poly);
        dragTo(canvas, 350, 395);
        release(canvas, 350, 395);
        List<int[]> vs = poly.getVertices();
        check("draw.io: вершина полигона перетащена",
                vs.get(2)[0] == 350 && vs.get(2)[1] == 395);
        history.undo();
        vs = poly.getVertices();
        check("draw.io: отмена перетаскивания вершины",
                vs.get(2)[0] == 350 && vs.get(2)[1] == 380);

        tools.setCurrent(Tool.ERASER);
        press(canvas, 350, 340);
        check("draw.io: ластик удаляет фигуру под курсором",
                canvas.getDrawing().indexOf(poly) < 0);

        polygonFinishTests();
    }

    private static void polygonFinishTests() {
        ToolSelector tools = new ToolSelector();
        History history = new History();
        Canvas canvas = new Canvas(new Drawing(), tools, new DecorationOptions(),
                new Settings(), history);
        tools.setCurrent(Tool.POLYGON);
        canvas.setDecorationChoice(0);
        press(canvas, 50, 50);
        press(canvas, 150, 50);
        press(canvas, 100, 140);
        press(canvas, 51, 51);
        check("Полигон: клик по первой вершине замыкает фигуру",
                canvas.getDrawing().size() == 1);
        Shape withFill = canvas.getDrawing().snapshot().get(0);
        check("Полигон: выбрана заливка", withFill instanceof FillDecorator);
        Shape base = withFill;
        while (base instanceof ShapeDecorator d) {
            base = d.getInner();
        }
        check("Полигон: 3 вершины без лишней точки",
                base instanceof Polygon && ((Polygon) base).getVertexCount() == 3);

        canvas.setDecorationChoice(1);
        press(canvas, 200, 200);
        press(canvas, 300, 200);
        press(canvas, 250, 290);
        press(canvas, 200, 200);
        Shape outlined = canvas.getDrawing().snapshot().get(1);
        check("Полигон: выбран только контур",
                outlined instanceof BorderDecorator && !(outlined instanceof FillDecorator));
        history.undo();
        check("Полигон: отмена создания убирает фигуру",
                canvas.getDrawing().size() == 1);

        Polygon tri = new Polygon(50, 50, 150, 50, 100, 140);
        Shape chain = new BorderDecorator(tri, Color.BLACK, 2f);
        chain = new FillDecorator(chain, Color.RED);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                200, 200, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        chain.draw(g);
        g.dispose();
        check("Полигон: заливка видна через цепочку декораторов",
                img.getRGB(100, 80) == Color.RED.getRGB());
        check("Полигон: фон вне фигуры не закрашен",
                img.getRGB(10, 180) == Color.WHITE.getRGB());
    }

    private static void press(Canvas c, int x, int y) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1));
    }

    private static void dragTo(Canvas c, int x, int y) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(), 0, x, y, 0, false, MouseEvent.NOBUTTON));
    }

    private static void release(Canvas c, int x, int y) {
        c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1));
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
