package com.rectang.xsm.pages.nav;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;

/**
 * The main CMS view tab
 *
 * @author Andrew Williams
 * @version $Id: LinkView.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class LinkView
        extends LinkPage
{
    public LinkView( PageParameters parameters )
    {
        super( parameters );
    }

    public void layout()
    {
        super.layout();

        ExternalLink link = new ExternalLink( "link", getXSMPage().getLink() );
        link.add( new Label( "link-label", getXSMPage().getLink() ) );
        add( link );
    }
}
