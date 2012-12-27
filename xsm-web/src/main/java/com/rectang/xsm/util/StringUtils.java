package com.rectang.xsm.util;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.List;

public class StringUtils
{

    /**
     * The alphabet that generated passwords should draw from
     */
    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Create a random password of eight characters length drawn from the set
     * ALPHABET.
     *
     * @return a random password of 8 characters length
     */
    public static String createPassword()
    {
        return createPassword( 8 );
    }

    /**
     * Create a random password of <code>length</code> characters length drawn
     * from the set ALPHABET.
     *
     * @return a random password of set length
     */
    public static String createPassword( int length )
    {
        String password = "";
        while ( password.length() < length )
        {
            int pos = (int) (Math.random() * (ALPHABET.length() - 1));
            password += ALPHABET.substring( pos, pos + 1 );
        }
        return password;
    }

    /**
     * Create a List of Strings from the contents of a comma separated String.
     *
     * @param in The comma separated String to unpack
     * @return A Vector of Strings containing the unpacked string elements
     */
    public static List /* String */ stringToList( String in )
    {
        List ret = new Vector();

        if ( in == null || in.equals( "" ) )
        {
            return ret;
        }
        String[] inSplit = in.split( "," );
        String inNext;
        for ( int i = 0; i < inSplit.length; i++ )
        {
            if ( !(inNext = inSplit[i].trim()).equals( "" ) )
            {
                ret.add( inNext );
            }
        }
        return ret;
    }

    /**
     * Create a String from the passed List. The string will be separated with
     * commas.
     *
     * @param in The Vector to pack into a String
     * @return A comma separated String containing all elements in the Vector
     */
    public static String listToString( List /* String */ in )
    {
        Iterator list = in.iterator();
        String all = "";
        while ( list.hasNext() )
        {
            Object next = list.next();
            if ( next instanceof String )
            {
                all += (String) next;
            }
            if ( list.hasNext() )
            {
                all += ",";
            }
        }

        return all;
    }

    /**
     * Summarise a string, returning a copy of the beginning of the String
     * <code>in</code> stopping at the last space before the position
     * <code>chars</code>. "..." will be appended to strings that are truncated.
     *
     * @param in    The String input to summarise
     * @param chars The maximum number of chars to appear in the summary
     * @return A new String summarising the input
     */
    public static String summarise( String in, int chars )
    {
        StringBuffer ret = new StringBuffer();

        int count = 0;
        StringTokenizer tokens = new StringTokenizer( in, " " );
        while ( tokens.hasMoreElements() )
        {
            String token = (String) tokens.nextElement();
            count += token.length() + 1;
            ret.append( token );
            if ( count > chars )
            {
                ret.append( "..." );
                break;
            }
            ret.append( " " );
        }

        return ret.toString();
    }
}