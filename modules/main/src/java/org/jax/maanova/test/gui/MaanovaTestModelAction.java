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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jax.maanova.Maanova;
import org.jax.maanova.project.MaanovaProjectManager;
import org.jax.r.gui.RCommandEditorAndPreviewPanel;
import org.jax.util.gui.WizardDialog;
import org.jax.util.gui.WizardDialog.WizardDialogType;

/**
 * The action to popup an {@code matest(...)} dialog for the user
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestModelAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3615121685565451317L;

    /**
     * Constructor
     */
    public MaanovaTestModelAction()
    {
        super("Test ANOVA Model...", null);
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        MaanovaTestWizardContentPanel testWizardPanel =
            new MaanovaTestWizardContentPanel(
                    MaanovaProjectManager.getInstance().getActiveProject());
        WizardDialog wizardDialog = new WizardDialog(
                testWizardPanel,
                new RCommandEditorAndPreviewPanel(testWizardPanel),
                Maanova.getInstance().getApplicationFrame(),
                "Test ANOVA Model", // TODO is this a good title??
                false,
                WizardDialogType.BACK_NEXT_FINISH_CANCEL_DIALOG);
        wizardDialog.setVisible(true);
    }
}
