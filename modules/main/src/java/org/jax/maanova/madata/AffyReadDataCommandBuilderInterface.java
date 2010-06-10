/*
 * Copyright (c) 2008 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
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

package org.jax.maanova.madata;

import org.jax.r.RCommandBuilder;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface AffyReadDataCommandBuilderInterface extends RCommandBuilder
{
    /**
     * Getter for the name that should be used for the resulting data object
     * @return the name to use
     */
    public String getResultObjectName();
    
    /**
     * @see #getResultObjectName()
     * @param resultObjectName the name
     */
    public void setResultObjectName(String resultObjectName);
    
    /**
     * Getter for the CEL file names
     * @return the celFiles
     */
    public String[] getCelFiles();
    
    /**
     * Setter for the CEL file names
     * @param celFiles the celFiles to set
     */
    public void setCelFiles(String[] celFiles);
    
    /**
     * Getter for the CDF file name
     * @return the cdfFile
     */
    public String getCdfFile();
    
    /**
     * Setter for the CDF file name
     * @param cdfFile the cdfFile to set
     */
    public void setCdfFile(String cdfFile);
    
    /**
     * Determine if the CEL files are compressed or not
     * @return true iff they're compressed
     */
    public boolean getCelFilesCompressed();
    
    /**
     * Determines if the CEL files are treated as compressed or not
     * @param celFilesCompressed the celFilesCompressed to set
     */
    public void setCelFilesCompressed(boolean celFilesCompressed);
}
