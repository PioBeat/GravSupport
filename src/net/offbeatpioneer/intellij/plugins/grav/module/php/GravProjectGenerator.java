package net.offbeatpioneer.intellij.plugins.grav.module.php;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dome on 11.08.2017.
 */
public class GravProjectGenerator extends WebProjectTemplate {
    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Grav";
    }

    @Nullable
    @Override
    public Icon getLogo() {
        return GravIcons.Grav;
    }

    @Override
    public String getDescription() {
        return "Grav Project description";
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull Object o, @NotNull Module module) {
        System.out.println("Generate Grav Project");
    }

    @NotNull
    @Override
    public GeneratorPeer createPeer() {
        return new GravInstallerGeneratorPeer();
    }
}
