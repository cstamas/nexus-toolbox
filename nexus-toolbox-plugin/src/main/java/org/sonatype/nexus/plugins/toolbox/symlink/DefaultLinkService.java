package org.sonatype.nexus.plugins.toolbox.symlink;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = LinkService.class )
public class DefaultLinkService
    implements LinkService
{
    public boolean isSymlink( Repository repository, String path )
        throws ItemNotFoundException, StorageException
    {
        ResourceStoreRequest request = new ResourceStoreRequest( path );

        try
        {
            return ( repository.retrieveItem( request ) instanceof StorageLinkItem );
        }
        catch ( Exception e )
        {
            // uh oh
            return false;
        }
    }

    public void symlink( Repository targetRepository, String targetPath, Repository linkRepository, String linkPath )
        throws StorageException
    {
        RepositoryItemUid target = targetRepository.createUid( targetPath );

        ResourceStoreRequest request = new ResourceStoreRequest( linkPath );

        DefaultStorageLinkItem link = new DefaultStorageLinkItem( linkRepository, request, true, true, target );

        try
        {
            linkRepository.storeItem( false, link );
        }
        catch ( Exception e )
        {
            throw new StorageException( "Could not link target \"" + target.toString() + "\" to \""
                + link.getRepositoryItemUid().toString() + "\"!", e );
        }
    }
}
