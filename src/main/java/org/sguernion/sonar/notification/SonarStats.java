package org.sguernion.sonar.notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.sguernion.sonar.notification.MailContentWriter.Block;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.TimeMachine;
import org.sonar.wsclient.services.TimeMachineColumn;
import org.sonar.wsclient.services.TimeMachineQuery;

/**
 * @author sguernio
 */
public class SonarStats
    implements Props
{
    protected static final String TRUE = "true";

    protected static final String SEP = ":";

    protected static final String HTTP = "http://";

    protected Sonar sonar;

    protected PropertiesConfiguration props;

    protected MailContentWriter mailWriter = new MailContentWriter();

    protected ProjetConfiguration project;

    protected SonarStats()
    {
    }

    /**
     * @param props
     * @param resource2
     * @return
     */
    public static SonarStats connect( PropertiesConfiguration props )
    {
        controleProperties( props );

        String[] ressources = props.getStringArray( Props.SONAR_RESOURCE );
        SonarStats stat = null;
        if ( ressources.length > 1 )
        {
            stat = SonarMultiStats.connect( props, ressources );

        }
        else
        {
            stat =
                SonarStats.connect( HTTP + props.getString( SONAR_HOST ) + SEP + props.getString( SONAR_PORT ),
                    props.getString( SONAR_USER ), props.getString( SONAR_PASSWORD ), ressources[0] );
            stat.props = props;
            stat.project.job = props.getString( JENKINS_JOB );
            stat.project.duplications = props.getBoolean( "content.duplications" );
            stat.project.coverage = props.getBoolean( "content.coverage.graph" );
            stat.project.testsGraph = props.getBoolean( "content.tests.graph" );
            stat.project.tests = props.getBoolean( "content.tests" );
            stat.project.violations = props.getBoolean( "content.violations" );
            stat.project.resource = ressources[0];
            stat.project.index =
                stat.sonar.find( ResourceQuery.createForMetrics( stat.project.resource, new String[] {} ) ).getId()
                    .toString();
            stat.project.jobUrl =
                HTTP + props.getString( JENKINS_HOST ) + SEP + props.getString( JENKINS_PORT ) + "/job/"
                    + stat.project.job;
            stat.project.sonarLink =
                HTTP + props.getString( SONAR_HOST ) + SEP + props.getString( SONAR_PORT ) + "/dashboard/index/"
                    + stat.project.index;
            // System.out.println( stat.project.toString() );
        }
        return stat;
    }

    public void getJenkinsStates()
    {
        String jsonUrl = project.jobUrl + "/api/json?";
    }

    /**
     * @param props2
     */
    private static void controleProperties( PropertiesConfiguration props )
    {
        assert ( props.containsKey( "content.coverage.graph" ) );
        assert ( props.containsKey( "content.tests.graph" ) );
        assert ( props.containsKey( "content.tests" ) );
        assert ( props.containsKey( "content.violations" ) );
        assert ( props.containsKey( "content.duplications" ) );

        assert ( props.containsKey( JENKINS_HOST ) );
        assert ( props.containsKey( JENKINS_USER ) );
        assert ( props.containsKey( JENKINS_PASSWORD ) );
        assert ( props.containsKey( JENKINS_PORT ) );
        assert ( props.containsKey( JENKINS_JOB ) );

        assert ( props.containsKey( SONAR_HOST ) );
        assert ( props.containsKey( SONAR_USER ) );
        assert ( props.containsKey( SONAR_PASSWORD ) );
        assert ( props.containsKey( SONAR_PORT ) );
        assert ( props.containsKey( SONAR_RESOURCE ) );
        assert ( props.containsKey( SONAR_DAYS ) );

        assert ( props.containsKey( MAIL_FROM ) );
        assert ( props.containsKey( MAIL_SMTP_HOST ) );
        assert ( props.containsKey( MAIL_SMTP_AUTH ) );
        assert ( props.containsKey( MAIL_TO ) );
        assert ( props.containsKey( MAIL_TITRE ) );
        assert ( props.containsKey( MAIL_PASSWORD ) );
        assert ( props.containsKey( MAIL_USER ) );
    }

    private static SonarStats connect( String url, String login, String password, String resource )
    {
        SonarStats stat = new SonarStats();
        stat.sonar = Sonar.create( url, login, password );
        stat.project = new ProjetConfiguration();
        stat.project.resource = resource;
        return stat;
    }

    protected List<Measure> get()
    {
        Resource struts = sonar.find( ResourceQuery.createForMetrics( project.resource, KEYS ) );
        return struts.getMeasures();
    }

    private TimeMachine getTimeMachine( String pResource, int nbJours )
    {
        return getTimeMachine( pResource, nbJours, KEYS );
    }

    public TimeMachine getTimeMachine( String pResource, int nbJours, String[] keys )
    {
        if ( pResource == null )
        {
            pResource = project.resource;
        }
        Date now = new Date();
        TimeMachineQuery query = TimeMachineQuery.createForMetrics( pResource, keys );
        Calendar calendar = new GregorianCalendar();
        calendar.setTime( now );
        calendar.add( Calendar.DATE, -nbJours );
        query.setFrom( calendar.getTime() );
        query.setTo( now );

        TimeMachine struts = sonar.find( query );
        return struts;
    }

    private Measure get( String key )
    {
        Resource struts = sonar.find( ResourceQuery.createForMetrics( project.resource, KEYS ) );
        return struts.getMeasure( key );
    }

    public static final String[] KEYS = new String[] { "blocker_violations", "branch_coverage", "classes",
        "comment_lines_density", "comment_lines", "complexity", "coverage", "critical_violations", "duplicated_blocks",
        "duplicated_files", "duplicated_lines_density", "duplicated_lines", "files", "line_coverage", "lines",
        "lines_to_cover", "major_violations", "minor_violations", "info_violations", "packages", "public_api",
        "public_documented_api_density", "public_undocumented_api", "skipped_tests", "test_errors",
        "test_execution_time", "test_failures", "test_success_density", "tests", "violations_density", "violations" };

    public static final String[] KEYS_ALL = new String[] { "abstractness", "accessors", "active_reviews", "ca",
        "alert_status", "blocker_violations", "branch_coverage_hits_data", "branch_coverage",
        "class_complexity_distribution", "class_complexity", "classes", "comment_blank_lines", "comment_lines_data",
        "comment_lines_density", "comment_lines", "commented_out_code_lines", "complexity", "conditions_by_line",
        "conditions_to_cover", "coverage", "coverage_line_hits_data", "covered_conditions_by_line",
        "critical_violations", "dsm", "dit", "directories", "distance", "duplicated_blocks", "duplicated_files",
        "duplicated_lines_density", "duplicated_lines", "duplications_data", "ce", "efficiency",
        "false_positive_reviews", "file_complexity_distribution", "file_complexity", "file_cycles",
        "file_edges_weight", "file_feedback_edges", "file_tangle_index", "file_tangles", "files",
        "function_complexity_distribution", "function_complexity", "functions", "generated_line", "it_coverage",
        "it_coverage_line_hits_data", "it_covered_conditions_by_line", "it_line_coverage", "it_lines_to_cover",
        "it_uncovered_conditions", "it_uncovered_lines", "lcom4_blocks", "lcom4_distribution", "lcom4",
        "line_coverage", "lines", "lines_to_cover", "maintainability", "major_violations", "minor_violations",
        "ncloc_data", "ncloc", "new_blocker_violations", "new_branch_coverage", "new_conditions_to_cover",
        "new_coverage", "new_critical_violations", "info_violations", "new_it_branch_coverage",
        "new_it_conditions_to_cover", "new_it_coverage", "new_it_line_coverage", "new_it_lines_to_cover",
        "new_it_uncovered_conditions", "new_it_uncovered_lines", "new_line_coverage", "new_lines_to_cover",
        "new_major_violations", "new_minor_violations", "new_overall_branch_coverage",
        "new_overall_conditions_to_cover", "new_overall_coverage", "new_overall_line_coverage",
        "new_overall_lines_to_cover", "new_overall_uncovered_conditions", "new_overall_uncovered_lines",
        "new_uncovered_conditions", "new_uncovered_lines", "new_unreviewed_violations", "new_violations", "noc",
        "overall_branch_coverage", "overall_conditions_by_line", "overall_conditions_to_cover", "overall_coverage",
        "overall_coverage_line_hits_data", "overall_covered_conditions_by_line", "overall_line_coverage",
        "overall_lines_to_cover", "overall_uncovered_conditions", "overall_uncovered_lines", "package_cycles",
        "package_edges_weight", "package_feedback_edges", "package_tangle_index", "package_tangles", "packages",
        "paragraph_complexity_distribution", "paragraph_complexity", "paragraphs", "portability", "profile",
        "profile_version", "projects", "public_api", "public_documented_api_density", "public_undocumented_api",
        "reliability", "rfc_distribution", "rfc", "authors_by_line", "last_commit_datetimes_by_line",
        "revisions_by_line", "skipped_tests", "statements", "suspect_lcom4_density", "test_data", "test_errors",
        "test_execution_time", "test_failures", "test_success_density", "tests", "unassigned_reviews",
        "uncovered_conditions", "uncovered_lines", "unplanned_reviews", "unreviewed_violations", "usability",
        "violations_density", "violations", "weighted_violations" };

    /**
     * @param string
     * @return
     */
    private String getFormattedValue( String key )
    {
        return get( key ).getFormattedValue();
    }

    public SonarStats sendMail( String sujet )
    {
        return sendMail( sujet, getContentHtml() );
    }

    /**
     * @param outStream
     */
    protected SonarStats sendMail( String sujet, String content )
    {
        try
        {
            Authenticator auth = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication( props.getString( MAIL_USER ), props.getString( MAIL_PASSWORD ) );
                }
            };

            Properties mailProps = new Properties();
            mailProps.put( "mail.smtp.auth", props.getString( "mail.smtp.auth" ) );
            mailProps.put( "mail.smtp.starttls.enable", props.getString( "mail.smtp.starttls.enable" ) );
            mailProps.put( "mail.smtp.host", props.getString( "mail.smtp.host" ) );

            Session session = Session.getInstance( mailProps, auth );

            Message message = new MimeMessage( session );

            String mailTo = props.getString( MAIL_TO );
            if ( mailTo.contains( MAIL_SEP ) )
            {
                String[] to = mailTo.split( MAIL_SEP );
                Address[] adresses = new Address[to.length];
                for ( int i = 0; i < to.length; i++ )
                {
                    adresses[i] = new InternetAddress( to[i] );
                }

                message.setRecipients( Message.RecipientType.TO, adresses );
            }
            else
            {
                InternetAddress recipient = new InternetAddress( mailTo );
                message.setRecipient( Message.RecipientType.TO, recipient );
            }

            message.setSubject( sujet );
            message.setContent( content, MAIL_MINE );

            Transport.send( message );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.out.println( "errorrrrr : " + e.getMessage() );
        }
        return this;
    }

    /**
     * @param sonnarS
     * @return
     */
    protected void buildContent( ProjetConfiguration pProject )
    {
        int nbDays = props.getInt( SONAR_DAYS );
        String duree = "depuis la semaine dernière";

        if ( nbDays != 7 )
        {
            duree = "depuis " + nbDays + " jours";
        }

        this.project = pProject;
        project.index =
            sonar.find( ResourceQuery.createForMetrics( project.resource, new String[] {} ) ).getId().toString();

        mailWriter.addHtml( "<p>Voici quelques indicateurs <a href=\"" + project.sonarLink
            + "?did=1\" style='text-decoration : none;'>Sonar</a> " + duree + ".</p>" );
        mailWriter.addBr();

        TimeMachine timeM = getTimeMachine( project.resource, nbDays );

        contentViolations( timeM );

        contentTests( timeM );

        contentDuplications( timeM );

        contentTestsGraph( duree );

        contentCoverage( duree );
    }

    public String getContentHtml()
    {
        buildContent( project );
        return mailWriter.getContent();
    }

    /**
     * @param mailWriter
     * @param duree
     */
    private void contentCoverage( String duree )
    {
        if ( project.coverage )
        {
            mailWriter.addBr().addHtml( "<p>Evolution de la couverture de tests " + duree + ".</p>" );
            mailWriter.addBr().addImage( project.jobUrl + "/cobertura/graph" );
        }
    }

    /**
     * @param mailWriter
     * @param duree
     */
    private void contentTestsGraph( String duree )
    {
        if ( project.testsGraph )
        {
            mailWriter.addHtml( "<p>Tendance des résultats des tests " + duree + ".</p>" );
            mailWriter.addBr().addImage( project.jobUrl + "/test/trend" );
        }
    }

    /**
     * @param mailWriter
     * @param timeM
     */
    private void contentDuplications( TimeMachine timeM )
    {
        if ( project.duplications )
        {
            Block blockComment = mailWriter.createBlock( "Commentaires" );
            blockComment.add( getLigne( timeM, "comment_lines_density", "", true ) ).br();
            blockComment.add( getLigne( timeM, "comment_lines", " lignes", true ) ).br();
            blockComment.add( getLigne( timeM, "public_documented_api_density", " API documentée", true ) ).br();
            blockComment.add( getLigne( timeM, "public_undocumented_api", " API non documentée" ) ).br();

            Block block = mailWriter.createBlock( "Duplications" );
            block.add( getLigne( timeM, "duplicated_lines_density", "" ) ).br();
            block.add( getLigne( timeM, "duplicated_lines", " lignes" ) ).br();
            block.add( getLigne( timeM, "duplicated_blocks", " blocs" ) ).br();
            block.add( getLigne( timeM, "duplicated_files", " fichiers", true ) ).br();

            mailWriter.addBlock( blockComment, block );
        }
    }

    /**
     * @param mailWriter
     * @param timeM
     */
    private void contentTests( TimeMachine timeM )
    {
        if ( project.tests )
        {
            Block blockCouverture = mailWriter.createBlock( "Couverture de code" );
            blockCouverture.add( getLigne( timeM, "coverage", "", true ) ).br();
            blockCouverture.add( getLigne( timeM, "line_coverage", " de couverture de ligne", true ) ).br();
            blockCouverture.add( getLigne( timeM, "branch_coverage", " de couverture de branche", true ) ).br();

            Block blockTests = mailWriter.createBlock( "Succès d'exécution des tests" );
            blockTests.add( getLigne( timeM, "test_success_density", "", true ) ).br();
            blockTests.add( getLigne( timeM, "test_failures", " en échec" ) ).br();
            blockTests.add( getLigne( timeM, "test_errors", " en erreur" ) ).br();
            blockTests.add( getLigne( timeM, "tests", " tests", true ) ).br();
            blockTests.add( getLigne( timeM, "skipped_tests", " tests désactivés", true ) ).br();
            blockTests.add( getLigne( timeM, "test_execution_time", "", false, Format.TIME ) ).br();

            mailWriter.addBlock( blockCouverture, blockTests );
        }
    }

    /**
     * @param mailWriter
     * @param timeM
     */
    private void contentViolations( TimeMachine timeM )
    {
        if ( project.violations )
        {
            Block blockViolation = mailWriter.createBlock( "Violations" );

            blockViolation.add( getLigne( timeM, "violations", "" ) );
            blockViolation.add( blockViolation.htmlTitle( "Taux de conformité" ) );
            blockViolation.add( getLigne( timeM, "violations_density", "" ) ).br();

            Block blockViolationL2 = mailWriter.createBlock();
            blockViolationL2.add( getLigne( timeM, "blocker_violations", " Bloquant" ) ).br();
            blockViolationL2.add( getLigne( timeM, "critical_violations", " Critique" ) ).br();
            blockViolationL2.add( getLigne( timeM, "major_violations", " Majeur" ) ).br();
            blockViolationL2.add( getLigne( timeM, "minor_violations", " Mineur" ) ).br();
            blockViolationL2.add( getLigne( timeM, "info_violations", " Info" ) ).br();

            mailWriter.addBlock( blockViolation, blockViolationL2 );
        }
    }

    enum Format
    {
        TIME, PERCENT, NOMBRE;
    }

    private String getLigne( TimeMachine timeM, String key, String text )
    {
        return getLigne( timeM, key, text, false );
    }

    private String getLigne( TimeMachine timeM, String key, String text, boolean negativeIndice )
    {
        return getLigne( timeM, key, text, negativeIndice, null );
    }

    private String getLigne( TimeMachine timeM, String key, String text, boolean negativeIndice, Format format )
    {
        return getFormattedValue( key ) + text + getCellValDeltaHtml( timeM, key, negativeIndice, format );
    }

    private Object getCellVal( TimeMachine timeM, String key, int index )
    {
        TimeMachineColumn coll = timeM.getColumn( key );
        if ( coll != null && index != -1 && timeM.getCells().length > index
            && timeM.getCells()[index].getValues().length > coll.getIndex() )
        {
            return timeM.getCells()[index].getValues()[coll.getIndex()];
        }
        return null;

    }

    private String getCellValDelta( TimeMachine timeM, String key )
    {
        Object before = getCellVal( timeM, key, 0 );
        Object after = getCellVal( timeM, key, timeM.getCells().length - 1 );

        return calculDelta( before, after );
    }

    private String getCellValDeltaHtml( TimeMachine timeM, String key, boolean negativeResult, Format format )
    {
        String val = getCellValDelta( timeM, key );
        String pre = "<span style=\"color:#8C8989;valign: super; line-height: 80%;\" ><b> (";
        String after = ") </b></span>";
        String signe = "-";

        if ( format != null && format.equals( Format.TIME ) )
        {
            SimpleDateFormat sdf = new SimpleDateFormat( "S" );
            try
            {
                Date date = sdf.parse( val );
                SimpleDateFormat sdf2 = new SimpleDateFormat( "mm:ss" );
                val = sdf2.format( date );

            }
            catch ( ParseException e )
            {

            }
        }

        if ( !val.contains( "-" ) )
        {
            val = "+" + val;
        }

        if ( !negativeResult )
        {
            signe = "+";
        }

        if ( !val.equals( "+0" ) && !val.equals( "+0.0" ) && !val.equals( "+00:00" ) )
        {

            if ( val.contains( signe ) )
            {
                pre = "<span style=\"color:red;valign: super; line-height: 80%;\"><b> (";
            }
            else
            {
                pre = "<span style=\"color:#92D050;valign: super; line-height: 80%;\"><b>  (";
            }
            after = ") </b></span>";
        }
        return pre + val + after;
    }

    /**
     * @param before
     * @param after
     * @return
     */
    private static String calculDelta( Object before, Object after )
    {
        if ( before instanceof Long )
        {
            return Long.valueOf( Math.round( ( (Long) after ) - ( (Long) before ) ) ).toString();
        }
        if ( before instanceof Integer )
        {
            return Integer.valueOf( ( (Integer) after ) - ( (Integer) before ) ).toString();
        }
        if ( before instanceof Double )
        {
            return Double.valueOf( Math.round( ( (Double) after ) - ( (Double) before ) ) ).toString();
        }
        return "";
    }

}
