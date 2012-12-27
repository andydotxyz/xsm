package com.rectang.xsm.doc;

import com.rectang.xsm.XSM;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.ResourceReference;

import org.jdom.Element;

import java.util.List;
import java.util.Arrays;
import java.util.Vector;

public class DocChoice
        extends DocList
{

    public DocChoice( String name, DocElement[] elements )
    {
        super( name, elements );
    }

    public void view( Element node, StringBuffer s )
    {
        for ( int i = 0; i < elements.length; i++ )
        {
            if ( elements[i] != null && node != null )
            {
                Element possible = node.getChild( elements[i].getName() );
                if ( possible != null )
                {
                    elements[i].view( possible, s );
                    return;
                }
            }
        }
    }

    public void publish( Element node, StringBuffer s )
    {
        for ( int i = 0; i < elements.length; i++ )
        {
            if ( elements[i] != null && node != null )
            {
                Element possible = node.getChild( elements[i].getName() );
                if ( possible != null )
                {
                    elements[i].publish( possible, s );
                    return;
                }
            }
        }
    }

    public WebMarkupContainer edit( String wicketId, Element node, String path )
    {
        Panel ret = new ChoicePanel( wicketId, node, path );

        return ret;
    }

    public void create( Element node )
    {
    /* no more created here just now - all optional */
    }

    public void destroy( Element node )
    {
        for ( int i = 0; i < elements.length; i++ )
        {
            if ( elements[i] != null && node != null )
            {
                Element possible = node.getChild( elements[i].getName() );
                if ( possible != null )
                {
                    elements[i].destroy( possible );
                }
            }
        }
    }

    class ChoicePanel
            extends Panel
    {
        public ChoicePanel( final String wicketId, final Element node, final String path )
        {
            super( wicketId );

            boolean added = false;
            for ( int i = 0; i < elements.length; i++ )
            {
                if ( elements[i] != null && node != null )
                {
                    Element possible = node.getChild( elements[i].getName() );
                    if ( possible != null )
                    {
                        add( elements[i].edit( "content", possible,
                                path + "/" + elements[i].getName() + "@0" ) );
                        added = true;
                        continue;
                    }
                }
            }
            if ( !added )
            {
                add( new WebMarkupContainer( "content" ) );
            }

            // only display the add links if we have not added content already
            List elementList = new Vector();
            if ( !added )
            {
                // TODO don't reconstruct this all the time
                elementList = Arrays.asList( elements );
            }

            add( new ListView( "elements", elementList )
            {
                protected void populateItem( ListItem listItem )
                {
                    final DocElement elem = (DocElement) listItem.getModelObject();

                    Link add;
                    listItem.add( add = new Link( "add" )
                    {
                        public void onClick()
                        {
                            Element child = new Element( elem.getName() );
                            node.addContent( child );
                            elem.create( child );
                        }
                    } );
                    add.add( new Image( "add-icon", new ResourceReference( XSM.class,
                            "icons/document-new.png" ) ) );
                    listItem.add( new Label( "add-label", getString( "add", new Model( elem ) ) ) );
                    listItem.add( new Label( "add-or", getString( "or" ) ).setVisible( listItem.getIndex() < ((List) listItem.getParent().getDefaultModelObject()).size() - 1 ) );
                }
            } );
        }
    }
}