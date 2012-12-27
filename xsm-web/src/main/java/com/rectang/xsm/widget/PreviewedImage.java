package com.rectang.xsm.widget;

import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.util.FileUtils;

import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.*;

import javax.imageio.*;

import org.jdom.Element;

public class PreviewedImage
        extends Image
        implements Serializable
{

    public static final SupportedOption KEEP_ORIG = new SupportedOption( "KEEP_ORIG",
            "Keep the origional (full sized) images? [Old images should be re-uploaded]", true );
    public static final SupportedOption THUMB_SIZE = new SupportedOption( "THUMB_SIZE",
            "The maximum size in pixels for image thumbnails [Old images should be re-uploaded]", 160 );
    public static final SupportedOption PREVIEW_SIZE = new SupportedOption( "PREVIEW_SIZE",
            "The maximum size in pixels for image previews [Old images should be re-uploaded]", 800 );
    private Vector options;

    public PreviewedImage( java.lang.String name )
    {
        super( name );

        options = new Vector();
        options.add( KEEP_ORIG );
        options.add( THUMB_SIZE );
        options.add( PREVIEW_SIZE );
    }

    protected java.lang.String getViewURL( Element node )
    {
        return getPath( FileUtils.getImageThumbnailName( node.getValue() ) );
    }

    public void publish( Element node, StringBuffer s )
    {
        if ( node.getValue() == null || node.getValue().equals( "" ) )
        {
            s.append( "(no image)\n" );
        }
        else
        {
            s.append( "<a href=\"" + getSite().getPrefixUrl() );
            s.append( getPath( FileUtils.getImagePreviewName( node.getValue() ) ) + "\"><img src=\"" );
            s.append( getSite().getPrefixUrl() );
            s.append( getPath( FileUtils.getImageThumbnailName( node.getValue() ) ) + "\" border=\"0\"/></a>\n" );
        }
    }

    public void destroy( Element node )
    {
        java.lang.String imageName = node.getText();
        if ( imageName == null || imageName.equals( "" ) )
        {
            return;
        }

        PublishedFile file = (getSite().getPublishedDoc( getPath(
                imageName ) ));
        if ( file.exists() )
        {
            file.delete();
        }

        file = (getSite().getPublishedDoc( getPath( FileUtils.getImagePreviewName(
                imageName ) ) ));
        if ( file.exists() )
        {
            file.delete();
        }

        file = (getSite().getPublishedDoc( getPath( FileUtils.getImageThumbnailName(
                imageName ) ) ));
        if ( file.exists() )
        {
            file.delete();
        }
    }

    private static BufferedImage thumbnail( BufferedImage in, int max )
    {
        int width = in.getWidth();
        int height = in.getHeight();

        int newWidth, newHeight;

        if ( width <= max && height <= max )
        {
            newWidth = width;
            newHeight = height;
        }
        else
        {
            double dw = (double) max / width;
            double dh = (double) max / height;
            double mind = Math.min( dw, dh );

            newWidth = (int) (((double) width) * mind);
            newHeight = (int) (((double) height) * mind);
        }

        BufferedImage out = new BufferedImage( newWidth, newHeight,
                BufferedImage.TYPE_INT_RGB );
        Graphics2D g2d = out.createGraphics();
        g2d.drawImage( in.getScaledInstance( newWidth, newHeight,
                java.awt.Image.SCALE_SMOOTH ), 0, 0, null );
        g2d.dispose();

        return out;
    }

    protected void clearOldFiles( Element node )
    {
        java.lang.String oldName = node.getText();

        if ( oldName != null && !oldName.equals( "" ) )
        {
            PublishedFile oldFile = getSite().getPublishedDoc(
                    getPath( oldName ) );
            if ( oldFile.exists() )
            {
                oldFile.delete();
            }
            oldFile = getSite().getPublishedDoc( getPath(
                    FileUtils.getImagePreviewName( oldName ) ) );
            if ( oldFile.exists() )
            {
                oldFile.delete();
            }
            oldFile = getSite().getPublishedDoc( getPath(
                    FileUtils.getImageThumbnailName( oldName ) ) );
            if ( oldFile.exists() )
            {
                oldFile.delete();
            }
            node.setText( "" );
        }
        super.clearOldFiles( node );
    }

    protected void processFile( java.lang.String fileName,
                                java.io.File in, PublishedFile pub )
            throws XSMDocument.GenerationException
    {
        boolean keep_orig = KEEP_ORIG.getBoolean( getDoc() );
        int thumb_size = THUMB_SIZE.getInteger( getDoc() );
        int preview_size = PREVIEW_SIZE.getInteger( getDoc() );

        try
        {
            BufferedImage full = ImageIO.read( new FileInputStream( in ) );

            PublishedFile preview = getSite().getPublishedDoc(
                    getPath( FileUtils.getImagePreviewName( fileName ) ) );
            BufferedImage capped = thumbnail( full, preview_size );
            preview.mkparentdirs(); /* needed as getOutputStream does not mkdir */
            OutputStream tmp = preview.getOutputStream();
            ImageIO.write( capped, "JPG", tmp );
            tmp.close();

            PublishedFile thumb = getSite().getPublishedDoc(
                    getPath( FileUtils.getImageThumbnailName( fileName ) ) );
            tmp = thumb.getOutputStream();
            ImageIO.write( thumbnail( capped, thumb_size ), "JPG", tmp );
            tmp.close();

            if ( keep_orig )
            {
                pub.uploadFile( in );
            }
        }
        catch ( IOException e )
        {
            throw new XSMDocument.GenerationException( "Error generating image thumbnails: " + e.getMessage() );
        }
    }

    public List getSupportedOptions()
    {
        return options;
    }
}
