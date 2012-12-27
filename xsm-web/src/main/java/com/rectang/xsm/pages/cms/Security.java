package com.rectang.xsm.pages.cms;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;

import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.site.Visitor;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.util.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Iterator;

/**
 * The main CMS security tab
 *
 * @author Andrew Williams
 * @version $Id: Security.java 816 2010-05-30 14:02:03Z andy $
 * @since 2.0
 */
public class Security
        extends DocumentPage
{
    private static final SupportedOption VISITORS = new SupportedOption( "SECURITY_VISITORS", "allowed users", "" );
    private static final SupportedOption ALL_USERS = new SupportedOption( "SECURITY_ALL_USERS", "allow all users", true );

    public Security( PageParameters parameters )
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

        // stop folk from loading this page if we have no apache support
        if ( !getXSMSession().getSite().getTechnologies().contains( "apache" ) )
        {
            setResponsePage( Options.class );
        }

        add( new OptionsForm( "securityform" ) );
    }

    private class OptionsForm
            extends Form
    {
        private boolean enable = getOverrideFile().exists();
        private boolean all = ALL_USERS.getBoolean( getDoc() );
        private List visitors = StringUtils.stringToList( VISITORS.getString( getDoc() ) );

        public OptionsForm( String id )
        {
            super( id );
            final boolean canEdit = getDoc().canEdit( getXSMSession().getUser() );

            add( new CheckBox( "enable", new PropertyModel( this, "enable" ) ) );
            add( new CheckBox( "all", new PropertyModel( this, "all" ) ) );

            CheckGroup group = new CheckGroup( "group", new PropertyModel( this, "visitors" ) );
            add( group );
            group.add( new ListView( "visitor", getXSMSession().getSite().getVisitors() )
            {
                protected void populateItem( ListItem listItem )
                {
                    Visitor visitor = (Visitor) listItem.getModelObject();
                    listItem.add( new Label( "username", visitor.getUsername() ) );

                    listItem.add( new Check( "allow", new Model( visitor.getUsername() ) ) );
                }
            } );

            Button save = new Button( "save" );
            save.setVisible( canEdit );
            add( save );

            Button reset = new Button( "reset" );
            reset.setVisible( canEdit );
            add( reset );
        }


        protected void onSubmit()
        {
            super.onSubmit();
            getDoc().setOption( VISITORS.getName(), StringUtils.listToString( visitors ) );
            getDoc().setOption( ALL_USERS.getName(), String.valueOf( all ) );
            getDoc().save();
            PublishedFile file = getOverrideFile();

            if ( enable )
            {
                Site site = getXSMSession().getSite();
                BufferedWriter out = null;
                try
                {
                    out = new BufferedWriter( new OutputStreamWriter( file.getOutputStream() ) );
                    out.write( "AuthUserFile " + site.getVisitorsFile().getAbsolutePath() );
                    out.newLine();
                    out.write( "AuthType Basic" );
                    out.newLine();
                    out.write( "AuthName \"" + site.getTitle() + " Security\"" );
                    out.newLine();

                    if ( all )
                    {
                        out.write( "require valid-user" );
                    }
                    else
                    {
                        out.write( "require user" );
                        if ( visitors.size() == 0 )
                        {
                            out.write( " totallynousersallowed" );
                        }
                        else
                        {
                            Iterator users = visitors.iterator();
                            while ( users.hasNext() )
                            {
                                String visitor = (String) users.next();

                                out.write( " " );
                                out.write( visitor );
                            }
                        }
                    }
                    out.newLine();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if ( out != null )
                        {
                            out.close();
                        }
                    }
                    catch ( IOException e )
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
            else
            {
                file.delete();
            }
        }

        protected PublishedFile getOverrideFile()
        {
            Site site = ((XSMSession) getSession()).getSite();
            return site.getPublishedDoc( getDoc().getPage().getPath() + "/.htaccess" );
        }
    }
}