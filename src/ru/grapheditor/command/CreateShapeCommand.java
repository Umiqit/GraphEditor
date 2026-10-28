package ru.grapheditor.command;

import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.Shape;

public class CreateShapeCommand implements Command {

    private final Drawing drawing;
    private final Shape shape;

    public CreateShapeCommand(Drawing drawing, Shape shape) {
        this.drawing = drawing;
        this.shape = shape;
    }

    @Override
    public void execute() {
        drawing.add(shape);
    }

    @Override
    public void undo() {
        drawing.remove(shape);
    }
}
