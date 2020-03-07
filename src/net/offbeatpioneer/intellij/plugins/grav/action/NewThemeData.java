package net.offbeatpioneer.intellij.plugins.grav.action;

/**
 * Created by Dome on 22.07.2017.
 */
public class NewThemeData {
    private String name;
    private String developer;
    private String description;
    private String email;
    private String githubId;

    public NewThemeData() {
        this(null, "", "", "", "");
    }

    public NewThemeData(String name, String developer, String description, String email, String githubId) {
        this.name = name;
        this.developer = developer;
        this.description = description;
        this.email = email;
        this.githubId = githubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGitHubId() {
        return githubId;
    }

    public void setGitHubId(String text) {
        this.githubId = text;
    }
}
