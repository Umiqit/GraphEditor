package ru.grapheditor.shape;

public abstract class ShapeDecorator implements Shape {

    protected final Shape inner;

    protected ShapeDecorator(Shape inner) {
        if (inner == null) {
            throw new IllegalArgumentException("Декорируемый объект не может быть null");
        }
        this.inner = inner;
    }

    public Shape getInner() {
        return inner;
    }

    @Override
    public boolean contains(int x, int y) {
        return inner.contains(x, y);
    }

    @Override
    public void move(int dx, int dy) {
        inner.move(dx, dy);
    }
}
