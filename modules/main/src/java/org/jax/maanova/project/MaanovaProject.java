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
