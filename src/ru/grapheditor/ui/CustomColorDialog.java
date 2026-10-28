package ru.grapheditor.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

/** Диалог своего цвета: RGB-слайдеры + предпросмотр, как «Изменить цвета» в Paint. */
public class CustomColorDialog extends JDialog {

    private final JSlider rSlider = slider();
    private final JSlider gSlider = slider();
    private final JSlider bSlider = slider();
    private final JSpinner rSpinner = spinner();
    private final JSpinner gSpinner = spinner();
    private final JSpinner bSpinner = spinner();
    private final JPanel preview = new JPanel();

    private Color result;
    private boolean confirmed;

    public CustomColorDialog(Window owner, Color initial) {
        super(owner, "Свой цвет", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        rSlider.setValue(initial.getRed());
        gSlider.setValue(initial.getGreen());
        bSlider.setValue(initial.getBlue());
        rSpinner.setValue(initial.getRed());
        gSpinner.setValue(initial.getGreen());
        bSpinner.setValue(initial.getBlue());

        JPanel grid = new JPanel(new GridLayout(3, 3, 6, 6));
        grid.add(new JLabel("Красный:"));
        grid.add(rSlider);
        grid.add(rSpinner);
        grid.add(new JLabel("Зелёный:"));
        grid.add(gSlider);
        grid.add(gSpinner);
        grid.add(new JLabel("Синий:"));
        grid.add(bSlider);
        grid.add(bSpinner);
        add(grid, BorderLayout.CENTER);

        preview.setPreferredSize(new java.awt.Dimension(80, 60));
        add(preview, BorderLayout.EAST);

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Отмена");
        ok.addActionListener(e -> {
            confirmed = true;
            result = currentColor();
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(ok);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        sync();

        JSlider[] sliders = {rSlider, gSlider, bSlider};
        JSpinner[] spinners = {rSpinner, gSpinner, bSpinner};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            sliders[i].addChangeListener(e -> {
                if (((Number) spinners[idx].getValue()).intValue() != sliders[idx].getValue()) {
                    spinners[idx].setValue(sliders[idx].getValue());
                }
                sync();
            });
            spinners[i].addChangeListener(e -> {
                int v = ((Number) spinners[idx].getValue()).intValue();
                if (sliders[idx].getValue() != v) {
                    sliders[idx].setValue(v);
                }
                sync();
            });
        }

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static JSlider slider() {
        JSlider s = new JSlider(0, 255);
        s.setPaintTicks(true);
        s.setMajorTickSpacing(51);
        return s;
    }

    private static JSpinner spinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    }

    private Color currentColor() {
        return new Color(rSlider.getValue(), gSlider.getValue(), bSlider.getValue());
    }

    private void sync() {
        preview.setBackground(currentColor());
        preview.setToolTipText(String.format("RGB %d, %d, %d",
                rSlider.getValue(), gSlider.getValue(), bSlider.getValue()));
    }

    public static Color pick(Window owner, Color initial) {
        CustomColorDialog dialog = new CustomColorDialog(owner, initial);
        dialog.setVisible(true);
        return dialog.confirmed ? dialog.result : null;
    }
}
