package com.rectang.xsm.types;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;

import java.util.List;
import java.util.Iterator;

import org.jdom.Element;

public class FAQ
        extends DocGroup
{

    public FAQ( java.lang.String name )
    {
        super( name, new Question( "entry" ) );
    }

    public void view( Element node, StringBuffer s )
    {
        publish( node, s );
    }

    public void publish( Element node, StringBuffer s )
    {
        s.append( "<p class=\"xsm_faq_list\">" );
        StringBuffer body = new StringBuffer();
        List children = node.getChildren( element.getName() );

        String index;
        int fakeIndex = 0;
        Iterator allChildren = children.iterator();
        while ( allChildren.hasNext() )
        {
            Element next = (Element) allChildren.next();

            index = next.getAttributeValue( "index" );
            if ( index == null || index.equals( "" ) )
            {
                index = "x" + fakeIndex++;
            }

            s.append( "<a href=\"#" + index + "\">" + next.getChildText( "question" ) );
            s.append( "</a><br />\n" );

            body.append( "<p><a name=\"" + index + "\"></a>\n" );
            element.publish( next, body );
        }

        s.append( "</p>\n" );
        s.append( body );
    }
}

class Question
        extends DocList
{

    public Question( String name )
    {
        super( name, new DocElement[]{
                new com.rectang.xsm.widget.String( "question" ),
                new com.rectang.xsm.widget.TextArea( "answer" )
        } );
    }

    public void view( Element root, StringBuffer s )
    {
        s.append( "<p><b>" );
        elements[0].view( root.getChild( "question" ), s );
        s.append( "</b></p>" );
        s.append( "<blockquote>" );
        elements[1].view( root.getChild( "answer" ), s );
        s.append( "</blockquote>\n" );
    }

    public void publish( Element root, StringBuffer s )
    {
        s.append( "<p class=\"xsm_faq_question\"><b>" );
        elements[0].view( root.getChild( "question" ), s );
        s.append( "</b></p>" );
        s.append( "<blockquote class=\"xsm_faq_answer\">" );
        elements[1].view( root.getChild( "answer" ), s );
        s.append( "</blockquote>\n" );
    }

}
