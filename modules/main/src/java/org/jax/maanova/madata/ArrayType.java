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
 * An enumeration for array types
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum ArrayType
{
    /**
     * For one color arrays
     */
    ONE_COLOR
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRValue()
        {
            return "oneColor";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "One Color";
        }
    },
    
    /**
     * For two color arrays
     */
    TWO_COLOR
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRValue()
        {
            return "twoColor";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Two Color";
        }
    };
    
    /**
     * Getter for the string representation that should be used for R
     * @return
     *          the R string
     */
    public abstract String getRValue();
}
