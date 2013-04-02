package org.sguernion.sonar.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.sonar.wsclient.Sonar;

/**
 * @author SGUERNIO
 */
public class SonarMultiStats
    extends SonarStats
{

    private static List<ProjetConfiguration> projects = new ArrayList<ProjetConfiguration>();

    /**
     * 
     */
    private SonarMultiStats()
    {
    }

    /**
     * @param props
     * @param ressources
     * @return
     */
    public static SonarStats connect( PropertiesConfiguration props, String[] pRessources )
    {
        SonarMultiStats stats = new SonarMultiStats();
        stats.props = props;
        String[] jobs = props.getStringArray( "jenkins.job" );

        for ( int i = 0; i < pRessources.length; i++ )
        {
            ProjetConfiguration project = new ProjetConfiguration();
            project.resource = pRessources[i];
            project.job = jobs[i];
            project.duplications = props.getStringArray( "content.duplications" )[i].equals( "true" );
            project.coverage = props.getStringArray( "content.coverage.graph" )[i].equals( "true" );
            project.testsGraph = props.getStringArray( "content.tests.graph" )[i].equals( "true" );
            project.tests = props.getStringArray( "content.tests" )[i].equals( "true" );
            project.violations = props.getStringArray( "content.violations" )[i].equals( "true" );
            projects.add( project );
            System.out.println( project.toString() );
        }
        return stats;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sguernion.sonar.notification.SonarStats#getContentHtml()
     */
    @Override
    public String getContentHtml()
    {

        for ( ProjetConfiguration pProject : projects )
        {
            mailWriter.addHtml( pProject.job );
            sonar =
                Sonar.create( HTTP + props.getString( SONAR_HOST ) + SEP + props.getString( SONAR_PORT ),
                    props.getString( SONAR_USER ), props.getString( SONAR_PASSWORD ) );
            mailWriter.addOnglet( pProject.job );
            buildContent( pProject );
            mailWriter.addBr().addBr();
        }
        return mailWriter.getContent();
    }

}
