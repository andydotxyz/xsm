package com.rectang.xsm.io;

import java.io.*;

import com.rectang.xsm.site.Site;

public class LocalPublishedFile
        extends PublishedFile
{

    public LocalPublishedFile( Site site, String fileName )
    {
        super( site, fileName );
    }

    public OutputStream getOutputStream()
            throws IOException
    {
        return new FileOutputStream( new File( file ) );
    }

    public String toString()
    {
        return "Local file: " + file;
    }

    public boolean exists()
    {
        return (new java.io.File( file )).exists();
    }

    public boolean mkparentdirs()
    {
        if ( (new File( (new java.io.File( file )).getParent() )).exists() )
        {
            return true;
        }
        return (new java.io.File( (new java.io.File( file )).getParent() )).mkdirs();
    }

    public boolean mkdir()
    {
        return (new File( file )).mkdir();
    }

    public boolean delete()
    {
        return deleteDir( new File( file ) );
    }

    public boolean rename( String newName )
    {
        String oldPath = file;
        setFile( newName );

        mkparentdirs();
        return (new File( oldPath )).renameTo( new File( file ) );
    }

    static private boolean deleteDir( File path )
    {
        if ( !path.exists() )
        {
            return true;
        }
        File[] files = path.listFiles();
        if ( files != null )
        {
            for ( int i = 0; i < files.length; i++ )
            {
                if ( files[i].isDirectory() )
                {
                    deleteDir( files[i] );
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public void uploadFile( File f )
    {
        uploadFile( f, true );
    }

    public void uploadFile( File f, boolean delete )
    {
        try
        {
            mkparentdirs();
            if ( delete )
            {
                f.renameTo( new File( file ) );
            }
            else
            {
                (new com.rectang.io.File( f )).copyTo( file );
            }
        }
        catch ( Exception io )
        {
            io.printStackTrace();
        }
    }

    public boolean isDirectory()
    {
        return (new File( file )).isDirectory();
    }

    public long length()
    {
        return (new File( file )).length();
    }
}