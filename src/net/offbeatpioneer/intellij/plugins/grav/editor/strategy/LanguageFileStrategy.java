package net.offbeatpioneer.intellij.plugins.grav.editor.strategy;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.editor.TranslationTableModel;

import java.awt.event.ActionEvent;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageFileStrategy extends FileEditorStrategy {


    public LanguageFileStrategy(String[] languages, Project project) {
        super(languages, project);
    }

    @Override
    public TranslationTableModel createTableModel(ConcurrentHashMap<String, VirtualFile> fileMap) {

        return new TranslationTableModel(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
