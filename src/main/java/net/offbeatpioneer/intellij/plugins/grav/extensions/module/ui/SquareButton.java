package net.offbeatpioneer.intellij.plugins.grav.extensions.module.ui;

import javax.swing.*;
import java.awt.*;

public class SquareButton extends JButton {

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = d.height = Math.max(d.width, d.height);
        return d;
    }
}
