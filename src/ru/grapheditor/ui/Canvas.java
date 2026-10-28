package ru.grapheditor.ui;

import ru.grapheditor.command.ClearDrawingCommand;
import ru.grapheditor.command.CreateShapeCommand;
import ru.grapheditor.command.DeleteShapeCommand;
import ru.grapheditor.command.History;
import ru.grapheditor.command.MoveShapeCommand;
import ru.grapheditor.command.RedecorateCommand;
import ru.grapheditor.Settings;
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

    private static final int MIN_DRAG = 6;

    private static final int[][] HIT_OFFSETS = {
            {0, 0}, {5, 0}, {-5, 0}, {0, 5}, {0, -5},
            {4, 4}, {-4, 4}, {4, -4}, {-4, -4}
    };

    private final Drawing drawing;
    private final ToolSelector tools;
    private final DecorationOptions options;
    private final Settings settings;
    private final History history;

    private final List<int[]> draft = new ArrayList<>();
    private int[] dragStart;
    private int[] dragNow;
    private Shape selected;
    private Runnable changeListener;

    public Canvas(Drawing drawing, ToolSelector tools, DecorationOptions options,
                  Settings settings, History history) {
        this.drawing = drawing;
        this.tools = tools;
        this.options = options;
        this.settings = settings;
        this.history = history;
        setPreferredSize(new Dimension(800, 600));
        applySettings();
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public History getHistory() {
        return history;
    }

    public Shape getSelected() {
        return selected;
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void notifyChanged() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public void applySettings() {
        setBackground(settings.getBackgroundColor());
        repaint();
    }

    public void newDrawing() {
        drawing.clear();
        history.clear();
        draft.clear();
        selected = null;
        notifyChanged();
        repaint();
    }

    public void setDrawingContent(Drawing loaded) {
        drawing.clear();
        drawing.addAll(loaded);
        history.clear();
        draft.clear();
        selected = null;
        notifyChanged();
        repaint();
    }

    public void deleteSelected() {
        if (selected != null) {
            Shape target = selected;
            selected = null;
            history.execute(new DeleteShapeCommand(drawing, target));
            notifyChanged();
            repaint();
        }
    }

    public void undo() {
        history.undo();
        if (selected != null && drawing.indexOf(selected) < 0) {
            selected = null;
        }
        notifyChanged();
        repaint();
    }

    public void redo() {
        history.redo();
        notifyChanged();
        repaint();
    }

    public void clearDrawing() {
        history.execute(new ClearDrawingCommand(drawing));
        selected = null;
        notifyChanged();
        repaint();
    }

    private void addShape(Shape shape) {
        history.execute(new CreateShapeCommand(drawing, shape));
        notifyChanged();
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
        Shape before = selected;
        drawing.remove(before);
        drawing.add(redecorated);
        history.pushExecuted(new RedecorateCommand(drawing, before, redecorated));
        selected = redecorated;
        notifyChanged();
        repaint();
    }

    private void finishDraftPolygon() {
        if (draft.size() >= 3) {
            int[] pts = new int[draft.size() * 2];
            for (int i = 0; i < draft.size(); i++) {
                pts[i * 2] = draft.get(i)[0];
                pts[i * 2 + 1] = draft.get(i)[1];
            }
            addShape(decorate(new Polygon(pts)));
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
            drawDragPreview(g2);
            drawDraft(g2);
            drawSelection(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawDragPreview(Graphics2D g) {
        if (dragStart == null || dragNow == null) {
            return;
        }
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{4f, 4f}, 0f));
        g.setColor(tools.getColor());
        int[] box = dragBox();
        if (tools.getCurrent() == Tool.CIRCLE) {
            g.drawOval(box[0], box[1], box[2], box[3]);
        } else if (tools.getCurrent() == Tool.SQUARE) {
            g.drawRect(box[0], box[1], box[2], box[3]);
        }
        g.setStroke(old);
    }

    private int[] dragBox() {
        int x = Math.min(dragStart[0], dragNow[0]);
        int y = Math.min(dragStart[1], dragNow[1]);
        int w = Math.abs(dragNow[0] - dragStart[0]);
        int h = Math.abs(dragNow[1] - dragStart[1]);
        if (tools.getCurrent() == Tool.CIRCLE || tools.getCurrent() == Tool.SQUARE) {
            w = h = Math.max(w, h);
        }
        return new int[]{x, y, w, h};
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
            for (int[] v : p.getVertices()) {
                g.fillRect(v[0] - 3, v[1] - 3, 6, 6);
            }
        } else if (selected instanceof Pixel pix) {
            int half = Math.max(12, hitRadius(pix));
            g.drawRect(pix.getX() - half, pix.getY() - half, half * 2, half * 2);
        }
        g.setStroke(old);
    }

    private int hitRadius(Pixel pix) {
        if (pix instanceof ru.grapheditor.pixel.Circle c) {
            return c.getRadius();
        }
        if (pix instanceof ru.grapheditor.pixel.Square s) {
            return s.getSide() / 2;
        }
        return 6;
    }

    private Shape findTolerant(int x, int y) {
        for (int[] off : HIT_OFFSETS) {
            Shape hit = drawing.findAt(x + off[0], y + off[1]);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            switch (tools.getCurrent()) {
                case POINT -> stamp(PixelType.POINT, x, y);
                case CIRCLE, SQUARE -> {
                    dragStart = new int[]{x, y};
                    dragNow = new int[]{x, y};
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
                    Shape hit = findTolerant(x, y);
                    if (hit == null) {
                        selected = null;
                    } else if (hit == selected) {
                        dragStart = new int[]{x, y};
                        dragNow = new int[]{x, y};
                    } else {
                        selected = hit;
                    }
                    repaint();
                }
                case ERASER -> eraseAt(x, y);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            switch (tools.getCurrent()) {
                case POINT -> stamp(PixelType.POINT, e.getX(), e.getY());
                case ERASER -> eraseAt(e.getX(), e.getY());
                case CIRCLE, SQUARE -> {
                    if (dragStart != null) {
                        dragNow = new int[]{e.getX(), e.getY()};
                        repaint();
                    }
                }
                case SELECT -> {
                    if (dragStart != null && selected != null) {
                        selected.move(e.getX() - dragNow[0], e.getY() - dragNow[1]);
                        dragNow = new int[]{e.getX(), e.getY()};
                        repaint();
                    }
                }
                default -> {
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (tools.getCurrent() == Tool.SELECT) {
                if (dragStart != null && selected != null
                        && (dragNow[0] != dragStart[0] || dragNow[1] != dragStart[1])) {
                    history.pushExecuted(new MoveShapeCommand(selected,
                            dragNow[0] - dragStart[0], dragNow[1] - dragStart[1]));
                    notifyChanged();
                }
                dragStart = null;
                dragNow = null;
                return;
            }
            if (dragStart == null
                    || (tools.getCurrent() != Tool.CIRCLE && tools.getCurrent() != Tool.SQUARE)) {
                return;
            }
            dragNow = new int[]{e.getX(), e.getY()};
            int[] box = dragBox();
            if (box[2] < MIN_DRAG || box[3] < MIN_DRAG) {
                stamp(tools.getCurrent() == Tool.CIRCLE ? PixelType.CIRCLE : PixelType.SQUARE,
                        e.getX(), e.getY());
            } else if (tools.getCurrent() == Tool.CIRCLE) {
                addShape(new ru.grapheditor.pixel.Circle(
                        box[0] + box[2] / 2, box[1] + box[3] / 2, box[2] / 2, tools.getColor()));
            } else {
                addShape(new ru.grapheditor.pixel.Square(
                        box[0] + box[2] / 2, box[1] + box[3] / 2, box[2], tools.getColor()));
            }
            dragStart = null;
            dragNow = null;
            repaint();
        }

        private void stamp(PixelType type, int x, int y) {
            addShape(PixelFactory.create(type, x, y, tools.getColor()));
            repaint();
        }

        private void eraseAt(int x, int y) {
            Shape hit = findTolerant(x, y);
            if (hit != null) {
                if (hit == selected) {
                    selected = null;
                }
                history.execute(new DeleteShapeCommand(drawing, hit));
                notifyChanged();
                repaint();
            }
        }
    }
}
