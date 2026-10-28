package ru.grapheditor.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class History {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void pushExecuted(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command c = undoStack.pop();
            c.undo();
            redoStack.push(c);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command c = redoStack.pop();
            c.execute();
            undoStack.push(c);
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
