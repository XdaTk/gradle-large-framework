package cloud.tenon.gradle.convention;

import org.gradle.api.Project;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.*;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;


public class MavenPublishingConventions {

    public void apply(Project project) {
        project.getPlugins()
                .withType(MavenPublishPlugin.class)
                .all((mavenPublish) -> {
                    PublishingExtension publishing = project.getExtensions()
                            .getByType(PublishingExtension.class);

                    if (project.hasProperty("deploymentRepository")) {
                        publishing.getRepositories()
                                .maven((mavenRepository) -> {
                                    mavenRepository.setUrl(project.property("deploymentRepository"));
                                    mavenRepository.setName("deployment");
                                });
                    }

                    publishing.getPublications()
                            .withType(MavenPublication.class)
                            .all((mavenPublication -> {
                                customizeMavenPublication(mavenPublication, project);
                            }));

                    project.getPlugins()
                            .withType(JavaPlugin.class)
                            .all((javaPlugin -> {
                                JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);
                                extension.withJavadocJar();
                                extension.withSourcesJar();
                            }));
                });
    }

    private void customizeMavenPublication(MavenPublication publication, Project project) {
        customizePom(publication.getPom(), project);
        project.getPlugins()
                .withType(JavaPlugin.class)
                .all((javaPlugin) -> {
                    customizeJavaMavenPublication(publication, project);
                });
    }


    private void customizePom(MavenPom pom, Project project) {
        pom.getUrl().set("https://github.com/xdatk/tenon");
        pom.getName().set(project.provider(project::getName));
        pom.getDescription().set(project.provider(project::getDescription));
        if (!isUserInherited(project)) {
            pom.organization(this::customizeOrganization);
        }
        pom.licenses(this::customizeLicences);
        pom.developers(this::customizeDevelopers);
        pom.scm((scm) -> customizeScm(scm, project));
        if (!isUserInherited(project)) {
            pom.issueManagement(this::customizeIssueManagement);
        }
    }

    private void customizeJavaMavenPublication(MavenPublication publication, Project project) {
        publication.versionMapping((strategy) -> {
            strategy.usage(Usage.JAVA_API, (mappingStrategy) -> {
                mappingStrategy.fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
            });
        });

        publication.versionMapping((strategy) -> {
            strategy.usage(Usage.JAVA_RUNTIME, VariantVersionMappingStrategy::fromResolutionResult);

        });
    }

    private boolean isUserInherited(Project project) {
        return "tenon-dependency-project".equals(project.getName()) || "tenon-boot-parent".equals(project.getName());
    }

    private void customizeOrganization(MavenPomOrganization organization) {
        organization.getName().set("Tenon.cloud, Inc.");
        organization.getUrl().set("https://tenon.cloud");
    }

    private void customizeLicences(MavenPomLicenseSpec licences) {
        licences.license((licence) -> {
            licence.getName().set("Apache License, Version 2.0");
            licence.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
        });
    }


    private void customizeDevelopers(MavenPomDeveloperSpec developers) {
        developers.developer((developer) -> {
            developer.getName().set("XdaTk");
            developer.getEmail().set("xdatk@icloud.com");
            developer.getOrganization().set("Tenon.cloud, Inc.");
            developer.getOrganizationUrl().set("https://www.tenon.cloud");
        });
    }


    private void customizeScm(MavenPomScm scm, Project project) {
        if (!isUserInherited(project)) {
            scm.getConnection().set("scm:git:git://github.com/xdatk/tenon.git");
            scm.getDeveloperConnection().set("scm:git:ssh://git@github.com/xdatk/tenon.git");
        }
        scm.getUrl().set("https://github.com/xdatk/tenon");
    }


    private void customizeIssueManagement(MavenPomIssueManagement issueManagement) {
        issueManagement.getSystem().set("GitHub");
        issueManagement.getUrl().set("https://github.com/xdatk/tenon/issues");
    }


}
