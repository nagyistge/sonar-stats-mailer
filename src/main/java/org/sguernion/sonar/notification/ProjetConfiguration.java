package org.sguernion.sonar.notification;

/**
 * @author SGUERNIO
 *
 */
public class ProjetConfiguration
{
    protected String resource;

    protected String index;

    protected boolean coverage;

    protected boolean testsGraph;

    protected boolean tests;

    protected boolean violations;

    protected boolean duplications;

    protected String job;

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "ProjetConfiguration [resource=" );
        builder.append( resource );
        builder.append( ", index=" );
        builder.append( index );
        builder.append( ", coverage=" );
        builder.append( coverage );
        builder.append( ", testsGraph=" );
        builder.append( testsGraph );
        builder.append( ", tests=" );
        builder.append( tests );
        builder.append( ", violations=" );
        builder.append( violations );
        builder.append( ", duplications=" );
        builder.append( duplications );
        builder.append( ", job=" );
        builder.append( job );
        builder.append( "]" );
        return builder.toString();
    }

}
