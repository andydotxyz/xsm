package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;
import com.rectang.xsm.io.PublishedFile;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.PropertyModel;

/**
 * Page for editing the output site settings
 *
 * @author Andrew Williams
 * @version $Id: Settings.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 */
public class Settings
        extends XSMPage
        implements Secure
{
    public Settings( PageParameters parameters )
    {
        super( parameters );
    }

    public int getLevel()
    {
        return AccessControl.MANAGER;
    }

    public void layout()
    {
        super.layout();

        add( new SiteForm( "site" ) );
    }

    class SiteForm
            extends Form
    {
        FileUploadField faviconField;
        FileUpload favicon;

        public SiteForm( String id )
        {
            super( id );
            com.rectang.xsm.site.Site site = getXSMSession().getSite();
            setMultiPart( true );

            setModel( new CompoundPropertyModel( site ) );
            setMultiPart( true );

            add( new TextField( "title" ) );
            add( new TextField( "keywords" ) );
            add( new TextField( "description" ) );

            WebMarkupContainer favicon = new WebMarkupContainer( "favicon-img" );
            favicon.add( new AttributeModifier( "src",
                    new Model( site.getRootUrl() + "/favicon.ico" ) ) );
            add( favicon );
            // TODO add ability to delete favicon.ico
            add( faviconField = new FileUploadField( "favicon", new PropertyModel( this, "favicon" ) ) );
        }

        public void onSubmit()
        {
            com.rectang.xsm.site.Site site = getXSMSession().getSite();
            if ( site.save() )
            {
                site.publish( getXSMSession().getUser() );
            }

            final FileUpload upload = faviconField.getFileUpload();
            if ( upload != null && upload.getClientFileName().toLowerCase().endsWith( ".ico" ) )
            {
                // Create a new file

                java.io.File tmpFile = XSM.getTempFile();
                try
                {
                    upload.writeTo( tmpFile );

                    PublishedFile file = site.getPublishedDoc( "/favicon.ico" );
                    file.uploadFile( tmpFile, true );
                }
                catch ( Exception e )
                {
                    throw new IllegalStateException( "Unable to write file" );
                }
            }
        }
    }
}
