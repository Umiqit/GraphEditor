package ru.grapheditor.ui;

import ru.grapheditor.command.History;
import ru.grapheditor.shape.Drawing;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class GraphEditor extends JFrame {

    private final ToolSelector tools = new ToolSelector();
    private final DecorationOptions options = new DecorationOptions();
    private final History history = new History();
    private final Canvas canvas;
    private final JLabel status = new JLabel("Инструмент: Точка");
    private final JMenuItem undoItem = new JMenuItem("Отменить");
    private final JMenuItem redoItem = new JMenuItem("Повторить");

    public GraphEditor() {
        super("GraphEditor (инкременты 1–3)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new Canvas(new Drawing(), tools, options, history);
        canvas.setChangeListener(this::updateState);
        add(canvas, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);
        add(status, BorderLayout.SOUTH);
        setJMenuBar(createMenuBar());
        bindKeys();

        updateState();
        pack();
        setLocationRelativeTo(null);
    }

    private void updateState() {
        status.setText("Инструмент: " + tools.getCurrent().getLabel()
                + "    |    Элементов: " + canvas.getDrawing().size()
                + "    |    Ctrl+Z отмена, Ctrl+Y повтор, Del удалить выбранное");
        undoItem.setEnabled(history.canUndo());
        redoItem.setEnabled(history.canRedo());
    }

    private javax.swing.JMenuBar createMenuBar() {
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        javax.swing.JMenu edit = new javax.swing.JMenu("Правка");
        undoItem.addActionListener(e -> canvas.undo());
        redoItem.addActionListener(e -> canvas.redo());
        javax.swing.JMenuItem deleteItem = new javax.swing.JMenuItem("Удалить выбранное");
        deleteItem.addActionListener(e -> canvas.deleteSelected());
        edit.add(undoItem);
        edit.add(redoItem);
        edit.add(deleteItem);
        bar.add(edit);
        return bar;
    }

    private void bindKeys() {
        javax.swing.InputMap im = canvas.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = canvas.getActionMap();
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "undo");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "redo");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
                java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK), "redo");
        im.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "delete");
        am.put("undo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                canvas.undo();
            }
        });
        am.put("redo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                canvas.redo();
            }
        });
        am.put("delete", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                canvas.deleteSelected();
            }
        });
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.add(createToolBar());
        bar.add(createDecorationBar());
        return bar;
    }

    private JPanel createToolBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Инструменты"));

        ButtonGroup group = new ButtonGroup();
        addToolButton(panel, group, Tool.POINT, "Точка: щёлкайте или ведите мышь, удерживая кнопку");
        addToolButton(panel, group, Tool.CIRCLE, "Круг: нажмите и протяните мышь, как в Paint");
        addToolButton(panel, group, Tool.SQUARE, "Квадрат: нажмите и протяните мышь, как в Paint");
        addToolButton(panel, group, Tool.POLYGON,
                "Полигон: щелчки задают вершины (минимум 3), двойной щелчок завершает");
        addToolButton(panel, group, Tool.SELECT,
                "Выбор: щёлкните по фигуре; повторный щелчок и перетаскивание двигают её");
        addToolButton(panel, group, Tool.ERASER, "Ластик: ведите мышью по фигурам, удерживая кнопку");

        panel.add(new JLabel("Цвет:"));
        ColorPalette penPalette = new ColorPalette(tools.getColor());
        penPalette.setToolTipText("Палитра: цвет рисования");
        penPalette.addColorListener(tools::setColor);
        panel.add(penPalette);

        JButton clearButton = new JButton("Очистить");
        clearButton.setToolTipText("Удалить все элементы с рабочей области");
        clearButton.addActionListener(e -> canvas.clearDrawing());
        panel.add(clearButton);

        return panel;
    }

    private void addToolButton(JPanel panel, ButtonGroup group, Tool tool, String tooltip) {
        JToggleButton button = new JToggleButton(tool.getLabel());
        button.setToolTipText(tooltip);
        button.addActionListener(e -> {
            tools.setCurrent(tool);
            updateState();
        });
        group.add(button);
        panel.add(button);
        if (tool == tools.getCurrent()) {
            button.setSelected(true);
        }
    }

    private JPanel createDecorationBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Оформление полигонов"));

        JCheckBox fill = new JCheckBox("Заливка", options.isFillEnabled());
        fill.setToolTipText("Закрасить внутреннюю область фигуры");
        ColorPalette fillPalette = new ColorPalette(options.getFillColor());
        fillPalette.setToolTipText("Цвет заливки");
        fillPalette.addColorListener(c -> {
            options.setFillColor(c);
            canvas.redecorateSelected();
        });
        fill.addActionListener(e -> {
            options.setFillEnabled(fill.isSelected());
            canvas.redecorateSelected();
        });

        JCheckBox border = new JCheckBox("Контур", options.isBorderEnabled());
        border.setToolTipText("Нарисовать границу фигуры");
        ColorPalette borderPalette = new ColorPalette(options.getBorderColor());
        borderPalette.setToolTipText("Цвет контура");
        borderPalette.addColorListener(c -> {
            options.setBorderColor(c);
            canvas.redecorateSelected();
        });
        JSpinner borderWidth = new JSpinner(new SpinnerNumberModel(
                (double) options.getBorderWidth(), 0.5, 20.0, 0.5));
        borderWidth.setPreferredSize(new Dimension(60, 24));
        borderWidth.setToolTipText("Толщина контура");
        borderWidth.addChangeListener(e -> {
            options.setBorderWidth(((Number) borderWidth.getValue()).floatValue());
            canvas.redecorateSelected();
        });
        border.addActionListener(e -> {
            options.setBorderEnabled(border.isSelected());
            canvas.redecorateSelected();
        });

        JCheckBox shadow = new JCheckBox("Тень", options.isShadowEnabled());
        shadow.setToolTipText("Добавить фигуре тень");
        ColorPalette shadowPalette = new ColorPalette(options.getShadowColor());
        shadowPalette.setToolTipText("Цвет тени");
        shadowPalette.addColorListener(c -> {
            options.setShadowColor(c);
            canvas.redecorateSelected();
        });
        shadow.addActionListener(e -> {
            options.setShadowEnabled(shadow.isSelected());
            canvas.redecorateSelected();
        });

        panel.add(fill);
        panel.add(fillPalette);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(border);
        panel.add(borderPalette);
        panel.add(new JLabel("толщина:"));
        panel.add(borderWidth);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(shadow);
        panel.add(shadowPalette);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphEditor().setVisible(true));
    }
}
