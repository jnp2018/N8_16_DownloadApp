package com.downloadTool.app;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProgressRenderer extends JProgressBar implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value, boolean b, boolean b1, int row, int col) {
        setValue((int) ((Float) value).floatValue());
        return this;
    }

    public ProgressRenderer(int min, int max) {
        super(min, max);
    }
}
