package ru.grapheditor.ui;

import ru.grapheditor.pixel.Pixel;
import ru.grapheditor.pixel.PixelFactory;
import ru.grapheditor.pixel.PixelType;
import ru.grapheditor.shape.BorderDecorator;
import ru.grapheditor.shape.Drawing;
import ru.grapheditor.shape.FillDecorator;
import ru.grapheditor.shape.Polygon;
import ru.grapheditor.shape.Shape;
import ru.grapheditor.shape.ShapeDecorator;
import ru.grapheditor.shape.ShadowDecorator;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {

    private final Drawing drawing;
    private final ToolSelector tools;
    private final DecorationOptions options;

    private final List<int[]> draft = new ArrayList<>();
    private Shape selected;

    public Canvas(Drawing drawing, ToolSelector tools, DecorationOptions options) {
        this.drawing = drawing;
        this.tools = tools;
        this.options = options;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public Shape getSelected() {
        return selected;
    }

    public void clearDrawing() {
        drawing.clear();
        draft.clear();
        selected = null;
        repaint();
    }

    private Shape decorate(Polygon polygon) {
        Shape shape = polygon;
        if (options.isShadowEnabled()) {
            shape = new ShadowDecorator(shape, options.getShadowOffsetX(),
                    options.getShadowOffsetY(), options.getShadowColor());
        }
        if (options.isBorderEnabled()) {
            shape = new BorderDecorator(shape, options.getBorderColor(), options.getBorderWidth());
        }
        if (options.isFillEnabled()) {
            shape = new FillDecorator(shape, options.getFillColor());
        }
        return shape;
    }

    public void redecorateSelected() {
        if (selected == null) {
            return;
        }
        Shape base = selected;
        while (base instanceof ShapeDecorator d) {
            base = d.getInner();
        }
        if (!(base instanceof Polygon polygon)) {
            return;
        }
        Shape redecorated = decorate(polygon);
        drawing.remove(selected);
        drawing.add(redecorated);
        selected = redecorated;
        repaint();
    }

    private void finishDraftPolygon() {
        if (draft.size() >= 3) {
            int[] pts = new int[draft.size() * 2];
            for (int i = 0; i < draft.size(); i++) {
                pts[i * 2] = draft.get(i)[0];
                pts[i * 2 + 1] = draft.get(i)[1];
            }
            drawing.add(decorate(new Polygon(pts)));
        }
        draft.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            drawing.drawAll(g2);
            drawDraft(g2);
            drawSelection(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawDraft(Graphics2D g) {
        if (draft.isEmpty()) {
            return;
        }
        g.setColor(tools.getColor());
        int n = draft.size();
        for (int i = 0; i < n; i++) {
            int[] v = draft.get(i);
            g.fillOval(v[0] - 3, v[1] - 3, 6, 6);
            if (i > 0) {
                int[] u = draft.get(i - 1);
                g.drawLine(u[0], u[1], v[0], v[1]);
            }
        }
    }

    private void drawSelection(Graphics2D g) {
        if (selected == null) {
            return;
        }
        Shape base = selected;
        while (base instanceof ShapeDecorator d) {
            base = d.getInner();
        }
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5f, 4f}, 0f));
        g.setColor(new Color(0, 120, 255));
        if (base instanceof Polygon p) {
            g.drawPolygon(p.xArray(), p.yArray(), p.getVertexCount());
        } else if (selected instanceof Pixel pix) {
            g.drawRect(pix.getX() - 12, pix.getY() - 12, 24, 24);
        }
        g.setStroke(old);
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            switch (tools.getCurrent()) {
                case POINT, CIRCLE, SQUARE -> {
                    PixelType type = switch (tools.getCurrent()) {
                        case CIRCLE -> PixelType.CIRCLE;
                        case SQUARE -> PixelType.SQUARE;
                        default -> PixelType.POINT;
                    };
                    drawing.add(PixelFactory.create(type, x, y, tools.getColor()));
                    repaint();
                }
                case POLYGON -> {
                    if (e.getClickCount() >= 2 && draft.size() >= 3) {
                        finishDraftPolygon();
                    } else {
                        draft.add(new int[]{x, y});
                        repaint();
                    }
                }
                case SELECT -> {
                    selected = drawing.findAt(x, y);
                    repaint();
                }
                case ERASER -> {
                    Shape hit = drawing.findAt(x, y);
                    if (hit != null) {
                        if (hit == selected) {
                            selected = null;
                        }
                        drawing.remove(hit);
                        repaint();
                    }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (tools.getCurrent() == Tool.POINT || tools.getCurrent() == Tool.CIRCLE
                    || tools.getCurrent() == Tool.SQUARE) {
                drawing.add(PixelFactory.create(
                        tools.getCurrent() == Tool.CIRCLE ? PixelType.CIRCLE
                                : tools.getCurrent() == Tool.SQUARE ? PixelType.SQUARE : PixelType.POINT,
                        e.getX(), e.getY(), tools.getColor()));
                repaint();
            }
        }
    }
}
