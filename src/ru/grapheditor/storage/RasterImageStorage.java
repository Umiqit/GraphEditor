package ru.grapheditor.storage;

import ru.grapheditor.pixel.Circle;
import ru.grapheditor.pixel.Point;
import ru.grapheditor.pixel.Square;
import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.Polygon;
import ru.grapheditor.shape.Shape;
import ru.grapheditor.shape.ShapeDecorator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class RasterImageStorage implements DrawingStorage {

    protected abstract byte[] embed(byte[] image, byte[] payload) throws IOException;

    protected abstract byte[] extract(byte[] image) throws IOException;

    @Override
    public void save(Drawing drawing, File file) throws IOException {
        BufferedImage img = render(drawing);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String format = extension().equals("png") ? "png" : "jpeg";
        if (!ImageIO.write(img, format, out)) {
            throw new IOException("Формат " + format + " не поддерживается");
        }
        byte[] payload = new TextStorage().export(drawing).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(file.toPath(), embed(out.toByteArray(), payload));
    }

    @Override
    public Drawing load(File file) throws IOException {
        byte[] payload = extract(Files.readAllBytes(file.toPath()));
        if (payload == null) {
            throw new IOException("В файле нет метаданных GraphEditor");
        }
        return new TextStorage().parse(new String(payload, java.nio.charset.StandardCharsets.UTF_8));
    }

    private BufferedImage render(Drawing drawing) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Shape s : drawing) {
            Shape base = unwrap(s);
            if (base instanceof Point p) {
                minX = Math.min(minX, p.getX() - 8); maxX = Math.max(maxX, p.getX() + 8);
                minY = Math.min(minY, p.getY() - 8); maxY = Math.max(maxY, p.getY() + 8);
            } else if (base instanceof Circle c) {
                minX = Math.min(minX, c.getX() - c.getRadius()); maxX = Math.max(maxX, c.getX() + c.getRadius());
                minY = Math.min(minY, c.getY() - c.getRadius()); maxY = Math.max(maxY, c.getY() + c.getRadius());
            } else if (base instanceof Square q) {
                minX = Math.min(minX, q.getX() - q.getSide() / 2); maxX = Math.max(maxX, q.getX() + q.getSide() / 2);
                minY = Math.min(minY, q.getY() - q.getSide() / 2); maxY = Math.max(maxY, q.getY() + q.getSide() / 2);
            } else if (base instanceof Polygon poly) {
                for (int[] v : poly.getVertices()) {
                    minX = Math.min(minX, v[0]); maxX = Math.max(maxX, v[0]);
                    minY = Math.min(minY, v[1]); maxY = Math.max(maxY, v[1]);
                }
            }
        }
        if (minX == Integer.MAX_VALUE) {
            minX = minY = 0; maxX = 100; maxY = 80;
        }
        int pad = 40;
        int w = Math.max(400, maxX - minX + 2 * pad);
        int h = Math.max(300, maxY - minY + 2 * pad);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.translate(pad - minX, pad - minY);
        drawing.drawAll(g);
        g.dispose();
        return img;
    }

    private Shape unwrap(Shape s) {
        while (s instanceof ShapeDecorator d) {
            s = d.getInner();
        }
        return s;
    }
}
