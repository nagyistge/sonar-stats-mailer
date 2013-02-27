package org.sguernion.sonar.notification;



/**
 * @author sguernio
 */
public class MailContentWriter
{

    private StringBuilder content;

    /**
     * 
     */
    public MailContentWriter()
    {
        content = new StringBuilder();
        content.append( "<html><body>" );
    }

    /**
     * @return
     */
    public String getHtmlTitle( String title )
    {
        return "<p><span style='font-size:12pt;color:#444444'><b>" + title + "</b></span><p>";
    }

    /**
     * @param string
     * @param string2
     */
    public MailContentWriter addBlock( String string, String string2 )
    {
        content.append( "<table style='width:700px;border:1px solid black'><tr><td>" );
        content.append( string );
        content.append( "</td><td>" );
        content.append( string2 );
        content.append( "</tr></table><br/>" );
        return this;
    }

    /**
     * @param block1
     * @param block2
     */
    public MailContentWriter addBlock( Block block1, Block block2 )
    {
        content.append( "<table style='width:700px;border:1px solid black'>" );
        content.append( "<tr><td>" );
        content.append( block1.title );
        content.append( "</td><td>" );
        content.append( block2.title );
        content.append( "</tr>" );

        content.append( "<tr><td>" );
        content.append( block1.getContent() );
        content.append( "</td><td>" );
        content.append( block2.getContent() );
        content.append( "</tr>" );
        content.append( "</table><br/>" );
        return this;
    }

    public MailContentWriter addHtml( String html )
    {
        content.append( html );
        return this;
    }

    public String getContent()
    {
        return content.toString() + "</body></html>";
    }

    /**
     * 
     */
    public MailContentWriter addBr()
    {
        addHtml( "<br />" );
        return this;
    }

    /**
     * @param string
     */
    public void addImage( String src )
    {
        addHtml( "<img src=\"" + src + "\" />" );
    }

    /**
     * @param htmlTitle
     * @return
     */
    public Block createBlock( String htmlTitle )
    {
        return new Block( htmlTitle );
    }

    public class Block
    {
        String title;

        StringBuilder sb = new StringBuilder();

        /**
         * @param htmlTitle
         */
        public Block( String htmlTitle )
        {
            title = htmlTitle;
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
            sb.append( "<br />" );
            return this;
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


    }

}
