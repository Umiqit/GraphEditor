package ru.grapheditor.storage;

import java.util.List;

public final class Storages {

    public static final List<DrawingStorage> ALL = List.of(
            new JavaSerializationStorage(), new TextStorage());

    private Storages() {
    }

    public static DrawingStorage byExtension(String extension) {
        for (DrawingStorage s : ALL) {
            if (s.extension().equalsIgnoreCase(extension)) {
                return s;
            }
        }
        return null;
    }

    public static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1);
    }
}
