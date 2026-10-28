package ru.grapheditor.command;

import ru.grapheditor.shape.Shape;

public class MoveShapeCommand implements Command {

    private final Shape shape;
    private final int dx;
    private final int dy;

    public MoveShapeCommand(Shape shape, int dx, int dy) {
        this.shape = shape;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        shape.move(dx, dy);
    }

    @Override
    public void undo() {
        shape.move(-dx, -dy);
    }
}
