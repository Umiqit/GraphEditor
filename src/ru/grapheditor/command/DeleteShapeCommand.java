package ru.grapheditor.command;

import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.Shape;

public class DeleteShapeCommand implements Command {

    private final Drawing drawing;
    private final Shape shape;
    private int index = -1;

    public DeleteShapeCommand(Drawing drawing, Shape shape) {
        this.drawing = drawing;
        this.shape = shape;
    }

    @Override
    public void execute() {
        index = drawing.indexOf(shape);
        if (index >= 0) {
            drawing.remove(shape);
        }
    }

    @Override
    public void undo() {
        drawing.add(index >= 0 ? index : drawing.size(), shape);
    }
}
