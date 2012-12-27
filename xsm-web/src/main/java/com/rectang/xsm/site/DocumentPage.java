/*
 * Created on Dec 20, 2004
 *
 */
package com.rectang.xsm.site;

import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.UserData;

/**
 * @author aje
 *
 * Represents a page in a site structure
 */
public class DocumentPage
        extends HierarchicalPage
{

    public DocumentPage( Site site, HierarchicalPage parent, String title )
    {
        this( site, parent, title, false );
    }

    public DocumentPage( Site site, HierarchicalPage parent, String title, boolean hidden )
    {
        super( site, parent, title, hidden );
    }

    public XSMDocument getXSMDocument()
    {
        try
        {
            return XSMDocument.getXSMDoc( getSite(), this );
        }
        catch ( RuntimeException e )
        {
            System.err.println( "Error reading file" + getTitle() );
            throw e;
        }
    }

    public boolean publish( UserData user )
    {
        XSMDocument doc = getXSMDocument();
        return (doc != null && doc.publish( user ));
    }

    public String getType()
    {
        return "page";
    }

    public boolean isPublishable()
    {
        return true;
    }
}
