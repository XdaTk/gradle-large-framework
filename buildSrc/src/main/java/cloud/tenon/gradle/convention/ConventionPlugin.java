package cloud.tenon.gradle.convention;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ConventionPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        new JavaConventions().apply(project);
        new MavenPublishingConventions().apply(project);
    }

}
