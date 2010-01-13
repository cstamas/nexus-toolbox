package org.sonatype.nexus.plugins.toolbox.symlink;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "LinkResource" )
public class LinkResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private LinkService linkService;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/toolbox/symlink";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repositories]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String targetRepositoryId = form.getFirstValue( "targetRepository" );

        String targetPath = form.getFirstValue( "targetPath" );

        String linkRepositoryId = form.getFirstValue( "linkRepository" );

        // fallback into "same repository" if not targetRepository is given
        if ( StringUtils.isBlank( linkRepositoryId ) )
        {
            linkRepositoryId = targetRepositoryId;
        }

        String linkPath = form.getFirstValue( "linkPath" );

        if ( StringUtils.isBlank( targetRepositoryId ) || StringUtils.isBlank( targetPath )
            || StringUtils.isBlank( linkRepositoryId ) || StringUtils.isBlank( linkPath ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                "Parameters 'targetRepository', 'targetPath', 'linkPath' are mandatory!" );
        }

        try
        {
            Repository targetRepository = getRepositoryRegistry().getRepository( targetRepositoryId );

            Repository linkRepository = getRepositoryRegistry().getRepository( linkRepositoryId );

            linkService.symlink( targetRepository, targetPath, linkRepository, linkPath );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e );
        }
        catch ( StorageException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }

        return "DONE";
    }
}
