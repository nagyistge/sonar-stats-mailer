package org.sguernion.sonar.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.ResourceQuery;

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
            stats.sonar =
                Sonar.create( HTTP + props.getString( SONAR_HOST ) + SEP + props.getString( SONAR_PORT ),
                    props.getString( SONAR_USER ), props.getString( SONAR_PASSWORD ) );
            ProjetConfiguration project = new ProjetConfiguration();
            project.resource = pRessources[i];
            project.job = jobs[i];
            project.duplications = props.getStringArray( "content.duplications" )[i].equals( "true" );
            project.coverage = props.getStringArray( "content.coverage.graph" )[i].equals( "true" );
            project.testsGraph = props.getStringArray( "content.tests.graph" )[i].equals( "true" );
            project.tests = props.getStringArray( "content.tests" )[i].equals( "true" );
            project.violations = props.getStringArray( "content.violations" )[i].equals( "true" );
            project.index =
                stats.sonar.find( ResourceQuery.createForMetrics( project.resource, new String[] {} ) ).getId()
                    .toString();
            project.jobUrl =
                HTTP + props.getString( JENKINS_HOST ) + SEP + props.getString( JENKINS_PORT ) + "/job/" + project.job;
            project.sonarLink =
                HTTP + props.getString( SONAR_HOST ) + SEP + props.getString( SONAR_PORT ) + "/dashboard/index/"
                    + project.index;
            projects.add( project );
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
            mailWriter.addOnglet( pProject.job );
            buildContent( pProject );
            mailWriter.addBr().addBr();
        }
        return mailWriter.getContent();
    }

}
