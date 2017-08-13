package net.offbeatpioneer.intellij.plugins.grav.helper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;

/**
 * Created by Dome on 16.07.2017.
 */
public class ProjectChecker {


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
