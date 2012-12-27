/*
 * Created on Aug 17, 2005
 *
 * Wrapper for language code, both detection of preferred and loading of data
 */
package com.rectang.xsm;

import org.apache.wicket.Session;

import java.util.*;

/**
 * @author aje
 *
 * Wrapper for language code, both detection of preferred and loading of data.
 * Language preferences have the following heirarchy.
 * <ul>
 *   <li>User preference - if the user has chosen a language in the preferences
 *       area this one is used</li>
 *   <li>Browser default - if no user preference is specified then the browsers
 *       default locale is used</li>
 *   <li>Server default - if neither of the above are used then the default is
 *       read from the server</li>
 * </ul>
 * If none of the above are specified, or a specified locale is not found then
 * the default XSM language pack is used.
 */
public class Locale
{

    private static ResourceBundle langs = ResourceBundle.getBundle( "language" );

    private static Map langMap = null;

    /**
     * Get the current locale according to the rules stated above.
     *
     * @param user The user who's preferences we are loading
     * @return a Locale object stating the currently applicable locale
     */
    public static java.util.Locale getLocale( UserData user )
    {
        if ( user != null )
        {
            String locale = user.getLocale();
            if ( locale != null && !locale.equals( "" ) )
            {
                return new java.util.Locale( locale );
            }
        }

        java.util.Locale ret = Session.get().getLocale();
        if ( ret != null )
        {
            return ret;
        }

        return java.util.Locale.getDefault();
    }

    /**
     * List the available languages.
     *
     * @return A list of the languages available
     */
    public static Enumeration getLangs()
    {
        return langs.getKeys();
    }

    /**
     * Get the title of a specific language
     *
     * @param lang The language identifier
     * @return The textual representation for the language
     */
    public static String getLangName( String lang )
    {
        return langs.getString( lang );
    }

    public static Map getLangMap()
    {
        if ( langMap == null )
        {
            langMap = new HashMap();

            Enumeration langs = getLangs();
            while ( langs.hasMoreElements() )
            {
                String lang = (String) langs.nextElement();

                langMap.put( lang, getLangName( lang ) );
            }
        }

        return langMap;
    }

    /**
     * Get the ResourceBundle for the current locale. This may be specified by the
     * user preferences, or by the browser, or (if neither other specified) the
     * server default.
     *
     * @param user The user who's preferences we are loading
     * @return The resourceBundle containing the language strings, or the default
     *         bundle if that specified is not found.
     */
    public static ResourceBundle getBundle( UserData user )
    {
        return ResourceBundle.getBundle( "com.rectang.xsm.XSMApplication",
                getLocale( user ) );
    }

    /**
     * Get a string from the current language
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key )
    {
        return getBundle( user ).getString( key );
    }

    /**
     * Get a string from the current language, replacing $1 with String s1.
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @param s1   The substituted string
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key, String s1 )
    {
        return getString( user, key ).replaceAll( "$1", s1 );
    }

    /**
     * Get a string from the current language, replacing $1 with String s1 and
     * $2 with s2.
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @param s1   The first substituted string
     * @param s2   The second substituted string
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key, String s1, String s2 )
    {
        return getString( user, key, s1 ).replaceAll( "$2", s2 );
    }

    /**
     * Get a string from the current language, replacing $1 with String s1, $2
     * with s2 and $3 with s3.
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @param s1   The first substituted string
     * @param s2   The second substituted string
     * @param s3   The third substituted string
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key, String s1, String s2,
                                    String s3 )
    {
        return getString( user, key, s1, s2 ).replaceAll( "$3", s3 );
    }

    /**
     * Get a string from the current language, replacing $1 with String s1, $2
     * with s2, $3 with s3 and $4 with s4.
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @param s1   The first substituted string
     * @param s2   The second substituted string
     * @param s3   The third substituted string
     * @param s4   The fourth substituted string
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key, String s1, String s2,
                                    String s3, String s4 )
    {
        return getString( user, key, s1, s2, s3 ).replaceAll( "$4", s4 );
    }

    /**
     * Get a string from the current language, replacing $1 with String s1, $2
     * with s2, $3 with s3, $4 with s4 and $5 with s5.
     *
     * @param user The user who's preferences we are loading
     * @param key  The name of the string to retrieve
     * @param s1   The first substituted string
     * @param s2   The second substituted string
     * @param s3   The third substituted string
     * @param s4   The fourth substituted string
     * @param s5   The fifth substituted string
     * @return The localised String requested from the language pack
     */
    public static String getString( UserData user, String key, String s1, String s2,
                                    String s3, String s4, String s5 )
    {
        return getString( user, key, s1, s2, s3, s4 ).replaceAll( "$5", s5 );
    }
}