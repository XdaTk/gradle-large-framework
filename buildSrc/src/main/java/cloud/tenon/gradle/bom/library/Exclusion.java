package cloud.tenon.gradle.bom.library;

public class Exclusion {
    private final String groupId;

    private final String artifactId;

    public Exclusion(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }
}
