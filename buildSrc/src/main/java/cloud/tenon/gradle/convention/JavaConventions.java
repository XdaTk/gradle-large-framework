package cloud.tenon.gradle.convention;

import cloud.tenon.gradle.maven.OptionalDependencyPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class JavaConventions {

    void apply(Project project) {
        project.getPlugins()
                .withType(JavaBasePlugin.class, (java) -> {
                    project.setProperty("sourceCompatibility", "1.8");
                    configureJavaCompileConventions(project);
                    configureJavadocConventions(project);
                    configureDependencyManagement(project);
                    configureTestConventions(project);
                    configureJarManifestConventions(project);
                });
    }


    private void configureJavaCompileConventions(Project project) {
        project.getTasks()
                .withType(JavaCompile.class, (compile) -> {
                            compile.getOptions()
                                    .setEncoding("UTF-8");
                            List<String> args = compile.getOptions().getCompilerArgs();
                            if (!args.contains("-parameters")) {
                                args.add("-parameters");
                            }

                        }
                );
    }


    private void configureJavadocConventions(Project project) {
        project.getTasks()
                .withType(Javadoc.class, javadoc -> javadoc.getOptions()
                        .source("1.8")
                        .encoding("UTF-8"));
    }

    private void configureTestConventions(Project project) {
        project.getTasks()
                .withType(Test.class, (test) -> {
                    test.useJUnitPlatform();
                    test.setMaxHeapSize("1024M");
                });
    }

    private void configureJarManifestConventions(Project project) {
        project.getTasks()
                .withType(Jar.class, (jar) -> {
                    project.afterEvaluate((evaluated) -> {
                        jar.manifest((manifest) -> {
                            Map<String, Object> attributes = new TreeMap<>();
                            attributes.put("Automatic-Module-Name", project.getName().replace("-", "."));
                            attributes.put("Build-Jdk-Spec", project.property("sourceCompatibility"));
                            attributes.put("Built-By", "Tenon");
                            attributes.put("Implementation-Title", project.getDescription());
                            attributes.put("Implementation-Version", project.getVersion());
                            manifest.attributes(attributes);
                        });
                    });
                });
    }


    private void configureDependencyManagement(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();


        Configuration dependencyManagement = configurations.create("dependencyManagement", (configuration) -> {
            configuration.setVisible(false);
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(false);
        });


        configurations
                .matching((configuration) -> configuration.getName().endsWith("Classpath") || JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME.equals(configuration.getName()))
                .all((configuration) -> configuration.extendsFrom(dependencyManagement));


        Dependency tenonParent = project.getDependencies().enforcedPlatform(project.getDependencies()
                .project(Collections.singletonMap("path", ":tenon-dependency-project")));


        dependencyManagement.getDependencies().add(tenonParent);

        project.getPlugins().withType(OptionalDependencyPlugin.class, (optionalDependencies) -> {
            configurations.getByName(OptionalDependencyPlugin.OPTIONAL_CONFIGURATION_NAME)
                    .extendsFrom(dependencyManagement);
        });
    }

    private boolean isCi() {
        return Boolean.parseBoolean(System.getenv("CI"));
    }

}
