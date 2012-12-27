package com.rectang.xsm;

import com.rectang.xsm.site.Site;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * A config class to read the xsm.properties and xsm_local.properties (if
 * available). All XSM configuration should be accessed through this class.
 *
 * @author aje
 */
public class Config
        implements Serializable
{

    private String rootDir;
    private String dataDir, logDir, tmpDir;
    private String version, url, theme;
    private String dedicatedSite;

    private String emailFrom, smtpHost, smtpUser, smtpPass;

    private static Config instance = null;

    public static Config getInstance( String rootDir )
    {
        if ( instance == null )
        {
            instance = new Config( rootDir );
        }
        return instance;
    }

    public static boolean isInstalled()
    {
        return (new File( "/etc/xsm" )).exists();
    }

    /**
     * Initiate the XSM config - this loads xsm.properties with overrides from
     * xsm_local.properties if it is available in either /etc/xsm
     * <tomcat_dir>/webapps or the classpath.
     *
     * @param rootDir The root directory of the web application (ends in xsm/)
     */
    private Config( String rootDir )
    {
        this.rootDir = rootDir;
        ResourceBundle props = ResourceBundle.getBundle( "xsm",
                new java.util.Locale( "local" ) /* dummy locale - allows overriding */,
                new XSMClassLoader( rootDir, this.getClass().getClassLoader() ) );
        load( props );
    }

    /**
     * Load the config from xsm_local.properties or (fallback) xsm.properties
     */
    protected void load( ResourceBundle props )
    {
        dataDir = props.getString( "xsm.data.dir" );
        logDir = props.getString( "xsm.log.dir" );
        tmpDir = props.getString( "xsm.tmp.dir" );
        version = props.getString( "xsm.version" );
        url = props.getString( "xsm.url" );
        theme = props.getString( "xsm.theme" );

        dedicatedSite = props.getString( "xsm.sitename" );

        emailFrom = props.getString( "xsm.email.from" );
        smtpHost = props.getString( "xsm.email.smtphost" );
        smtpUser = props.getString( "xsm.email.smtpuser" );
        smtpPass = props.getString( "xsm.email.smtppass" );
    }

    /**
     * Get the root directory if this XSM application
     *
     * @return the root directory of this XSM (&lt;tomcat_root&gt;/webapps/xsm/)
     */
    public String getRootDir()
    {
        return rootDir;
    }

    public String getSiteDataDir( Site site )
    {
        if ( site == null )
        {
            return null;
        }
        return (new File( new File( dataDir, site.getId() ), "data" )).getPath();
    }

    public String getSiteTemplateDir( Site site )
    {
        if ( site == null )
        {
            return null;
        }
        return (new File( new File( dataDir, site.getId() ), "template" )).getPath();
    }

    /**
     * Get the directory containing the XSM data. Defaults to "/etc/xsm/".
     *
     * @return The directory containing the XSM data.
     */
    public String getDataDir()
    {
        return dataDir;
    }

    /**
     * Get the directory XSM should log to. Defaults to "/var/log/xsm/".
     *
     * @return The directory XSM should log to.
     */
    public String getLogDir()
    {
        return logDir;
    }

    /**
     * Get the directory XSM should use for temporary files.
     * Defaults to "/tmp/".
     *
     * @return The directory XSM should use for tmp files.
     */
    public String getTmpDir()
    {
        return tmpDir;
    }

    /**
     * Get the XSM version number.
     *
     * @return The current XSM version.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Get the URL at which XSM is located. Defaults to "http://localhost:8080/xsm/".
     *
     * @return The URL at which XSM is located.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Get the sitename for a dedicated site, if one is configured - default is null.
     *
     * @return the sitename to use if we are dedicated to one site.
     */
    public String getDedicatedSitename()
    {
        return dedicatedSite;
    }

    /**
     * Get whether or not we are serving a dedicated site - default is false.
     *
     * @return the true if a dedicated site is configured, false otherwise.
     */
    public boolean isDedicated()
    {
        return dedicatedSite != null && dedicatedSite.length() > 0;
    }

    /**
     * Get the default theme for XSM. Defaults to "blue".
     *
     * @return The URL default theme for XSM.
     */
    public String getTheme()
    {
        return theme;
    }

    /**
     * Get the email address XSM should use in the From: field of outgoing emails.
     *
     * @return The email address XSM should send emails from
     */
    public String getEmailFrom()
    {
        return emailFrom;
    }

    /**
     * Get the host for the SMTP server used to send emails.
     *
     * @return The host address for the SMTP server
     */
    public String getSmtpHost()
    {
        return smtpHost;
    }

    /**
     * Get the smtp username (default is blank)
     *
     * @return The username for logging in to the SMTP server
     */
    public String getSmtpPass()
    {
        return smtpPass;
    }

    /**
     * Get the smtp password (default is blank)
     *
     * @return The password for logging in to the SMTP server
     */
    public String getSmtpUser()
    {
        return smtpUser;
    }
}

/**
 * A simple ClassLoader that searches for resources /etc/xsm and 
 * &lt;tomcat_root&gt;/webapps/ as well as the classpath.
 *
 * @author aje
 */
class XSMClassLoader
        extends ClassLoader
{

    String[] locations;

    protected XSMClassLoader( String rootDir, ClassLoader parent )
    {
        super( parent );

        locations = new String[]{"/etc/xsm/",
                rootDir.substring( 0, rootDir.length() - 4 ) /* tomcat webapp dir */};
    }

    /**
     * Searches for the resource named <code>name</code>. First searching in
     * /etc/xsm/ then &lt;tomcat_root&gt;/webapps/ and then falls back to the
     * parent classloader which will search the classpath.
     *
     * @see java.lang.ClassLoader#getResource(java.lang.String)
     */
    public URL getResource( String name )
    {

        for ( int i = 0; i < locations.length; i++ )
        {
            String next = locations[i] + name;
            if ( (new File( next )).exists() )
            {
                try
                {
                    return new URL( "file:" + next );
                }
                catch ( Exception e )
                {
          /* fall through */
                }
            }
        }

        return super.getResource( name );
    }
}