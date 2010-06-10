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
