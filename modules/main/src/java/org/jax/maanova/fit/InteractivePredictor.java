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

package org.jax.maanova.fit;

import java.util.Arrays;

/**
 * An interactive predictor object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class InteractivePredictor
{
    private final String[] interactiveTerms;

    /**
     * Constructor
     * @param interactiveTerms
     *          see {@link #getInteractiveTerms()}
     */
    public InteractivePredictor(String[] interactiveTerms)
    {
        if(interactiveTerms.length == 0)
        {
            throw new IllegalArgumentException(
                    "there should be at least one term");
        }
        
        this.interactiveTerms = interactiveTerms;
    }
    
    /**
     * Constructor for a single term predictor
     * @param singleTerm
     *          the term
     */
    public InteractivePredictor(String singleTerm)
    {
        this.interactiveTerms = new String[] {singleTerm};
    }

    /**
     * Getter for the interactive terms that make up this predictor.
     * If there is only one array element then there are no interactions
     * @return
     *          the interactive terms
     */
    public String[] getInteractiveTerms()
    {
        return this.interactiveTerms;
    }
    
    /**
     * Determine if this is a single term or not
     * @return
     *          true if there's only one term in the list from
     *          {@link #getInteractiveTerms()}
     */
    public boolean isSingleTerm()
    {
        return this.interactiveTerms.length == 1;
    }
    
    /**
     * Only makes sense if {@link #isSingleTerm()} is true
     * @return
     *          the single term for this predictor
     */
    public String getTerm()
    {
        return this.interactiveTerms[0];
    }
    
    /**
     * Convert the given additive predictor groups into a string
     * @param interactivePredictorsToAdd
     *          the groups to add
     * @return
     *          the string representation
     */
    public static String toRFormulaString(
            InteractivePredictor[] interactivePredictorsToAdd)
    {
        StringBuffer buffer = new StringBuffer("~");
        for(int i = 0; i < interactivePredictorsToAdd.length; i++)
        {
            if(i >= 1)
            {
                buffer.append('+');
            }
            
            buffer.append(interactivePredictorsToAdd[i].toString());
        }
        
        return buffer.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer(this.interactiveTerms[0]);
        for(int i = 1; i < this.interactiveTerms.length; i++)
        {
            buffer.append(':');
            buffer.append(this.interactiveTerms[i]);
        }
        
        return buffer.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObject)
    {
        if(otherObject instanceof InteractivePredictor)
        {
            InteractivePredictor otherInteractivePredictor =
                (InteractivePredictor)otherObject;
            String[] otherInteractiveTerms =
                otherInteractivePredictor.getInteractiveTerms();
            
            return Arrays.asList(this.interactiveTerms).equals(
                    Arrays.asList(otherInteractiveTerms));
        }
        else
        {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = this.interactiveTerms[0].hashCode();
        for(int i = 1; i < this.interactiveTerms.length; i++)
        {
            hash ^= this.interactiveTerms[i].hashCode();
        }
        
        return hash;
    }
}
