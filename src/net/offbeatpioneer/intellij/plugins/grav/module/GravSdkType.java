package net.offbeatpioneer.intellij.plugins.grav.module;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Dome on 16.07.2017.
 */
public class GravSdkType extends SdkType {
    public GravSdkType() {
        super("Grav");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return System.getenv().get("GRAV_HOME");
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return true;
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return "Grav SDK";
    }

    @Override
    public Icon getIcon() {
        return GravIcons.Grav;
    }

    @Override
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    public static SdkTypeId getInstance() {
        return SdkType.findInstance(GravSdkType.class);
    }

    //TODO
    @Nullable
    @Override
    public String getVersionString(Sdk sdk) {
        String path = sdk.getHomePath();
        if (path == null) {
            NotificationHelper.showBaloon("No home path specified", MessageType.ERROR, ProjectManager.getInstance().getDefaultProject());
            return null;
        }
//
//        File file = new File(path);
//        VirtualFile home = LocalFileSystem.getInstance().findFileByIoFile(file);
//        if (home != null) {
//            VirtualFile lib = home.findChild("lib");
//            if (lib != null) {
//                for (VirtualFile jar : lib.getChildren()) {
//                    String name = jar.getName();
//                    if (name.startsWith("grav-") && "jar".equalsIgnoreCase(jar.getExtension())) {
//                        name = name.substring(8);
//                        name = name.replace("-SNAPSHOT", "");
//                        name = name.replace(".jar", "");
//                        return name;
//                    }
//                }
//            }
//        }
        //TODO
        return "";
    }

    public static boolean isValidGravSDK(VirtualFile root) {
        return root.findChild("user") != null &&
                VfsUtil.findRelativeFile(root, "system") != null &&
                root.findFileByRelativePath("bin") != null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Grav SDK";
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

    }
}
