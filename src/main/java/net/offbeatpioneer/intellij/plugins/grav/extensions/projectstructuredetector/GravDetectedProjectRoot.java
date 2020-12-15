package net.offbeatpioneer.intellij.plugins.grav.extensions.projectstructuredetector;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GravDetectedProjectRoot extends DetectedProjectRoot {
    protected GravDetectedProjectRoot(@NotNull File directory) {
        super(directory);
    }

    @Override
    public @NotNull String getRootTypeName() {
        return PhpLanguage.INSTANCE.getID();
    }
}
