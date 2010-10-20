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

import java.util.Arrays;

import org.jax.util.datastructure.SequenceUtilities;

/**
 * Holds a Probeset ID and associated value array
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ProbesetRow
{
    private final String id;
    
    private final Double[] values;

    private final int index;

    /**
     * Constructor
     * @param id
     *          the probeset IDs
     * @param values
     *          the probeset values
     */
    public ProbesetRow(String id, Double[] values)
    {
        this(id, values, -1);
    }
    
    /**
     * Constructor
     * @param id
     *          the probeset IDs
     * @param values
     *          the probeset values
     * @param index
     *          the index of this probeset (use -1 for unknown/don't care)
     */
    public ProbesetRow(String id, Double[] values, int index)
    {
        this.id = id;
        this.values = values;
        this.index = index;
    }
    
    /**
     * Getter for the probeset ID
     * @return the ID
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * Getter for the probeset values. This can be intensities, statistics
     * etc...
     * @return the values
     */
    public Double[] getValues()
    {
        return this.values;
    }
    
    /**
     * Getter for the index. -1 means unknown
     * @return the index
     */
    public int getIndex()
    {
        return this.index;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return
            "Probeset Row: ID = " + this.getId() + ", Values = " +
            SequenceUtilities.toString(Arrays.asList(this.values));
    }
}
