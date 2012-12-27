package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.*;

import com.rectang.xsm.io.XSMDocument;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class Upgrade4
        implements UpgradeUnit
{

    public int getFromVersion()
    {
        return 4;
    }

    public int getToVersion()
    {
        return 5;
    }

    public String getTitle()
    {
        return "Add comments and user IDs to news articles";
    }

    public boolean upgrade( Site site )
    {
        Iterator pages = site.getAllPages();
        while ( pages.hasNext() )
        {
            DocumentPage next = (DocumentPage) pages.next();

            try
            {
                XSMDocument doc = next.getXSMDocument();

                Element gallery = doc.getContentElement();
                if ( !gallery.getName().equals( "news" ) )
                {
                    continue;
                }

                System.out.println( "upgrade 4 - " + doc.getPath() );

                List items = gallery.getChildren( "article" );
                if ( items == null || items.size() == 0 )
                {
                    continue;
                }

                Iterator itemIter = items.iterator();
                while ( itemIter.hasNext() )
                {
                    Element nextItem = (Element) itemIter.next();
                    int index = nextItem.indexOf( nextItem.getChild( "author" ) );

                    nextItem.addContent( ++index, new Element( "uid" ) );
                    index = nextItem.indexOf( nextItem.getChild( "time" ) );
                    Element comments;
                    nextItem.addContent( ++index, comments = new Element( "comments" ) );
                    comments.setAttribute( "next_index", "1" );
                }
                doc.save();
            }
            catch ( Exception e )
            {
                System.err.println( "Failed on file " + next.getPath() + e.getMessage() );
                return false;
            }

        }
        return true;
    }

}
