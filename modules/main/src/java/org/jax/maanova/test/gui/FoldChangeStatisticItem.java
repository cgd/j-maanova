/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://churchill.jax.org/).
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

/**
 * This class is intended to be used with GUI components like JMenuItem etc. In
 * particular the class can be used to indicate a fold-change item
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FoldChangeStatisticItem extends StatisticItem
{
    /**
     * Constructor (uses plain formatting)
     */
    public FoldChangeStatisticItem()
    {
        this(Formatting.PLAIN);
    }
    
    /**
     * Constructor
     * @param formatting    the formatting to use
     */
    public FoldChangeStatisticItem(Formatting formatting)
    {
        super(formatting);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public StatisticItem copyWithNewFormatting(Formatting formatting)
    {
        return new FoldChangeStatisticItem(formatting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        switch(this.getFormatting())
        {
            case PLAIN: return "Fold Change";
            
            case FILTER: return "Abs. Fold Change: Greater Than";
            
            case SORT: return "Abs. Fold Change: Descending";
            
            default: throw new IllegalStateException(
                    "Internal error: unexpected formatting type: " +
                    this.getFormatting());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        // just some number since we consider all instances equal
        return 234325;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        // We consider all of these "equal" even when the formatting differs
        return obj instanceof FoldChangeStatisticItem;
    }
}
