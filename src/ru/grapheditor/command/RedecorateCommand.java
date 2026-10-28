package ru.grapheditor.command;

import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.Shape;

public class RedecorateCommand implements Command {

    private final Drawing drawing;
    private final Shape before;
    private final Shape after;

    public RedecorateCommand(Drawing drawing, Shape before, Shape after) {
        this.drawing = drawing;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() {
        drawing.remove(before);
        drawing.add(after);
    }

    @Override
    public void undo() {
        drawing.remove(after);
        drawing.add(before);
    }
}
