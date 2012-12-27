package com.rectang.xsm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A cool class that gobbles stream output
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public class StreamGobbler
        extends Thread
{
    private InputStream in;
    private boolean complete = false;

    private StringBuilder content;
    private String lastLine;

    public StreamGobbler( InputStream in )
    {
        this.in = in;
        content = new StringBuilder();
    }

    public void run()
    {
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
            String line;
            while ( (line = reader.readLine()) != null )
            {
                content.append( line );
                content.append( "\n" );
                lastLine = line;
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        complete = true;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public StringBuilder getContent()
    {
        return content;
    }

    public String getLastLine()
    {
        return lastLine;
    }
}
