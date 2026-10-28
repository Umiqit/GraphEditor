package ru.grapheditor.storage;

import ru.grapheditor.shape.Drawing;

import java.io.File;
import java.io.IOException;

public interface DrawingStorage {

    String formatName();

    String extension();

    void save(Drawing drawing, File file) throws IOException;

    Drawing load(File file) throws IOException;
}
