package org.sonatype.nexus.plugins.toolbox.symlink;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Provides easy access to links.
 * 
 * @author cstamas
 */
public interface LinkService
{
    /**
     * Creates a symlink in linkRepository on linkPath, that is pointing into targetRepository to targetPath.
     * 
     * @param targetRepository
     * @param targetPath
     * @param linkRepository
     * @param linkPath
     */
    void symlink( Repository targetRepository, String targetPath, Repository linkRepository, String linkPath )
        throws ItemNotFoundException, StorageException;

    /**
     * Returns true if the specified path in given repository is a symlink, false otherwise.
     * 
     * @param repository
     * @param path
     * @return
     */
    boolean isSymlink( Repository repository, String path )
        throws ItemNotFoundException, StorageException;
}
