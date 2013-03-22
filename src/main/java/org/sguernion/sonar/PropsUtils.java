package org.sguernion.sonar;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author sguernion
 */
public class PropsUtils
{
    private PropsUtils()
    {
    }

    /**
     * Load a Properties File
     * 
     * @param propsFile
     * @return Properties
     * @throws IOException
     * @throws ConfigurationException
     */
    public static PropertiesConfiguration load( File propsFile )
        throws ConfigurationException
    {
        PropertiesConfiguration props = new PropertiesConfiguration();

        props.load( propsFile );

        return props;
    }

}
