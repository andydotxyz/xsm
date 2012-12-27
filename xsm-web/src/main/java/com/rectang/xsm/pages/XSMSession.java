package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.XSM;
import com.rectang.xsm.site.Site;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.Request;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: XSMSession.java 827 2011-09-25 12:18:02Z andy $
 * @since 2.0
 */
public class XSMSession
        extends WebSession
{
    private UserData user;
    private Site fallback;
    private String requestedSite;

    public XSMSession( Request request )
    {
        super( request );

        if ( XSM.getConfig().isDedicated() )
        {
            fallback = new Site( XSM.getConfig().getDedicatedSitename() );
        }
    }

    public UserData getUser()
    {
        return user;
    }

    public void setUser( UserData user )
    {
        this.user = user;
    }

    public void reset()
    {
        user = null;
    }

    public boolean isUserLoggedIn()
    {
        return user != null;
    }

    public Site getSite()
    {
        if ( user == null )
        {
            if ( requestedSite != null && !XSM.getConfig().isDedicated() )
            {
                Site site = new Site( requestedSite );
                if ( site.exists() )
                {
                    return site;
                }
            }

            return fallback;
        }

        return user.getSite();
    }

    public void setSite( Site another )
    {
        if ( !XSM.getConfig().isDedicated() )
        {
            fallback = another;
        }
    }

    public String getRequestedSite()
    {
        return requestedSite;
    }

    public void setRequestedSite( String requestedSite )
    {
        this.requestedSite = requestedSite;
    }
}
