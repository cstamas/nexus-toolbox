package org.sonatype.nexus.plugins.toolbox.gav;

import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * Brings Maven logical GAV coordinates to "lower" FS operation level.
 * 
 * @author cstamas
 */
public interface GavFSOperationsService
{
    /**
     * This simply links the complete list of artifacts
     * 
     * @param sourceRepository
     * @param targetRepository
     * @return the number of artifacts linked.
     */
    int linkGav( MavenRepository sourceRepository, MavenRepository targetRepository, GavCoordinates gavCoordinates );

    /**
     * Deletes from a repository by GAVCoordinates.
     * 
     * @param reository from where to delete.
     * @param gavCoordinates
     * @return the number of artifacts deleted.
     */
    int deleteByGav( MavenRepository repository, GavCoordinates gavCoordinates );
}
