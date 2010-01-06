package org.sonatype.nexus.toolbox.reposnap;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.StorageException;

public interface RepositorySnapshotCreator
{
    /**
     * Will take a snapshot or sourceRepository into targetRepositoryId. If target exists, will not do anything, unless
     * force is true. Otherwise, it will create a repository on the fly, and continue.
     * 
     * @param sourceRepositoryId the id of the source repository. Must exist.
     * @param targetRepositoryId the id of the target repository. Will be created if not exists.
     * @param force to force snapshot taking, even if target exists.
     * @throws NoSuchRepositoryException if source does not exists
     * @throws ConfigurationException if replica repository should be created but it results in a problem
     * @throws StorageException in case of some storage exception
     * @throws IOException
     */
    void snapshot( String sourceRepositoryId, String targetRepositoryId, boolean force )
        throws NoSuchRepositoryException, ConfigurationException, StorageException, IOException;
}
