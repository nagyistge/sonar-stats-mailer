package org.sguernion.sonar;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.sguernion.sonar.notification.SonarStats;
import org.sonar.wsclient.services.TimeMachine;
import org.sonar.wsclient.services.TimeMachineCell;
import org.sonar.wsclient.services.TimeMachineColumn;

public class App
{

    public static void main( String[] args )
    {
        PropertiesConfiguration props = null;

        try
        {
            System.out.println( "Load properties file" );
            String path = App.class.getProtectionDomain().getCodeSource().getLocation().toString().substring( 6 );
            path = path.substring( 0, path.length() - 15 );
            System.out.println( path );
            props = PropsUtils.load( new File( path + "/conf.properties" ) );

            System.out.println( "Connexion Sonar" );
            SonarStats sonnarS = SonarStats.connect( props );

            if ( args.length == 1 && args[0] == "-t" )
            {

                    System.out.println( getContentText( sonnarS ) );

            }
            else
            {
                System.out.println( "send Mail" );
                sonnarS.sendMail( props.getString( "mail.titre" ) );
                System.out.println( "envoyé" );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.err.println( "Erreur " + e.getMessage() );
        }
    }

    /**
     * @param sonnarS
     * @return
     */
    private static String getContentText( SonarStats sonnarS )
    {
        StringWriter writer = new StringWriter();
        PrintWriter outStream = new PrintWriter( writer );

        outStream.println( "---------------------------" );
        outStream.println( "- Sonar Stats TimeMachine -" );
        outStream.println( "---------------------------" );

        TimeMachine timeM = sonnarS.getTimeMachine( null, 10, SonarStats.KEYS );

        int[] taille = new int[timeM.getColumns().length];
        int i = 0;
        outStream.print( " " );
        for ( TimeMachineColumn coll : timeM.getColumns() )
        {
            outStream.print( coll.getMetricKey() + " | " );
            taille[i] = coll.getMetricKey().length();
            i++;
        }
        outStream.println( "" );
        for ( TimeMachineCell cell : timeM.getCells() )
        {
            i = 0;
            outStream.print( " " );
            for ( Object val : cell.getValues() )
            {
                outStream.print( val );
                int nb = taille[i] - getSize( val );
                for ( int j = 0; j < nb; j++ )
                {
                    outStream.print( " " );
                }
                outStream.print( " | " );
                i++;
            }
            outStream.println( "" );
        }

        return writer.toString();
    }

    /**
     * @param val
     * @return
     */
    private static int getSize( Object val )
    {
        if ( val instanceof String )
        {
            return ( (String) val ).length();
        }
        if ( val instanceof Long )
        {
            return ( (Long) val ).toString().length();
        }
        if ( val instanceof Integer )
        {

            return ( (Integer) val ).toString().length();
        }
        if ( val instanceof Double )
        {
            return ( (Double) val ).toString().length();
        }
        if ( val == null )
        {
            return 4;
        }
        return 0;
    }

}
