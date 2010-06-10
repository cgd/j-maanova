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