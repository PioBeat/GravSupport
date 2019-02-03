package net.offbeatpioneer.intellij.plugins.grav.module.wizard;

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
