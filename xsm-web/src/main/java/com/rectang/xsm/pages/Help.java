package com.rectang.xsm.pages;

import com.rectang.xsm.AccessControl;

import com.rectang.xsm.XSMApplication;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.PageParameters;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Help.java 672 2007-10-06 21:42:14Z aje $
 * @since 2.0
 */
public class Help
        extends XSMPage
{
    private String rootHelpUrl = "http://ajwillia-ms.github.com/xsm/help-content/";

    public int getLevel()
    {
        return AccessControl.MEMBER;
    }

    public Help( PageParameters parameters )
            throws Exception
    {
        super( parameters );
    }

    public void layout()
    {
        super.layout();

        add( HeaderContributor.forCss( XSMApplication.class, "xsm-help.css" ) );

        add( new Include( "content", rootHelpUrl + "index.html" ) );
    }
}
