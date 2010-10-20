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

package org.jax.maanova.madata;

/**
 * An enumeration for picking out how we're going to summarize replicate
 * information
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum ReplicateSummaryMethod
{
    /**
     * Dont summarize at all
     */
    NO_SUMMARIZATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public int getRValue()
        {
            return 0;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Keep All Replicates";
        }
    },
    
    /**
     * summarize by taking the mean value
     */
    MEAN_SUMMARIZATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public int getRValue()
        {
            return 1;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Mean Value";
        }
    },
    
    /**
     * summarize by taking the median value
     */
    MEDIAN_SUMMARIZATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public int getRValue()
        {
            return 2;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Median Value";
        }
    };
    
    /**
     * Getter for the R value that should be used for this summary method
     * @return
     *          the R value
     */
    public abstract int getRValue();
}