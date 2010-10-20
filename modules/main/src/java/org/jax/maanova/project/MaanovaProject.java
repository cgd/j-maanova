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

package org.jax.maanova.project;

import org.jax.maanova.jaxbgenerated.JMaanovaProjectMetadata;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.project.RProject;

/**
 * The class that holds the data model and takes care of all of the eventing
 * for a J/maanova project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaProject extends RProject
{
    private final MaanovaDataModel dataModel;
    
    /**
     * our JAXB object factory
     */
    private final org.jax.maanova.jaxbgenerated.ObjectFactory objectFactory =
        new org.jax.maanova.jaxbgenerated.ObjectFactory();
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface to use
     * @param projectMetadata
     *          the project metadata to use
     */
    public MaanovaProject(
            RInterface rInterface,
            JMaanovaProjectMetadata projectMetadata)
    {
        super(rInterface, projectMetadata);
        
        this.dataModel = new MaanovaDataModel(rInterface);
    }

    /**
     * Constructor to use for starting out with no project name
     * @param rInterface
     *          the R interface to use for the project
     */
    public MaanovaProject(RInterface rInterface)
    {
        super(rInterface);
        
        this.dataModel = new MaanovaDataModel(rInterface);
    }
    
    /**
     * Getter for the MAANOVA data model
     * @return
     *          the data model
     */
    public MaanovaDataModel getDataModel()
    {
        return this.dataModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMaanovaProjectMetadata getMetadata()
    {
        JMaanovaProjectMetadata metadata =
            this.objectFactory.createJMaanovaProjectMetadata();
        
        metadata.setProjectName(this.getName());
        metadata.getRHistoryItem().addAll(
                this.getRHistory());
        
        return metadata;
    }
}
