package net.offbeatpioneer.intellij.plugins.grav.helper;

import org.jetbrains.yaml.psi.*;

public class GravYAMLUtils {

    // put this in some class
    public static String[] splitKey(String key) {
        return key.split("\\.");
    }

    public static String prettyPrint(YAMLSequence yamlSequence) {
        StringBuilder builder = new StringBuilder("[");
        if(yamlSequence.getItems().size() != 0) {
            for (YAMLSequenceItem each : yamlSequence.getItems()) {
                builder.append(each.getText()).append(",");
            }
            int ix = builder.lastIndexOf(",");
            if (ix != -1)
                builder.deleteCharAt(ix);
        }
        builder.append("]");
        return builder.toString();
    }
}
