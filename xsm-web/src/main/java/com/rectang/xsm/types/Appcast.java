package com.rectang.xsm.types;

/**
 * A podcast document - basic blog with binary enclosures
 *
 * @author Andrew Williams
 * @version $Id: Appcast.java 797 2009-04-20 20:07:51Z andy $
 * @since 1.0
 */
public class Appcast
        extends Podcast
{
    public Appcast( String name )
    {
        super( name, new AppcastArticle( "article" ) );
    }
}

class AppcastArticle
        extends PodcastArticle
{
    AppcastArticle( String name )
    {
        super( name );
    }

    protected String[] getExtensions()
    {
        return null;
    }

    protected String getMime( String ext )
    {
        return "application/octet-stream";
    }
}
