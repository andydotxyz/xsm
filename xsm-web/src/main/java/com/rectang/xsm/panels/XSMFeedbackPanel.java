package com.rectang.xsm.panels;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.ErrorLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.StringResourceModel;
import com.rectang.xsm.XSM;

import java.util.Map;
import java.util.HashMap;

/**
 * A feedback panel to render the user-facing logging.
 *
 * @author Andrew Williams
 * @version $Id: XSMFeedbackPanel.java 707 2007-11-01 18:05:15Z aje $
 * @since 2.0
 */
public class XSMFeedbackPanel
        extends Panel
{
    private static Map names = new HashMap();
    private static Map icons = new HashMap();

    static
    {
        names.put( new Integer( FeedbackMessage.UNDEFINED ), "undefined" );
        names.put( new Integer( FeedbackMessage.DEBUG ), "debug" );
        names.put( new Integer( FeedbackMessage.INFO ), "info" );
        names.put( new Integer( FeedbackMessage.WARNING ), "warn" );
        names.put( new Integer( FeedbackMessage.ERROR ), "error" );
        names.put( new Integer( FeedbackMessage.FATAL ), "fatal" );

        icons.put( new Integer( FeedbackMessage.UNDEFINED ), "" );
        icons.put( new Integer( FeedbackMessage.DEBUG ), "" );
        icons.put( new Integer( FeedbackMessage.INFO ), "icons/info.png" );
        icons.put( new Integer( FeedbackMessage.WARNING ), "icons/warning.png" );
        icons.put( new Integer( FeedbackMessage.ERROR ), "icons/error.png" );
        icons.put( new Integer( FeedbackMessage.FATAL ), "icons/error.png" );
    }

    public XSMFeedbackPanel( String id )
    {
        super( id );

        add( new ListView( "messages", new AllMessagesFilter() )
        {
            protected void populateItem( ListItem listItem )
            {

                FeedbackMessage message = (FeedbackMessage) listItem.getModelObject();
                listItem.add( new Label( "message", new PropertyModel( message, "message" ) ) );
                listItem.add( new Label( "level", new StringResourceModel(
                        (String) names.get( new Integer( message.getLevel() ) ), this, null ) ) );
                listItem.add( new Image( "icon", new ResourceReference( XSM.class,
                        (String) icons.get( new Integer( message.getLevel() ) ) ) ) );

                message.markRendered();
            }
        } );
    }

    class AllMessagesFilter
            extends AbstractReadOnlyModel
    {
        public Object getObject()
        {
            boolean hasMessages = getSession().getFeedbackMessages().size() > 0;

            IFeedbackMessageFilter allMessagesFilter = new ErrorLevelFeedbackMessageFilter( FeedbackMessage.DEBUG );
            if ( hasMessages )
            {
                // Return messages from both the page and the session
                return getSession().getFeedbackMessages().messages( allMessagesFilter );
            }

            return null;
        }
    }
}
