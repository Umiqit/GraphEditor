package ru.grapheditor.storage;

import ru.grapheditor.pixel.Circle;
import ru.grapheditor.pixel.Pixel;
import ru.grapheditor.pixel.Point;
import ru.grapheditor.pixel.Square;
import ru.grapheditor.shape.BorderDecorator;
import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.FillDecorator;
import ru.grapheditor.shape.Polygon;
import ru.grapheditor.shape.Shape;
import ru.grapheditor.shape.ShapeDecorator;
import ru.grapheditor.shape.ShadowDecorator;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TextStorage implements DrawingStorage {

    private static final String HEADER = "GEDTXT1";

    @Override
    public String formatName() {
        return "Текстовый (*.txt)";
    }

    @Override
    public String extension() {
        return "txt";
    }

    @Override
    public void save(Drawing drawing, File file) throws IOException {
        Files.writeString(file.toPath(), export(drawing), StandardCharsets.UTF_8);
    }

    @Override
    public Drawing load(File file) throws IOException {
        return parse(Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    public String export(Drawing drawing) throws IOException {
        StringBuilder sb = new StringBuilder(HEADER).append('\n');
        for (Shape s : drawing) {
            sb.append(encode(s)).append('\n');
        }
        return sb.toString();
    }

    public Drawing parse(String text) throws IOException {
        String[] lines = text.split("\n", -1);
        if (lines.length == 0 || !HEADER.equals(lines[0].trim())) {
            throw new IOException("Файл не является рисунком GraphEditor");
        }
        Drawing drawing = new Drawing();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                try {
                    drawing.add(decode(line, i + 1));
                } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                    throw new IOException("Ошибка в строке " + (i + 1), e);
                }
            }
        }
        return drawing;
    }

    private static String encode(Shape s) {
        if (s instanceof Point p) {
            return "P POINT " + p.getX() + " " + p.getY() + " " + rgb(p.getColor());
        }
        if (s instanceof Circle c) {
            return "P CIRCLE " + c.getX() + " " + c.getY() + " " + c.getRadius() + " " + rgb(c.getColor());
        }
        if (s instanceof Square q) {
            return "P SQUARE " + q.getX() + " " + q.getY() + " " + q.getSide() + " " + rgb(q.getColor());
        }
        if (s instanceof ShapeDecorator) {
            List<ShapeDecorator> decorations = new ArrayList<>();
            Shape base = s;
            while (base instanceof ShapeDecorator d) {
                decorations.add(d);
                base = d.getInner();
            }
            if (!(base instanceof Polygon p)) {
                throw new IllegalArgumentException("Неподдерживаемый элемент: " + s.getClass());
            }
            StringBuilder sb = new StringBuilder("G ").append(p.getVertexCount());
            for (int[] v : p.getVertices()) {
                sb.append(" ").append(v[0]).append(" ").append(v[1]);
            }
            for (ShapeDecorator d : decorations) {
                if (d instanceof FillDecorator f) {
                    sb.append(" F ").append(rgb(f.getFillColor()));
                } else if (d instanceof BorderDecorator b) {
                    sb.append(" B ").append(rgb(b.getBorderColor())).append(" ").append(b.getWidth());
                } else if (d instanceof ShadowDecorator sh) {
                    sb.append(" S ").append(sh.getOffsetX()).append(" ").append(sh.getOffsetY())
                            .append(" ").append(rgb(sh.getShadowColor()));
                }
            }
            return sb.toString();
        }
        throw new IllegalArgumentException("Неподдерживаемый элемент: " + s.getClass());
    }

    private static Shape decode(String line, int lineNo) throws IOException {
        String[] t = line.split("\\s+");
        if (t[0].equals("P")) {
            return decodePixel(t);
        }
        if (t[0].equals("G")) {
            return decodePolygon(t);
        }
        throw new IOException("Ошибка в строке " + lineNo + ": неизвестный элемент");
    }

    private static Pixel decodePixel(String[] t) {
        int x = Integer.parseInt(t[2]);
        int y = Integer.parseInt(t[3]);
        return switch (t[1]) {
            case "POINT" -> new Point(x, y, color(t, 4));
            case "CIRCLE" -> new Circle(x, y, Integer.parseInt(t[4]), color(t, 5));
            case "SQUARE" -> new Square(x, y, Integer.parseInt(t[4]), color(t, 5));
            default -> throw new NumberFormatException("неизвестный тип " + t[1]);
        };
    }

    private static Shape decodePolygon(String[] t) {
        int n = Integer.parseInt(t[1]);
        int[] pts = new int[n * 2];
        for (int i = 0; i < n * 2; i++) {
            pts[i] = Integer.parseInt(t[2 + i]);
        }
        Shape shape = new Polygon(pts);
        int i = 2 + n * 2;
        List<String[]> decorations = new ArrayList<>();
        while (i < t.length) {
            switch (t[i]) {
                case "F" -> {
                    decorations.add(new String[]{t[i], t[i + 1], t[i + 2], t[i + 3]});
                    i += 4;
                }
                case "B" -> {
                    decorations.add(new String[]{t[i], t[i + 1], t[i + 2], t[i + 3], t[i + 4]});
                    i += 5;
                }
                case "S" -> {
                    decorations.add(new String[]{t[i], t[i + 1], t[i + 2], t[i + 3], t[i + 4], t[i + 5]});
                    i += 6;
                }
                default -> throw new NumberFormatException("неизвестный декоратор " + t[i]);
            }
        }
        for (int k = decorations.size() - 1; k >= 0; k--) {
            String[] d = decorations.get(k);
            shape = switch (d[0]) {
                case "F" -> new FillDecorator(shape, color(d, 1));
                case "B" -> new BorderDecorator(shape, color(d, 1), Float.parseFloat(d[4]));
                case "S" -> new ShadowDecorator(shape, Integer.parseInt(d[1]),
                        Integer.parseInt(d[2]), color(d, 3));
                default -> shape;
            };
        }
        return shape;
    }

    private static String rgb(Color c) {
        return c.getRed() + " " + c.getGreen() + " " + c.getBlue();
    }

    private static Color color(String[] t, int offset) {
        return new Color(Integer.parseInt(t[offset]),
                Integer.parseInt(t[offset + 1]), Integer.parseInt(t[offset + 2]));
    }
}
