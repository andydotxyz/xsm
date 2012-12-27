package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.*;
import com.rectang.xsm.site.Visitor;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Page for adding a visitor account to the site
 *
 * @author Andrew Williams
 * @version $Id: AddVisitor.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class AddVisitor
        extends XSMPage
        implements Secure
{
    public AddVisitor( PageParameters parameters )
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

        add( new UserForm( "addvisitor", getXSMSession().getSite() ) );
    }

    class UserForm
            extends Form
    {
        private com.rectang.xsm.site.Site site;
        private String username, password, repeatPassword;

        public UserForm( String id, com.rectang.xsm.site.Site site )
        {
            super( id );
            this.site = site;

            setModel( new CompoundPropertyModel( this ) );

            PasswordTextField password, password2;
            add( new TextField( "username" ).setRequired( true ) );
            add( (password = new PasswordTextField( "password" )).setRequired( true ) );
            add( (password2 = new PasswordTextField( "repeatPassword" )).setRequired( true ) );

            add( new EqualPasswordInputValidator( password, password2 ) );
            add( new Button( "add" ) );
        }

        public void onSubmit()
        {
            if ( username.indexOf( ' ' ) > -1 )
            {
                warn( "Could not create visitor, username may not contain spaces" );
                return;
            }
            Visitor exists = site.getVisitor( username );
            if ( exists != null )
            {
                warn( "Could not create visitor, username" + username + " is already taken" );
                return;
            }

            Visitor visitor = new Visitor( username, password );
            site.setVisitor( visitor );
            getSession().info( "Successfully added visitor " + username );
            this.setResponsePage( Users.class );
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername( String username )
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword( String password )
        {
            this.password = password;
        }

        public String getRepeatPassword()
        {
            return repeatPassword;
        }

        public void setRepeatPassword( String repeatPassword )
        {
            this.repeatPassword = repeatPassword;
        }
    }
}