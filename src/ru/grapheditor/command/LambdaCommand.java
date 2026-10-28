package ru.grapheditor.command;

public class LambdaCommand implements Command {

    private final Runnable redo;
    private final Runnable undo;

    public LambdaCommand(Runnable redo, Runnable undo) {
        this.redo = redo;
        this.undo = undo;
    }

    @Override
    public void execute() {
        redo.run();
    }

    @Override
    public void undo() {
        undo.run();
    }
}
