package ru.grapheditor.ui;

import ru.grapheditor.pixel.Pixel;
import ru.grapheditor.pixel.PixelFactory;
import ru.grapheditor.pixel.PixelType;
import ru.grapheditor.shape.Drawing;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Canvas extends JPanel {

    private final Drawing drawing;
    private final ToolSelector tools;

    public Canvas(Drawing drawing, ToolSelector tools) {
        this.drawing = drawing;
        this.tools = tools;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public void clearDrawing() {
        drawing.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            drawing.drawAll(g2);
        } finally {
            g2.dispose();
        }
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            stampAt(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            stampAt(e.getX(), e.getY());
        }

        private void stampAt(int x, int y) {
            PixelType type = switch (tools.getCurrent()) {
                case CIRCLE -> PixelType.CIRCLE;
                case SQUARE -> PixelType.SQUARE;
                default -> PixelType.POINT;
            };
            Pixel pixel = PixelFactory.create(type, x, y, tools.getColor());
            drawing.add(pixel);
            repaint();
        }
    }
}
