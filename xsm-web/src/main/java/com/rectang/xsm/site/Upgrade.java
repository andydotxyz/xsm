package com.rectang.xsm.site;

import java.util.Vector;
import java.util.List;

import com.rectang.xsm.site.upgrades.UpgradeUnit;

public class Upgrade
{

    public static List /* UpgradeUnit */ listRequiredUpgrades( Site site )
    {
        int version = site.getVersion();
        Vector list = new Vector();
        while ( version < Site.getCurrentVersion() )
        {
            UpgradeUnit next = loadUnit( version );

            //FIXME next could be null...
            list.add( next );
            version = next.getToVersion();
        }

        return list;
    }

    public static UpgradeUnit loadUnit( int versionFrom )
    {

        try
        {
            Class uuClass = Class.forName( "com.rectang.xsm.site.upgrades.Upgrade"
                    + versionFrom );
            return (UpgradeUnit) uuClass.newInstance();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
