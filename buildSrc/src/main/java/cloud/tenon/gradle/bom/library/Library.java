package cloud.tenon.gradle.bom.library;

import java.util.List;
import java.util.Locale;


public class Library {

    private final String name;

    private final String version;

    private final List<Group> groups;

    private final String versionProperty;


    public Library(String name, String version, List<Group> groups) {
        this.name = name;
        this.version = version;
        this.groups = groups;
        this.versionProperty = name.toLowerCase(Locale.ENGLISH).replace(' ', '-') + ".version";
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public List<Group> getGroups() {
        return this.groups;
    }

    public String getVersionProperty() {
        return this.versionProperty;
    }
}
