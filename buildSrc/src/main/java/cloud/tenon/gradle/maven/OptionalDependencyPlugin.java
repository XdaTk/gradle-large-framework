package cloud.tenon.gradle.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;


public class OptionalDependencyPlugin implements Plugin<Project> {

    public static final String OPTIONAL_CONFIGURATION_NAME = "optional";

    @Override
    public void apply(Project project) {
        Configuration optional = project.getConfigurations()
                .create(OPTIONAL_CONFIGURATION_NAME);

        optional.attributes((attributes) -> {
            attributes.attribute(
                    Usage.USAGE_ATTRIBUTE,
                    project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)
            );
        });

        project.getPlugins()
                .withType(JavaPlugin.class, (javaPlugin -> {
                    SourceSetContainer sourceSets = project.getConvention()
                            .getPlugin(JavaPluginConvention.class)
                            .getSourceSets();

                    sourceSets.all((sourceSet -> {
                        sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(optional));
                        sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(optional));
                    }));

                    project.getTasks()
                            .withType(Javadoc.class)
                            .all((javadoc -> {
                                javadoc.setClasspath(javadoc.getClasspath().plus(optional));
                            }));

                }));

        project.getPlugins().withType(EclipsePlugin.class, (eclipsePlugin) -> {
            project.getExtensions()
                    .getByType(EclipseModel.class)
                    .classpath((classpath) -> classpath.getPlusConfigurations().add(optional));
        });
    }
}
