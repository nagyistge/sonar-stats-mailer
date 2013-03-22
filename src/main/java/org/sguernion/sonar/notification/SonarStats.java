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
    private static final String TRUE = "true";

    private static final String SEP = ":";

    private static final String HTTP = "http://";

    private Sonar sonar;

    private Properties props;

    private String resource;


    private SonarStats()
    {
    }

    /**
     * @param props
     * @return
     */
    public static SonarStats connect( Properties props )
    {
        controleProperties( props );
        SonarStats stat =
            SonarStats.connect( HTTP + props.getProperty( SONAR_HOST ) + SEP + props.getProperty( SONAR_PORT ),
                props.getProperty( SONAR_USER ), props.getProperty( SONAR_PASSWORD ),
                props.getProperty( SONAR_RESOURCE ) );
        stat.props = props;
        return stat;
    }

    /**
     * @param props2
     */
    private static void controleProperties( Properties props )
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

    public static SonarStats connect( String url, String login, String password, String resource )
    {
        SonarStats stat = new SonarStats();
        stat.sonar = Sonar.create( url, login, password );
        stat.resource = resource;
        return stat;
    }

    public List<Measure> get()
    {
        Resource struts = sonar.find( ResourceQuery.createForMetrics( resource, KEYS ) );
        return struts.getMeasures();
    }

    public TimeMachine getTimeMachine( int nbJours )
    {
        return getTimeMachine( nbJours, KEYS );
    }

    public TimeMachine getTimeMachine( int nbJours, String[] keys )
    {
        Date now = new Date();
        TimeMachineQuery query = TimeMachineQuery.createForMetrics( resource, keys );
        Calendar calendar = new GregorianCalendar();
        calendar.setTime( now );
        calendar.add( Calendar.DATE, -nbJours );
        query.setFrom( calendar.getTime() );
        query.setTo( now );

        TimeMachine struts = sonar.find( query );
        return struts;
    }

    public Measure get( String key )
    {
        Resource struts = sonar.find( ResourceQuery.createForMetrics( resource, KEYS ) );
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
    public String getFormattedValue( String key )
    {
        return get( key ).getFormattedValue();
    }

    /**
     * @param outStream
     */
    public SonarStats sendMail( String sujet, String content )
    {
        try
        {
            Authenticator auth = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication( props.getProperty( MAIL_USER ),
                        props.getProperty( MAIL_PASSWORD ) );
                }
            };
            Session session = Session.getInstance( props, auth );

            Message message = new MimeMessage( session );

            String mailTo = props.getProperty( MAIL_TO );
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
        }
        return this;
    }

    /**
     * @param sonnarS
     * @return
     */
    public String getContentHtml()
    {
        MailContentWriter mailWriter = new MailContentWriter();

        int nbDays = Integer.valueOf( props.getProperty( SONAR_DAYS ) );

        String duree = "depuis la semaine dernière";

        if ( nbDays != 7 )
        {
            duree = "depuis " + nbDays + " jours";
        }

        String resourceId =
            sonar.find( ResourceQuery.createForMetrics( resource, new String[] {} ) ).getId().toString();

        mailWriter.addHtml( "<p>Voici quelques indicateurs <a href=\"" + HTTP + props.getProperty( SONAR_HOST ) + SEP
            + props.getProperty( SONAR_PORT ) + "/dashboard/index/" + resourceId + "?did=1\">Sonar</a> " + duree
            + ".</p>" );
        mailWriter.addHtml( "<br />" );

        TimeMachine timeM = getTimeMachine( nbDays );

        contentViolations( mailWriter, timeM );

        contentTests( mailWriter, timeM );

        contentDuplications( mailWriter, timeM );

        contentTestsGraph( mailWriter, duree );

        contentCoverage( mailWriter, duree );

        return mailWriter.getContent();
    }

    /**
     * @param mailWriter
     * @param duree
     */
    private void contentCoverage( MailContentWriter mailWriter, String duree )
    {
        if ( props.getProperty( "content.coverage.graph" ).equals( TRUE ) )
        {
            mailWriter.addBr().addHtml( "<p>Evolution de la couverture de tests " + duree + ".</p>" );
            mailWriter.addBr().addImage(
                HTTP + props.getProperty( JENKINS_HOST ) + SEP + props.getProperty( JENKINS_PORT )
                    + "/job/"+props.getProperty( JENKINS_JOB )+"/cobertura/graph" );
        }
    }

    /**
     * @param mailWriter
     * @param duree
     */
    private void contentTestsGraph( MailContentWriter mailWriter, String duree )
    {
        if ( props.getProperty( "content.tests.graph" ).equals( TRUE ) )
        {
            mailWriter.addHtml( "<p>Tendance des résultats des tests " + duree + ".</p>" );
            mailWriter.addBr().addImage(
                HTTP + props.getProperty( JENKINS_HOST ) + SEP + props.getProperty( JENKINS_PORT )
                    + "/job/"+props.getProperty( JENKINS_JOB )+"/test/trend" );
        }
    }

    /**
     * @param mailWriter
     * @param timeM
     */
    private void contentDuplications( MailContentWriter mailWriter, TimeMachine timeM )
    {
        if ( props.getProperty( "content.duplications" ).equals( TRUE ) )
        {
            Block blockComment = mailWriter.createBlock( getHtmlTitle( "Commentaires" ) );
            blockComment.add( getLigne( timeM, "comment_lines_density", "", true ) ).br();
            blockComment.add( getLigne( timeM, "comment_lines", " lignes", true ) ).br();
            blockComment.add( getLigne( timeM, "public_documented_api_density", " API documentée", true ) ).br();
            blockComment.add( getLigne( timeM, "public_undocumented_api", " API non documentée" ) ).br();

            Block block = mailWriter.createBlock( getHtmlTitle( "Duplications" ) );
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
    private void contentTests( MailContentWriter mailWriter, TimeMachine timeM )
    {
        if ( props.getProperty( "content.tests" ).equals( TRUE ) )
        {
            Block blockCouverture = mailWriter.createBlock( getHtmlTitle( "Couverture de code" ) );
            blockCouverture.add( getLigne( timeM, "coverage", "", true ) ).br();
            blockCouverture.add( getLigne( timeM, "line_coverage", " de couverture de ligne", true ) ).br();
            blockCouverture.add( getLigne( timeM, "branch_coverage", " de couverture de branche", true ) ).br();

            Block blockTests = mailWriter.createBlock( getHtmlTitle( "Succès d'exécution des tests" ) );
            blockTests.add( getLigne( timeM, "test_success_density", "", true ) ).br();
            blockTests.add( getLigne( timeM, "test_failures", " en échec" ) ).br();
            blockTests.add( getLigne( timeM, "test_errors", " en erreur" ) ).br();
            blockTests.add( getLigne( timeM, "tests", " tests", true ) ).br();
            blockTests.add( getLigne( timeM, "skipped_tests", " tests désactivés", true ) ).br();
            blockTests.add( getLigne( timeM, "test_execution_time", "", true, Format.TIME ) ).br().br();

            mailWriter.addBlock( blockCouverture, blockTests );
        }
    }

    /**
     * @param mailWriter
     * @param timeM
     */
    private void contentViolations( MailContentWriter mailWriter, TimeMachine timeM )
    {
        if ( props.getProperty( "content.violations" ).equals( TRUE ) )
        {
            Block blockViolation = mailWriter.createBlock( getHtmlTitle( "Violations" ) );

            blockViolation.add( getLigne( timeM, "violations", "" ) );
            blockViolation.add( getHtmlTitle( "Taux de conformité" ) );
            blockViolation.add( getLigne( timeM, "violations_density", "" ) ).br();

            Block blockViolationL2 = mailWriter.createBlock( "" );
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

    public String getLigne( TimeMachine timeM, String key, String text )
    {
        return getLigne( timeM, key, text, false );
    }

    public String getLigne( TimeMachine timeM, String key, String text, boolean negativeIndice )
    {
        return getLigne( timeM, key, text, negativeIndice, null );
    }

    public String getLigne( TimeMachine timeM, String key, String text, boolean negativeIndice, Format format )
    {
        return getFormattedValue( key ) + text + getCellValDeltaHtml( timeM, key, negativeIndice, format );
    }

    /**
     * @param string
     * @return
     */
    private String getHtmlTitle( String title )
    {
        MailContentWriter mailWriter = new MailContentWriter();
        return mailWriter.getHtmlTitle( title );
    }

    private Object getCellVal( TimeMachine timeM, String key, int index )
    {
        TimeMachineColumn coll = timeM.getColumn( key );
        if ( coll != null )
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
        String pre = "<span style=\"valign: super; line-height: 80%;\" ><b> (";
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
