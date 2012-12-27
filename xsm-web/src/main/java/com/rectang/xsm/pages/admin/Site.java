package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;
import com.rectang.xsm.io.PublishedFile;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.PropertyModel;

/**
 * Page for summarising site management options
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class Site
        extends XSMPage
        implements Secure
{
    public Site( PageParameters parameters )
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

        add( new PluginLink( "settingsPlugin", com.rectang.xsm.pages.admin.Settings.class, "settings" ) );
        add( new PluginLink( "themePlugin", com.rectang.xsm.pages.admin.Theme.class, "theme" ) );
        add( new PluginLink( "usersPlugin", com.rectang.xsm.pages.admin.Users.class, "users" ) );
        add( new PluginLink( "backupPlugin", com.rectang.xsm.pages.admin.Backup.class, "backup" ) );
        add( new PluginLink( "systemPlugin", com.rectang.xsm.pages.admin.System.class, "system" ) );
    }
}
