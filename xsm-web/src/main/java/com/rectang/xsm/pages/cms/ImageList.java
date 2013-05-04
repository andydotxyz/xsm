package com.rectang.xsm.pages.cms;

import org.apache.wicket.PageParameters;
import org.jdom.Element;

import java.util.List;
import java.util.Vector;

import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;

/**
 * The list of available page images for WYSIWYG editing in CMS
 *
 * @author Andrew Williams
 * @version $Id: ImageList.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class ImageList
        extends DocumentPage
{
    public ImageList( PageParameters parameters )
    {
        super( parameters );
    }

    public void layout()
    {
        List files = new Vector();

        if ( getDoc() != null )
        {
            Element rootElem = getDoc().getContentElement();
            if ( rootElem != null )
            {
                Element images = rootElem.getChild( "images" );
                if ( images != null )
                {
                    files = images.getChildren( "image" );
                }
            }
        }

        add( new ListView( "images", files )
        {
            protected void populateItem( ListItem listItem )
            {
                Element next = (Element) listItem.getModelObject();
                String path = getDocumentPage().getPublishedPath() + "/_images/" + next.getChildText( "path" );
                String caption = next.getChildText( "caption" );
                if ( caption == null || caption.equals( "" ) )
                {
                    caption = next.getChildText( "path" );
                }

                String line = "  [\"" + caption + "\", \"" + getXSMSession().getSite().getPrefixUrl() + path + "\"]";
                if ( listItem.getIndex() < ((List) listItem.getParent().getDefaultModelObject()).size() - 1 )
                {
                    line += ",\n";
                }

                listItem.add( new Label( "image", line ).setEscapeModelStrings( false ).setRenderBodyOnly( true ) );
                listItem.setRenderBodyOnly( true );
            }

        }.setRenderBodyOnly( true ) );
    }
}
