package com.rectang.xsm.types;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.widget.Boolean;
import com.rectang.xsm.widget.HTMLTextArea;
import com.rectang.xsm.widget.ComboBox;
import com.rectang.xsm.widget.TextArea;

import org.jdom.Element;

import java.lang.String;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

/**
 * A simple form type used to create "Contact Us" forms and simple feedback pages.
 *
 * @author Andrew Williams
 * @version $Id: Form.java 786 2009-04-02 22:25:55Z andy $
 * @since 1.2
 */
public class Form
        extends DocList
        implements PHPFile
{
    public static final SupportedOption FROM_EMAIL = new SupportedOption( "FROM_EMAIL",
            "who should the email appear to be sent from?", "" );
    private Vector options;

    public Form( String name )
    {
        super( name, new DocElement[]{new com.rectang.xsm.widget.String( "to" ),
                new HTMLTextArea( "intro" ), new HTMLTextArea( "response" ),
                new DocGroup( "fields", new FormField( "field" ) )
                {
                    public String getNewline()
                    {
                        return "\n";
                    }
                }} );

        options = new Vector();
        options.add( FROM_EMAIL );
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

    public void view( Element root, StringBuffer s )
    {
        s.append( "<p>Form will be submitted to " );
        elements[0].view( root.getChild( "to" ), s );
        s.append( "</p>" );

        elements[1].view( root.getChild( "intro" ), s );

        s.append( "<table>" );
        elements[3].view( root.getChild( "fields" ), s );
        s.append( "</table>" );
    }

    public void publish( Element root, StringBuffer s )
    {
        elements[1].publish( root.getChild( "intro" ), s );

        String headers = "";
        String from = FROM_EMAIL.getString( getDoc() );
        if ( from != null && from.length() > 0 )
        {
            headers = "From: " + from;
        }

        s.append( "<?php $send=FALSE;\n" );
        s.append( "if ($_POST[\"submit\"]) {" );
        s.append( "$send=TRUE;\n$headers=\"" );
        s.append( headers );
        s.append( "\";\n$body=\'Form \"" + getDoc().getPage().getTitle() + "\" submitted as follows:\n\n\';\n" );
        Iterator fields = root.getChild( "fields" ).getChildren( "field" ).iterator();
        while ( fields.hasNext() )
        {
            Element field = (Element) fields.next();
            boolean required = "true".equalsIgnoreCase( field.getChildText( "required" ) );

            s.append( "$value=$_POST[\"field" + field.getAttributeValue( "index" ) + "\"];\n" );
            if ( required )
            {
                s.append( "if (!$value) $send=FALSE;" );
            }
            s.append( "$body .= \"" );
            elements[0].publish( field.getChild( "question" ), s );
            s.append( "\n\t \".$value.\"\n\";\n" );
        }

        s.append( "if ($send) {" );
        s.append( "  mail('" );
        elements[0].publish( root.getChild( "to" ), s );
        s.append( "', 'Form \"" + getDoc().getPage().getTitle() + "\" submission results', $body, $headers);\n?>" );
        elements[2].publish( root.getChild( "response" ), s );
        s.append( "<?php } else { \n" );
        // TODO remove this and have the form below re-render with the correct values input
        s.append( "$send=TRUE;" );

        s.append( "?><p>Missing required field, please go back and try again.</p>\n" );
        s.append( "<?php }} ?>" );

        s.append( "<?php if (!$send) { ?>" );
        s.append( "<form action=\"?\" method=\"post\" class=\"xsm_form\">" );
        s.append( "<table>" );
        elements[3].publish( root.getChild( "fields" ), s );
        s.append( "</table>" );
        s.append( "<input type=\"submit\" name=\"submit\"/></form>" );
        s.append( "<?php } ?>\n" );
    }
}

class FormField
        extends DocList
{
    public FormField( String name )
    {
        super( name, new DocElement[]{
                new com.rectang.xsm.widget.String( "question" ),
                new TextArea( "description" ),
                new ComboBox( "type", Arrays.asList( new String[]{"text", "textarea", "checkbox", "yesno", "yesnomaybe", "hidden"} ) ),
                new Boolean( "required" )
        } );
    }

    public void publish( Element node, StringBuffer s )
    {
        draw( node, s, true );
    }

    public void view( Element node, StringBuffer s )
    {
        draw( node, s, false );
    }

    private void draw( Element node, StringBuffer s, boolean enabled )
    {
        boolean required = "true".equalsIgnoreCase( node.getChildText( "required" ) );
        String enabledStr = "";
        if ( !enabled )
        {
            enabledStr = "disabled=\"disabled\" ";
        }
        String requiredStr = "";
        if ( required )
        {
            requiredStr = "required=\"required\" ";
        }

        String index = node.getAttributeValue( "index" );
        s.append( "<tr><td class=\"xsm_form_question field" );
        s.append( index );
        s.append( "\">" );
        elements[0].publish( node.getChild( "question" ), s );
        if ( required )
        {
            s.append( "<span class=\"xsm_form_required\">*</span>" );
        }
        s.append( "</td><td class=\"xsm_form_answer field" );
        s.append( index );
        s.append( "\">" );
        String type = node.getChildText( "type" );
        if ( type.equals( "text" ) )
        {
            s.append( "<input type=\"text\" name=\"field" + index + "\" " + enabledStr + requiredStr + " />" );
        }
        else if ( type.equals( "textarea" ) )
        {
            s.append( "<textarea name=\"field" + index + "\" " + enabledStr + requiredStr + "></textarea>" );
        }
        else if ( type.equals( "checkbox" ) )
        {
            s.append( "<input type=\"checkbox\" name=\"field" + index + "\" " + enabledStr + requiredStr + "/>" );
        }
        else if ( type.equals( "yesno" ) )
        {
            s.append( "<input type=\"radio\" name=\"field" + index + "\" " + enabledStr + requiredStr + " value=\"Yes\" /> Yes <input type=\"radio\" name=\"field" + index + "\" " + enabledStr + requiredStr + " value=\"No\" /> No" );
        }
        else if ( type.equals( "yesnomaybe" ) )
        {
            s.append( "<input type=\"radio\" name=\"field" );
            s.append( index );
            s.append( "\" " );
            s.append( enabledStr );
            s.append( requiredStr );
            s.append( " value=\"Yes\" /> Yes " );

            s.append( "<input type=\"radio\" name=\"field" );
            s.append( index );
            s.append( "\" " );
            s.append( enabledStr );
            s.append( requiredStr );
            s.append( " value=\"No\" /> No " );

            s.append( "<input type=\"radio\" name=\"field" );
            s.append( index );
            s.append( "\" " );
            s.append( enabledStr );
            s.append( requiredStr );
            s.append( " value=\"Maybe\" /> Maybe" );
        }
        else
        {
            // TODO allow input of a value
            s.append( "<input type=\"hidden\" name=\"field" + index + "\" />" );
        }
        s.append( "</td></tr>" );

        String description = node.getChildText( "description" );
        if ( description != null && description.length() > 0 )
        {
            s.append( "<tr><td colspan=\"2\" class=\"xsm_form_description\"><span>&nbsp;&nbsp;&nbsp;&nbsp;" );
            elements[1].publish( node.getChild( "description" ), s );
            s.append( "</span></td></tr>" );
        }
    }
}
