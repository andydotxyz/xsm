/*
 * Created on Apr 20, 2005
 *
 * MetaData - contain a RemoteDocuments metadata and routines
 */
package com.rectang.xsm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;

import org.jdom.Element;

import com.rectang.xsm.io.XSMDocument;

/**
 * @author aje
 *
 * A class for handling the metadata for a RemoteDocument
 */
public class MetaData
        implements Serializable
{

    private XSMDocument owner;
    private Element root;
    private String title, description, lastEditor;
    private Date lastEdited;

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "EE, dd MMM yyyy HH:mm:ss ZZZZ" );

    public MetaData( XSMDocument doc )
    {
        this.owner = doc;
        this.root = doc.getMetadataElement();

        this.title = notNull( root.getChildText( "title" ) );
        this.description = notNull( root.getChildText( "description" ) );
        this.lastEditor = notNull( root.getChildText( "lastEditor" ) );
        try
        {
            this.lastEdited = DATE_FORMAT.parse( root.getChildText( "lastEdited" ) );
        }
        catch ( Exception e )
        {
            this.lastEdited = new Date();
        }
    }

    public void save()
    {
        String locker = owner.getLocked();
        if ( locker != null && !locker.equals( "" ) )
        {
            lastEditor = locker;
        }
        lastEdited = new Date();

        Element node = getNode( "title", root );
        node.setText( title );
        node = getNode( "description", root );
        node.setText( description );
        node = getNode( "lastEditor", root );
        node.setText( lastEditor );
        node = getNode( "lastEdited", root );
        node.setText( DATE_FORMAT.format( lastEdited ) );
    }

    private String notNull( String in )
    {
        if ( in == null )
        {
            return "";
        }
        return in;
    }

    Element getNode( String name, Element parent )
    {
        Element ret = parent.getChild( name );
        if ( ret == null )
        {
            parent.addContent( new Element( name ) );
            ret = parent.getChild( name );
        }
        return ret;
    }

    public XSMDocument getDocument()
    {
        return owner;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle( String title )
    {
        this.title = title;
    }

    /**
     * @return Returns the lastEdited.
     */
    public Date getLastEdited()
    {
        return lastEdited;
    }

    /**
     * @return Returns the lastEditor.
     */
    public String getLastEditor()
    {
        return lastEditor;
    }
}
