package cloud.tenon.gradle.maven;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RepositoryPlugin implements Plugin<Project> {

    public static final String MAVEN_REPOSITORY_CONFIGURATION_NAME = "mavenRepository";

    public static final String PUBLISH_TO_PROJECT_REPOSITORY_TASK_NAME = "publishMavenPublicationToProjectRepository";

    public static final String PUBLISH_PLUGIN_MAVEN_PUBLICATION_TO_PROJECT_REPOSITORY = "publishPluginMavenPublicationToProjectRepository";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(MavenPublishPlugin.class);
        PublishingExtension publishing = project
                .getExtensions()
                .getByType(PublishingExtension.class);

        File repositoryLocation = new File(project.getBuildDir(), "maven-repository");

        publishing.getRepositories().maven((repository) -> {
            repository.setName("project");
            repository.setUrl(repositoryLocation.toURI());
        });


        project.getTasks()
                .matching(task -> task.getName().equals(PUBLISH_TO_PROJECT_REPOSITORY_TASK_NAME))
                .all(task -> setUpProjectRepository(project, task, repositoryLocation));

        project.getTasks()
                .matching(task -> task.getName().equals(PUBLISH_PLUGIN_MAVEN_PUBLICATION_TO_PROJECT_REPOSITORY))
                .all(task -> setUpProjectRepository(project, task, repositoryLocation));
    }

    private void setUpProjectRepository(Project project, Task task, File repositoryLocation) {
        task.doFirst(new CleanAction(repositoryLocation));
        Configuration projectRepository = project.getConfigurations().create(MAVEN_REPOSITORY_CONFIGURATION_NAME);

        project.getArtifacts().add(
                projectRepository.getName(),
                repositoryLocation,
                (artifact) -> artifact.builtBy(task)
        );

        DependencySet target = projectRepository.getDependencies();

        project.getPlugins()
                .withType(JavaPlugin.class)
                .all((javaPlugin) -> addMavenRepositoryDependencies(project, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, target));

        project.getPlugins()
                .withType(JavaLibraryPlugin.class)
                .all((javaLibraryPlugin) -> addMavenRepositoryDependencies(project, JavaPlugin.API_CONFIGURATION_NAME, target));

        project.getPlugins()
                .withType(JavaPlatformPlugin.class)
                .all((javaPlugin) -> addMavenRepositoryDependencies(project, JavaPlatformPlugin.API_CONFIGURATION_NAME, target));
    }

    private void addMavenRepositoryDependencies(Project project, String sourceConfigurationName, DependencySet target) {
        project
                .getConfigurations()
                .getByName(sourceConfigurationName)
                .getDependencies()
                .withType(ProjectDependency.class)
                .all((dependency) -> {
                    Map<String, String> dependencyDescriptor = new HashMap<>();
                    dependencyDescriptor.put("path", dependency.getDependencyProject().getPath());
                    dependencyDescriptor.put("configuration", MAVEN_REPOSITORY_CONFIGURATION_NAME);
                    target.add(project.getDependencies().project(dependencyDescriptor));
                });
    }


    private static final class CleanAction implements Action<Task> {

        private final File location;

        private CleanAction(File location) {
            this.location = location;
        }

        @Override
        public void execute(Task task) {
            task.getProject().delete(this.location);
        }

    }

}
