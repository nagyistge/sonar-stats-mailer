package org.sguernion.sonar.notification;

/**
 * @author sguernio
 */
public class MailContentWriter
{

    private StringBuilder content;

    private boolean onglet;

    private StringBuilder ongletContent;

    private static final String BR = "<br />";

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
            ongletContent.append( "<ul>" );
            onglet = true;
        }
        else
        {
            addHtml( "</div>" );
        }
        ongletContent.append( "<li onclick='display(" + job + ")'>" );
        ongletContent.append( job );
        ongletContent.append( "</li>" );
        addHtml( "<div id='" + job + "' style='display:none;'>" );
        // TODO onglet

        return this;
    }

    /**
     * @param string
     * @param string2
     */
    public MailContentWriter addBlock( String text1, String text2 )
    {
        addHtml( "<table style='width:700px;border:1px solid black'><tr><td>" );
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
            ongletContent.append( "</ul>" );
        }
        return "<html><body><script type=\"text/javascript\" language=\"javascript\">"
            + "function display(idDisplay) {" + " if(document.getElementById(idDisplay).style.display == 'none'){"
            + "   document.getElementById(idDisplay).style.display = 'block';" + "}else{"
            + "    document.getElementById(idHide).style.display = 'none';}" + " }" + "</script>"
            + ongletContent.toString() + content.toString() + "</body></html>";
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
        return new Block();
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
