package com.rectang.xsm.util;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.types.News;
import org.jdom.Element;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id: RenderUtils.java 731 2008-09-01 19:08:40Z aje $
 * @since 1.0
 */
public class RenderUtils
{
    public static String publish( DocElement type, Element node )
    {
        StringBuffer ret = new StringBuffer();
        type.element.publish( node, ret );
        return ret.toString();
    }

    public static XSMDocument getXSMDoc( Site site, DocumentPage page )
    {
        return XSMDocument.getXSMDoc( site, page );
    }

    public static String renderNewsArchiveBlock( Site site, XSMDocument docPage )
    {
        DateFormat linkFormat = new SimpleDateFormat( "MMMM yyyy" );
        List children = docPage.getContentElement().getChildren( "article" );
        Iterator allChildren = children.iterator();
        int year = 0;
        int month = 0;
        Calendar cal = Calendar.getInstance();
        StringBuffer content = new StringBuffer();
        content.append( "<ul>" );
        while ( allChildren.hasNext() )
        {
            Element next = (Element) allChildren.next();
            Date oldDate = cal.getTime();
            try
            {
                cal.setTime( News.storedFormat.parse( next.getChild( "time" ).getText() ) );
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
                continue;
            }

            if ( year == cal.get( Calendar.YEAR ) && month == cal.get( Calendar.MONTH ) )
            {
                continue;
            }

            if ( year != 0 )
            {
                String link = docPage.getPage().getLink() + year + "/" + (month + 1) + "/";

                content.append( "<li><a href=\"" );
                content.append( link );
                content.append( "\">" );
                content.append( linkFormat.format( oldDate ) );
                content.append( "</a></li>" );
            }

            year = cal.get( Calendar.YEAR );
            month = cal.get( Calendar.MONTH );
        }

        String link = docPage.getPage().getLink() + year + "/" + (month + 1) + "/";

        content.append( "<li><a href=\"" );
        content.append( link );
        content.append( "\">" );
        content.append( linkFormat.format( cal.getTime() ) );
        content.append( "</a></li>" );

        content.append( "</ul>" );
        return content.toString();
    }
}

