package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.Site;

import java.io.Serializable;

public interface UpgradeUnit
        extends Serializable
{

    public int getFromVersion();

    public int getToVersion();

    public String getTitle();

    public boolean upgrade( Site site );
}
