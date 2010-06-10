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
