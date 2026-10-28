package ru.grapheditor.ui;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Палитра цветов как в Paint: сетка образцов, последний «+» открывает диалог своего цвета. */
public class ColorPalette extends JPanel {

    private static final Color[] COLORS = {
            Color.BLACK, new Color(0x80, 0x80, 0x80), new Color(0xC0, 0xC0, 0xC0), Color.WHITE,
            new Color(0x7F, 0x3F, 0x00), new Color(0xFF, 0x80, 0x00), new Color(0xFF, 0xFF, 0x00), new Color(0x00, 0x80, 0x00),
            new Color(0x00, 0xFF, 0x00), new Color(0x00, 0x80, 0x80), new Color(0x00, 0xFF, 0xFF), new Color(0x00, 0x00, 0x80),
            Color.BLUE, new Color(0x80, 0x00, 0x80), Color.MAGENTA, Color.RED,
            new Color(0x80, 0x00, 0x00), new Color(0xFF, 0x00, 0xFF), new Color(0xFF, 0xC0, 0xCB), new Color(0xFF, 0xA5, 0x00)
    };

    private final List<Consumer<Color>> listeners = new ArrayList<>();
    private Color selected;
    private final JButton customSwatch;

    public ColorPalette(Color initial) {
        super(new GridLayout(2, 11, 1, 1));
        setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (Color c : COLORS) {
            JToggleButton swatch = swatch(c, group);
            swatch.setToolTipText(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
            swatch.addActionListener(e -> select(c));
            if (c.equals(initial)) {
                swatch.setSelected(true);
                selected = c;
            }
        }
        customSwatch = new JButton("+");
        customSwatch.setPreferredSize(new Dimension(18, 18));
        customSwatch.setMargin(new java.awt.Insets(0, 0, 0, 0));
        customSwatch.setFocusPainted(false);
        customSwatch.setContentAreaFilled(true);
        customSwatch.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        customSwatch.setToolTipText("Свой цвет по RGB...");
        customSwatch.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            Color picked = CustomColorDialog.pick(owner, selected);
            if (picked != null) {
                customSwatch.setBackground(picked);
                select(picked);
            }
        });
        add(customSwatch);
        if (selected == null) {
            selected = COLORS[0];
            ((JToggleButton) getComponent(0)).setSelected(true);
        }
    }

    private JToggleButton swatch(Color c, ButtonGroup group) {
        JToggleButton b = new JToggleButton();
        b.setBackground(c);
        b.setPreferredSize(new Dimension(18, 18));
        b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        b.setFocusPainted(false);
        group.add(b);
        add(b);
        return b;
    }

    public Color getSelected() {
        return selected;
    }

    public void addColorListener(Consumer<Color> listener) {
        listeners.add(listener);
    }

    private void select(Color c) {
        selected = c;
        for (Consumer<Color> l : listeners) {
            l.accept(c);
        }
    }
}
