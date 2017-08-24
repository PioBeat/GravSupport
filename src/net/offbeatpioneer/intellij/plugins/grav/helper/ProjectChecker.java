package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VfsUtil;

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
        if (VfsUtil.findRelativeFile(project.getBaseDir(), "src", "user") != null
                && VfsUtil.findRelativeFile(project.getBaseDir(), "src", "bin") != null
                && VfsUtil.findRelativeFile(project.getBaseDir(), "src", "vendor") != null
                ) {
            return true;
        }
        return false;
    }
}
