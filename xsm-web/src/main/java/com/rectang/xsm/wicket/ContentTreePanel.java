package com.rectang.xsm.wicket;

import com.rectang.xsm.pages.cms.View;
import com.rectang.xsm.pages.nav.LinkView;
import com.rectang.xsm.site.HierarchicalPage;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.PageParameters;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

import com.rectang.xsm.site.Page;

/**
 * A simple tree renderer
 *
 * @author Andrew Williams
 * @version $Id: ContentTreePanel.java 831 2011-09-25 12:59:18Z andy $
 * @since 1.0
 */
public class ContentTreePanel
        extends Panel
{
    public ContentTreePanel( String id, HierarchicalPage rootPage, final String current )
    {
        super( id );

        add( new ListView<Page>( "pages", rootPage.getSubPages() )
        {

            protected void populateItem( ListItem listItem )
            {
                final Page page = (Page) listItem.getModelObject();
                Class linkClass = View.class;
                if ( page.getType().equals( "link" ) )
                {
                    linkClass = LinkView.class;
                }

                PageParameters params = new PageParameters();
                params.add( "page", page.getPath() );
                BookmarkablePageLink link = new BookmarkablePageLink( "page", linkClass, params );
                listItem.add( link );

                String title = page.getTitle();
                if ( page.getHidden() )
                {
                    title = "(" + title + ")";
                }
                link.add( new Label( "page-label", title ) );

                listItem.add( new AttributeModifier( "class", new Model()
                {
                    public String getObject()
                    {
                        String style = "xsm_menu_item";
                        if ( page.getPath().equals( current ) )
                        {
                            style += " xsm_menu_item_selected";
                        }
                        return style + " " + page.getType();
                    }
                } ) );

                if ( page instanceof HierarchicalPage
                        && ((HierarchicalPage) page).getSubPages().size() > 0 )
                {
                    listItem.add( new ContentTreePanel( "subpages", (HierarchicalPage) page, current ) );
                }
                else
                {
                    listItem.add( new WebMarkupContainer( "subpages" ).setVisible( false ) );
                }
            }
        } );
    }
}
