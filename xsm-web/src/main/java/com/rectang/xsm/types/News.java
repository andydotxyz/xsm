package com.rectang.xsm.types;

import com.rectang.xsm.*;
import com.rectang.xsm.util.HTMLUtils;
import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.widget.*;

import java.io.*;
import java.io.File;
import java.lang.String;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.Model;

/* TODO remove links "my articles" when viewing a users page and remove "read more" if there is no more to read - maybe a  comments indicator too? */
public class News
        extends DocGroup
{

    public static final SupportedOption PAGE_LENGTH = new SupportedOption( "PAGE_LENGTH",
            "The number of articles to be displayed on the main news page", 10 );
    public static final SupportedOption RSS_LENGTH = new SupportedOption( "RSS_LENGTH",
            "The number of articles to be published in the RSS feed", 10 );
    public static final SupportedOption AUTHOR_PAGES = new SupportedOption( "AUTHOR_PAGES",
            "Generage a news page for each author as well as the main page", false );
    public static final SupportedOption ARTICLE_LINK = new SupportedOption( "ARTICLE_LINK",
            "Should we display a [Full article] link for each article summary", true );
    public static final SupportedOption SUMMARY_LENGTH = new SupportedOption( "SUMMARY_LENGTH",
            "The number of characters (approx) in a news article summary", 500 );

    private Vector options;
    // TODO fix access to this
    public static DateFormat storedFormat = new SimpleDateFormat( "EE, dd MMM yyyy HH:mm:ss ZZZZ" );

    /* FIXME - don't allow folk to save to items they cannot edit */
    public News( java.lang.String name )
    {
        this( name, new NewsArticle( "article" ) );
    }

    protected News( java.lang.String name, DocList list )
    {
        super( name, list );

        options = new Vector();
        options.add( PAGE_LENGTH );
        options.add( RSS_LENGTH );
        options.add( AUTHOR_PAGES );
        options.add( ARTICLE_LINK );
        options.add( SUMMARY_LENGTH );
    }

    public void view( Element node, StringBuffer s )
    {
        int page_length = PAGE_LENGTH.getInteger( getDoc() );
        publishNNodes( node.getChildren( element.getName() ), page_length, s );
    /* FIXME maybe summarise the archive too? */
    }

    public WebMarkupContainer edit( String wicketId, Element node, String path )
    {
        Panel ret = new NewsPanel( wicketId, node, path, getEditCount() );

        return ret;
    }

    protected int getEditCount()
    {
    /* FIXME we need to allow editing of the archive */

        return 10;
    }

    public void publish( Element node, StringBuffer s )
    {
        int page_length = PAGE_LENGTH.getInteger( getDoc() );

    /* publish front page */
        publishNNodes( node.getChildren( element.getName() ), page_length, s );
    /* publish user pages */
        boolean author_pages = AUTHOR_PAGES.getBoolean( getDoc() );
        if ( author_pages )
        {
            String author_dir = getPublishedPath() + File.separatorChar + "_authors"
                    + File.separatorChar;
            (getSite().getPublishedDoc( author_dir )).mkdir();
            Vector authors = new Vector();
            Iterator articles = node.getChildren( "article" ).iterator();
            while ( articles.hasNext() )
            {
                Element next = (Element) articles.next();
                String uidStr = next.getChild( "uid" ).getText();
                if ( uidStr != null && !uidStr.equals( "" ) )
                {
                    if ( !authors.contains( uidStr ) )
                    {
                        authors.add( uidStr );
                    }
                }
            }
            Iterator authorIter = authors.iterator();
            while ( authorIter.hasNext() )
            {
                String author = (String) authorIter.next();
                publishNAuthorNodes( node, author, author_dir, page_length );
            }
        }

    /* publish each article */
        publishPermaNodes( node );
        publishArchive( node );

        publishRSS( node );

// TODO do we need to output something else in this place?
//    List children = node.getChildren(element.getName());
//    if (children.size() > page_length) {
//      String archivePage = getPublishedPath() + "/archive.html";
//      s.append("<p align=\"right\">more in the <a href=\"");
//      s.append(getSite().getPrefixUrl() + archivePage + "\">archive</a></p>");
//
//      PublishedFile archive = getSite().getPublishedDoc(archivePage);
//      StringBuffer out = new StringBuffer();
//      publishNNodes(node, 0, out);
//      getDoc().publishContent(archive, out.toString(), getUser());
//    }
    }

    private void publishNNodes( List children, int n, StringBuffer s )
    {
        Iterator allChildren = children.iterator();

        int counter = 0;
        if ( n == 0 )
        {
            n = children.size();
        }
        String index;
        int fakeIndex = 0;
        while ( allChildren.hasNext() && counter++ < n )
        {
            Element next = (Element) allChildren.next();
            index = next.getAttributeValue( "index" );
            if ( index == null || index.equals( "" ) )
            {
                index = "x" + fakeIndex++;
            }

            ((NewsArticle) element).publish( next, true, index, s );
        }
    }

    private void publishNAuthorNodes( Element node, String author, String dir, int n )
    {
        StringBuffer content = new StringBuffer();
        List children = node.getChildren( element.getName() );
        Iterator allChildren = children.iterator();
        int counter = 0;

        if ( n == 0 )
        {
            n = children.size();
        }
        String index;
        int fakeIndex = 0;
        while ( allChildren.hasNext() && counter < n )
        {
            Element next = (Element) allChildren.next();
            index = next.getAttributeValue( "index" );
            if ( index == null || index.equals( "" ) )
            {
                index = "x" + fakeIndex++;
            }

            String uid = next.getChild( "uid" ).getText();
            if ( uid != null && uid.equals( author ) )
            {
                ((NewsArticle) element).publish( next, true, index, content );
                counter++;
            }
        }
        PublishedFile out = getSite().getPublishedDoc( dir + author + ".html" );
        getDoc().publishContent( out, content.toString(), getUser() );
    }

    private void publishPermaNodes( Element node )
    {
        (getSite().getPublishedDoc( getPublishedPath() )).mkdir();

        List children = node.getChildren( element.getName() );
        Iterator allChildren = children.iterator();
        String index;
        int fakeIndex = 0;
        Calendar cal = Calendar.getInstance();
        while ( allChildren.hasNext() )
        {
            Element next = (Element) allChildren.next();
            index = next.getAttributeValue( "index" );
            if ( index == null || index.equals( "" ) )
            {
                index = "x" + fakeIndex++;
            }

            StringBuffer content = new StringBuffer();
            element.publish( next, content );

            try
            {
                cal.setTime( storedFormat.parse( next.getChild( "time" ).getText() ) );
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
                // will default to current time
            }

            String path = getPublishedPath() + getArticlePath( next, index );
            (getSite().getPublishedDoc( new File( getPublishedPath() ).getParent() )).mkdir();
            PublishedFile out = getSite().getPublishedDoc( path );
            getDoc().publishContent( out, content.toString(), getUser() );
        }
    }

    private void publishArchive( Element node )
    {
        String dir = getPublishedPath() + File.separatorChar;
        (getSite().getPublishedDoc( dir )).mkdir();

        List children = node.getChildren( element.getName() );
        Iterator allChildren = children.iterator();
        String index;
        int fakeIndex = 0;
        int year = 0;
        int month = 0;
        Calendar cal = Calendar.getInstance();
        List monthNodes = new ArrayList();
        while ( allChildren.hasNext() )
        {
            Element next = (Element) allChildren.next();
            try
            {
                cal.setTime( storedFormat.parse( next.getChild( "time" ).getText() ) );
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
                continue;
            }

            if ( year == cal.get( Calendar.YEAR ) && month == cal.get( Calendar.MONTH ) )
            {
                monthNodes.add( next );
                continue;
            }

            StringBuffer content = new StringBuffer();

            if ( year != 0 )
            {
                String monthDir = dir + year + File.separatorChar + (month + 1);
                (getSite().getPublishedDoc( monthDir )).mkdir();

                publishNNodes( monthNodes, 0, content );
                PublishedFile out = getSite().getPublishedDoc( monthDir + File.separatorChar + "index.html" );
                getDoc().publishContent( out, content.toString(), getUser() );

                monthNodes.clear();
            }

            year = cal.get( Calendar.YEAR );
            month = cal.get( Calendar.MONTH );
            monthNodes.add( next );
        }

        String monthDir = dir + year + File.separatorChar + (month + 1);
        (getSite().getPublishedDoc( monthDir )).mkdir();

        StringBuffer content = new StringBuffer();
        publishNNodes( monthNodes, 0, content );
        PublishedFile out = getSite().getPublishedDoc( monthDir + File.separatorChar + "index.html" );
        getDoc().publishContent( out, content.toString(), getUser() );
    }

    public void publishRSS( Element root )
    {
        PublishedFile rss = getSite().getPublishedDoc( getPublishedPath() + File.separatorChar
                + "feed.xml" );
        try
        {
            OutputStream os = rss.getOutputStream();
            Writer out = new OutputStreamWriter( os );

            out.write( "<?xml version=\"1.0\" ?>\n" );
            out.write( "<rss version=\"2.0\">\n" );
            out.write( "  <channel>\n" );
            out.write( "    <title>" + escape( getSite().getTitle() ) + "</title>\n" );
            out.write( "    <link>" + escape( getSite().getRootUrl() + getPublishedPath() )
                    + "/feed.xml</link>\n" );
            out.write( "    <description>RSS generated from " + escape(
                    getSite().getRootUrl() + getPublishedPath() ) + "/</description>\n" );
            out.write( "    <generator>Rectang XSM</generator>\n" );

            List elements = root.getChildren( "article" );
            Iterator all = elements.iterator();
            int counter = 0;
            String index;
            int fakeIndex = 0;
            while ( all.hasNext() && (counter++ < RSS_LENGTH.getInteger( getDoc() )) )
            {
                Element next = (Element) all.next();
                index = next.getAttributeValue( "index" );
                if ( index == null || index.equals( "" ) )
                {
                    index = "x" + fakeIndex++;
                }

                String url = getArticlePath( next, index ).replace( File.separatorChar, '/' );
                java.lang.String link = escape( getSite().getRootUrl() + getPublishedPath() + url );
                java.lang.String guid = getSite().getRootUrl() + getPublishedPath() + url;
                StringBuffer tmp = new StringBuffer();
                ((NewsArticle) element).publishRSS( next, link, guid, tmp );
                out.write( tmp.toString() );
            }

            out.write( "  </channel>\n" );
            out.write( "</rss>\n" );
            out.close();
            os.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static String escape( String in )
    {
        return NewsArticle.escape( in );
    }

    static String getArticlePath( Element node, String index )
    {
        Calendar cal = Calendar.getInstance();
        try
        {
            cal.setTime( storedFormat.parse( node.getChild( "time" ).getText() ) );
        }
        catch ( ParseException e )
        {
            e.printStackTrace();
            // will default to current time
        }

        String slug = node.getChildText( "slug" );
        if ( slug != null && slug.length() > 0 )
        {
            index = slug;
        }
        return File.separator + cal.get( Calendar.YEAR ) + File.separator + (cal.get( Calendar.MONTH ) + 1) + File.separator +
                index + ".html";
    }

    public List getSupportedOptions()
    {
        Vector ret = new Vector();
        ret.addAll( options );
        ret.addAll( element.getSupportedOptions() );
        return ret;
    }

    class NewsPanel
            extends Panel
    {
        public NewsPanel( final String wicketId, final Element node,
                          final String path, final int childCount )
        {
            super( wicketId );

            Link add = new Link( "add" )
            {
                public void onClick()
                {
                    addChild( node, 0 );
                }
            };
            add.add( new Image( "add-icon", new ResourceReference( XSM.class,
                    "icons/document-new.png" ) ) );
            add( add );
            add( new Label( "add-label", new StringResourceModel( "add", add, new Model( element ) ) ) );

            List children = node.getChildren( element.getName() );
            add( new ListView( "elements", children )
            {
                protected void populateItem( ListItem listItem )
                {
                    // not at all glamarous - but using sublists currently breaks the insertion code
                    if ( childCount > 0 && listItem.getIndex() > childCount )
                    {
                        listItem.setVisible( false );
                        return;
                    }
                    final Element child = (Element) listItem.getModelObject();
                    final int i = listItem.getIndex();

                    Link delete = new Link( "delete" )
                    {
                        public void onClick()
                        {
                            //TODO add confirmation input
                            delete( node, element.getName() + "@" + i );
                        }
                    };
                    listItem.add( delete );
                    delete.add( new Image( "delete-icon", new ResourceReference( XSM.class,
                            "icons/edit-delete.png" ) ) );

                    listItem.add( element.edit( "content", child, path + "/" + element.getName() + "@" + i ) );
                }
            } ).setRenderBodyOnly( true );
        }
    }
}

class NewsArticle
        extends DocList
{
    protected DocElement[] embed = new DocElement[0];
    private DateFormat storedFormat = News.storedFormat;
    private DateFormat renderFormat = new SimpleDateFormat( "dd MMM yyyy HH:mm" );

    public NewsArticle( String name )
    {
        this( name, new DocElement[]{
                new com.rectang.xsm.widget.String( "subject" ),
        /* other elements should be inserted here */
                new HTMLTextArea( "body" ),
                new Value( "author", Value.FULLNAME ),
                new Value( "uid", Value.USERNAME ),
                new Value( "email", Value.EMAIL ),
                new Value( "time", Value.DATE ),
                new com.rectang.xsm.widget.String( "slug" ),
                new GalleryCommentList( "comments" ) /* FIXME- have a central comments definition */
        } );
    }

    protected void setEmbeds( DocElement[] elems )
    {
        embed = elems;

        ArrayList list = new ArrayList();
        list.add( elements[0] );
        for ( int i = 0; i < elems.length; i++ )
        {
            list.add( elems[i] );
        }
        for ( int i = 1; i < elements.length; i++ )
        {
            list.add( elements[i] );
        }

        elements = (DocElement[]) list.toArray( new DocElement[list.size()] );
    }

    protected NewsArticle( String name, DocElement[] elements )
    {
        super( name, elements );
    }

    public void view( Element root, StringBuffer s )
    {
        // not used anymore - view of News uses publish here
    }

    public void publish( Element root, StringBuffer s )
    {
        publish( root, false, String.valueOf( 0 ), s );
    }

    public void publish( Element root, boolean summarise, String id, StringBuffer s )
    {
        int inc = embed.length;
        String url = News.getArticlePath( root, id ).replace( File.separatorChar, '/' );
        s.append( "<p class=\"xsm_news_title\"><b><a name=\"" );
        s.append( root.getAttributeValue( "index" ) + "\"></a>" );
        if ( summarise )
        {
            s.append( "<a href=\"" + getSite().getPrefixUrl() );
            s.append( getPublishedPath() + url + "\" title=\"permalink\">" );
        }
        elements[0].publish( root.getChild( "subject" ), s );
        if ( summarise )
        {
            s.append( "</a>" );
        }
        s.append( "</b> <span class=\"xsm_news_posted\">posted " );
        try
        {
            s.append( renderFormat.format( storedFormat.parse( root.getChild( "time" ).getText() ) ) );
        }
        catch ( ParseException e )
        {
            e.printStackTrace();
            elements[5 + inc].view( root.getChild( "time" ), s );
        }
        s.append( " by <span class=\"xsm_news_author\">" );
        elements[2 + inc].publish( root.getChild( "author" ), s );
        if ( News.AUTHOR_PAGES.getBoolean( getDoc() ) )
        {
            String uid = root.getChildText( "uid" );
            if ( uid != null && !uid.equals( "" ) )
            {
                s.append( " [<a href=\"" );
                s.append( getSite().getPrefixUrl() + getPublishedPath() );
                s.append( "/_authors/" + root.getChildText( "uid" ) + ".html\">" );
                s.append( "All my articles</a>]" );
            }
        }
        s.append( "</span></span></p>\n" );

        publishEmbeded( root, embed, s );

        s.append( "<div class=\"xsm_news_article\">" );
        StringBuffer body = new StringBuffer();
        elements[1 + inc].publish( root.getChild( "body" ), body );

        boolean summarised = false;
        if ( summarise )
        {
            String summary = summarise( body.toString() );
            summarised = summary.endsWith( "..." );
            s.append( summary );
        }
        else
        {
            s.append( body );
        }

        if ( summarise && summarised && News.ARTICLE_LINK.getBoolean( getDoc() ) )
        {
            s.append( " <a class=\"xsm_news_fulllink\"href=\"" + getSite().getPrefixUrl() );
            s.append( getPublishedPath() + url + "\" title=\"permalink\">" );
            s.append( "[Full article]</a>" );
        }
        s.append( "</div>\n" );

        if ( !summarise )
        {
            Element comments = root.getChild( "comments" );
            int commentCount = comments.getChildren( "comment" ).size();
            if ( commentCount > 0 )
            {
                s.append( "<div class=\"xsm_comments\"><p><b>Comments:</b></p>" );
                elements[7 + inc].publish( comments, s );
                s.append( "</div>" );
            }
        }
    }

    public void destroy( Element root )
    {
        int index;
        try
        {
            index = Integer.parseInt( root.getAttributeValue( "index" ) );
        }
        catch ( NumberFormatException e )
        {
            // if there is no index we cannot just guess what page to delete
            return;
        }

        getSite().getPublishedDoc( getPublishedPath() + News.getArticlePath( root, "" + index ) ).delete();
    }

    public void publishRSS( Element root, String link, String guid, StringBuffer s )
    {
        StringBuffer tmp = new StringBuffer();
        int inc = embed.length;

        s.append( "<item>\n" );
        elements[0].view( root.getChild( "subject" ), tmp );
        s.append( "  <title>" + escape( tmp.toString() ) + "</title>\n" );
        s.append( "  <link>" + link + "</link>\n" );
        tmp = new StringBuffer();
        elements[4 + inc].view( root.getChild( "email" ), tmp );
        s.append( "  <author>" + escape( tmp.toString() ) );
        tmp = new StringBuffer();
        elements[2 + inc].view( root.getChild( "author" ), tmp );
        s.append( " (" + escape( tmp.toString() ) + ")</author>\n" );

        publishEmbededRSS( root, embed, s );

        tmp = new StringBuffer();
        elements[1 + inc].view( root.getChild( "body" ), tmp );
        s.append( "  <description>" + escape( tmp.toString() ) + "</description>\n" );
        s.append( "  <pubDate>" );
        elements[5 + inc].view( root.getChild( "time" ), s );
        s.append( "</pubDate>\n" );
        s.append( "  <guid isPermaLink=\"true\">" + guid + "</guid>\n" );
        s.append( "</item>\n" );
    }

    protected static String escape( String in )
    {
        if ( in == null )
        {
            return "";
        }
        return in.replaceAll( "&", "&amp;" ).replaceAll( "<", "&lt;" )
                .replaceAll( ">", "&gt;" );
    }

    private String summarise( String in )
    {
        return HTMLUtils.summarise( in, News.SUMMARY_LENGTH.getInteger( getDoc() ) );
    }

    protected void publishEmbeded( Element root, DocElement[] embed, StringBuffer s )
    {
    }

    protected void publishEmbededRSS( Element root, DocElement[] embed, StringBuffer s )
    {
    }
}
