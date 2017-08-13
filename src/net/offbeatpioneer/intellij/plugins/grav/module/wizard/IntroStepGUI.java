package net.offbeatpioneer.intellij.plugins.grav.module.wizard;

import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.FileDownloader;
import com.intellij.util.io.ZipUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * First Wizard for creating a new grav module
 * <p>
 * Specify installtion folder or download grav.
 * Currently the latest grav version is specified in a properties files. Make changes in it to change the
 * download url etc.
 *
 * @author Dominik Grzelak
 */
public class IntroStepGUI {
    boolean downloaded = false;
    GravModuleBuilder builder;

    private JPanel mainPanel;
    private JLabel lblIntro;
    private JPanel content;
    private ActionLink myDownloadLink;
    private FieldPanel fieldPanel;
    private JLabel lblHint;
    private BrowseFilesListener browseFilesListener;

    public IntroStepGUI(GravModuleBuilder builder) {
        this.builder = builder;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public Properties loadDownloadConfig() {
        String resourceName = "download-config.properties"; // could also be a constant
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            props.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public void createUIComponents() {
        lblIntro = new JLabel();
        lblIntro.setText("Willkommen<br/> Select a valid Grav installtion or click the link to download the latest version.");
        lblHint = new JLabel();
        lblHint.setVisible(false);
        Properties downloadProps = loadDownloadConfig();
        myDownloadLink = new ActionLink("Download and Install Grav", new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                PathChooserDialog pathChooser = FileChooserFactory.getInstance()
                        .createPathChooser(descriptor, null, mainPanel);
                pathChooser.choose(VfsUtil.getUserHomeDir(), new FileChooser.FileChooserConsumer() {

                    @Override
                    public void consume(List<VirtualFile> virtualFiles) {
                        if (virtualFiles.size() != 1) {
                            return;
                        }

                        VirtualFile dir = virtualFiles.get(0);
                        String dirName = dir.getName();
                        fieldPanel.setText(dir.getPath());


//
                        try {
                            if (!dirName.toLowerCase().contains("grav")) {
                                AccessToken accessToken = ApplicationManager.getApplication().acquireWriteActionLock(mainPanel.getClass());
                                try {
                                    dir = dir.createChildDirectory(this, "grav-sdk");
                                } catch (IOException e) {//
                                } finally {
                                    accessToken.close();
                                }
                            }
                            String newdirpath = dir.getPresentableUrl() + File.separator + "grav-admin" + File.separator;
                            fieldPanel.setText(newdirpath);

                            DownloadableFileService fileService = DownloadableFileService.getInstance();
                            DownloadableFileDescription fileDescription = fileService.createFileDescription(downloadProps.getProperty("downloadUrl"), downloadProps.getProperty("filename"));

                            List<DownloadableFileDescription> descriptions = new ArrayList<>();
                            descriptions.add(fileDescription);
                            FileDownloader downloader = fileService.createDownloader(descriptions, downloadProps.getProperty("presentableDownloadName"));

                            List<VirtualFile> files = downloader.downloadFilesWithProgress(dir.getPath(), builder.getProject(), mainPanel);//downloader.toDirectory(dir.getPath()).download();
                            if (files != null && files.size() == 1) {
                                try {
                                    VirtualFile finalDir = dir;


                                    boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                                        ProgressIndicator parentIndicator = ProgressManager.getInstance().getProgressIndicator();
                                        parentIndicator = new EmptyProgressIndicator();
                                        parentIndicator.setModalityProgress(parentIndicator);
                                        parentIndicator.setText("Please wait ...");

                                        try {
                                            ZipUtil.extract(VfsUtil.virtualToIoFile(files.get(0)), VfsUtil.virtualToIoFile(finalDir), null);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        } finally {
//                                                accessToken.finish();
                                        }

                                    }, "Extract zip file", false, builder.getProject(), mainPanel);

//                                    AccessToken accessToken = ApplicationManager.getApplication().acquireWriteActionLock(panel.getClass());
                                    if (completed) {

                                    }
                                    PropertiesComponent.getInstance().setValue(GravIntroWizardStep.LAST_USED_GRAV_HOME, newdirpath);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    dir.refresh(false, true);
                                }
                            }


                        } finally {
//                            accessToken.finish();
                        }
                    }

                    @Override
                    public void cancelled() {

                    }
                });
            }
        });
        JTextField textField = new JTextField();
        browseFilesListener = new BrowseFilesListener(textField, "Select Grav Download Directory", "", FileChooserDescriptorFactory.createSingleFileDescriptor());

        fieldPanel = ModuleWizardStep.createFieldPanel(textField, "Select Grav Installation", browseFilesListener);
    }

    public void setDefaultInstallationPath(String path) {
        if ((fieldPanel.getText() == null || fieldPanel.getText().isEmpty()) && path != null) {
            fieldPanel.setText(path);
        }
    }

    public String getGravDirectory() {
        return fieldPanel.getText();
    }

    public void showHint(boolean flag) {
        lblHint.setVisible(flag);
    }
}
