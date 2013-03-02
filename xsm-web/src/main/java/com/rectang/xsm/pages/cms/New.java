package com.rectang.xsm.pages.cms;

import com.rectang.xsm.doc.Type;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.HierarchicalPage;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.UserData;
import com.rectang.xsm.io.XSMDocument;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.PageParameters;

import java.io.File;
import java.util.Iterator;

import org.jdom.Element;

/**
 * The main CMS new tab
 *
 * @author Andrew Williams
 * @version $Id: New.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 */
public class New
        extends Page
{
    public New( PageParameters parameters )
    {
        super( parameters );
    }

    public void layout()
    {
        super.layout();
        if ( hasError() )
        {
            return;
        }

        add( new NewForm( "newform" ) );
    }

    private class NewForm
            extends Form
    {
        private String type = "html", name, level = "child";

        public NewForm( String id )
        {
            super( id );
            Site site = getXSMSession().getSite();
            UserData user = getXSMSession().getUser();

            WebMarkupContainer grouping =
                    new RadioGroup( "typegroup", new PropertyModel( this, "type" ) ).setRequired( true );
            add( grouping );

            grouping.add( new ListView( "types", Type.listTypes() )
            {
                protected void populateItem( ListItem listItem )
                {
                    Type type = (Type) listItem.getModelObject();
                    boolean visible = !type.isHidden() && type.isSupportedBy( getXSMSession().getSite() );

                    listItem.add( new Radio( "type", new Model( type.getName() ) ) );
                    listItem.add( new Label( "typename", type.getName() ) );
                    listItem.add( new Label( "typelabel", type.getDescription() ) );
                    listItem.setVisible( visible );
                }
            } );

            add( new TextField( "name", new PropertyModel( this, "name" ) ).setRequired( true ) );

            add( grouping =
                    new RadioGroup( "levelgroup", new PropertyModel( this, "level" ) ).setRequired( true ) );

            boolean canAddSibling = false;
            if ( getXSMPage().getParent() != null )
            {
                if ( getXSMPage().getParent().equals( site.getRootPage() ) )
                {
                    canAddSibling = user.isSiteAdmin();
                }
                else
                {
                    if ( getXSMPage().getParent() instanceof DocumentPage )
                    {
                        canAddSibling = ((DocumentPage) getXSMPage().getParent()).getXSMDocument().canEdit( user );
                    }
                    else
                    {
                        canAddSibling = user.isSiteAdmin();
                    }
                }
            }
            boolean canAddChild;
            if ( getXSMPage().equals( site.getRootPage() ) )
            {
                canAddChild = user.isSiteAdmin();
            }
            else
            {
                canAddChild = (getXSMPage() instanceof HierarchicalPage) && canEdit();
            }

            grouping.add( new Radio( "siblinglevel", new Model( "sibling" ) ).setVisible( canAddSibling ) );
            grouping.add( new Label( "siblingtext", new StringResourceModel( "level.sibling", this, new Model( getXSMPage() ) ) ) );
            grouping.add( new Radio( "childlevel", new Model( "child" ) ).setVisible( canAddChild ) );
            grouping.add( new Label( "childtext", new StringResourceModel( "level.child", this, new Model( getXSMPage() ) ) ) );
        }

        protected void onSubmit()
        {
            super.onSubmit();

            Site site = getXSMSession().getSite();
            UserData user = getXSMSession().getUser();
            String file = XSMDocument.encode( name );

            HierarchicalPage parent;
            if ( level.equals( "sibling" ) )
            {
                parent = getXSMPage().getParent();
            }
            else
            {
                if ( !(getXSMPage() instanceof HierarchicalPage) )
                {
                    getSession().error( "Cannot add a child page to a non-heirarchical parent" );
                    return;
                }
                parent = (HierarchicalPage) getXSMPage();
            }

            XSMDocument parentDoc = null;
            if ( parent instanceof DocumentPage )
            {
                parentDoc = ((DocumentPage) parent).getXSMDocument();
            }

            if ( file.charAt( 0 ) == '_' )
            { /* spaces already converted */
                warn( "Pages names cannot begin with the characters _& ?+/\"'" );
                return;
            }


            String newPath = new File( parent.getPath(), file ).getPath();
            com.rectang.xsm.site.Page existing = site.getPage( newPath );
            if ( existing != null )
            {
                warn( "Page " + newPath + " already exists" );
                return;
            }

      /* Add entry to contents for new page */
            DocumentPage newPage = new DocumentPage( site, parent, name, true );
            parent.addSubPage( newPage );
            XSMDocument newDoc = XSMDocument.getXSMDoc( site, newPage, true );
            if ( !site.save() )
            {
                fatal( "Error saving site, could not create new page" );
                return;
            }

      /* Create new page */
            newDoc.setContentElement( new Element( type ) );
      /* having created the base structure we need to lock it */
            newDoc.getRootElement().setAttribute( "lock", user.getUsername() );
            newDoc.getRootElement().setAttribute( "owner", user.getUsername() );

      /* If parent is of same type we copy options down */
            if ( parentDoc != null && parentDoc.getContentElement().getName().equals( type ) )
            {
                Iterator options = parentDoc.getSupportedOptions( getXSMSession().getUser() ).iterator();
                while ( options.hasNext() )
                {
                    SupportedOption next = (SupportedOption) options.next();

                    String value = parentDoc.getOption( next.getName() );
                    if ( value != null )
                    {
                        newDoc.setOption( next.getName(), value );
                    }
                }
            }

            if ( !newDoc.save( user, true ) )
            {
                fatal( "Could not create page " + newPath );
            }

            try
            {
                newDoc.add( "@0", "", getXSMSession().getUser() );
                newDoc.save();
            }
            catch ( Exception e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            PageParameters params = new PageParameters();
            params.add( "page", newPath );
            setResponsePage( Edit.class, params );
        }

        public String getType()
        {
            return type;
        }

        public void setType( String type )
        {
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public String getLevel()
        {
            return level;
        }

        public void setLevel( String level )
        {
            this.level = level;
        }
    }
}
