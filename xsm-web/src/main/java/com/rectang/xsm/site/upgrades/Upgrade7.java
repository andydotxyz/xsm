package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.Site;
import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.XSM;
import com.rectang.xsm.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.io.File;

import org.jdom.Element;
import org.apache.wicket.Session;

public class Upgrade7
        implements UpgradeUnit
{

    public int getFromVersion()
    {
        return 7;
    }

    public int getToVersion()
    {
        return 8;
    }

    public String getTitle()
    {
        return "Convert the old XSM admin configuration to proper admin accounts";
    }

    public boolean upgrade( Site site )
    {
        // old code from the XSMConfig file, now removed
        String configStr = XSM.getConfig().getDataDir() + "config.xml";
        File target = new File( XSM.getConfig().getDataDir(), "admins" );
        if ( !(new File( configStr )).exists() )
        {
            return true;
        }
        target.mkdir();

        RemoteDocument config = RemoteDocument.getDoc( configStr );
        Element root = config.getRootElement();
        if ( root == null )
        {
            return true;
        }
        List admins = StringUtils.stringToList( root.getChildText( "admins" ) );

        if ( admins == null || admins.size() == 0 )
        {
            if ( !target.exists() || target.listFiles().length == 0 )
            {
                Session.get().error( "There are no XSM admins configured, please copy a username.xml file from "
                        + XSM.getConfig().getDataDir() + site.getId() + "/members/ to "
                        + target );
            }

            deleteEmptyConfig();
            return true;
        }

        Iterator adminIter = admins.iterator();
        while ( adminIter.hasNext() )
        {
            String admin = (String) adminIter.next();

            String[] parts = admin.split( "@" );
            if ( parts[1].equals( site.getId() ) )
            {
                String username = parts[0];
                // move the user profile over
                File adminFile = new File( target, username + ".xml" );
                File userFile = new File( XSM.getConfig().getDataDir(), site.getId()
                        + "/members/" + username + ".xml" );

                if ( adminFile.exists() )
                {
                    // if the user is already copied over then just remove this extra entry
                    userFile.delete();
                }
                else
                {
                    userFile.renameTo( adminFile );
                }
            }
            adminIter.remove();
        }

        if ( admins.size() == 0 )
        {
            return deleteEmptyConfig();
        }

        return true;
    }

    private boolean deleteEmptyConfig()
    {
        File configFile = new File( XSM.getConfig().getDataDir(), "config.xml" );
        return configFile.delete();
    }
}
