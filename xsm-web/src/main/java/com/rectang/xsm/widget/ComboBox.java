package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;

import java.io.Serializable;
import java.util.List;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

/**
 * A combobox item that lists a set of strings for the user to choose from.
 *
 * @author Andrew Williams
 * @version $Id: ComboBox.java 786 2009-04-02 22:25:55Z andy $
 * @since 1.2
 */
public class ComboBox
        extends DocWidget
        implements Serializable
{
    private List items;

    public ComboBox( java.lang.String name, List items )
    {
        super( name );

        this.items = items;
    }

    public void view( Element node, StringBuffer s )
    {
        s.append( node.getValue() );
    }

    public void publish( Element node, StringBuffer s )
    {
        s.append( node.getValue() );
    }

    public WebMarkupContainer edit( java.lang.String wicketId, Element node, java.lang.String path )
    {
        return new ComboBoxPanel( wicketId, node );
    }

    public void create( Element node )
    {
        node.setText( "" );
    }

    class ComboBoxPanel
            extends Panel
    {
        public ComboBoxPanel( java.lang.String id, Element node )
        {
            super( id );

            add( new Label( "label", name ) );
            add( new DropDownChoice( "value", new PropertyModel( node, "text" ), items ) );
        }
    }
}