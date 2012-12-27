package com.rectang.xsm.doc;

import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.UserData;

import java.util.Vector;
import java.io.Serializable;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class DocElement
        implements Serializable
{

    protected String name;
    public DocElement element;
    private DocumentPage page;
    private UserData user;
    private XSMDocument doc;

    /**
     * @return Returns the page.
     */
    public DocumentPage getPage()
    {
        return page;
    }

    /**
     * @return The path to our page
     */
    public String getPath()
    {
        return page.getPath();
    }

    /**
     * @return The path to our published page
     */
    public String getPublishedPath()
    {
        return page.getPublishedPath();
    }

    /**
     * @param page The page to set.
     */
    public void setPage( DocumentPage page )
    {
        if ( element != null )
        {
            element.setPage( page );
        }
        this.page = page;
    }

    public String getName()
    {
        return name;
    }

    public DocElement()
    {
        this( "", null );
    }

    public DocElement( String name )
    {
        this( name, null );
    }

    public DocElement( String name, DocElement element )
    {
        this.name = name;
        this.element = element;
    }

    public void view( Element node, StringBuffer s )
    {
        publish( node, s );
    }

    public void publish( Element node, StringBuffer s )
    {
        if ( element != null )
        {
            element.publish( node.getChild( element.name ), s );
        }
    }

    public WebMarkupContainer edit( String wicketId, Element node, String path )
    {
        if ( element != null )
        {
            return element.edit( wicketId, node.getChild( element.name ), path + "/" + element.name );
        }

        return new WebMarkupContainer( wicketId );
    }

    public void create( Element node )
    {
        Element newNode;
        if ( element != null )
        {
            node.addContent( newNode = new Element( element.name ) );
            element.create( newNode );
        }
    }

    public void destroy( Element node )
    {
        if ( element != null )
        {
            element.destroy( node.getChild( element.name ) );
        }
    }

    public DocElement getElement( String name )
    {
        if ( element != null && element.name != null && element.name.equals( name ) )
        {
            return element;
        }
        return null;
    }

    public String pathToId( String path )
    {
        return "xsm_val" + path.replaceAll( "//", "_" ).replace( '/', '_' );
    }

    /**
     * @return Returns the userdata.
     */
    public UserData getUser()
    {
        return user;
    }

    /**
     * @param user The userdata to set.
     */
    public void setUser( UserData user )
    {
        this.user = user;
        if ( element != null )
        {
            element.setUser( user );
        }
    }

    /**
     * @return the site the current user is logged in to
     */
    public Site getSite()
    {
        return user.getSite();
    }

    /**
     * @return Returns the document.
     */
    public XSMDocument getDoc()
    {
        return doc;
    }

    /**
     * @param doc The doc to set.
     */
    public void setDoc( XSMDocument doc )
    {
        if ( element != null )
        {
            element.setDoc( doc );
        }
        this.doc = doc;
    }

    /**
     * A list of all supported options for this page type. This returns an iterator of a vector containing
     * <code>SupportedOption</code>s.
     *
     * @return an iteration through the <code>SupportedOption</code>s for this page
     */
    public java.util.List getSupportedOptions()
    {
        if ( element != null )
        {
            return element.getSupportedOptions();
        }
        return new Vector();
    }
}