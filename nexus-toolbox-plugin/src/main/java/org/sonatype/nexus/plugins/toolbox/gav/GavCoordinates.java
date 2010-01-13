package org.sonatype.nexus.plugins.toolbox.gav;

/**
 * Represents the "logical" handle that targets your operation. By filling in more and more, you able to narrow what
 * will be selected to operate on.
 * 
 * @author cstamas
 */
public class GavCoordinates
{
    public static final String MATCH_ALL = "*";

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String classifier;

    public GavCoordinates( String groupId )
    {
        this( groupId, MATCH_ALL, MATCH_ALL );
    }

    public GavCoordinates( String groupId, String artifactId, String version )
    {
        this( groupId, artifactId, version, MATCH_ALL );
    }

    public GavCoordinates( String groupId, String artifactId, String version, String classifier )
    {
        this.groupId = groupId;

        this.artifactId = artifactId;

        this.version = version;

        this.classifier = classifier;
    }

    protected String getGroupId()
    {
        return groupId;
    }

    protected String getArtifactId()
    {
        return artifactId;
    }

    protected String getVersion()
    {
        return version;
    }

    protected String getClassifier()
    {
        return classifier;
    }
}
