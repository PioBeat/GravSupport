package net.offbeatpioneer.intellij.plugins.grav.editor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

public class BasicPopupListener implements PopupMenuListener {
    int colAtPoint = -1;
    private JTable table1;
    private JPopupMenu popupMenu;
    private JMenuItem deleteItem;
    private JMenuItem jumpToItem;
    int rowAtPoint = -1;
    int lastRowAtPoint = -1;

    public BasicPopupListener(JTable table1, JPopupMenu popupMenu, JMenuItem deleteItem, JMenuItem jumpToItem) {
        this.table1 = table1;
        this.popupMenu = popupMenu;
        this.deleteItem = deleteItem;
        this.jumpToItem = jumpToItem;
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        SwingUtilities.invokeLater(() -> {
            colAtPoint = table1.columnAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));
            rowAtPoint = table1.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table1));
            lastRowAtPoint = rowAtPoint;
            if (colAtPoint != 0)
                deleteItem.setEnabled(false);
            else {
                deleteItem.setEnabled(true);
            }

            if (colAtPoint == 0)
                jumpToItem.setEnabled(false);
            else {
                jumpToItem.setEnabled(true);
            }
        });
    }

    public void resetIndices() {
        rowAtPoint = -1;
        colAtPoint = -1;
    }

    public void resetLastIndices() {
        lastRowAtPoint = -1;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }
}
