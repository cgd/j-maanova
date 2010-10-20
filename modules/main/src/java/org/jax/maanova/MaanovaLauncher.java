/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.maanova;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.rintegration.RLauncher;

/**
 * Our MAANOVA application launcher
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaLauncher extends RLauncher
{
    /**
     * Our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MaanovaLauncher.class.getName());
    
    /**
     * the resource location of the zip file that contains all of the jars
     * that are used in J/maanova's classpath.
     */
    private static final String CLASSPATH_ZIP_FILE_RESOURCE =
        "/j-maanova-classpath-bundle.zip";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getApplicationMainClass()
    {
        return Maanova.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClasspathZipFileResourcePath()
    {
        return CLASSPATH_ZIP_FILE_RESOURCE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationConfigurationManager getApplicationConfigurationManager()
    {
        return MaanovaApplicationConfigurationManager.getInstance();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadableApplicationName()
    {
        return "J/maanova";
    }
    
    /**
     * The main application entry point for MAANOVA
     * @param args
     *          application arguments
     */
    public static void main(String[] args)
    {
        try
        {
            MaanovaLauncher launcher = new MaanovaLauncher();
            launcher.launchApplication();
            System.exit(0);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Caught exception trying to launch application",
                    ex);
        }
    }
}
