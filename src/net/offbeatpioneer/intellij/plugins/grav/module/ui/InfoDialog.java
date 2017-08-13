package net.offbeatpioneer.intellij.plugins.grav.module.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dome on 15.07.2017.
 */
public class InfoDialog extends DialogWrapper {

    public InfoDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
        setModal(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();

        panel.add(new JLabel("Please wait"));
        return panel;
    }
}
