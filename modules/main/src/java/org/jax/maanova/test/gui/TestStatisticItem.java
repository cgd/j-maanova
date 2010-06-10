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

package org.jax.maanova.test.gui;

import org.jax.maanova.test.MaanovaTestStatisticSubtype;
import org.jax.maanova.test.MaanovaTestStatisticType;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TestStatisticItem
{
    /**
     * What text formatting should be used in the combo box
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    public enum Formatting
    {
        /**
         * Nothing special. do something like "Fs: Nominal P-Values"
         */
        PLAIN,
        
        /**
         * do something like "Fs: Nominal P-Values Less Than"
         */
        FILTER,
        
        /**
         * do something like "Fs: Nominal P-Values Ascending"
         */
        SORT
    }
    
    private final MaanovaTestStatisticType testStatisticType;
    
    private final MaanovaTestStatisticSubtype testStatisticSubtype;
    
    private final Formatting formatting;

    /**
     * Constructor with formatting set to {@link Formatting#PLAIN}
     * @param testStatisticType
     *          the type of test statistic (Fs, F)
     * @param testStatisticSubtype
     *          the test statistic subtype
     */
    public TestStatisticItem(
            MaanovaTestStatisticType testStatisticType,
            MaanovaTestStatisticSubtype testStatisticSubtype)
    {
        this(testStatisticType, testStatisticSubtype, Formatting.PLAIN);
    }
    
    /**
     * Constructor
     * @param testStatisticType
     *          the type of test statistic (Fs, F)
     * @param testStatisticSubtype
     *          the test statistic subtype
     * @param formatting
     *          the formatting to use
     */
    public TestStatisticItem(
            MaanovaTestStatisticType testStatisticType,
            MaanovaTestStatisticSubtype testStatisticSubtype,
            Formatting formatting)
    {
        this.testStatisticType = testStatisticType;
        this.testStatisticSubtype = testStatisticSubtype;
        this.formatting = formatting;
    }
    
    /**
     * Getter for the subtype
     * @return the subtype
     */
    public MaanovaTestStatisticSubtype getTestStatisticSubtype()
    {
        return this.testStatisticSubtype;
    }
    
    /**
     * Getter for the test statistic type
     * @return the test statistic type
     */
    public MaanovaTestStatisticType getTestStatisticType()
    {
        return this.testStatisticType;
    }
    
    /**
     * Getter for the formatting that should be used by {@link #toString()}
     * @return the formatting
     */
    public Formatting getFormatting()
    {
        return this.formatting;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        switch(this.testStatisticType)
        {
            case F_STAT:
            {
                sb.append("F");
            }
            break;
            
            case FS_STAT:
            {
                sb.append("Fs");
            }
            break;
            
            default:
                throw new IllegalStateException(
                        "Internal Error: Unexpected argument type: " +
                        this.testStatisticType);
        }
        
        sb.append(": ");
        sb.append(this.testStatisticSubtype.toString());
        
        switch(this.formatting)
        {
            case PLAIN:
            {
                // nothing more to do for plain formatting
            }
            break;
            
            case FILTER:
            {
                switch(this.testStatisticSubtype)
                {
                    case F_OBSERVED:
                    {
                        sb.append(" Greater Than");
                    }
                    break;
                    
                    default:
                    {
                        sb.append(" Less Than");
                    }
                    break;
                }
            }
            break;
            
            case SORT:
            {
                switch(this.testStatisticSubtype)
                {
                    case F_OBSERVED:
                    {
                        sb.append(" Descending");
                    }
                    break;
                    
                    default:
                    {
                        sb.append(" Ascending");
                    }
                    break;
                }
            }
            break;
            
            default: throw new IllegalStateException(
                    "Internal error: unexpected formatting type: " +
                    this.formatting);
        }
        
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return
            this.testStatisticType.hashCode() |
            (this.testStatisticSubtype.hashCode() << 16);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObj)
    {
        if(otherObj instanceof TestStatisticItem)
        {
            TestStatisticItem otherTestStatItem = (TestStatisticItem)otherObj;
            return
                this.testStatisticType == otherTestStatItem.testStatisticType &&
                this.testStatisticSubtype == otherTestStatItem.testStatisticSubtype;
        }
        else
        {
            return false;
        }
    }
}
