package com.rectang.xsm.io;

import java.io.*;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.rectang.xsm.XSM;

import com.rectang.xsm.site.Site;

import com.jcraft.jsch.*;

public class SshPublishedFile
        extends PublishedFile
{

    private static int TIMEOUT = 60000;
    private static JSch sshController;

    public SshPublishedFile( Site site, String fileName )
    {
        super( site, fileName );

        if ( sshController == null )
        {
            sshController = new JSch();
            try
            {
                sshController.addIdentity( "/etc/xsm/id_rsa" );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public OutputStream getOutputStream()
            throws IOException
    {
        File tmp = XSM.getTempFile();
        return new OutputStreamWrapper( tmp );
    }

    public String toString()
    {
        return "Ssh file: " + getSite().getRemoteHost() + ":" + file;
    }

    public boolean exists()
    {
        return exists( file );
    }

    private boolean exists( String f )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );
        try
        {
            return sshChannel.ls( f ).size() > 0;
        }
        catch ( SftpException e )
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean mkparentdirs()
    {
        String parent = (new File( file )).getParentFile().getPath();
        return mkdir( parent ); /* this checks for existance */
    }

    public boolean mkdir()
    {
        return mkdir( file );
    }

    private boolean mkdir( String f )
    {
        if ( exists( f ) )
        {
            return true;
        }
        ChannelSftp sshChannel = getSftpChannel( getSite() );
        try
        {
            File parentFile = (new File( f )).getParentFile();
            if ( parentFile != null )
            {
                String parent = parentFile.getPath();
                mkdir( parent ); /* this checks for existance */
            }

            sshChannel.mkdir( f );
            return true;
        }
        catch ( SftpException e )
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete()
    {
        return deleteDir( file );
    }

    private boolean delete( String rmFile )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );
        try
        {
            sshChannel.rm( rmFile );
            return true;
        }
        catch ( SftpException e )
        {
            try
            {
                sshChannel.rmdir( rmFile );
                return true;
            }
            catch ( SftpException e2 )
            {
                e2.printStackTrace();
            }
        }
        return false;
    }

    private boolean deleteDir( String path )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );
        if ( !exists( path ) )
        {
            return true;
        }

        Vector files = null;
        try
        {
            files = sshChannel.ls( path );
        }
        catch ( Exception e )
        {
      /* fall through to deleting the path */
        }
        if ( files != null && files.size() != 0 )
        {
            Iterator fileList = files.iterator();
            while ( fileList.hasNext() )
            {
                String next = fileList.next().toString().trim();
                int pos = next.lastIndexOf( ' ' );

                String name = next.substring( pos + 1 );
                if ( name.equals( "." ) || name.equals( ".." ) )
                {
                    continue;
                }

                String subPath = path + "/" + name;
                if ( isDirectory( subPath ) )
                {
                    deleteDir( subPath );
                }
                else
                {
                    delete( subPath );
                }
            }
        }

        return delete( path );
    }

    public boolean isDirectory()
    {
        return isDirectory( file );
    }

    private boolean isDirectory( String path )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );

        try
        {
            Vector list = sshChannel.ls( path );
            if ( list == null || (list.size() == 1  /* 1 entry for non-directory */
          /* if 1 entry make sure it is a file */
                    && ((String) list.get( 0 )).trim().endsWith(
                    ((new File( path )).getParentFile().getName()) )) )
            {
                return false;
            }
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    public long length()
    {
        return length( file );
    }

    private long length( String path )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );

        try
        {
            SftpATTRS atts = sshChannel.lstat( path );
            if ( atts == null )
            {
                return 0;
            }
            return atts.getSize();
        }
        catch ( Exception e )
        {
            return 0;
        }
    }

    public boolean rename( String newName )
    {
        ChannelSftp sshChannel = getSftpChannel( getSite() );
        try
        {
            String oldPath = file;
            setFile( newName );

            mkparentdirs();
            sshChannel.rename( oldPath, file );
            return true;
        }
        catch ( SftpException e )
        {
            e.printStackTrace();
        }
        return false;
    }

    public void uploadFile( File f )
    {
        uploadFile( f, true );
    }

    public void uploadFile( File f, boolean delete )
    {
        try
        {
            if ( !f.exists() )
            {
                return;
            }

            mkparentdirs();
            ChannelSftp sshChannel = getSftpChannel( getSite() );
            try
            {
                sshChannel.put( new FileInputStream( f ), file );

                if ( delete )
                {
                    f.delete();
                }
            }
            catch ( SftpException e )
            {
                e.printStackTrace();
            }
        }
        catch ( Exception io )
        {
            io.printStackTrace();
        }
    }

    class OutputStreamWrapper
            extends FileOutputStream
    {
        private File f;

        public OutputStreamWrapper( File f )
                throws IOException
        {
            super( f );
            this.f = f;
        }

        public void close()
                throws IOException
        {
            super.close();
            uploadFile( f, true );
        }
    }

    private static Hashtable channelHash = new Hashtable();

    public static ChannelSftp getSftpChannel( Site site )
    {
        TimeoutChannel chan = (TimeoutChannel) channelHash.get( site );

        if ( chan != null )
        {
            if ( chan.timeout < System.currentTimeMillis() )
            {
                try
                {
                    chan.channel.exit();
                    chan.channel.disconnect();
                }
                catch ( Exception e )
                {
          /* we ignore these */
                }
                chan = null;
            }
        }
        if ( chan == null )
        {
            try
            {
                Session sess =
                        sshController.getSession( site.getRemoteUser(), site.getRemoteHost(), 22 );

                java.util.Hashtable config = new java.util.Hashtable();
                config.put( "StrictHostKeyChecking", "no" );
                sess.setConfig( config );

                sess.connect();
                chan = new TimeoutChannel( (ChannelSftp) sess.openChannel( "sftp" ),
                        System.currentTimeMillis() + TIMEOUT );
                chan.channel.connect();
                channelHash.put( site, chan );
            }
            catch ( Exception e )
            {
                e.printStackTrace();

        /* TODO better way of erroring... */
                return null;
            }
        }
        return chan.channel;
    }

    private static class TimeoutChannel
    {
        protected long timeout;
        protected ChannelSftp channel;

        public TimeoutChannel( ChannelSftp chan, long time )
        {
            this.channel = chan;
            this.timeout = time;
        }
    }

}