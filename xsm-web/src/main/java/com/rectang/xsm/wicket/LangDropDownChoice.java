package com.rectang.xsm.wicket;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;

import java.util.LinkedList;

import com.rectang.xsm.Locale;
import com.rectang.xsm.pages.XSMSession;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: LangDropDownChoice.java 663 2007-10-04 22:50:25Z aje $
 * @since 1.0
 */
public class LangDropDownChoice
        extends DropDownChoice
{
    private XSMSession session;
    private boolean updates;

    public LangDropDownChoice( String id, XSMSession session )
    {
        this( id, session, false );
    }

    public LangDropDownChoice( String id, XSMSession session, boolean updates )
    {
        super( id, new LinkedList( Locale.getLangMap().keySet() ),
                new LangRenderer() );

        this.session = session;
        this.updates = updates;
    }

    public LangDropDownChoice( String id, IModel model, XSMSession session )
    {
        this( id, model, session, false );
    }

    public LangDropDownChoice( String id, IModel model, XSMSession session, boolean updates )
    {
        super( id, model, new LinkedList( Locale.getLangMap().keySet() ),
                new LangRenderer() );

        this.session = session;
        this.updates = updates;
    }

    protected boolean wantOnSelectionChangedNotifications()
    {
        return updates;
    }

    protected void onSelectionChanged( Object object )
    {
        java.util.Locale locale = new java.util.Locale( (String) object );
        session.setLocale( locale );
    }
}

class LangRenderer
        extends ChoiceRenderer
{
    public Object getDisplayValue( Object object )
    {
        return Locale.getLangName( (String) object );
    }
}
