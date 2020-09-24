package cloud.tenon.gradle.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class DeployedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(MavenPublishPlugin.class);
        project.getPlugins().apply(RepositoryPlugin.class);

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
        MavenPublication mavenPublication = publishing.getPublications()
                .create("maven", MavenPublication.class);


        project.afterEvaluate((evaluated) -> {
            project.getPlugins().withType(JavaPlugin.class).all((javaPlugin) -> {
                if (project.getTasks().getByName(JavaPlugin.JAR_TASK_NAME).getEnabled()) {
                    project.getComponents()
                            .matching((component) -> "java".equals(component.getName()))
                            .all(mavenPublication::from);
                }
            });
        });

        project.getPlugins().withType(JavaPlatformPlugin.class)
                .all((javaPlugin) ->
                        project.getComponents()
                                .matching((component) -> "javaPlatform".equals(component.getName()))
                                .all(mavenPublication::from));


    }

}
