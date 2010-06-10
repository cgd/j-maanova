/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.maanova.configuration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jax.maanova.jaxbgenerated.JMaanovaApplicationState;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.RApplicationConfiguration;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.util.ConfigurationUtilities;

/**
 * The J/maanova configuration manager.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaApplicationConfigurationManager extends RApplicationConfigurationManager
{
    /**
     * the default config file name to use
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = "j-maanova-config.xml";
    
    /**
     * the default file name to use for application state
     */
    private static final String DEFAULT_APPLICATION_STATE_FILE_NAME =
        "j-maanova-application-state.xml";
    
    /**
     * the default config zip resource location
     */
    private static final String DEFAULT_CONFIG_ZIP_RESOURCE = "/j-maanova-configuration.zip";
    
    /**
     * the config-path-relative dir name where all of the sample data
     * that comes bundled with J/maanova lives
     */
    private static final String SAMPLE_DATA_DIR_NAME = "sample-data";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MaanovaApplicationConfigurationManager.class.getName());
    
    /**
     * our singleton instance
     */
    private static final MaanovaApplicationConfigurationManager instance;
    static
    {
        MaanovaApplicationConfigurationManager tempInstance = null;
        try
        {
            tempInstance = new MaanovaApplicationConfigurationManager(
                    JAXBContext.newInstance(
                            RApplicationConfiguration.class,
                            JMaanovaApplicationState.class));
        }
        catch(JAXBException ex)
        {
            LOG.log(Level.SEVERE,
                    "Failed to initialize JAXB");
        }
        finally
        {
            instance = tempInstance;
        }
    }
    
    /**
     * Getter for the singleton instance of this class
     * @return
     *          the instance
     */
    public static MaanovaApplicationConfigurationManager getInstance()
    {
        return MaanovaApplicationConfigurationManager.instance;
    }
    
    /**
     * Private constructor (use {@link #getInstance()} to get a handle on
     * the singleton instance of this class) 
     * @param jaxbContext
     *          the JAXB context to use for creating new objects
     */
    private MaanovaApplicationConfigurationManager(JAXBContext jaxbContext)
    {
        super(jaxbContext);
    }
    
    /**
     * Set the starting data directory. This will persist in application
     * state
     * @see MaanovaApplicationConfigurationManager#getApplicationState()
     * @param startingDataDirectory
     *          the starting dir
     */
    public void setStartingDataDirectory(File startingDataDirectory)
    {
        String absolutePath = startingDataDirectory.getAbsolutePath();
        if(absolutePath != null)
        {
            MaanovaApplicationConfigurationManager manager =
                MaanovaApplicationConfigurationManager.getInstance();
            manager.getJaxbRecentMicroarrayDataDirectory().setFileName(
                    absolutePath);
        }
    }
    
    /**
     * Get the starting directory for loading data.
     * @return
     *          the starting data
     */
    public File getStartingDataDirectory()
    {
        FileType dataDirs = this.getJaxbRecentMicroarrayDataDirectory();
        File startingMicroarrayDataDir = null;
        if(dataDirs.getFileName() == null || dataDirs.getFileName().length() == 0)
        {
            // since it's not set, use the samples dir
            ConfigurationUtilities configUtil;
            try
            {
                configUtil = new ConfigurationUtilities();
                File baseDir = configUtil.getBaseDirectory();
                startingMicroarrayDataDir = new File(baseDir, SAMPLE_DATA_DIR_NAME);
                this.setStartingDataDirectory(startingMicroarrayDataDir);
            }
            catch(Exception ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to get default data dir",
                        ex);
            }
        }
        else
        {
            startingMicroarrayDataDir = new File(dataDirs.getFileName());
        }
        
        return startingMicroarrayDataDir;
    }
    
    /**
     * Convenience function for getting (and initializing if needed) the
     * JAXB data directory.
     * @see MaanovaApplicationConfigurationManager#getApplicationState()
     * @return
     *          the data directory
     */
    public FileType getJaxbRecentMicroarrayDataDirectory()
    {
        JMaanovaApplicationState applicationState =
            this.getApplicationState();
        
        FileType microarrayDataDir = applicationState.getRecentMicroarrayDataDirectory();
        if(microarrayDataDir == null)
        {
            org.jax.r.jaxbgenerated.ObjectFactory objectFactory =
                new org.jax.r.jaxbgenerated.ObjectFactory();
            microarrayDataDir = objectFactory.createFileType();
            applicationState.setRecentMicroarrayDataDirectory(microarrayDataDir);
        }
        
        return microarrayDataDir;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JMaanovaApplicationState getApplicationState()
    {
        return (JMaanovaApplicationState)super.getApplicationState();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationStateType createNewApplicationState()
    {
        org.jax.maanova.jaxbgenerated.ObjectFactory objectFactory =
            new org.jax.maanova.jaxbgenerated.ObjectFactory();
        return objectFactory.createJMaanovaApplicationState();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getApplicationStateFileName()
    {
        return DEFAULT_APPLICATION_STATE_FILE_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigurationFileName()
    {
        return DEFAULT_CONFIG_FILE_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigurationZipResourceName()
    {
        return DEFAULT_CONFIG_ZIP_RESOURCE;
    }
}
