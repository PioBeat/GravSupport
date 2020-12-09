package net.offbeatpioneer.intellij.plugins.grav.extensions.module.wizard;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.templates.github.DownloadUtil;
import com.intellij.platform.templates.github.Outcome;
import com.intellij.ui.FieldPanel;
import com.intellij.ui.OptionGroup;
import com.intellij.ui.components.labels.ActionLink;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.FileDownloader;
import com.intellij.util.io.ZipUtil;
import com.intellij.util.net.IOExceptionDialog;
import com.intellij.util.ui.JBUI;
import net.offbeatpioneer.intellij.plugins.grav.helper.GithubApi;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.ui.SquareButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wizard interface for creating a new Grav module.
 * <p>
 * The wizard offers the options to specify a local Grav installation or to download a Grav release from GitHub.
 * The latest Grav versions are retrieved directly from GitHub.
 *
 * @author Dominik Grzelak
 */
public class CreateGravProjectWizardGUI implements ActionListener {
    // points to the file that defines the URL format of the Grav release in GitHub
    private static final String DOWNLOAD_CONFIG_PROPERTIES = "download-config.properties";
    private final Project project;
    private String[] DEFAULT_GRAV_VERSIONS = new String[]{"1.6.30", "1.6.29", "1.6.28"};
    private AtomicBoolean gotLatestVersions; //flag indicates if latest version tags could be retrieved from github
    private String[] gravVersions = null;
    private JPanel mainPanel;
    private JPanel ptop;
    private JPanel pbottom;
    private JLabel lblIntro;
    private ActionLink myDownloadLink;
    private FieldPanel fieldPanel;
    private JLabel lblHint;
    private JComboBox<String> gravVersionComboBox;
    private JLabel lblVersionPrompt;
    private JLabel lblVersionDetermined;
    private ActionLink refreshLink;
    private JRadioButton rbDownloadGrav;
    private JRadioButton rbSelectGrav;
    private JPanel panelSelectGrav;
    private JPanel panelDownloadGrav;
    private JButton btnRefreshGravVersions;
    private JLabel lblGravDownloadPath;
    private OptionGroup panel1;
    private BrowseFilesListener browseFilesListener;
    Properties downloadProps;

    public enum WizardOption {
        SELECT, DOWNLOAD
    }

    private WizardOption wizardOption;

    public CreateGravProjectWizardGUI(Project project) {
        this.project = project;
        initDownloadConfigurationFile();
        gotLatestVersions = Objects.isNull(gotLatestVersions) ? new AtomicBoolean(false) : gotLatestVersions;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void initDownloadConfigurationFile() {
        if (Objects.isNull(downloadProps)) {
            downloadProps = loadDownloadConfig();
        }
    }

    public Properties loadDownloadConfig() {
        Properties props = new Properties();
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(DOWNLOAD_CONFIG_PROPERTIES)) {
            props.load(resourceStream);
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return props;
    }

    public void initLayout() {
        if (!gotLatestVersions.get()) {
//            //download versions
            refreshVersionAction();
        }
        rbSelectGrav.setSelected(true);
        rbSelectGrav.setSelected(false);
        enablePanel(panelSelectGrav, true);
        enablePanel(panelDownloadGrav, false);
        wizardOption = WizardOption.SELECT;

    }

    public void createUIComponents() {
        gotLatestVersions = new AtomicBoolean(false);
        initDownloadConfigurationFile();

        lblIntro = new JLabel();
        lblIntro.setText("Select a valid Grav installation or click the link to download the latest version.");
        lblIntro.setVisible(true);
        lblHint = new JLabel();
        lblHint.setVisible(false);


        panelSelectGrav = new JPanel();
        panelDownloadGrav = new JPanel();

        rbSelectGrav = new JRadioButton("Use existing Grav 'SDK'");
        rbSelectGrav.setActionCommand("selectGrav");
        rbSelectGrav.addActionListener(this);
        rbDownloadGrav = new JRadioButton("Download a new Grav 'SDK'");
        rbDownloadGrav.setActionCommand("downloadGrav");
        rbDownloadGrav.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(rbSelectGrav);
        group.add(rbDownloadGrav);

        // Select Grav


        // Download Grav

        btnRefreshGravVersions = new SquareButton();
        btnRefreshGravVersions.setBorderPainted(false);
        btnRefreshGravVersions.setBorder(null);
        btnRefreshGravVersions.setFocusable(false);
        btnRefreshGravVersions.setMargin(JBUI.emptyInsets());
        btnRefreshGravVersions.setContentAreaFilled(false);
        btnRefreshGravVersions.setIcon(AllIcons.Actions.Refresh);
        btnRefreshGravVersions.setFocusPainted(false);
        btnRefreshGravVersions.addActionListener(this);
        btnRefreshGravVersions.setActionCommand("refreshGrav");

        gravVersionComboBox = new ComboBox<>(DEFAULT_GRAV_VERSIONS);
//        if (!gotLatestVersions.get()) {
//            //download versions
//            refreshVersionAction();
//        }


//        initRefreshLink();
        myDownloadLink = new ActionLink("Download selected Grav 'SDK'", new AnAction() {
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

                        AtomicReference<VirtualFile> dir = new AtomicReference<>(virtualFiles.get(0));
                        String dirName = dir.get().getName();
                        fieldPanel.setText(dir.get().getPath());
                        boolean completed = false;
                        if (!dirName.toLowerCase().contains("grav")) {
                            WriteAction.compute(() -> {
                                try {
                                    dir.set(dir.get().createChildDirectory(this, "grav-sdk"));
                                    return dir;
                                } catch (IOException e) {//
                                }
                                return dir;
                            });
                        }
                        String newdirpath = dir.get().getPresentableUrl() + File.separator + "grav-admin" + File.separator;
//                        fieldPanel.setText(newdirpath);

                        String downloadUrl = MessageFormat.format(downloadProps.getProperty("downloadUrl"), getSelectedGravVersion(), getSelectedGravVersion());
                        String filename = MessageFormat.format(downloadProps.getProperty("filename"), getSelectedGravVersion());
                        String presentableName = MessageFormat.format(downloadProps.getProperty("presentableDownloadName"), getSelectedGravVersion());

                        DownloadableFileService fileService = DownloadableFileService.getInstance();
                        DownloadableFileDescription fileDescription = fileService.createFileDescription(downloadUrl, filename);

                        List<DownloadableFileDescription> descriptions = new ArrayList<>();
                        descriptions.add(fileDescription);
                        FileDownloader downloader = fileService.createDownloader(descriptions, presentableName);

                        List<VirtualFile> files = downloader.downloadFilesWithProgress(dir.get().getPath(), project, mainPanel);//downloader.toDirectory(dir.getPath()).download();
                        if (files != null && files.size() == 1) {
                            try {
                                VirtualFile finalDir = dir.get();


                                completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
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

                                }, "Extracting Zip File", false, project, mainPanel);

//                                    AccessToken accessToken = ApplicationManager.getApplication().acquireWriteActionLock(panel.getClass());
                                PropertiesComponent.getInstance().setValue(CreateGravProjectWizardStep.LAST_USED_GRAV_HOME, newdirpath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                dir.get().refresh(false, true);
                                if (completed) {
                                    String gravSdkVersion = GravSdkType.findGravSdkVersion(newdirpath);
                                    setDeterminedGravVersion(gravSdkVersion);
                                    showDeterminedVersion(true);
                                    fieldPanel.setText(newdirpath);//validation will be performed automatically (see GravInstallerGeneratorPeer)
                                    lblGravDownloadPath.setText(newdirpath);
                                }
                            }
                        }
                    }

                    @Override
                    public void cancelled() {

                    }
                });
            }
        });
        // must be place after downloadLink, otherwise downloadLink doesn't work ...
        JTextField textField = new JTextField();
        browseFilesListener = new BrowseFilesListener(textField, "Select Grav 'SDK' Download Folder", "", FileChooserDescriptorFactory.createSingleFileDescriptor());
        fieldPanel = ModuleWizardStep.createFieldPanel(textField, "", browseFilesListener);

    }

    private void refreshVersionAction() {
//        btnRefreshGravVersions.setEnabled(false);
        //download versions
        Outcome<String[]> outcome = DownloadUtil.provideDataWithProgressSynchronously(
                project,
                "Grav versions",
                "Retrieving latest Grav versions ...",
                () -> {
//                    ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
                    List<String> gravVersionReleases = GithubApi.getGravVersionReleases(downloadProps.getProperty("gravReleases"));
                    gotLatestVersions.set(true);
                    return gravVersionReleases.toArray(new String[0]);
                }, () -> IOExceptionDialog.showErrorDialog("Download Error", "Could not retrieve current Grav releases from: '" + GithubApi.GravRepoUrl + "'")
        );

        gravVersions = outcome.get();
        Exception e = outcome.getException();
        if (e != null || gravVersions == null) {
            gravVersions = DEFAULT_GRAV_VERSIONS;
        }
        gravVersionComboBox.setModel(new DefaultComboBoxModel<>(gravVersions));
//        btnRefreshGravVersions.setEnabled(true);
    }

    public void initRefreshLink() {
        refreshLink = new ActionLink("Refresh Grav versions", new AnAction() {

            @Override
            public void actionPerformed(AnActionEvent e) {
                refreshVersionAction();
            }
        });
        refreshLink.setVisible(false);
    }

    public void showDeterminedVersion(boolean show) {
        lblVersionPrompt.setVisible(show);
        lblVersionDetermined.setVisible(show);
    }

    public void setDeterminedGravVersion(String version) {
        lblVersionDetermined.setText(version);
    }

    public void setDefaultInstallationPath(String path) {
        if ((fieldPanel.getText() == null || fieldPanel.getText().isEmpty()) && path != null) {
            fieldPanel.setText(path);
            String gravSdkVersion = GravSdkType.findGravSdkVersion(path);
            setDeterminedGravVersion(gravSdkVersion);
            showDeterminedVersion(true);
        }
    }

    public String getSelectedGravVersion() {
        return (String) gravVersionComboBox.getSelectedItem();
    }

    public FieldPanel getFieldPanel() {
        return fieldPanel;
    }

    /**
     * Dependent on the option (Select or Download)
     *
     * @return the path to the grav directory, or empty string
     */
    public String getGravDirectory() {
        if (wizardOption == WizardOption.SELECT) {
            return fieldPanel.getText();
        } else if (wizardOption == WizardOption.DOWNLOAD) {
            return lblGravDownloadPath.getText();
        }
        return "";
    }

    public void showHint(boolean flag) {
        lblHint.setVisible(flag);
        showDeterminedVersion(!flag);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JRadioButton) {
            if (e.getActionCommand().equals("selectGrav")) {
                wizardOption = WizardOption.SELECT;
                enablePanel(panelDownloadGrav, false);
                enablePanel(panelSelectGrav, true);
            } else if (e.getActionCommand().equals("downloadGrav")) {
                wizardOption = WizardOption.DOWNLOAD;
                enablePanel(panelDownloadGrav, true);
                enablePanel(panelSelectGrav, false);
            }
        }

        if (e.getSource() instanceof JButton) {
            if (e.getActionCommand().equals("refreshGrav")) {
                refreshVersionAction();
            }
        }
    }

    private void enablePanel(JPanel panel, boolean enable) {
        Component[] comps = panel.getComponents();
        for (Component comp : comps) {
            comp.setEnabled(enable);
        }
    }

    public WizardOption getWizardOption() {
        return wizardOption;
    }
}
