package net.offbeatpioneer.intellij.plugins.grav.action;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.*;

public class CreateThemeDialog extends JDialog implements CaretListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    public static final int CANCELED = -1;
    public static final int OK = 0;

    public int EXIT_CODE = CANCELED;

    private NewThemeData themeData = new NewThemeData();

    public CreateThemeDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        textField1.addCaretListener(this);
        textField2.addCaretListener(this);
        textField3.addCaretListener(this);
        textField4.addCaretListener(this);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        boolean valid = validateData();
        if (valid) {
            EXIT_CODE = OK;
            // add your code here
            dispose();
        } else {

        }
    }


    boolean validateData() {
        //ToDo check if theme exists
        return true;
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public NewThemeData showDialog() {
        this.pack();
        this.setVisible(true);
        return themeData;
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
}
