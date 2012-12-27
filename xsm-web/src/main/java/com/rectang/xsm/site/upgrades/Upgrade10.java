package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Page;
import com.rectang.xsm.site.Site;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;

public class Upgrade10
        implements UpgradeUnit
{

    public int getFromVersion()
    {
        return 10;
    }

    public int getToVersion()
    {
        return 11;
    }

    public String getTitle()
    {
        return "Add required parameter to Form fields";
    }

    public boolean upgrade( Site site )
    {
        Iterator pages = site.getAllPages();
        while ( pages.hasNext() )
        {
            Page nextPage = (Page) pages.next();
            if ( !(nextPage instanceof DocumentPage) )
            {
                continue;
            }
            DocumentPage next = (DocumentPage) nextPage;

            try
            {
                XSMDocument doc = next.getXSMDocument();

                Element previewedFile = doc.getContentElement();
                if ( !previewedFile.getName().equals( "form" ) )
                {
                    continue;
                }

                System.out.println( "upgrade 10 - " + doc.getPath() );

                List items = previewedFile.getChild( "fields" ).getChildren( "field" );
                if ( items == null || items.size() == 0 )
                {
                    continue;
                }

                Iterator itemIter = items.iterator();
                while ( itemIter.hasNext() )
                {
                    Element nextItem = (Element) itemIter.next();
                    int index = nextItem.indexOf( nextItem.getChild( "type" ) );

                    nextItem.addContent( ++index, new Element( "required" ) );
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
