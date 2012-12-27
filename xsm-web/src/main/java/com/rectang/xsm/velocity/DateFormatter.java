package com.rectang.xsm.velocity;

import com.rectang.xsm.MetaData;

import java.util.Date;
import java.text.DateFormat;

/**
 * Convenience code for formatting dates in Velocity
 *
 * @author Andrew Williams
 * @version $Id: DateFormatter.java 693 2007-10-25 19:53:29Z aje $
 * @since 2.0
 */
public class DateFormatter
{
    public String formatISO( Date date )
    {
        return MetaData.DATE_FORMAT.format( date );
    }

    public String formatShortDate( Date date )
    {
        return DateFormat.getDateInstance( java.text.DateFormat.SHORT ).format( date );
    }

    public String formatDate( Date date )
    {
        return DateFormat.getDateInstance( java.text.DateFormat.LONG ).format( date );
    }

    public String formatShortTime( Date date )
    {
        return DateFormat.getTimeInstance( java.text.DateFormat.SHORT ).format( date );
    }

    public String formatTime( Date date )
    {
        return DateFormat.getTimeInstance( java.text.DateFormat.LONG ).format( date );
    }
}
