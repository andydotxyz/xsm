package com.rectang.xsm.pages.nav;

import com.rectang.xsm.pages.XSMPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.PageParameters;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.UserData;
import com.rectang.xsm.pages.cms.Page;
import com.rectang.xsm.site.HierarchicalPage;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Site;

import java.io.File;

/**
 * The main CMS contents tab rename page
 *
 * @author Andrew Williams
 * @version $Id: Rename.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 */
public class Rename
        extends Page
{
    public Rename( PageParameters parameters )
    {
        super( parameters );
    }

    public void layout()
    {
        super.layout();

        if ( !canEdit() )
        {
            warn( "You do not have permission to rename this page" );
            setResponsePage( Contents.class, getPageNameParams() );
            return;
        }

        add( new Label( "rename", getString( "newname", new Model( getXSMPage() ) ) ) );
        add( new RenameForm( "renameform", getXSMPage().getTitle() ) );
    }

    private class RenameForm
            extends Form
    {
        private String title;

        public RenameForm( String id, String title )
        {
            super( id );
            this.title = title;

            add( new TextField( "title", new PropertyModel( this, "title" ) ) );

            Button cancel = new Button( "cancel" )
            {
                public void onSubmit()
                {
                    setResponsePage( Contents.class, getPageNameParams() );
                }
            };
            cancel.setDefaultFormProcessing( false );
            add( cancel );
        }

        protected void onSubmit()
        {
            super.onSubmit();
            Site site = getXSMSession().getSite();
            UserData userdata = getXSMSession().getUser();
            boolean wasHidden = getXSMPage().getHidden();
            String oldTitle = getXSMPage().getTitle();
            String pagePath = getXSMPage().getPath();

            String newTitle, escaped, path;
            if ( title.charAt( 0 ) != '/' )
            {
                newTitle = title;
                escaped = XSMDocument.encode( newTitle );
                path = new File( getXSMPage().getParent().getPath(), escaped ).getPath();
            }
            else
            {
                newTitle = (new File( title )).getName();
                escaped = XSMDocument.encode( newTitle );
                path = new File( (new File( title )).getParent(), escaped ).getPath();
                oldTitle = new File( getXSMPage().getParent().getPath(), oldTitle ).getPath();
                newTitle = new File( (new File( title )).getParent(), newTitle ).getPath();
            }

      /* check for name sanity */
            if ( escaped.charAt( 0 ) == '_' )
            { /* spaces already converted */
                warn( "Pages names cannot begin with the characters _& ?+/\"'" );
                return;
      /* check for name existance */
            }
            else if ( site.getPage( path ) != null )
            {
                info( "Page \"" + path + "\" already exists" );
                return;
            }
            else
            {
                HierarchicalPage newParent = (HierarchicalPage) site.getPage( (new File( path )).getParent() );
                if ( (newParent.equals( site.getRootPage() ) && !userdata.isSiteAdmin())
                        || (!newParent.equals( site.getRootPage() ) &&
                        (!(newParent instanceof DocumentPage) || !(XSMDocument.getXSMDoc( site, (DocumentPage) newParent, false ))
                                .canEdit( userdata ))) )
                {
                    warn( "You do not have permission to create the destination page" );
                    return;
                }
                else
                {
                    boolean renamed = true;
                    com.rectang.xsm.site.Page xsmPage = getXSMPage();
                    if ( xsmPage instanceof com.rectang.xsm.site.DocumentPage )
                    {
                        renamed = ((com.rectang.xsm.site.DocumentPage) xsmPage).getXSMDocument()
                                .rename( site, new DocumentPage( site, newParent, escaped ) );
                    }

                    if ( renamed && xsmPage.rename( newTitle ) )
                    {
                        (RemoteDocument.getDoc( site, "/data" + pagePath, false )).rename( site, "/data" + path, false );
                        // this means there is no slug set, so rename the output too
                        if ( xsmPage.getPath().equals( xsmPage.getPublishedPath() ) )
                        {
                            site.getPublishedDoc( xsmPage.getPath() ).rename( path );
                        }
                        site.save();
                        getSession().info( "Page \"" + oldTitle + "\" successfully renamed to \""
                                + newTitle + "\"" );
                        if ( !wasHidden )
                        {
                            site.publish( getXSMSession().getUser() );
                        }

                        PageParameters newPage = new PageParameters();
                        newPage.add( "page", path );
                        setResponsePage( Contents.class, newPage );
                    }
                    else
                    {
                        getSession().info( "Failed to rename page \"" + oldTitle + "\"" );
                        setResponsePage( Contents.class, getPageNameParams() );
                    }
                }
            }
        }

        public String getTitle()
        {
            return title;
        }

        public void setTitle( String title )
        {
            this.title = title;
        }
    }
}
