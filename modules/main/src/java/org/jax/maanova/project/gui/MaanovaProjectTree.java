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

package org.jax.maanova.project.gui;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jax.maanova.fit.FitMaanovaResult;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.maanova.project.MaanovaDataModel;
import org.jax.maanova.project.MaanovaDataModelListener;
import org.jax.maanova.project.MaanovaProject;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.maanova.test.MaanovaTestResult;
import org.jax.util.gui.SwingTreeUtilities;
import org.jax.util.project.Project;
import org.jax.util.project.gui.ProjectTree;

/**
 * A {@link JTree} that shows the interactive contents of a J/maanova
 * project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaProjectTree extends ProjectTree
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1354914290025074652L;
    
    private MaanovaDataModelListener dataModelListener = new MaanovaDataModelListener()
    {
        /**
         * {@inheritDoc}
         */
        public void microarrayExperimentAdded(
                MaanovaDataModel source,
                MicroarrayExperiment microarrayExperiment)
        {
            this.changeOccured();
        }
        
        /**
         * {@inheritDoc}
         */
        public void microarrayExperimentRemoved(
                MaanovaDataModel source,
                MicroarrayExperiment microarrayExperiment)
        {
            this.changeOccured();
        }

        private void changeOccured()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    MaanovaProjectTree.this.refreshMicroarrayExperimentNodes();
                }
            });
        }
    };
    
    /**
     * Constructor 
     */
    public MaanovaProjectTree()
    {
        this.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent treeSelectionEvent)
            {
                MaanovaProjectTree.this.treeSelectionChanged(treeSelectionEvent);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void refreshProjectTree()
    {
        MaanovaProject activeProject =
            this.getProjectManager().getActiveProject();
        final boolean activeProjectIsRoot;
        {
            final Object rootObject = this.getModel().getRoot();
            if(rootObject instanceof ProjectTreeNode)
            {
                final ProjectTreeNode root = (ProjectTreeNode)rootObject;
                activeProjectIsRoot =
                    root.getProject() == activeProject;
            }
            else
            {
                activeProjectIsRoot = false;
            }
        }
        
        if(!activeProjectIsRoot)
        {
            this.getModel().setRoot(new ProjectTreeNode(activeProject));
        }
        
        this.refreshMicroarrayExperimentNodes();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MaanovaProject getActiveProject()
    {
        return (MaanovaProject)super.getActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveProject(Project activeProject)
    {
        if(activeProject == null)
        {
            throw new NullPointerException("Project cannot be null");
        }
        
        MaanovaProject maanovaActiveProject = (MaanovaProject)activeProject;
        {
            MaanovaProject oldActiveProject = this.getActiveProject();
            if(oldActiveProject != null)
            {
                oldActiveProject.getDataModel().removeMaanovaDataModelListener(
                        this.dataModelListener);
            }
        }
        
        maanovaActiveProject.getDataModel().addMaanovaDataModelListener(
                this.dataModelListener);
        
        super.setActiveProject(activeProject);
    }
    
    /**
     * refresh all of the microarray experiment nodes
     */
    private void refreshMicroarrayExperimentNodes()
    {
        ProjectTreeNode projectNode = (ProjectTreeNode)this.getModel().getRoot();
        MaanovaProject project = projectNode.getProject();
        
        MaanovaDataModel dataModel = project.getDataModel();
        
        boolean experimentCountMayHaveChanged = false;
        
        // remove tree nodes not in the MAANOVA data model
        for(int microarrayNodeIndex = projectNode.getChildCount() - 1;
            microarrayNodeIndex >= 0;
            microarrayNodeIndex--)
        {
            MicroarrayExperimentTreeNode currMicroarrayNode =
                (MicroarrayExperimentTreeNode)projectNode.getChildAt(microarrayNodeIndex);
            boolean microarrayInDataModel = dataModel.getMicroarrayExperimentMap().containsKey(
                    currMicroarrayNode.getMicroarrayExperiment().getAccessorExpressionString());
            
            if(!microarrayInDataModel)
            {
                this.getModel().removeNodeFromParent(
                        (MutableTreeNode)projectNode.getChildAt(microarrayNodeIndex));
                experimentCountMayHaveChanged = true;
            }
        }
        
        // add nodes that are missing (refresh existing)
        for(MicroarrayExperiment currMicroarray: dataModel.getMicroarrayExperimentMap().values())
        {
            int indexOfMicroarray = SwingTreeUtilities.indexOfChildWithUserObject(
                    projectNode,
                    currMicroarray);
            
            MicroarrayExperimentTreeNode currMicroarrayExperimentNode;
            if(indexOfMicroarray == -1)
            {
                // append the microarray to the end of the project node
                currMicroarrayExperimentNode = new MicroarrayExperimentTreeNode(currMicroarray);
                this.getModel().insertNodeInto(
                        currMicroarrayExperimentNode,
                        projectNode,
                        projectNode.getChildCount());
                this.getModel().insertNodeInto(
                        currMicroarrayExperimentNode.getFitMaanovasTreeNode(),
                        currMicroarrayExperimentNode,
                        currMicroarrayExperimentNode.getChildCount());
                this.getModel().insertNodeInto(
                        currMicroarrayExperimentNode.getMaanovaTestsTreeNode(),
                        currMicroarrayExperimentNode,
                        currMicroarrayExperimentNode.getChildCount());
                this.getModel().insertNodeInto(
                        currMicroarrayExperimentNode.getGeneListsTreeNode(),
                        currMicroarrayExperimentNode,
                        currMicroarrayExperimentNode.getChildCount());
                this.expandPath(new TreePath(
                        currMicroarrayExperimentNode.getPath()));
                
                experimentCountMayHaveChanged = true;
            }
            else
            {
                currMicroarrayExperimentNode =
                    (MicroarrayExperimentTreeNode)projectNode.getChildAt(indexOfMicroarray);
            }
            
            this.refreshFitResultsNode(
                    currMicroarrayExperimentNode.getFitMaanovasTreeNode());
            this.refreshTestResultsNode(
                    currMicroarrayExperimentNode.getMaanovaTestsTreeNode());
            this.refreshGeneListsNode(
                    currMicroarrayExperimentNode.getGeneListsTreeNode());
        }
        
        // if the experiment count changes it can cause the project node's
        // label to change, so we need to notify
        if(experimentCountMayHaveChanged)
        {
            this.getModel().nodeChanged(projectNode);
        }
    }
    
    /**
     * Refresh the given gene list node
     * @param geneListsTreeNode the node to refresh
     */
    private void refreshGeneListsNode(GeneListsTreeNode geneListsTreeNode)
    {
        final MicroarrayExperiment microarrayExperiment =
            geneListsTreeNode.getMicroarrayExperiment();
        final int childNodeCount = geneListsTreeNode.getChildCount();
        final DefaultTreeModel treeModel = this.getModel();
        
        // prune old gene lists and the gene lists to add in the same loop
        final List<String> geneListNamesToAdd = microarrayExperiment.getGeneListNames();
        for(int i = childNodeCount - 1; i >= 0; i--)
        {
            GeneListTreeNode geneListNode =
                (GeneListTreeNode)geneListsTreeNode.getChildAt(i);
            if(!geneListNamesToAdd.remove(geneListNode.getGeneListId()))
            {
                treeModel.removeNodeFromParent(geneListNode);
            }
        }
        
        // append the new nodes
        for(String geneListName: geneListNamesToAdd)
        {
            GeneListTreeNode geneListNode = new GeneListTreeNode(
                    microarrayExperiment,
                    geneListName);
            treeModel.insertNodeInto(
                    geneListNode,
                    geneListsTreeNode,
                    geneListsTreeNode.getChildCount());
        }
        
        // update the label (the count may have changed)
        treeModel.nodeChanged(geneListsTreeNode);
    }

    /**
     * Refresh the given test node
     * @param maanovaTestsTreeNode
     *          the node to refresh
     */
    private void refreshTestResultsNode(
            MaanovaTestsTreeNode maanovaTestsTreeNode)
    {
        final MicroarrayExperiment microarrayExperiment =
            maanovaTestsTreeNode.getMicroarrayExperiment();
        final int childNodeCount = maanovaTestsTreeNode.getChildCount();
        final Set<MaanovaTestResult> testResultsToAdd = new HashSet<MaanovaTestResult>(
                microarrayExperiment.getMaanovaTestResults());
        final DefaultTreeModel treeModel = this.getModel();
        
        // clean the test results to add and old tree nodes in a single loop
        for(int i = childNodeCount - 1; i >= 0; i--)
        {
            MaanovaTestTreeNode maanovaTestTreeNode =
                (MaanovaTestTreeNode)maanovaTestsTreeNode.getChildAt(i);
            
            if(!testResultsToAdd.remove(maanovaTestTreeNode.getMaanovaTestResult()))
            {
                // remove nodes that no longer belong
                treeModel.removeNodeFromParent(maanovaTestTreeNode);
            }
        }
        
        // append the new nodes
        for(MaanovaTestResult testToAdd: testResultsToAdd)
        {
            treeModel.insertNodeInto(
                    new MaanovaTestTreeNode(testToAdd),
                    maanovaTestsTreeNode,
                    maanovaTestsTreeNode.getChildCount());
        }
        
        // update the label (the count may have changed)
        treeModel.nodeChanged(maanovaTestsTreeNode);
    }

    /**
     * Refresh the fit results node
     * @param fitMaanovasTreeNode
     *          the node to refresh
     */
    private void refreshFitResultsNode(FitMaanovasTreeNode fitMaanovasTreeNode)
    {
        final MicroarrayExperiment microarrayExperiment =
            fitMaanovasTreeNode.getMicroarrayExperiment();
        final int childNodeCount = fitMaanovasTreeNode.getChildCount();
        final Set<FitMaanovaResult> fitResultsToAdd = new HashSet<FitMaanovaResult>(
                microarrayExperiment.getFitMaanovaResults());
        final DefaultTreeModel treeModel = this.getModel();
        
        // clean the fit results to add and old tree nodes in a single loop
        for(int i = childNodeCount - 1; i >= 0; i--)
        {
            FitMaanovaTreeNode fitMaanovaTreeNode =
                (FitMaanovaTreeNode)fitMaanovasTreeNode.getChildAt(i);
            
            if(!fitResultsToAdd.remove(fitMaanovaTreeNode.getFitMaanovaResult()))
            {
                // remove nodes that no longer belong
                treeModel.removeNodeFromParent(fitMaanovaTreeNode);
            }
        }
        
        // append the new nodes
        for(FitMaanovaResult fitToAdd: fitResultsToAdd)
        {
            treeModel.insertNodeInto(
                    new FitMaanovaTreeNode(fitToAdd),
                    fitMaanovasTreeNode,
                    fitMaanovasTreeNode.getChildCount());
        }
        
        // update the label (the count may have changed)
        treeModel.nodeChanged(fitMaanovasTreeNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultTreeModel getModel()
    {
        return (DefaultTreeModel)super.getModel();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MaanovaProjectManager getProjectManager()
    {
        return (MaanovaProjectManager)super.getProjectManager();
    }
    
    /**
     * Respond to a tree selection event
     * @param treeSelectionEvent
     *          the event we're responding to
     */
    private void treeSelectionChanged(TreeSelectionEvent treeSelectionEvent)
    {
        // TODO implement me
    }
}
