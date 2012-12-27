package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.cms.Edit;
import com.rectang.xsm.types.News;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Dashboard.java 819 2010-05-30 14:21:32Z andy $
 * @since 2.0
 */
public class Dashboard
        extends XSMPage
        implements Secure
{
    DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.MEDIUM );
    DateFormat timeFormat = DateFormat.getTimeInstance( DateFormat.SHORT );

    public Dashboard( PageParameters parameters )
    {
        super( parameters );
    }

    public int getLevel()
    {
        return AccessControl.MEMBER;
    }

    public void layout()
    {
        super.layout();

        UserData user = getXSMSession().getUser();

        add( new Label( "name", user.getName() ) );

        Date now = new Date();
        add( new Label( "date", dateFormat.format( now ) ) );
        add( new Label( "time", timeFormat.format( now ) ) );

        if ( user.isFirstLogin() )
        {
            add( new Label( "lastlog", "This is your first login, congratulations!" ) );
        }
        else
        {
            add( new Label( "lastlog", "You last logged in on " +
                    dateFormat.format( user.getLastLogin() ) + " at " +
                    timeFormat.format( user.getLastLogin() ) + "." ) );
        }

        add( new ListView( "newsPages", getNewsPages() )
        {
            public void populateItem( ListItem item )
            {
                final DocumentPage next = (DocumentPage) item.getModelObject();
                WebMarkupContainer link = new Link( "newsPage" )
                {
                    public void onClick()
                    {
                        News type = (News) next.getXSMDocument().getType( getXSMSession().getUser() );
                        type.addChildAtTop( next.getXSMDocument().getContentElement() );
                        if ( next.getXSMDocument().save( getXSMSession().getUser() ) )
                        {

                            PageParameters params = new PageParameters();
                            params.add( "page", next.getPath() );
                            setResponsePage( Edit.class, params );
                        }
                    }
                };
                link.add( new Label( "newsPageLabel", next.getTitle() ) );
                item.add( link );
            }
        } );
        add( new ListView( "recentPages", getRecentPages() )
        {
            public void populateItem( ListItem item )
            {
                Page next = (Page) item.getModelObject();
                PageParameters params = new PageParameters();
                params.add( "page", next.getPath() );
                BookmarkablePageLink link = new BookmarkablePageLink( "recentPage",
                        Edit.class, params );
                link.add( new Label( "recentPageLabel", next.getTitle() ) );
                item.add( link );
            }
        } );
    }

    // TODO for both check permissions !!

    // news pages must be DocumentPages
    public List getNewsPages()
    {
        List newsPages = new Vector();

        Iterator pages = getXSMSession().getSite().getAllPages();
        while ( pages.hasNext() )
        {
            Page next = (Page) pages.next();
            if ( !(next instanceof DocumentPage) )
            {
                continue;
            }
            DocumentPage page = (DocumentPage) next;

            if ( page.getXSMDocument().getType( getXSMSession().getUser() )
                    instanceof News )
            {
                newsPages.add( page );
            }
        }

        return newsPages;
    }

    // TODO return top 5 recent pages
    // Only document pages returned, they are the only pages with metadata at the moment
    public List getRecentPages()
    {
        DocumentPage newest = null;
        Date newestEdit = new Date( 0 );

        Iterator pages = getXSMSession().getSite().getAllPages();
        while ( pages.hasNext() )
        {
            Page next = (Page) pages.next();
            if ( !(next instanceof DocumentPage) )
            {
                continue;
            }
            DocumentPage page = (DocumentPage) next;

            Date edit = page.getXSMDocument().getMetadata().getLastEdited();
            if ( edit.after( newestEdit ) )
            {
                newestEdit = edit;
                newest = page;
            }
        }

        List ret = new Vector();
        ret.add( newest );
        return ret;
    }
}
