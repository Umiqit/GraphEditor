package ru.grapheditor.command;

public interface Command {
    void execute();

    void undo();
}
