package net.offbeatpioneer.intellij.plugins.grav.editor.dialogs;

import javax.swing.*;
import java.awt.event.*;

public class InsertKeyValueDialogUI extends JDialog {
    private JPanel contentPane;
    private JTextField keyText;
    private JTextField valueText;

    public InsertKeyValueDialogUI() {
        setContentPane(contentPane);
    }

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    public String getKeyText() {
        return keyText.getText();
    }

    public String getValueText() {
        return valueText.getText();
    }

    public JTextField getKeyField() {
        return keyText;
    }

    public JTextField getValueField() {
        return valueText;
    }
}
