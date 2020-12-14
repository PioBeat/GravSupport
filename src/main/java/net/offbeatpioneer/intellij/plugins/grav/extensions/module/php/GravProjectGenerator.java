package net.offbeatpioneer.intellij.plugins.grav.extensions.module.php;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.util.PlatformUtils;
import net.offbeatpioneer.intellij.plugins.grav.extensions.icons.GravIcons;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleBuilder;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravProjectGeneratorUtil;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravPersistentStateComponent;
import net.offbeatpioneer.intellij.plugins.grav.storage.GravProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

import static net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard.CreateGravProjectWizardStep.LAST_USED_GRAV_HOME;

/**
 * Directory project generator for PhpStorm.
 * <p>
 * The module type is set to {@link GravModuleType}, as done in IU (automatically).
 *
 * @author Dominik Grzelak
 * @since 2017-08-11
 */
public class GravProjectGenerator extends WebProjectTemplate<GravProjectSettings> { //projecttemplate

    private GravProjectPeer generatorPeer;
    private final GravPersistentStateComponent storage;

    GravProjectGenerator() {
        this.storage = GravPersistentStateComponent.getInstance();
    }

    @Override
    public @NotNull ModuleBuilder createModuleBuilder() {
        return new GravModuleBuilder(); //super.createModuleBuilder();
    }

    @Override
    public String getDescription() {
        return "Create a new Grav project. A PHP interpreter is required.";
    }

    @Override
    public Icon getIcon() {
        return GravIcons.GravDefaultIcon;
    }

    @Override
    public Icon getLogo() {
        return GravIcons.GravDefaultIcon;
    }

    @Override
    public @Nullable String getHelpId() {
        return "grav help id";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Grav";
    }

    @NotNull
    @Override
    public ProjectGeneratorPeer<GravProjectSettings> createPeer() {
        if (generatorPeer == null)
            generatorPeer = new GravProjectPeer();
        return generatorPeer;
    }

    //after create button was pressed
    // the parent directory with the project name is automatically created
    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull GravProjectSettings settings, @NotNull Module module) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(settings.gravInstallationPath));
        if (vf == null || !GravSdkType.isValidGravSDK(vf)) {
            NotificationHelper.showErrorNotification(project, "Project '" + project.getName() + "' couldn't be created because the selected Grav download seems invalid");
        } else {
            module.setModuleType(createModuleBuilder().getModuleType().getId());
            storage.setDefaultGravDownloadPath(settings.gravInstallationPath);
            PropertiesComponent.getInstance().setValue(LAST_USED_GRAV_HOME, new File(settings.gravInstallationPath).getAbsolutePath());
            GravProjectGeneratorUtil projectGenerator = new GravProjectGeneratorUtil(project);
            projectGenerator.generateProject(project, baseDir, settings, module);
        }
    }

    @Override
    public boolean postponeValidation() {
        return false; //super.postponeValidation();
    }

    @Override
    public @Nullable ValidationInfo validateSettings() {
        return generatorPeer.validate();
    }

    public boolean isPrimaryGenerator() {
        return PlatformUtils.isPhpStorm();
    }
}
