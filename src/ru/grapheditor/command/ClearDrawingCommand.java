package ru.grapheditor.command;

import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.Shape;

import java.util.List;

public class ClearDrawingCommand implements Command {

    private final Drawing drawing;
    private List<Shape> snapshot;

    public ClearDrawingCommand(Drawing drawing) {
        this.drawing = drawing;
    }

    @Override
    public void execute() {
        snapshot = drawing.snapshot();
        drawing.clear();
    }

    @Override
    public void undo() {
        drawing.restore(snapshot);
    }
}
