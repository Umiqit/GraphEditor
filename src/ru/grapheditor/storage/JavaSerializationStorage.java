package ru.grapheditor.storage;

import ru.grapheditor.shape.Drawing;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class JavaSerializationStorage implements DrawingStorage {

    @Override
    public String formatName() {
        return "Сериализация Java (*.ged)";
    }

    @Override
    public String extension() {
        return "ged";
    }

    @Override
    public void save(Drawing drawing, File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {
            out.writeObject(drawing);
        }
    }

    @Override
    public Drawing load(File file) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            Object obj = in.readObject();
            if (!(obj instanceof Drawing drawing)) {
                throw new IOException("Файл не содержит рисунок GraphEditor");
            }
            return drawing;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Файл повреждён или имеет неверный формат", e);
        }
    }
}
