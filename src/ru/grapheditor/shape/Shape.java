package ru.grapheditor.shape;

import java.awt.Graphics2D;

public interface Shape extends java.io.Serializable {
    void draw(Graphics2D g);

    boolean contains(int x, int y);

    void move(int dx, int dy);
}
