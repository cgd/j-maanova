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
 * Some common base functionality shared by statistic items. This class and its
 * subclasses are intended to be used with GUI components like JMenuItem etc
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class StatisticItem
{
    private final Formatting formatting;

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

    /**
     * Constructor
     * @param formatting    the formatting to use
     */
    public StatisticItem(Formatting formatting)
    {
        this.formatting = formatting;
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
     * Create a copy of this statistic except change the formatting
     * @param formatting    the new formatting to use
     * @return              the "copy"
     */
    public abstract StatisticItem copyWithNewFormatting(Formatting formatting);
}
