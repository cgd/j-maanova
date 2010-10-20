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

package org.jax.maanova.test;

/**
 * This enum allows you to specify what kind of test statistic you want
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum MaanovaTestStatisticType
{
    /**
     * for the F statistics
     */
    F_STAT
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return F_STAT_TEXT;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRComponentAccessorString()
        {
            return F_STATISTICS_ACCESSOR;
        }
    },
    
    /**
     * For the Fs statistics
     */
    FS_STAT
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return FS_STAT_TEXT;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRComponentAccessorString()
        {
            return FS_STATISTICS_ACCESSOR;
        }
    };
    
    private static final String F_STAT_TEXT = "F Statistic";
    private static final String FS_STAT_TEXT = "Fs Statistic";
    
    private static final String F_STATISTICS_ACCESSOR = "$F1";
    private static final String FS_STATISTICS_ACCESSOR = "$Fs";
    
    /**
     * Get the R accessor string
     * @return
     *          the string
     */
    public abstract String getRComponentAccessorString();
}
