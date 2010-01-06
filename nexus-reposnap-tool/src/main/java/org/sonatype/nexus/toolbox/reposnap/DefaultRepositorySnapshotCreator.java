package org.sonatype.nexus.toolbox.reposnap;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

@Component( role = RepositorySnapshotCreator.class )
public class DefaultRepositorySnapshotCreator
    implements RepositorySnapshotCreator
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private TemplateManager templateManager;

    @Requirement
    private NexusScheduler nexusScheduler;

    public void snapshot( String sourceRepositoryId, String targetRepositoryId, boolean force )
        throws NoSuchRepositoryException, ConfigurationException, StorageException, IOException
    {
        Repository source = repositoryRegistry.getRepository( sourceRepositoryId );

        Repository target = null;

        if ( repositoryRegistry.repositoryIdExists( targetRepositoryId ) )
        {
            if ( !force )
            {
                throw new StorageException( "Target repository already exists (and forced=false)!" );
            }
            else
            {
                target = repositoryRegistry.getRepository( targetRepositoryId );
            }
        }
        else
        {
            // create one replica of the source
            RepositoryTemplate template =
                (RepositoryTemplate) templateManager.getTemplates().getTemplates( source.getRepositoryContentClass() )
                    .getTemplates( source.getRepositoryKind().getMainFacet() ).pick();

            template.getConfigurableRepository().setId( targetRepositoryId );

            // TODO: name should be descriptive what is this
            template.getConfigurableRepository().setName( source.getName() + " (snapshot@YYYYMMDD)" );

            template.getConfigurableRepository().setExposed( false );

            template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

            target = template.create();
        }

        SnapshotTask task = nexusScheduler.createTaskInstance( SnapshotTask.class );

        task.setSource( source );

        task.setTarget( target );

        nexusScheduler.submit( "Taking snapshot of " + source.getId(), task );
    }

}
