package net.offbeatpioneer.intellij.plugins.grav.module.php;

import com.intellij.lang.javascript.boilerplate.AbstractGithubTagDownloadedProjectGenerator;
import com.intellij.platform.templates.github.GithubTagInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Dome on 11.08.2017.
 */
public class Grav2ProjectGenerator extends AbstractGithubTagDownloadedProjectGenerator {
    @NotNull
    @Override
    protected String getDisplayName() {
        return "GRAVY";
    }

    @NotNull
    @Override
    public String getGithubUserName() {
        return "angular";
    }

    @NotNull
    @Override
    public String getGithubRepositoryName() {
        return "baaaaa";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "<html>asdfasdfasdf</html>";
    }

    @Nullable
    @Override
    public String getPrimaryZipArchiveUrlForDownload(@NotNull GithubTagInfo tag) {
        return null;
    }
}
