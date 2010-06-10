/*
 * Copyright (c) 2010 The Jackson Laboratory
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

package org.jax.maanova.madata.gui;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import org.jax.maanova.Maanova;
import org.jax.maanova.madata.MicroarrayExperiment;
import org.jax.r.RUtilities;
import org.jax.util.gui.OKCancelWizardController;
import org.jax.util.gui.WizardDialog;
import org.jax.util.gui.WizardDialog.WizardDialogType;

/**
 * An action for editing a gene list
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditGeneListAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2541647567719202420L;
    private static final Logger LOG = Logger.getLogger(
            EditGeneListAction.class.getName());
    
    private MicroarrayExperiment experiment;
    private String geneListId;

    /**
     * Constructor
     * @param experiment the experiment
     * @param geneListId the gene ID
     */
    public EditGeneListAction(MicroarrayExperiment experiment, String geneListId)
    {
        super("Edit Gene List");
        
        this.experiment = experiment;
        this.geneListId = geneListId;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        String[] genesInList = this.experiment.getGeneListNamed(this.geneListId);
        String[] allGenes = this.experiment.getProbesetIds();
        
        final EditGeneListPanel thePanel = new EditGeneListPanel(genesInList, allGenes);
        OKCancelWizardController editGeneListWizardController = new OKCancelWizardController()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean ok() throws IllegalStateException
            {
                EditGeneListAction.this.ok(thePanel.getGeneList());
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public boolean cancel()
            {
                // you can always cancel
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public void help()
            {
                Maanova.getInstance().showHelp("gene-list", thePanel);
            }
        };
        
        WizardDialog wizardDialog = new WizardDialog(
                editGeneListWizardController,
                thePanel,
                Maanova.getInstance().getApplicationFrame(),
                "Edit Gene List Named: " + RUtilities.fromRIdentifierToReadableName(this.geneListId),
                false,
                WizardDialogType.OK_CANCEL_DIALOG);
        wizardDialog.setVisible(true);
    }

    /**
     * OK the gene list
     * @param geneList the gene list
     */
    private void ok(List<String> geneList)
    {
        this.experiment.putGeneListNamed(this.geneListId, geneList);
    }
}
