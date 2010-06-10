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

package org.jax.maanova.test;

/**
 * This enum specifies the statistic subtype where F or Fs would be the
 * "super" type.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum MaanovaTestStatisticSubtype
{
    /**
     * 
     */
    UNADJUSTED
    {
        @Override
        public String getRComponentAccessorString()
        {
            return UNADJUSTED_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Unadjusted Tabulated P-Values";
        }
    },
    
    /**
     * 
     */
    NOMINAL_VALUES
    {
        @Override
        public String getRComponentAccessorString()
        {
            return NOMINAL_VALUES_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Nominal Permutation P-Values";
        }
    },
    
    /**
     * 
     */
    FWER_ADJUSTED
    {
        @Override
        public String getRComponentAccessorString()
        {
            return FWER_ADJUSTED_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "FWER One-Step Adjusted P-Values";
        }
    },
    
    /**
     * 
     */
    FDR_ADJUSTED
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRComponentAccessorString()
        {
            return FDR_ADJUSTED_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "FDR Adjusted Tabulated P-Values";
        }
    },
    
    /**
     * 
     */
    FDR_ADJUSTED_PERMUTATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRComponentAccessorString()
        {
            return FDR_ADJUSTED_PERMUTATION_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "FDR Adjusted Nominal Permutation P-Values";
        }
    },
    
    /**
     * 
     */
    F_OBSERVED
    {
        @Override
        public String getRComponentAccessorString()
        {
            return F_OBSERVED_ACCESSOR;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "F Observed";
        }
    };
    
    // Fobs and Ptab should always be available
    private static final String F_OBSERVED_ACCESSOR = "$Fobs";
    
    private static final String UNADJUSTED_ACCESSOR = "$Ptab";
    
    // Pvalperm and Pvalmax are only available in the case of permutations
    private static final String NOMINAL_VALUES_ACCESSOR = "$Pvalperm";
    
    private static final String FWER_ADJUSTED_ACCESSOR = "$Pvalmax";
    
    // adjPtab is only available in the case where adjPval(...) was run
    private static final String FDR_ADJUSTED_ACCESSOR = "$adjPtab";
    
    // adjPvalperm is only available in the case where adjPval(...) was run
    // and permutations were run
    private static final String FDR_ADJUSTED_PERMUTATION_ACCESSOR = "$adjPvalperm";
    
    /**
     * Get the R accessor string
     * @return
     *          the string
     */
    public abstract String getRComponentAccessorString();
}
