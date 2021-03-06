package net.offbeatpioneer.intellij.plugins.grav.editor;


import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationTableModel extends AbstractTableModel {
    static final int EMPTY = 0;
    private static final int OK = 1;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> data = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap;
    private List<String> languages;
    private Collection<String> availableKeys;
    private ArrayList<String> columnNames = new ArrayList<>();
    int rowCount = 0;
    //should a language yaml key be prefixed when the value is fetched?
    private Boolean prefixKey = false;

    public TranslationTableModel(Collection<String> availableKeys, ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap) {
        this.availableKeys = availableKeys;
        this.dataMap = dataMap;

        this.languages = Collections.list(dataMap.keys());
        this.columnNames.add("Key");
        this.columnNames.addAll(languages);

        for (Map.Entry<String, Collection<YAMLKeyValue>> each : this.dataMap.entrySet()) {
            this.data.put(each.getKey(), new ConcurrentHashMap<>());
            for (YAMLKeyValue eachYaml : each.getValue()) {
                addElement0(each.getKey(), eachYaml.getKeyText(), eachYaml.getValueText());
            }
        }
    }

    /**
     * Only adds a key value pair to data for a specific language
     *
     * @param lang
     * @param key
     * @param value
     */
    private void addElement0(String lang, String key, String value) {
        data.get(lang).put(key, value);
    }

    private void addElement0(String language, YAMLKeyValue value) {
        dataMap.computeIfAbsent(language, k -> new HashSet<>()).add(value);
    }

    public void addElement(String lang, YAMLKeyValue value) {
        this.addElement(lang, value, value.getKeyText());
    }

    /**
     * Updates the model
     * Possibility to add the name of the key by yourself. The models needs always the full qualified key name
     * of the <code>value</code> for the {@code availableKeys} list
     *
     * @param lang             language
     * @param value            value
     * @param fullQualifiedKey full qualified key name of {@code value}
     */
    public void addElement(String lang, YAMLKeyValue value, String fullQualifiedKey) {
        addElement0(lang, value);
        availableKeys.add(fullQualifiedKey);
    }

    public void removeElement(String lang, YAMLKeyValue value, String fullQualifiedKey) {
        data.get(lang).remove(value.getKeyText()); //, value);
        dataMap.get(lang).remove(value);
        availableKeys.remove(fullQualifiedKey);
    }

    public void fireChange() {
        fireTableDataChanged();
    }

    /**
     * Adds a key value pair for a specific langauge and inserts for all
     * other present language an empty value. Furthermore the availableKeys
     * are updated.
     *
     * @param lang
     * @param key
     * @param value
     */
    @Deprecated
    public void addElement(String lang, String key, String value) {
        addElement0(lang, key, value);
        availableKeys.add(key);
        for (String each : languages) {
            if (!each.equalsIgnoreCase(lang)) {
                data.get(each).put(key, "");
            }
        }
        fireTableDataChanged();
    }


    public String getKeyName(int row) {
        List<String> keys = new ArrayList<>(availableKeys);
        if (row < 0 || row >= keys.size()) return null;
        return keys.get(row);
    }

    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    @Override
    public int getRowCount() {
        return this.availableKeys.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (languages == null || languages.size() == 0) {
            return null;
        }
        List<String> keys = getKeys(true);
        switch (columnIndex) {
            case 0:
                return keys.get(rowIndex);
            default:
                Collection<YAMLKeyValue> collection = dataMap.get(languages.get(columnIndex - 1));
                YAMLKeyValue correctKeyValue = findByKey(keys.get(rowIndex), collection);
                if (collection == null || collection.size() == 0) {
                    return "";
                }
                YAMLFile yamlFile;
                if (correctKeyValue == null) { //may be null because keys can be also written with dots and the above function couldn't find it
                    yamlFile = (YAMLFile) new ArrayList<>(collection).get(0).getContainingFile();
                } else {
                    yamlFile = (YAMLFile) correctKeyValue.getContainingFile();
                }
                String keyValueText = prefixKey ? languages.get(columnIndex - 1) + "." + keys.get(rowIndex) : keys.get(rowIndex);
                String[] keySplitted = GravYAMLUtils.splitKey(keyValueText);
                YAMLKeyValue keyValue = null;
                try {
                    keyValue = YAMLUtil.getQualifiedKeyInFile(
                            yamlFile,
                            Arrays.asList(keySplitted));
                } catch (NullPointerException npe) {

                }
                if (keyValue == null && correctKeyValue == null) return "";
                if (keyValue == null) {
                    //additional search because intellij yaml support doesn't know about keys with dot notation ...
                    //so try to find it in the previous collected list
                    keyValue = correctKeyValue;
                }

                String valueText;
                if (keyValue.getValue() instanceof YAMLSequence) {
                    valueText = GravYAMLUtils.prettyPrint((YAMLSequence) keyValue.getValue());
                } else {
                    valueText = keyValue.getValueText();
                }

                return valueText;
        }
    }

    public List<String> getKeys(boolean sorted) {
        List<String> keys = new ArrayList<>(availableKeys);
        if (sorted) Collections.sort(keys);
        return keys;
    }

    YAMLKeyValue findByKey(String key, Collection<YAMLKeyValue> valueCollection) {
        if (valueCollection == null) return null;
        for (YAMLKeyValue each : valueCollection) {
            if (each.getKeyText().equalsIgnoreCase(key)) {
                return each;
            }
        }
        return null;
    }

    public String getValueOfYamlKeyValue(YAMLKeyValue keyValue, YAMLValue value) {
        if (!(value instanceof YAMLCompoundValue))
            return value.getText();
        if (value instanceof YAMLSequence) { //
            return ((YAMLSequence) value).getTextValue();
        }
        return getValueOfYamlKeyValue(keyValue, keyValue.getValue());
    }

    public int getStatus(int row, int col) {
        String value = (String) getValueAt(row, col);
        if (value == null || value.isEmpty())
            return EMPTY;
        return OK;
    }


    /**
     * Adds a new language and inserts an empty entry in data
     *
     * @param lang
     */
    public void addLanguage(String lang) {
        data.put(lang, new ConcurrentHashMap<>());
        languages.add(lang);
        columnNames.add(lang);
        fireTableStructureChanged();
    }

    public void removeLanguage(String lang) {
        languages.remove(lang);
        columnNames.remove(lang);
        data.remove(lang);
        fireTableStructureChanged();
    }

//    public void setLanguages(List< languages) {
//        this.languages = languages;
//    }

    public List<String> getLanguages() {
        return languages;
    }

    public Collection<String> getAvailableKeys() {
        return availableKeys;
    }

    public TranslationTableModel setPrefixKey(Boolean prefixKey) {
        this.prefixKey = prefixKey;
        return this;
    }
}
