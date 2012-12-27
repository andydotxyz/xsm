package com.rectang.xsm.pages.admin;

import org.apache.wicket.Resource;
import org.apache.wicket.Session;
import org.apache.wicket.Response;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import com.rectang.io.File;
import com.rectang.xsm.XSM;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * A resource that is used to stream a backup archive to the user.
 *
 * @author Andrew Williams
 * @version $Id: BackupResource.java 688 2007-10-13 19:52:33Z aje $
 * @since 1.0
 */
public class BackupResource
        extends Resource
{
    com.rectang.xsm.site.Site site;
    String type;

    public BackupResource( com.rectang.xsm.site.Site site, String type )
    {
        this.site = site;
        this.type = type;
    }


    protected void configureResponse( Response response )
    {
        if ( type.equals( "tar.gz" ) )
        {
            response.setContentType( "application/x-gzip" );
        }
        else
        {
            response.setContentType( "application/zip" );
        }
    }

    public IResourceStream getResourceStream()
    {
        File source = new File( XSM.getConfig().getDataDir(), site.getId() );

        try
        {
            File backup;
            if ( type.equals( "tar.gz" ) )
            {
                backup = source.tarCompress( false ).gzipCompress( true );
            }
            else
            {
                backup = source.zipCompress( false );
            }

            return new DeleteAfterReadFileResourceStream( backup );
        }
        catch ( IOException e )
        {
            Session.get().error( "Unable to archive site for backup, " + e.getMessage() );
        }

        return null;
    }
}

class DeleteAfterReadFileResourceStream
        extends FileResourceStream
{
    public DeleteAfterReadFileResourceStream( File file )
    {
        super( file );
    }


    public InputStream getInputStream()
            throws ResourceStreamNotFoundException
    {
        try
        {
            return new FileInputStream( getFile() )
            {

                public void close()
                        throws IOException
                {
                    if ( getFile().exists() )
                    {
                        getFile().delete();
                    }
                }
            };
        }
        catch ( IOException e )
        {
            throw new ResourceStreamNotFoundException( e );
        }
    }
}