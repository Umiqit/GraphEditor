package ru.grapheditor.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Drawing implements Iterable<Shape>, java.io.Serializable {

    private final List<Shape> shapes = new ArrayList<>();

    public void add(Shape shape) {
        if (shape == null) {
            throw new IllegalArgumentException("Элемент не может быть null");
        }
        shapes.add(shape);
    }

    public void add(int index, Shape shape) {
        shapes.add(index, shape);
    }

    public void addAll(Drawing other) {
        for (Shape s : other) {
            add(s);
        }
    }

    public int indexOf(Shape shape) {
        return shapes.indexOf(shape);
    }

    public List<Shape> snapshot() {
        return new ArrayList<>(shapes);
    }

    public void restore(List<Shape> snapshot) {
        shapes.clear();
        shapes.addAll(snapshot);
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
