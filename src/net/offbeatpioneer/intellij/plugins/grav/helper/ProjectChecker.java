package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.Objects;

/**
 * @author Dominik Grzelak
 * @since 16.07.2017.
 */
public class ProjectChecker {

    public static Project getFirstOpenedProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        return projects.length > 0 ? projects[0] : null;
    }

    public static boolean checkProject(Project project) {
        if (Objects.isNull(project.getBasePath())) {
            return false;
        }
        VirtualFile projectPath = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
        if (VfsUtil.findRelativeFile(projectPath, "src", "user") != null
                && VfsUtil.findRelativeFile(projectPath, "src", "bin") != null
                && VfsUtil.findRelativeFile(projectPath, "src", "vendor") != null
        ) {
            return true;
        }
        return false;
    }
}
