package org.sonatype.nexus.plugins.toolbox.symlink;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;

@Component( role = LinkService.class )
public class DefaultLinkService
    implements LinkService
{
    @Requirement
    private Walker walker;

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
        throws ItemNotFoundException, StorageException
    {
        try
        {
            // 1st, we retrieve the target to see what is it
            ResourceStoreRequest targetRequest = new ResourceStoreRequest( targetPath );

            StorageItem target = targetRepository.retrieveItem( targetRequest );

            if ( target instanceof StorageFileItem || target instanceof StorageLinkItem )
            {
                // is a plain file, just link it
                ResourceStoreRequest request = new ResourceStoreRequest( linkPath );

                DefaultStorageLinkItem link =
                    new DefaultStorageLinkItem( linkRepository, request, true, true, target.getRepositoryItemUid() );

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
            else if ( target instanceof StorageCollectionItem )
            {
                // delete the crawled repo 1st!
                try
                {
                    ResourceStoreRequest linkRequest = new ResourceStoreRequest( linkPath );

                    linkRepository.deleteItem( false, linkRequest );
                }
                catch ( ItemNotFoundException e )
                {
                    // ignore item not found!
                }

                // this is a directory, we need to crawl
                WalkerContext ctx = new DefaultWalkerContext( targetRepository, targetRequest );

                ctx.getProcessors().add( new LinkCreatorProcessor( targetPath, linkRepository, linkPath ) );

                walker.walk( ctx );
            }
            else
            {
                throw new IllegalArgumentException( "Only files, links and collections are supported!" );
            }
        }
        catch ( Exception e )
        {
            throw new StorageException( "Could not create link [targetRepo=\"" + targetRepository.getId()
                + "\", targetPath=\"" + targetPath + "\", linkRepo=\"" + linkRepository.getId() + "\", linkPath=\""
                + linkPath + "\"]!", e );
        }
    }

    // ==

    public static class LinkCreatorProcessor
        extends AbstractFileWalkerProcessor
    {
        private final String targetPath;

        private final Repository linkRepository;

        private final String linkPath;

        public LinkCreatorProcessor( String targetPath, Repository linkRepository, String linkPath )
        {
            this.targetPath = targetPath.startsWith( "/" ) ? targetPath : "/" + targetPath;

            this.linkRepository = linkRepository;

            this.linkPath = linkPath.endsWith( "/" ) ? linkPath.substring( 0, linkPath.length() - 1 ) : linkPath;
        }

        @Override
        protected void processFileItem( WalkerContext context, StorageFileItem fItem )
            throws Exception
        {
            String linkItemPath = fItem.getPath();

            // strip of targetPath from fItem path
            if ( StringUtils.isNotBlank( targetPath ) && fItem.getPath().startsWith( targetPath ) )
            {
                linkItemPath = linkItemPath.substring( targetPath.length() );
            }

            // add linkPath to fItemPath
            if ( StringUtils.isNotBlank( linkPath ) )
            {
                linkItemPath = linkPath + linkItemPath;
            }

            ResourceStoreRequest request = new ResourceStoreRequest( linkItemPath );

            DefaultStorageLinkItem link =
                new DefaultStorageLinkItem( linkRepository, request, true, true, fItem.getRepositoryItemUid() );

            linkRepository.storeItem( false, link );
        }
    }
}
