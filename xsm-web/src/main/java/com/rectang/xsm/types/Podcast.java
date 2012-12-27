package com.rectang.xsm.types;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.widget.File;
import com.rectang.xsm.site.Site;
import org.jdom.Element;

/**
 * A podcast document - basic blog with mp3 enclosures
 *
 * @author Andrew Williams
 * @version $Id: Podcast.java 802 2009-05-16 17:25:24Z andy $
 * @since 1.0
 */
public class Podcast
        extends News
{
    public Podcast( String name )
    {
        this( name, new PodcastArticle( "article" ) );
    }

    protected Podcast( String name, DocList child )
    {
        super( name, child );
    }
}

class PodcastArticle
        extends NewsArticle
{

    public PodcastArticle( String name )
    {
        super( name );

        setEmbeds( new DocElement[]{new File( "enclosure" ).setAcceptedExtensions( getExtensions() )} );
    }

    protected void publishEmbeded( Element root, DocElement[] embed, StringBuffer s )
    {
        java.lang.String path = root.getChildText( "enclosure" );
        if ( path == null || path.length() == 0 )
        {
            return;
        }

        s.append( "  <p class=\"xsm_news_enclosure\">Download file: <a href=\"" );
        s.append( getSite().getRootUrl() );
        s.append( File.getPath( embed[0], path ) );
        s.append( "\">" );
        s.append( path );
        s.append( "</a></p>\n" );
    }

    protected void publishEmbededRSS( Element root, DocElement[] embed, StringBuffer s )
    {
        java.lang.String path = root.getChildText( "enclosure" );
        if ( path == null || path.length() == 0 )
        {
            return;
        }

        s.append( "  <enclosure url=\"" );
        s.append( getSite().getRootUrl() );
        s.append( File.getPath( embed[0], path ) );
        s.append( "\" length=\"" );

        Site site = this.getPage().getSite();
        s.append( site.getPublishedDoc( getDoc().getPage().getPath() + "/_files/" + path ).length() );

        s.append( "\" type=\"" );
        s.append( getMime( path ) );
        s.append( "\" />\n" );
    }

    protected String[] getExtensions()
    {
        return new String[]{"mp3"};
    }

    protected String getMime( String ext )
    {
        return "audio/mpeg";
    }
}