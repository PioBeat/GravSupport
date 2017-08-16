package net.offbeatpioneer.intellij.plugins.grav.project;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Project component gets instantiated for every GRAV project created
 *
 * @author Dominik Grzelak
 */
public class GravProjectComponent implements ProjectComponent {
    final private static Logger LOG = Logger.getInstance("Grav-Plugin");

    @Override
    public void projectOpened() {
        LOG.debug("projectOpened");
        System.out.println("projectOpened");
    }

    @Override
    public void projectClosed() {
        LOG.debug("projectClosed");
    }

    @Override
    public void initComponent() {
        LOG.debug("initComponent");
        System.out.println("initComponent");
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "GravProjectComponent";
    }
}
