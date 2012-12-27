package com.rectang.xsm.util;

import com.rectang.xsm.site.Site;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.MutableAttributeSet;
import java.util.*;
import java.io.StringReader;

/**
 * Html utilities
 *
 * @author Andrew Williams
 * @version $Id: HTMLUtils.java 793 2009-04-20 19:01:21Z andy $
 * @since 2.0
 */
public class HTMLUtils
{
    /**
     * Summarise an html string, returning a copy of the beginning of the String
     * <code>in</code> stopping at the last space before the position
     * <code>chars</code>. "..." will be appended to strings that are truncated.
     * All HTML tags remaining open at the end of the summary will be closed.
     * The <code>char</limit> does <b>not</b> include tags.
     *
     * @param in    The String input to summarise
     * @param chars The maximum number of chars to appear in the summary
     * @return A new String summarising the input
     */
    public static synchronized String summarise( String in, int chars )
    {

        try
        {
            SummaryParser parser = new SummaryParser( chars );
            // compensate for parser decoding HTML entities
            parser.getParser().parse( new StringReader( in.replaceAll( "&", "&amp;" ) ),
                    parser.new ParserCallback(), true );
            return parser.getSummary();
        }
        catch ( Exception e )
        {
            e.printStackTrace();

      /* if we cannot parse just return a trimmed version */
            return StringUtils.summarise( in, chars );
        }
    }

    public static String toAbsolute( String link, Site site )
    {
        if ( link.indexOf( "://" ) != -1 )
        {
            return link;
        }

        if ( link.charAt( 0 ) == '/' )
        {
            String server = site.getRootUrl().substring( 0,
                    site.getRootUrl().length() - site.getPrefixUrl().length() );
            return server + link;
        }

        //TODO handle relative links from the page, not the site root
        return link;
    }

    public static synchronized String toAbsoluteLinks( String in, Site site )
    {
        LinkParser parser = new LinkParser( site );
        try
        {
            // compensate for parser decoding HTML entities
            parser.getParser().parse( new StringReader( in.replaceAll( "&", "&amp;" ) ),
                    parser.new ParserCallback(), true );
            return parser.getAbsoluteHTML();
        }
        catch ( Exception e )
        {
            return in;
        }
    }
}

/**
 * A simple HTML parser based on hte HTMLEditorKit in javax.swing.text.html.
 * This parser summarises HTML by stripping all tags and returning the text cut
 * to the last space before the limit. "..." is appended to truncated strings.
 * The limit does not include tags.
 *
 * @author aje
 *
 */
class SummaryParser
        extends HTMLEditorKit
{

    private StringBuffer summary;
    private boolean appending;
    private int chars, count;
    private Vector open;

    public SummaryParser( int chars )
    {
        this.summary = new StringBuffer();
        this.appending = true;
        this.chars = chars;
        this.count = 0;
        this.open = new Vector();
    }

    public HTMLEditorKit.Parser getParser()
    {
        return super.getParser();
    }

    public class ParserCallback
            extends HTMLEditorKit.ParserCallback
    {

        public void handleText( char[] data, int pos )
        {
            if ( count > chars )
            {
                return;
            }

            StringTokenizer tokens = new StringTokenizer( new String( data ), " ", true );
            while ( tokens.hasMoreElements() && appending )
            {
                String token = (String) tokens.nextElement();
        /* weird that the parser should return these brackets... */
                if ( token.startsWith( ">" ) )
                {
                    if ( token.length() <= 1 )
                    {
                        continue;
                    }
                    else
                    {
                        token = token.substring( 1 );
                    }
                }
                count += token.length() + 1;

                if ( count > chars )
                {
                    appending = false;
                    break;
                }

                summary.append( token );
            }
        }

        public void handleSimpleTag( HTML.Tag t, MutableAttributeSet a, int pos )
        {
            if ( !appending || a.containsAttribute( IMPLIED, Boolean.TRUE ) )
            {
                return;
            }
            summary.append( "<" + t.toString() );
            printAttributes( a );
            summary.append( "/>" );
        }

        public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos )
        {
            if ( !appending || a.containsAttribute( IMPLIED, Boolean.TRUE ) )
            {
                return;
            }
            open.add( t );
            summary.append( "<" + t.toString() );
            printAttributes( a );
            summary.append( ">" );
        }

        public void handleEndTag( HTML.Tag t, int pos )
        {
            if ( !appending )
            {
                return;
            }
            for ( int i = open.size() - 1; i >= 0; i-- )
            {
                if ( open.get( i ).equals( t ) )
                {
                    summary.append( "</" + t.toString() + ">" );
                    open.remove( i );
                    break;
                }
            }
        }

        public void handleEndOfLineString( String eol )
        {
            for ( int i = open.size() - 1; i >= 0; i-- )
            {
                summary.append( "</" + open.get( i ).toString() + ">" );
                open.remove( i );
            }
            if ( !appending )
            {
                summary.append( "..." );
            }
        }

        public void printAttributes( MutableAttributeSet a )
        {
            Enumeration att = a.getAttributeNames();
            while ( att.hasMoreElements() )
            {
                Object next = att.nextElement();
                summary.append( " " + next.toString() + "=\"" + a.getAttribute( next ) +
                        "\"" );
            }
        }
    }

    public String getSummary()
    {
        return summary.toString();
    }
}

class LinkParser
        extends HTMLEditorKit
{
    private StringBuffer absolute;
    private Site site;

    public LinkParser( Site site )
    {
        this.absolute = new StringBuffer();
        this.site = site;
    }

    public String getAbsoluteHTML()
    {
        return absolute.toString();
    }

    public HTMLEditorKit.Parser getParser()
    {
        return super.getParser();
    }

    public class ParserCallback
            extends HTMLEditorKit.ParserCallback
    {
        private Set implied = new HashSet();

        public void handleText( char[] data, int pos )
        {
            if ( data[0] == '>' )
            {
                absolute.append( data, 1, data.length - 1 );
            }
            else
            {
                absolute.append( data );
            }
        }

        public void handleSimpleTag( HTML.Tag t, MutableAttributeSet a, int pos )
        {
            if ( a.containsAttribute( IMPLIED, Boolean.TRUE ) || a.containsAttribute( HTML.Attribute.ENDTAG, "true" ) )
            {
                return;
            }
            absolute.append( "<" + t.toString() );
            printAttributes( a );
            absolute.append( "/>" );
        }

        public void handleStartTag( HTML.Tag t, MutableAttributeSet a, int pos )
        {
            if ( a.containsAttribute( IMPLIED, Boolean.TRUE ) )
            {
                implied.add( t );
                return;
            }
            absolute.append( "<" + t.toString() );
            printAttributes( a );
            absolute.append( ">" );
        }

        public void handleEndTag( HTML.Tag t, int pos )
        {
            if ( implied.contains( t ) )
            {
                implied.remove( t );
                return;
            }

            absolute.append( "</" + t.toString() + ">" );
        }

        public void handleEndOfLineString( String eol )
        {
            absolute.append( eol );
        }

        public void printAttributes( MutableAttributeSet a )
        {
            Enumeration att = a.getAttributeNames();
            while ( att.hasMoreElements() )
            {
                Object name = att.nextElement();
                Object value = a.getAttribute( name );

                if ( name.equals( HTML.Attribute.HREF ) ||
                        name.equals( HTML.Attribute.SRC ) )
                {
                    value = HTMLUtils.toAbsolute( (String) value, site );
                }

                absolute.append( " " + name.toString() + "=\"" + value + "\"" );
            }
        }
    }
}