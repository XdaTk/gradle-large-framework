package cloud.tenon.gradle.project;

import cloud.tenon.gradle.convention.ConventionPlugin;
import cloud.tenon.gradle.maven.OptionalDependencyPlugin;
import cloud.tenon.gradle.maven.DeployedPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.PluginContainer;

public class ProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        PluginContainer plugins = project.getPlugins();
        plugins.apply(DeployedPlugin.class);
        plugins.apply(OptionalDependencyPlugin.class);
        plugins.apply(JavaLibraryPlugin.class);
        plugins.apply(ConventionPlugin.class);
    }
}
