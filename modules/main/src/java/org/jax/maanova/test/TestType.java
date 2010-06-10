/*
 * Copyright (c) 2010 The Jackson Laboratory
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
package org.jax.maanova.test;

/**
 * An enumeration for the test type to run
 */
public enum TestType {
    /**
     * If we should perform an F-test
     */
    F_TEST
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRParameterString()
        {
            return "ftest";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "F-test";
        }
    },
    
    /**
     * If we should perform a T-test
     */
    T_TEST
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRParameterString()
        {
            return "ttest";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "t-test";
        }
    };
    
    /**
     * Getter for the string that R/maanova uses to represent this test
     * type
     * @return  the R/maanova string (no quotes, you have to add those)
     */
    public abstract String getRParameterString();
}