package cloud.tenon.gradle.bom;

import cloud.tenon.gradle.bom.library.*;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.util.ConfigureUtil;

import java.util.*;
import java.util.stream.Collectors;

public class BomExtension {

    private final DependencyHandler dependencyHandler;

    private final Map<String, String> properties = new LinkedHashMap<>();

    private final Map<String, String> artifactVersionProperties = new HashMap<>();

    private final List<Library> libraries = new ArrayList<>();

    public BomExtension(DependencyHandler dependencyHandler, Project project) {
        this.dependencyHandler = dependencyHandler;
    }

    public void parent(String groupId, String artifactId, String version, String relativePath) {
        Object parentDependency = createDependencyNotation(groupId, artifactId, version);
        this.dependencyHandler.add(JavaPlatformPlugin.API_CONFIGURATION_NAME,
                this.dependencyHandler.platform(parentDependency));
        this.dependencyHandler.add(BomPlugin.API_ENFORCED_CONFIGURATION_NAME,
                this.dependencyHandler.enforcedPlatform(parentDependency));
    }

    public void library(String name, String version, Closure<?> closure) {
        LibraryHandler libraryHandler = new LibraryHandler();
        ConfigureUtil.configure(closure, libraryHandler);
        addLibrary(new Library(name, version, libraryHandler.groups));
    }

    List<Library> getLibraries() {
        return this.libraries;
    }

    Map<String, String> getProperties() {
        return this.properties;
    }

    String getArtifactVersionProperty(String groupId, String artifactId) {
        String coordinates = String.format("%s:%s", groupId, artifactId);
        return this.artifactVersionProperties.get(coordinates);
    }

    private void addLibrary(Library library) {
        this.libraries.add(library);
        String versionProperty = library.getVersionProperty();
        if (versionProperty != null) {
            this.properties.put(versionProperty, library.getVersion());
        }
        for (Group group : library.getGroups()) {
            for (Module module : group.getModules()) {
                putArtifactVersionProperty(group.getId(), module.getName(), versionProperty);
                this.dependencyHandler.getConstraints().add(JavaPlatformPlugin.API_CONFIGURATION_NAME,
                        createDependencyNotation(group.getId(), module.getName(), library.getVersion()));
            }


            for (String bomImport : group.getBoms()) {
                putArtifactVersionProperty(group.getId(), bomImport, versionProperty);
                String bomDependency = createDependencyNotation(group.getId(), bomImport, library.getVersion());
                this.dependencyHandler.add(JavaPlatformPlugin.API_CONFIGURATION_NAME,
                        this.dependencyHandler.platform(bomDependency));
                this.dependencyHandler.add(BomPlugin.API_ENFORCED_CONFIGURATION_NAME,
                        this.dependencyHandler.enforcedPlatform(bomDependency));
            }
        }
    }

    private void putArtifactVersionProperty(String groupId, String artifactId, String versionProperty) {
        String coordinates = String.format("%s:%s", groupId, artifactId);
        String existing = this.artifactVersionProperties.putIfAbsent(coordinates, versionProperty);
        if (existing != null) {
            throw new InvalidUserDataException(
                    String.format("Cannot put version property for '%s'. Version property '%s' has already been stored.",
                            coordinates, existing));
        }
    }

    private String createDependencyNotation(String groupId, String artifactId, String version) {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public static class LibraryHandler {

        private final List<Group> groups = new ArrayList<>();

        public void group(String id, Closure<?> closure) {
            GroupHandler groupHandler = new GroupHandler(id);
            ConfigureUtil.configure(closure, groupHandler);
            this.groups
                    .add(new Group(groupHandler.id, groupHandler.modules, groupHandler.plugins, groupHandler.imports));
        }

        public static class GroupHandler extends GroovyObjectSupport {

            private final String id;

            private List<Module> modules = new ArrayList<>();

            private List<String> imports = new ArrayList<>();

            private List<String> plugins = new ArrayList<>();

            public GroupHandler(String id) {
                this.id = id;
            }

            public void setModules(List<Object> modules) {
                this.modules = modules.stream()
                        .map((input) -> (input instanceof Module) ? (Module) input : new Module((String) input))
                        .collect(Collectors.toList());
            }

            public void setImports(List<String> imports) {
                this.imports = imports;
            }

            public void setPlugins(List<String> plugins) {
                this.plugins = plugins;
            }

            public Object methodMissing(String name, Object args) {
                if (args instanceof Object[] && ((Object[]) args).length == 1) {
                    Object arg = ((Object[]) args)[0];
                    if (arg instanceof Closure) {
                        ExclusionHandler exclusionHandler = new ExclusionHandler();
                        ConfigureUtil.configure((Closure<?>) arg, exclusionHandler);
                        return new Module(name, exclusionHandler.exclusions);
                    }
                }
                throw new InvalidUserDataException(
                        String.format("Invalid exclusion configuration for module '%s'", name));
            }

            public static class ExclusionHandler {

                private final List<Exclusion> exclusions = new ArrayList<>();

                public void exclude(Map<String, String> exclusion) {
                    this.exclusions.add(new Exclusion(exclusion.get("group"), exclusion.get("module")));
                }

            }

        }

    }

}
