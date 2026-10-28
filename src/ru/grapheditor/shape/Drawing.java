package ru.grapheditor.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Drawing implements Iterable<Shape> {

    private final List<Shape> shapes = new ArrayList<>();

    public void add(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Элемент не может быть null");
        }
        shapes.add(shape);
    }

    public boolean remove(Shape shape) {
        return shapes.remove(shape);
    }

    public void clear() {
        shapes.clear();
    }

    public int size() {
        return shapes.size();
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    public Shape findAt(int x, int y) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape s = shapes.get(i);
            if (s.contains(x, y)) {
                return s;
            }
        }
        return null;
    }

    public void drawAll(Graphics2D g) {
        for (Shape s : shapes) {
            s.draw(g);
        }
    }

    @Override
    public Iterator<Shape> iterator() {
        return shapes.iterator();
    }
}
