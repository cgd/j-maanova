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
public interface AffyRMACommandBuilderInterface extends RCommandBuilder
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
     * Determines if background correction should be performed
     * @return true if we should do background correction
     */
    public boolean getDoBackgroundCorrection();
    
    /**
     * @see #getDoBackgroundCorrection()
     * @param doBackgroundCorrection the doBackgroundCorrection to set
     */
    public void setDoBackgroundCorrection(boolean doBackgroundCorrection);
    
    /**
     * Determines if we should do quantile normalization on the affy data or
     * not
     * @return the doQuantileNormalization
     */
    public boolean getDoQuantileNormalization();
    
    /**
     * @see #getDoQuantileNormalization()
     * @param doQuantileNormalization the doQuantileNormalization to set
     */
    public void setDoQuantileNormalization(boolean doQuantileNormalization);
}
