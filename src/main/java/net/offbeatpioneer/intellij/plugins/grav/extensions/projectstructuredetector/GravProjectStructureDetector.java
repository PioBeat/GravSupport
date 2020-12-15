package net.offbeatpioneer.intellij.plugins.grav.extensions.projectstructuredetector;

import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import net.offbeatpioneer.intellij.plugins.grav.extensions.module.GravSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class GravProjectStructureDetector extends ProjectStructureDetector {


    @Override
    public @NotNull DirectoryProcessingResult detectRoots(@NotNull File dir, File @NotNull [] children, @NotNull File base, @NotNull List<DetectedProjectRoot> result) {
        VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(dir, true);
        if (fileByIoFile != null && GravSdkType.isValidGravSDK(fileByIoFile)) {
            GravDetectedProjectRoot root = new GravDetectedProjectRoot(dir);
            result.add(root);
        }
        return DirectoryProcessingResult.SKIP_CHILDREN;
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots, @NotNull ProjectDescriptor projectDescriptor, @NotNull ProjectFromSourcesBuilder builder) {
        super.setupProjectStructure(roots, projectDescriptor, builder);
    }

    @Override
    public String getDetectorId() {
        return "GravProjectStructureDetector";
    }
}
