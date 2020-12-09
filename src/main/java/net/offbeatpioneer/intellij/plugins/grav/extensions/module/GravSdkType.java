package net.offbeatpioneer.intellij.plugins.grav.extensions.module;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.assets.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
        return GravIcons.GravDefaultIcon;
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

    public static String findGravSdkVersion(String directory) {
        VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(new File(directory));
        String version = "Version could not be determined.";
        if (VfsUtil.findRelativeFile(root, "system") != null) {
            VirtualFile system = VfsUtil.findRelativeFile(root, "system");
            try {
                VirtualFile child = system.findChild("defines.php");
                String content = getFileContent(child.getPath(), Charset.defaultCharset());
                String pattern = "define\\(\\s*'GRAV_VERSION'\\s*,\\s*'(.+)'\\s*\\)";
                Pattern p = Pattern.compile(pattern, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(content);
                if (m.find()) {
                    version = m.group(1);
                }
            } catch (NullPointerException | IOException | IllegalStateException | PatternSyntaxException | IndexOutOfBoundsException ignored) {
            }
        }
        return version;
    }

    private static String getFileContent(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
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
