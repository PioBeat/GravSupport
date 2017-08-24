package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.util.ObjectUtils;
import org.jetbrains.yaml.psi.*;

import java.util.List;

public class GravYAMLUtils {

    // put this in some class
    public static String[] splitKey(String key) {
        return key.split("\\.");
    }

    public static String prettyPrint(YAMLSequence yamlSequence) {
        StringBuilder builder = new StringBuilder("[");
        if (yamlSequence.getItems().size() != 0) {
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

    public static YAMLKeyValue getKeyValue(YAMLFile yamlFile, List<String> key) {
        YAMLDocument document = (YAMLDocument) yamlFile.getDocuments().get(0);
        YAMLMapping mapping = (YAMLMapping) ObjectUtils.tryCast(document.getTopLevelValue(), YAMLMapping.class);
        for (int i = 0; i < key.size(); ++i) {
            if (mapping == null) {
                return null;
            }
            YAMLKeyValue keyValue = null;
            for (YAMLKeyValue each : mapping.getKeyValues()) {
                if (each.getKeyText().equals(key.get(i))) {
                    keyValue = each;
                    break;
                }
            }

            if (keyValue == null || i + 1 == key.size()) {
                return keyValue;
            }

            mapping = ObjectUtils.tryCast(keyValue.getValue(), YAMLMapping.class);
        }

        throw new IllegalStateException("Should have returned from the loop");
    }
}
