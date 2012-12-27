package com.rectang.xsm.pages.admin;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.UserData;
import com.rectang.xsm.XSM;

import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.pages.XSMPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.util.*;

/**
 * Regenerate the site - A nasty hack until we can stop using jsp!
 *
 * @author Andrew Williams
 * @version $Id: Publish.java 832 2011-09-26 21:45:04Z andy $
 * @since 2.0
 */
public class Publish
        extends XSMPage
        implements Secure
{
    public Publish( PageParameters parameters )
    {
        super( parameters );
    }

    public int getLevel()
    {
        return AccessControl.MANAGER;
    }

    public void layout()
    {
        super.layout();

        UserData user = getXSMSession().getUser();

        final Map results = user.getSite().publish( user );
        List paths = new LinkedList( results.keySet() );
        Collections.sort( paths );

        add( new ListView<String>( "status-output", paths )
        {
            protected void populateItem( ListItem listItem )
            {
                String path = (String) listItem.getModelObject();
                listItem.add( new Label( "path", path ) );

                Boolean ok = (Boolean) results.get( path );
                if ( ok == Boolean.TRUE )
                {
                    listItem.add( new Image( "icon", new ResourceReference( XSM.class, "icons/emblem-default.png" ) ) );
                }
                else
                {
                    listItem.add( new Image( "icon", new ResourceReference( XSM.class, "icons/emblem-important.png" ) ) );
                }
            }

        } );
    }
}