package com.rectang.xsm.types;

import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.widget.HTMLTextArea;

public class Html
        extends DocList
{

    public static final SupportedOption WYSIWYG_ENABLED = new SupportedOption( "WYSIWYG_ENABLED",
            "Allow use of WYSIWYG editors on this page (set to 'no' if you want to use javascript etc)", true );
    public static final SupportedOption WELCOME_PAGE = new SupportedOption( "WELCOME_PAGE",
            "Is this a \"welcome page\" (a page without the normal site design). "
                    + "Welcome pages define the whole page content", false );

    private Vector options;

    public Html( java.lang.String name )
    {
        super( name, new DocElement[]{new HTMLTextArea( "content" ),
                new DocGroup( "images", new GalleryItem( "image" ) ),
                new DocGroup( "files", new FilesItem( "file" ) )} );

        options = new Vector();
        options.add( WYSIWYG_ENABLED );
        options.add( WELCOME_PAGE );
    }

    public void view( Element root, StringBuffer s )
    {
        elements[0].view( root.getChild( "content" ), s );
    }

    public WebMarkupContainer edit( String wicketId, Element node, String path )
    {
        ((HTMLTextArea) elements[0]).setSupportWYSIWYG(
                WYSIWYG_ENABLED.getBoolean( getDoc() ) );
        return super.edit( wicketId, node, path );
    }

    public void publish( Element root, StringBuffer s )
    {
        elements[0].publish( root.getChild( "content" ), s );
    }

    public List getSupportedOptions()
    {
        Vector ret = new Vector();
        ret.addAll( options );
        for ( int i = 0; i < elements.length; i++ )
        {
            ret.addAll( elements[i].getSupportedOptions() );
        }
        return ret;
    }

}