package net.offbeatpioneer.intellij.plugins.grav.helper;

import org.jetbrains.yaml.psi.*;

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
        builder.deleteCharAt(builder.lastIndexOf(",")).append("]"); //TODO bugfix
        return builder.toString();
    }
}
