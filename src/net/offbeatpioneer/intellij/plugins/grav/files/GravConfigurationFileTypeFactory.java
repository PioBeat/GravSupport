package net.offbeatpioneer.intellij.plugins.grav.files;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dome on 16.07.2017.
 */
public class GravConfigurationFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(ThemeBlueprintsFileType.INSTANCE);
    }
}
