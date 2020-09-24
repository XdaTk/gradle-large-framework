package cloud.tenon.gradle.bom.library;

import java.util.Collections;
import java.util.List;

public class Module {

    private final String name;

    private final List<Exclusion> exclusions;

    public Module(String name) {
        this(name, Collections.emptyList());
    }

    public Module(String name, List<Exclusion> exclusions) {
        this.name = name;
        this.exclusions = exclusions;
    }

    public String getName() {
        return this.name;
    }

    public List<Exclusion> getExclusions() {
        return this.exclusions;
    }

}
