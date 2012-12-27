package com.rectang.xsm.types;

import org.jdom.Element;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.widget.HTMLTextArea;

import java.util.Vector;
import java.util.List;
import java.util.regex.Pattern;

public class PHP
        extends DocList
        implements PHPFile
{

    public static final SupportedOption WELCOME_PAGE = new SupportedOption( "WELCOME_PAGE",
            "Is this a \"welcome page\" (a page without the normal site design). "
                    + "Welcome pages define the whole page content", false );

    private Vector options;

    public PHP( java.lang.String name )
    {
        super( name, new DocElement[]{new HTMLTextArea( "content", false ),
                new Gallery( "images" ),
                new DocGroup( "files", new FilesItem( "file" ) )} );

        options = new Vector();
        options.add( WELCOME_PAGE );
    }

    public void view( Element root, StringBuffer s )
    {
        String old = root.getChildText( "content" );

        // hide php code from the preview :)
        Pattern p = Pattern.compile( "<\\?.*?\\?>", Pattern.DOTALL );
        String replaced = p.matcher( old ).replaceAll( "<span class=\"xsm-editor-php-mask\">php code</span>" );

        Element node = new Element( "content" );
        node.setText( replaced );
        elements[0].view( node, s );
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