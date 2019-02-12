package net.offbeatpioneer.intellij.plugins.grav.errorreporting;

import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.util.ExceptionUtil;

import java.util.Collections;
import java.util.List;

/**
 * Copy of the deprecated {@link com.intellij.errorreport.bean.ErrorBean} class of IntelliJ.
 * Kept for compatibility reasons.
 */
public class GravErrorBean {
    private final String stackTrace;
    private final String lastAction;
    private String message;
    private String description;
    private String pluginName;
    private String pluginVersion;
    private List<Attachment> attachments = Collections.emptyList();
    private Integer assigneeId;
    private Integer previousException;

    public GravErrorBean(Throwable throwable, String lastAction) {
        this.stackTrace = throwable != null ? ExceptionUtil.getThrowableText(throwable) : null;
        this.lastAction = lastAction;
        if (throwable != null) {
            setMessage(throwable.getMessage());
        }
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Integer getPreviousException() {
        return previousException;
    }

    public void setPreviousException(Integer previousException) {
        this.previousException = previousException;
    }
}