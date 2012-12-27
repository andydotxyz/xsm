package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.*;

import com.rectang.xsm.io.XSMDocument;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class Upgrade1
        implements UpgradeUnit
{

    public int getFromVersion()
    {
        return 1;
    }

    public int getToVersion()
    {
        return 2;
    }

    public String getTitle()
    {
        return "Add metadata to PreviewedFile pages";
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

                Element previewedFile = doc.getContentElement();
                if ( !previewedFile.getName().equals( "previewedfile" ) )
                {
                    continue;
                }

                System.out.println( "upgrade 1 - " + doc.getPath() );

                List items = previewedFile.getChildren( "item" );
                if ( items == null || items.size() == 0 )
                {
                    continue;
                }

                Iterator itemIter = items.iterator();
                while ( itemIter.hasNext() )
                {
                    Element nextItem = (Element) itemIter.next();
                    int index = nextItem.indexOf( nextItem.getChild( "title" ) );

                    nextItem.addContent( ++index, new Element( "author" ) );
                    nextItem.addContent( ++index, new Element( "lastupdated" ) );
                    nextItem.addContent( ++index, new Element( "license" ) );
                    nextItem.addContent( ++index, new Element( "comment" ) );

                    index = nextItem.indexOf( nextItem.getChild( "description" ) );
                    Element miscdata;
                    nextItem.addContent( ++index, miscdata = new Element( "miscdata" ) );
                    miscdata.setAttribute( "next_index", "1" );
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
