package cloud.tenon.gradle.bom.library;

import java.util.List;

public class Group {

    private final String id;

    private final List<Module> modules;

    private final List<String> plugins;

    private final List<String> boms;

    public Group(String id, List<Module> modules, List<String> plugins, List<String> boms) {
        this.id = id;
        this.modules = modules;
        this.plugins = plugins;
        this.boms = boms;
    }

    public String getId() {
        return this.id;
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public List<String> getPlugins() {
        return this.plugins;
    }

    public List<String> getBoms() {
        return this.boms;
    }

}
