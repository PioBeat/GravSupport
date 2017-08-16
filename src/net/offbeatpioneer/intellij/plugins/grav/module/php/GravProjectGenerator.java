package net.offbeatpioneer.intellij.plugins.grav.module.php;

import com.intellij.ide.util.projectWizard.EmptyWebProjectTemplate;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.lang.javascript.boilerplate.AbstractGithubTagDownloadedProjectGenerator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.templates.github.GithubTagInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Dome on 11.08.2017.
 */
public class GravProjectGenerator extends EmptyWebProjectTemplate {

    @Override
    public String getDescription() {
        return "Create a Grav project";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Grav";
    }

//    @Override
//    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Object settings, @NotNull Module module) {
//        System.out.println("generateProject");
//    }
}
