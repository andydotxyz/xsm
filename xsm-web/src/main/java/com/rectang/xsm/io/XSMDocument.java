package com.rectang.xsm.io;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import org.jdom.*;

import com.rectang.xsm.*;
import com.rectang.xsm.types.*;
import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.Type;

import com.rectang.xsm.site.Site;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Page;
import com.rectang.xsm.util.*;
import org.apache.wicket.Component;
import org.apache.velocity.VelocityContext;
import org.headsupdev.support.java.IOUtil;

public class XSMDocument
        implements Serializable
{

    private RemoteDocument doc;
    private long LOCK_TIMEOUT = 1000 * 60 * 60 * 2; // 2 hour timeout on locks

    private MetaData metadata;
    private DocumentPage page;

    public static XSMDocument getXSMDoc( Site site, DocumentPage page )
    {
        try
        {
            return getXSMDoc( site, page, false );
        }
        catch ( RuntimeException e )
        {
            System.err.println( "Unable to read document for page " + page.getPublishedPath() + ": " + e.getMessage() );
            throw e;
        }
    }

    public static XSMDocument getXSMDoc( Site site, DocumentPage page, boolean create )
    {
        return new XSMDocument( site, page, create );
    }

    private XSMDocument( Site site, DocumentPage page, boolean create )
    {
        doc = RemoteDocument.getDoc( makeFileName( page ) );

        this.page = page;
        if ( doc.root == null )
        {
            if ( create )
            {
                doc.mkparentdirs();
                try
                {
                    OutputStreamWriter out = new OutputStreamWriter( doc.getOutputStream() );
                    out.write( "<xsmdoc><metadata></metadata><data></data></xsmdoc>" );
                    out.close();
                    doc.dom = RemoteDocument.builder.build( doc.getInputStream() );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }

        doc.root = doc.dom.getRootElement();
        if ( getMetadataElement() != null )
        {
            metadata = new MetaData( this );
        }
    }

    public static String makeFileName( Page page )
    {
        if ( page == null )
        {
            return null;
        }
        String fileName = page.getPath();
        if ( fileName.charAt( 0 ) != File.separatorChar )
        {
            fileName = File.separatorChar + fileName;
        }

        return XSM.getConfig().getSiteDataDir( page.getSite() ) + fileName + ".xml";
    }

    public DocumentPage getPage()
    {
        return page;
    }

    public Element getContentElement()
    {
        try
        {
            return (Element) doc.root.getChild( "data" ).getChildren().get( 0 );
        }
        catch ( NullPointerException e )
        {
            return null;
        }
    }

    public Element getRootElement()
    {
        return doc.getRootElement();
    }

    public String getPath()
    {
        return doc.getPath();
    }

    public boolean exists()
    {
        return doc.exists();
    }

    /**
     * Just a handy method for system code to save without checking the user permissions
     * @return whether or not the save was successfull
     */
    public boolean save()
    {
        return save( null );
    }

    public boolean delete()
    {
        return doc.delete();
    }

    public Element getMetadataElement()
    {
        return (doc.root == null) ? null : doc.root.getChild( "metadata" );
    }

    public MetaData getMetadata()
    {
        return metadata;
    }

    public void setContentElement( Element content )
    {
        if ( doc.root == null )
        {
            doc.dom.setRootElement( new Element( "xsmdoc" ) );
            doc.root = doc.dom.getRootElement();
        }
        Element data = doc.root.getChild( "data" );
        if ( data == null )
        {
            doc.root.addContent( new Element( "data" ) );
            data = doc.root.getChild( "data" );
        }
        data.removeContent();
        data.addContent( content );
    }

    public boolean save( UserData user )
    {
        return save( user, false );
    }

    public boolean save( UserData user, boolean force )
    {
        if ( user != null && (!force && !canEdit( user )) )
        {
            return false;
        }

        metadata.save();
        return doc.save();
    }

    public String getLocked()
    {
        long modified = doc.getModifiedTime();
        if ( modified >= System.currentTimeMillis() - LOCK_TIMEOUT )
        {
            return doc.root.getAttributeValue( "lock" );
        }
    
    /* lock has timed out */
        return null;
    }

    public boolean isLocked()
    {
        String lock = getLocked();
        return (lock != null && !lock.equals( "" ));
    }

    public boolean lockedBy( String user )
    {
        String lock = getLocked();
        return (lock != null && lock.equals( user ));
    }

    public boolean lockedByMe( UserData user )
    {
        return lockedBy( user.getUsername() );
    }

    public boolean lock( UserData user )
    {
        if ( !canEdit( user ) )
        {
            return false;
        }
        if ( isLocked() )
        {
            return lockedByMe( user );
        }

        doc.root.setAttribute( "lock", user.getUsername() );
        return save( user );
    }

    public boolean unlock( UserData user )
    {
        return unlock( user, false );
    }

    public boolean unlockForced( UserData user )
    {
        return unlock( user, true );
    }

    private boolean unlock( UserData user, boolean force )
    {
        if ( !canEdit( user ) )
        {
            return false;
        }
        if ( !isLocked() )
        {
            return true;
        }
        if ( force )
        {
            if ( !user.isSiteAdmin() && !isOwner( user ) )
            {
                return false;
            }
        }
        else
        {
            if ( !lockedByMe( user ) )
            {
                return false;
            }
        }
        String att = doc.root.getAttributeValue( "lock" );
        if ( att == null || att.equals( "" ) )
        {
            return true;
        }
        doc.root.removeAttribute( "lock" );
        return save( user );
    }

    public String getOwner()
    {
        String owner = doc.root.getAttributeValue( "owner" );
        if ( owner == null )
        {
            return "";
        }
        return owner;
    }

    public boolean isOwner( String username )
    {
        return getOwner().equals( username );
    }

    public boolean isOwner( UserData user )
    {
        return getOwner().equals( user.getUsername() );
    }

    public boolean setOwner( UserData user, String owner )
    {
        if ( !isOwner( user.getUsername() ) )
        {
            return false;
        }
        doc.root.setAttribute( "owner", owner );
        return save( user );
    }

    public boolean canEdit( UserData user )
    {
        return isOwner( user.getUsername() )
                || getEditors().contains( user.getUsername() )
                || user.isSiteEditor()
                || user.isSiteAdmin()
                || user.isXSMAdmin();
    }

    public List /* String */ getEditors()
    {
        return StringUtils.stringToList( doc.root.getAttributeValue( "editors" ) );
    }

    public boolean setEditors( List /* String */ editors, UserData user )
    {
        doc.root.setAttribute( "editors", StringUtils.listToString( editors ) );
        return save( user, true );
    }

    public List /* String */ getWatchers()
    {
        return StringUtils.stringToList( doc.root.getAttributeValue( "watchers" ) );
    }

    public boolean setWatchers( List /* String */ watchers, UserData user )
    {
        doc.root.setAttribute( "watchers", StringUtils.listToString( watchers ) );
        return save( user, true );
    }

    public boolean publish( UserData user )
    {
        String content;
        RootPair roots = getRootsAtIndex( "", user );

        StringBuffer s = new StringBuffer();
        roots.getType().publish( roots.getData(), s );
        content = s.toString();

        String fileName = "index.html";
        if ( getType( user ) instanceof PHPFile )
        {
            fileName = "index.php";
        }

        PublishedFile pubFile = user.getSite().getPublishedDoc( page.getPublishedPath()
                + File.separatorChar + fileName );

        return publishContent( pubFile, content, user );
    }

    public boolean publishContent( PublishedFile pubFile, String content, UserData user )
    {
        Writer writer = null;
        try
        {
      /*  create a context and add data */
            VelocityContext context = new VelocityContext();
            Map<String, Object> contextItems = Engine.getContext( this, page, getType( user ), pubFile, user.getSite(), content, user );
            for ( String key : contextItems.keySet() )
            {
                context.put( key, contextItems.get( key ) );
            }

            pubFile.mkparentdirs();

            writer = new OutputStreamWriter( pubFile.getOutputStream() );
            Engine.process( user.getSite(), context, writer );
            IOUtil.close( writer );

      /* only publish to index.html in the base of the site if we are the default file AND we are currently
       * writing this pages "index file" */
            if ( user.getSite().getDefault().equals( getPage().getPath() ) )
            {
                if ( pubFile.getFileName().equals( "index.html" ) ||
                        pubFile.getFileName().equals( "index.php" ) )
                {
                    pubFile = user.getSite().getPublishedDoc( pubFile.getFileName() );

                    try
                    {
                        writer = new OutputStreamWriter( pubFile.getOutputStream() );
                        Engine.process( user.getSite(), context, writer );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                        return false;
                    }
                    finally
                    {
                        IOUtil.close( writer );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            System.err.println( "Unable to publish page " + getPage().getPublishedPath() + ": " + e.getMessage() );
            return false;
        }
        finally
        {
            IOUtil.close( writer );
        }

        return true;
    }

    public boolean rename( Site site, DocumentPage newPage )
    {
        File newLoc = new File( makeFileName( newPage ) );
        newLoc.getParentFile().mkdir(); // we may be the first child page
        boolean ret = doc.renameTo( newLoc );
        if ( ret )
        {
            page = newPage;
        }
        return ret;
    }

    public String view( String index, UserData user )
    {
        RootPair roots = getRootsAtIndex( index, user );

        StringBuffer s = new StringBuffer();
        roots.getType().view( roots.getData(), s );

        return s.toString();
    }

    public Component edit( String wicketId, String index, UserData user )
    {
        RootPair roots = getRootsAtIndex( index, user );

        return roots.getType().edit( wicketId, roots.getData(), index );
    }

    // TODO figure if we missing create - was it ever called directly?
    public void add( String addNode, String index, UserData user )
            throws Exception
    {
        RootPair roots = getRootsAtIndex( index, user );
        DocElement typeRoot = roots.getType();
        Element root = roots.getData();

        if ( addNode == null )
        {
            throw new Exception( "No parameter addnode specified" );
        }
        String[] datum = addNode.split( "@" );
    /* if no insert part we want to create a file */
        if ( !datum[0].equals( "" ) )
        {
            boolean isList = typeRoot instanceof com.rectang.xsm.doc.DocGroup;
            int auto_inc = 1;
            try
            {
                auto_inc = Integer.parseInt( root.getAttributeValue( "next_index" ) );
            }
            catch ( NumberFormatException e )
            {
        /* auto_inc is 1 */
            }
            Element insert = new Element( datum[0] );
            if ( isList )
            {
                insert.setAttribute( "index", "" + auto_inc++ );
                root.setAttribute( "next_index", "" + auto_inc );
            }
            int num = 0;
            if ( datum.length > 1 )
            {
                num = Integer.parseInt( datum[1] );
            }
            RemoteDocument.addContentAfterElement( root, num, insert );
            root = insert;
      /* we do not want this to execute if we are recursing */
            if ( !typeRoot.getName().equals( datum[0] ) )
            {
                typeRoot = typeRoot.getElement( datum[0] );
            }
        }

        typeRoot.create( root );
    }

    private RootPair getRootsAtIndex( String index, UserData user )
    {
        Element root = getContentElement();
        DocElement typeRoot = loadType( root.getName(), page, user );
        if ( index == null || index.equals( "" ) )
        {
            index = "/";
        }
        if ( !index.equals( "/" ) )
        {
            String[] elements = index.split( "/" );
            for ( int x = 0; x < elements.length; x++ )
            {
                String element = elements[x];

                if ( element.equals( "" ) )
                {
                    continue;
                }
                String[] split = element.split( "@" );
                int count = 0;
                try
                {
                    count = Integer.parseInt( split[1] );
                }
                catch ( NumberFormatException nfe )
                {/* no problem */
                }
                List kids = root.getChildren(/*name*/ );
                root = (Element) kids.get( count );
        /* we do not want this to execute if we are recursing */
                if ( !typeRoot.getName().equals( root.getName() ) )
                {
                    typeRoot = typeRoot.getElement( root.getName() );
                }
            }

        }

        return new RootPair( typeRoot, root );
    }

    /**
     * Get the named option for this page.
     *
     * @param name The name of the option to look up
     * @return The value of the option, or null if it is not set
     */
    public String getOption( String name )
    {
        return getContentElement().getAttributeValue( name );
    }

    /**
     * Set an option on this page.
     *
     * @param name The name of the option to set
     * @param value The value to set the option to
     */
    public void setOption( String name, String value )
    {
        getContentElement().setAttribute( name, value );
    }

    public List getSupportedOptions( UserData user )
    {
        try
        {
            return getType( user ).getSupportedOptions();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return new Vector();
        }
    }

    public DocElement getType( UserData user )
    {
        return loadType( getContentElement().getName(), getPage(), user );
    }

    public static String getTypeClass( String typeName )
    {
        Type type = Type.getType( typeName );

        if ( type == null )
        {
            return "";
        }
        return type.getClassName();
    }

    public static String getTypeDescription( String typeName )
    {
        Type type = Type.getType( typeName );

        if ( type == null )
        {
            return "";
        }
        return type.getDescription();
    }

    private DocElement loadType( String type, DocumentPage page, UserData user )
    {
        try
        {
            Class typeClass = Class.forName( getTypeClass( type ) );
            Constructor con = typeClass.getConstructor( new Class[]{
                    java.lang.String.class} );
            DocElement ret = (DocElement) con.newInstance( new Object[]{type} );
            ret.setPage( page );
            ret.setUser( user );
            ret.setDoc( this );
            return ret;
        }
        catch ( Exception e )
        {
            System.err.println( "Error, doctype " + type + " could not be found" );
            e.printStackTrace();
            return null;
        }
    }

    public static String encode( String in )
    {
        if ( in == null )
        {
            return null;
        }
        char[] ret = new char[in.length()];
        in.getChars( 0, in.length(), ret, 0 );
        for ( int i = 0; i < ret.length; i++ )
        {
            if ( ret[i] >= 126 )
            {
                ret[i] = '_';
            }
            switch ( ret[i] )
            {
                case ' ':
                case '/':
                case '&':
                case '?':
                case '+':
                case '"':
                case '\'':
                    ret[i] = '_';
                    break;
                default:
            }
        }
        return new String( ret );
    }

    public static class GenerationException
            extends Exception
    {
        public GenerationException( String message )
        {
            super( message );
        }
    }
}

class RootPair
{
    private DocElement type;
    private Element root;

    public RootPair( DocElement type, Element root )
    {
        this.type = type;
        this.root = root;
    }

    public DocElement getType()
    {
        return type;
    }

    public Element getData()
    {
        return root;
    }
}