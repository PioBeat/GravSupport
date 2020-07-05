package net.offbeatpioneer.intellij.plugins.grav.editor;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * edited source from https://stackoverflow.com/questions/5673430/java-jtable-change-cell-color
 */
public class ValueEnteredTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        // Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        //Get the status for the current row.
        TranslationTableModel tableModel = (TranslationTableModel) table.getModel();
        if (col != 0 && tableModel.getStatus(row, col) == TranslationTableModel.EMPTY) {
            l.setBackground(new JBColor(new Color(244, 128, 36, 60), new Color(244, 128, 36)));
        } else {
            if(isSelected) {
                l.setForeground(EditorColorsManager.getInstance().getSchemeForCurrentUITheme().getDefaultForeground());
            }
            l.setBackground(EditorColorsManager.getInstance().getSchemeForCurrentUITheme().getDefaultBackground());
        }

        return l;
    }
}