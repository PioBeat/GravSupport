package net.offbeatpioneer.intellij.plugins.grav.helper;

import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GravYAMLUtils {

    // put this in some class
    public static String[] splitKey(String key) {
        return key.split("\\.");
    }

    public static String prettyPrint(YAMLSequence yamlSequence) {
        StringBuilder builder = new StringBuilder("[");
        for (YAMLSequenceItem each : yamlSequence.getItems()) {
            builder.append(each.getText()).append(",");
        }
        builder.deleteCharAt(builder.lastIndexOf(",")).append("]");
        return builder.toString();
    }
}
