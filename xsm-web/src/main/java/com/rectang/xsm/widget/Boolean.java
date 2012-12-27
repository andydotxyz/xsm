package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jdom.Element;

import java.io.Serializable;
import java.lang.*;

public class Boolean
        extends DocWidget
        implements Serializable
{

    public Boolean( java.lang.String name )
    {
        super( name );
    }

    private java.lang.String format( java.lang.String in )
    {
        if ( in.equalsIgnoreCase( "true" ) )
        {
            return "YES";
        }

        return "NO";
    }

    public void view( Element node, StringBuffer s )
    {
        s.append( format( node.getValue() ) );
    }

    public void publish( Element node, StringBuffer s )
    {
        s.append( format( node.getValue() ) );
    }

    public WebMarkupContainer edit( java.lang.String wicketId, Element node, java.lang.String path )
    {
        return new BooleanPanel( wicketId, node );
    }

    public void create( Element node )
    {
        node.setText( "" );
    }

    class BooleanPanel
            extends Panel
    {
        boolean checked;

        public BooleanPanel( java.lang.String id, final Element node )
        {
            super( id );
            checked = java.lang.Boolean.parseBoolean( node.getText() );

            add( new Label( "label", name ) );
            add( new org.apache.wicket.markup.html.form.CheckBox( "value", new PropertyModel( node, "text" ) )
            {
                protected void onSelectionChanged( Object newSelection )
                {
                    node.setText( newSelection.toString() );
                }

                protected boolean wantOnSelectionChangedNotifications()
                {
                    return true;
                }
            } );
        }
    }
}