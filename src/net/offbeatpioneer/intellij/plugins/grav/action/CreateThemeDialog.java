package net.offbeatpioneer.intellij.plugins.grav.action;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.*;

public class CreateThemeDialog extends JDialog implements CaretListener {
    private JPanel mainPanel;
    public JTextField textField1;
    public JTextField textField2;
    public JTextField textField3;
    public JTextField textField4;

    private NewThemeData themeData = new NewThemeData();

    public CreateThemeDialog() {
        setContentPane(mainPanel);
        setModal(true);

        textField1.addCaretListener(this);
        textField2.addCaretListener(this);
        textField3.addCaretListener(this);
        textField4.addCaretListener(this);
    }

    public static void main(String[] args) {
        CreateThemeDialog dialog = new CreateThemeDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (e.getSource() == textField1) {
            themeData.setName(textField1.getText());
        }
        if (e.getSource() == textField2) {
            themeData.setDescription(textField2.getText());
        }
        if (e.getSource() == textField3) {
            themeData.setDeveloper(textField3.getText());
        }
        if (e.getSource() == textField4) {
            themeData.setEmail(textField4.getText());
        }
    }

    public NewThemeData getThemeData() {
        return themeData;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
