package net.offbeatpioneer.intellij.plugins.grav.editor;


import net.offbeatpioneer.intellij.plugins.grav.helper.GravYAMLUtils;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.*;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationTableModel extends AbstractTableModel {
    public static final int EMPTY = 0;
    public static final int OK = 1;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> data;
    private ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap;
    private String[] languages;
    private Collection<String> availableKeys;
    private ArrayList<String> columnNames = new ArrayList<>();
    int rowCount = 0;

    public TranslationTableModel(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> data) {
        this.data = data;
    }

    public TranslationTableModel(String[] languages, Collection<String> availableKeys, ConcurrentHashMap<String, Collection<YAMLKeyValue>> dataMap) {
        this(new ConcurrentHashMap<>());
        this.languages = languages;
        this.availableKeys = availableKeys;
        this.dataMap = dataMap;
        columnNames.add("Key");
        columnNames.addAll(Arrays.asList(languages));

        for (Map.Entry<String, Collection<YAMLKeyValue>> each : this.dataMap.entrySet()) {
            data.put(each.getKey(), new ConcurrentHashMap<>());
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
        dataMap.get(language).add(value);
    }

    public void addElement(String lang, YAMLKeyValue value) {
        addElement0(lang, value);
        availableKeys.add(value.getKeyText());
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
        //TODO für alle anderen sprachen dann leer setzen
        for (String each : languages) {
            if (!each.equalsIgnoreCase(lang)) {
                data.get(each).put(key, "");
            }
        }
        fireTableDataChanged();
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
        if (languages == null || languages.length == 0) {
            return null;
        }
        List<String> keys = new ArrayList<>(availableKeys);
        Collections.sort(keys);
        switch (columnIndex) {
            case 0:
                return keys.get(rowIndex);
            default:
                Collection<YAMLKeyValue> collection = dataMap.get(languages[columnIndex - 1]);
                YAMLKeyValue correctKeyValue = findByKey(keys.get(rowIndex), collection);
                if (collection == null || collection.size() == 0) {
                    return "";
                }
                YAMLFile yamlFile;
                if (correctKeyValue == null) { //may be null because keys can be also written with dots and the above function couldn't find it
                    yamlFile = (YAMLFile) new ArrayList<YAMLKeyValue>(collection).get(0).getContainingFile();
                } else {
                    yamlFile = (YAMLFile) correctKeyValue.getContainingFile();
                }
                String[] keySplitted = GravYAMLUtils.splitKey(keys.get(rowIndex));
                YAMLKeyValue keyValue = YAMLUtil.getQualifiedKeyInFile(
                        yamlFile,
                        Arrays.asList(keySplitted));
                if (keyValue == null) {
                    //additional search because intellij yaml support doesn't know about keys with dot notation ...
                    //so try to find it in the previous collected list

                    return correctKeyValue == null ? "" : correctKeyValue.getValueText();
                }
                return keyValue.getValueText();
//                return data.get(languages[columnIndex - 1]).get();
        }
    }

    YAMLKeyValue findByKey(String key, Collection<YAMLKeyValue> valueCollection) {
        if(valueCollection == null) return null;
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
        List<String> langs = new ArrayList<>(Arrays.asList(languages));
        langs.add(lang);
        languages = langs.toArray(new String[langs.size()]);
        columnNames.add(lang);
        data.put(lang, new ConcurrentHashMap<>());
        fireTableStructureChanged();
    }

    public void removeLanguage(String lang) {
        List<String> langs = new ArrayList<>(Arrays.asList(languages));
        langs.remove(lang);
        languages = langs.toArray(new String[langs.size()]);
        columnNames.remove(lang);
        data.remove(lang);
        fireTableStructureChanged();
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String[] getLanguages() {
        return languages;
    }

    public Collection<String> getAvailableKeys() {
        return availableKeys;
    }
}
