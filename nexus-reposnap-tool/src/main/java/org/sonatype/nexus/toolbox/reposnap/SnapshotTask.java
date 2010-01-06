package org.sonatype.nexus.toolbox.reposnap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "SnapshotTask", instantiationStrategy = "per-lookup" )
public class SnapshotTask
    extends AbstractNexusTask<Object>
{
    @Requirement
    private Walker walker;

    private Repository source;

    private Repository target;

    public Repository getSource()
    {
        return source;
    }

    public void setSource( Repository source )
    {
        this.source = source;
    }

    public Repository getTarget()
    {
        return target;
    }

    public void setTarget( Repository target )
    {
        this.target = target;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        WalkerContext ctx = new DefaultWalkerContext( getSource(), new ResourceStoreRequest( "/" ) );

        ctx.getProcessors().add( new SnapshotWalkerProcessor( getTarget() ) );

        walker.walk( ctx );

        return Boolean.TRUE;
    }

    @Override
    protected String getAction()
    {
        return "Snapshot";
    }

    @Override
    protected String getMessage()
    {
        return "Taking snapshot of repository \"" + getSource().getName() + "\" (id=\"" + getSource().getId()
            + "\") into target repository \"" + getTarget().getName() + "\" (id=\"" + getTarget().getId() + "\").";
    }

    private class SnapshotWalkerProcessor
        extends AbstractFileWalkerProcessor
    {
        private final Repository target;

        public SnapshotWalkerProcessor( Repository target )
        {
            this.target = target;
        }

        @Override
        protected void processFileItem( WalkerContext ctx, StorageFileItem fileItem )
            throws Exception
        {
            target.storeItem( true, fileItem );
        }
    }

}
