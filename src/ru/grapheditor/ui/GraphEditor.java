package ru.grapheditor.ui;

import ru.grapheditor.shape.Drawing;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

public class GraphEditor extends JFrame {

    private final ToolSelector tools = new ToolSelector();
    private final Canvas canvas;

    public GraphEditor() {
        super("GraphEditor (инкремент 1)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new Canvas(new Drawing(), tools);
        add(canvas, BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createToolBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Инструменты"));

        ButtonGroup group = new ButtonGroup();
        for (Tool tool : Tool.values()) {
            JToggleButton button = new JToggleButton(tool.getLabel());
            button.setToolTipText("Рисование: " + tool.getLabel().toLowerCase());
            button.addActionListener(e -> tools.setCurrent(tool));
            group.add(button);
            panel.add(button);
            if (tool == tools.getCurrent()) {
                button.setSelected(true);
            }
        }

        JButton colorButton = new JButton("Цвет");
        colorButton.setToolTipText("Выбор цвета рисования");
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Цвет", tools.getColor());
            if (c != null) {
                tools.setColor(c);
            }
        });
        panel.add(colorButton);

        JButton clearButton = new JButton("Очистить");
        clearButton.setToolTipText("Удалить все элементы");
        clearButton.addActionListener(e -> canvas.clearDrawing());
        panel.add(clearButton);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphEditor().setVisible(true));
    }
}
