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

package org.jax.maanova.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jax.maanova.jaxbgenerated.JMaanovaProjectMetadata;
import org.jax.r.CleanEnvironmentCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.ConfigurationUtilities;
import org.jax.util.io.FileChooserExtensionFilter;
import org.jax.util.io.FileUtilities;
import org.jax.util.project.ProjectManager;

/**
 * The project manager for J/maanova
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaProjectManager extends ProjectManager
{
    /**
     * File extension used for j-maanova project files
     */
    public static final String MAANOVA_PROJECT_EXTENSION = "jmaanova";
    
    private static final FileFilter MAANOVA_PROJECT_FILE_FILTER =
        new FileChooserExtensionFilter(
                MAANOVA_PROJECT_EXTENSION,
                "J/maanova Project (*.jmaanova)");
    
    private static final MaanovaProjectManager instance = new MaanovaProjectManager(
            RInterfaceFactory.getRInterfaceInstance());
    
    private static final Logger LOG = Logger.getLogger(
            MaanovaProjectManager.class.getName());

    private final RInterface rInterface;
    
    /**
     * the file name that is used for project metadata
     */
    private static final String PROJECT_METADATA_FILENAME_1_0_0 =
        "project-metadata-1.0.0.xml";
    
    /**
     * the file name that is used for R data
     */
    private static final String PROJECT_R_DATA_FILENAME =
        "maanova-data.RData";
    
    /**
     * the temporary directory name that we use for short-term storage of
     * project data (in the long term, project data is stored in a
     * zip file... usually with a .jmaanova extension)
     */
    private static final String TEMP_PROJECT_DIR_NAME =
        "temp-proj";
    
    /**
     * the jaxb context for marshalling and unmarshalling
     */
    private JAXBContext jaxbContext;
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface to use
     */
    public MaanovaProjectManager(final RInterface rInterface)
    {
        this.rInterface = rInterface;
        
        try
        {
            this.jaxbContext = JAXBContext.newInstance(
                    JMaanovaProjectMetadata.class);
        }
        catch(JAXBException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to initialize project manager",
                    ex);
        }
        
        
        this.createNewActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MaanovaProject createNewActiveProject()
    {
        // clear the current r data
        this.rInterface.evaluateCommand(new SilentRCommand(
                new CleanEnvironmentCommand()));
        
        this.setActiveProjectFile(null);
        this.setActiveProjectModified(false);
        
        MaanovaProject newProject = new MaanovaProject(this.rInterface);
        this.setActiveProject(newProject);
        return newProject;
    }
    
    /**
     * Getter for the instance.
     * @return the instance
     */
    public static MaanovaProjectManager getInstance()
    {
        return MaanovaProjectManager.instance;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public FileFilter getProjectFileFilter()
    {
        return MAANOVA_PROJECT_FILE_FILTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProjectDataStructures()
    {
        this.getActiveProject().getDataModel().updateAll();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MaanovaProject getActiveProject()
    {
        return (MaanovaProject)super.getActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveActiveProject(File projectFile)
    {
        try
        {
            File tempProjDir = this.getCleanedTempProjectDir();
            if(tempProjDir == null)
            {
                return false;
            }
            else
            {
                try
                {
                    // create temp r data file
                    File rDataFile = new File(tempProjDir, PROJECT_R_DATA_FILENAME);
                    String saveDataCommandString =
                        "save(list = ls(), file = " +
                        RUtilities.javaStringToRString(rDataFile.getAbsolutePath()) +
                        ")";
                    this.rInterface.evaluateCommand(
                            new SilentRCommand(saveDataCommandString));
                    
                    // create temp metadata file
                    FileOutputStream configFileOut = new FileOutputStream(
                            new File(tempProjDir, PROJECT_METADATA_FILENAME_1_0_0));
                    Marshaller marshaller = this.jaxbContext.createMarshaller();
                    marshaller.setProperty(
                            Marshaller.JAXB_FORMATTED_OUTPUT,
                            Boolean.TRUE);
                    marshaller.marshal(
                            this.getActiveProject().getMetadata(),
                            configFileOut);
                    configFileOut.close();
                    
                    // zip up the directory and save it to the file
                    ZipOutputStream zipOut = new ZipOutputStream(
                            new FileOutputStream(projectFile));
                    FileUtilities.compressDirectoryToZip(
                            tempProjDir,
                            zipOut);
                    zipOut.close();
                    
                    // update and notify
                    this.setActiveProjectFile(projectFile);
                    this.setActiveProjectModified(false);
                }
                finally
                {
                    // blow away the temp dir
                    FileUtilities.recursiveDelete(tempProjDir);
                }
                
                return true;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception saving project data",
                    ex);
            return false;
        }
    }
    
    /**
     * Get a clean version of the temporary project directory.
     * @return
     *          return the project directory
     */
    private File getCleanedTempProjectDir()
    {
        try
        {
            ConfigurationUtilities configurationUtilities =
                new ConfigurationUtilities();
            File configDir = configurationUtilities.getBaseDirectory();
            File tempProjDir = new File(configDir, TEMP_PROJECT_DIR_NAME);
            if(tempProjDir.exists())
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(
                            "Temporary project directory already exists: " +
                            tempProjDir);
                }
                
                if(!FileUtilities.recursiveDelete(tempProjDir))
                {
                    return null;
                }
            }
            
            if(tempProjDir.mkdir())
            {
                return tempProjDir;
            }
            else
            {
                LOG.warning(
                        "Failed to create temporary project directory");
                return null;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to clean temporary project directory",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadActiveProject(File projectFile)
    {
        try
        {
            File tempProjDir = this.getCleanedTempProjectDir();
            if(tempProjDir == null)
            {
                return false;
            }
            else
            {
                try
                {
                    // expand project file to temp dir
                    ZipInputStream zipIn = new ZipInputStream(
                            new FileInputStream(projectFile));
                    FileUtilities.unzipToDirectory(
                            zipIn,
                            tempProjDir);
                    
                    // clear the current r data
                    this.rInterface.evaluateCommand(new SilentRCommand(
                            "rm(list=ls())"));
                    
                    // load the r data
                    File rDataFile = new File(tempProjDir, PROJECT_R_DATA_FILENAME);
                    this.rInterface.evaluateCommandNoReturn(new SilentRCommand(
                            new CleanEnvironmentCommand()));
                    String loadDataCommandString =
                        "load(" +
                        RUtilities.javaStringToRString(rDataFile.getAbsolutePath()) +
                        ")";
                    this.rInterface.evaluateCommand(new SilentRCommand(
                            loadDataCommandString));
                    
                    // load the meta data
                    InputStream configFileIn = new FileInputStream(new File(
                            tempProjDir,
                            PROJECT_METADATA_FILENAME_1_0_0));
                    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
                    JMaanovaProjectMetadata jaxbProjectMetatata =
                        (JMaanovaProjectMetadata)unmarshaller.unmarshal(configFileIn);
                    
                    // create the project
                    MaanovaProject newProject = new MaanovaProject(
                            this.rInterface,
                            jaxbProjectMetatata);
                    
                    // update and notify
                    this.setActiveProjectFile(projectFile);
                    this.setActiveProjectModified(false);
                    this.setActiveProject(newProject);
                }
                finally
                {
                    // blow away the temp dir
                    FileUtilities.recursiveDelete(tempProjDir);
                }
                
                return true;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception loading project data",
                    ex);
            return false;
        }
    }
}
