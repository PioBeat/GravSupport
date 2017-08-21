package net.offbeatpioneer.intellij.plugins.grav.module.builder;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import net.offbeatpioneer.intellij.plugins.grav.helper.NotificationHelper;
import net.offbeatpioneer.intellij.plugins.grav.helper.ProcessUtils;
import net.offbeatpioneer.intellij.plugins.grav.module.GravModuleType;
import net.offbeatpioneer.intellij.plugins.grav.module.GravModuleWizardStep;
import net.offbeatpioneer.intellij.plugins.grav.module.wizard.CopyFileVisitor;
import net.offbeatpioneer.intellij.plugins.grav.module.wizard.GravIntroWizardStep;
import net.offbeatpioneer.intellij.plugins.grav.project.settings.GravProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.ide.projectView.actions.MarkRootActionBase.findContentEntry;

public class GravModuleBuilder extends ModuleBuilder implements ModuleBuilderListener {
    private Project project;

    private VirtualFile gravInstallPath;
    GravProjectSettings settings;
    boolean withSrcDirectory;

    public VirtualFile getGravInstallPath() {
        return gravInstallPath;
    }

    public void setGravInstallPath(VirtualFile gravInstallPath) {
        this.gravInstallPath = gravInstallPath;
    }

    public boolean isWithSrcDirectory() {
        return withSrcDirectory;
    }

    public void setWithSrcDirectory(boolean withSrcDirectory) {
        this.withSrcDirectory = withSrcDirectory;
    }

    public GravModuleBuilder() {
        addListener(this);
    }

    @Override
    public String getBuilderId() {
        return "grav-builder";
    }

    @Override
    public String getPresentableName() {
        return "Grav";
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        setProject(rootModel.getProject());
        ContentEntry contentEntry = doAddContentEntry(rootModel);
    }


    @Override
    public String getGroupName() {
        return super.getGroupName();
    }

    @Override
    public String getDescription() {
        return "Grav module";
    }


    @Override
    public ModuleType getModuleType() {
        return GravModuleType.getInstance();
    }

    @Override
    @Nullable
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        setProject(context.getProject());
        GravIntroWizardStep step = new GravIntroWizardStep(this);
        Disposer.register(parentDisposable, step);
        return step;
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{
//                new GravIntroWizardStep(this),
//                new GravModuleWizardStep(this)
        };
    }

    @Override
    public void moduleCreated(@NotNull Module module) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        String msg = String.format("Please wait while module for project %s is created", project.getName());
        settings = GravProjectSettings.getInstance(project);

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(msg, MessageType.WARNING, null)
                .setFadeoutTime(4000)
                .createBalloon()
                .show(RelativePoint.getSouthEastOf(statusBar.getComponent()), Balloon.Position.above);

        VirtualFile[] roots1 = ModuleRootManager.getInstance(module).getContentRoots();
        if (roots1.length != 0) {
            final VirtualFile src = roots1[0];
            settings.withSrcDirectory = withSrcDirectory;
            settings.gravInstallationPath = getGravInstallPath().getPath();
//            settings.withSrcDirectory =
            GravProjectGeneratorUtil generatorUtil = new GravProjectGeneratorUtil();
            generatorUtil.generateProject(project, src, settings, module);
        }
    }

    private static String getUrlByPath(final String path) {
        return VfsUtil.getUrlForLibraryRoot(new File(path));
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public GravProjectSettings getSettings() {
        return settings;
    }
}