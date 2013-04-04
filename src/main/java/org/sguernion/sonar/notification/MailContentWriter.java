package org.sguernion.sonar.notification;

import java.io.UnsupportedEncodingException;

/**
 * @author sguernio
 */
public class MailContentWriter
{

    private StringBuilder content;

    private boolean onglet;

    private StringBuilder ongletContent;

    private static final String BR = "<br />";

    private static String STYLE = "<style type='text/css'>.menu, .menu table{line-height : 31px;text-align : center;}"
        + ".menu a{text-decoration : none;color : #fff;}"
        + ".menu tr{ border-right : 2px solid #fff;background-color : #000;}" + "</style>";

    /**
     * 
     */
    public MailContentWriter()
    {
        content = new StringBuilder();
        ongletContent = new StringBuilder();
    }

    /**
     * @param job
     */
    public MailContentWriter addOnglet( String job )
    {

        if ( !onglet )
        {
            // TODO init onglet
            ongletContent
                .append( "<table style='width:700px;border:1px solid black;background-color : #FFF;' class='menu'><tr>" );
            onglet = true;
        }
        else
        {
            addHtml( "</div>" );
        }
        ongletContent.append( "<td><a href='#" + job + "' >" );
        ongletContent.append( job );
        ongletContent.append( "</a></td>" );

        addHtml(
            "<br/><div style='width:700px;border:1px solid black;text-align: center;background-color : #FFF;'  >_ONGLET_<a name='"
                + job + "'></a>" ).addBr();
        addHtml( job );
        // TODO onglet

        return this;
    }

    /**
     * @param string
     * @param string2
     */
    public MailContentWriter addBlock( String text1, String text2 )
    {
        addHtml( "<table style='width:700px;border:1px solid black;background-color : #FFF;'><tr><td>" );
        addHtml( text1 ).addHtml( "</td><td>" );
        addHtml( text2 ).addHtml( "</tr></table>" );
        addBr();
        return this;
    }

    /**
     * @param block1
     * @param block2
     */
    public MailContentWriter addBlock( Block block1, Block block2 )
    {
        addHtml( "<table style='width:700px;border:1px solid black'>" );
        addHtml( "<tr><td>" ).addHtml( block1.title ).addHtml( "</td><td>" ).addHtml( block2.title ).addHtml( "</tr>" );

        addHtml( "<tr><td>" ).addHtml( block1.getContent() ).addHtml( "</td><td>" ).addHtml( block2.getContent() )
            .addHtml( "</tr>" );
        addHtml( "</table>" ).addBr();
        return this;
    }

    public MailContentWriter addHtml( String html )
    {
        content.append( html );
        return this;
    }

    public String getContent()
    {
        if ( onglet )
        {
            addHtml( "</div>" );
            ongletContent.append( "</tr></table>" );
        }

        String sContent = content.toString();
        sContent = sContent.replace( "_ONGLET_", ongletContent.toString() );

        String result =
            "<html><head><meta http-equiv='Content-Type' content='text/html; charset=iso-8859-1'/>" + STYLE
                + "</head><body>" + sContent + "</body></html>";

        try
        {
            return new String( result.getBytes(), "iso-8859-1" );
        }
        catch ( UnsupportedEncodingException e )
        {
            return result;
        }
    }

    /**
     * 
     */
    public MailContentWriter addBr()
    {
        return addHtml( BR );
    }

    /**
     * @param string
     */
    public MailContentWriter addImage( String src )
    {
        return addHtml( "<img src=\"" + src + "\" />" );
    }

    /**
     * @param htmlTitle
     * @return
     */
    public Block createBlock()
    {
        return new Block( "" );
    }

    public Block createBlock( String title )
    {
        return new Block( title );
    }

    public class Block
    {

        String title;

        StringBuilder sb = new StringBuilder();

        /**
         * @param htmlTitle
         */
        public Block()
        {
        }

        /**
         * @param title2
         */
        public Block( String pTitle )
        {
            this.title = htmlTitle( pTitle );
        }

        public Block setHtmlTitle( String pTitle )
        {
            this.title = htmlTitle( pTitle );
            return this;
        }

        public Block add( String html )
        {
            sb.append( html );
            return this;
        }

        /**
         * @return
         */
        public Block br()
        {
            return add( BR );
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return title + getContent();
        }

        /**
         * @return
         */
        public String getContent()
        {
            return sb.toString();
        }

        /**
         * @param string
         * @return
         */
        public String htmlTitle( String pTitle )
        {
            return "<p><span style='font-size:12pt;color:#444444'><b>" + pTitle + "</b></span><p>";
        }

    }

}
