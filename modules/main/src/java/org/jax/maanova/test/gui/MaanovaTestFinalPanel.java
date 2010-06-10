/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.maanova.test.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.jax.maanova.test.TestModelCommandBuilder;
import org.jax.maanova.test.TestModelCommandBuilder.ShuffleMethod;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.gui.Validatable;

/**
 * The final panel for testing which includes a lot of the permutation
 * parameters etc.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MaanovaTestFinalPanel
extends RCommandEditorPanel
implements Validatable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -7546715947670713264L;
    
    private final TestModelCommandBuilder commandBuilder;

    private final SpinnerNumberModel permutationCountSpinnerModel = new SpinnerNumberModel(
            1000,               // initial value
            1,                  // min value
            Integer.MAX_VALUE,  // max value
            100);               // step size
    
    private final SpinnerNumberModel criticalThresholdSpinnerModel = new SpinnerNumberModel(
            0.9,                // initial value
            0.0,                // min value
            1.0,                // max value
            0.1);               // step size
    
    /**
     * Constructor
     * @param commandBuilder
     *          the command builder that this panel modifies
     */
    public MaanovaTestFinalPanel(TestModelCommandBuilder commandBuilder)
    {
        this.commandBuilder = commandBuilder;
        
        this.initComponents();
        this.posetGuiInit();
    }
    
    /**
     * Take care of the initialization that the Netbeans GUI builder doesn't
     * do for us
     */
    private void posetGuiInit()
    {
        this.nameTextField.getDocument().addDocumentListener(
                new SimplifiedDocumentListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void anyUpdate(DocumentEvent e)
                    {
                        MaanovaTestFinalPanel.this.nameChanged();
                    }
                });
        
        this.performPermutationsCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                MaanovaTestFinalPanel.this.performPermutationsChanged();
            }
        });
        
        this.permutationCountSpinner.setModel(
                this.permutationCountSpinnerModel);
        this.permutationCountSpinnerModel.addChangeListener(new ChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                MaanovaTestFinalPanel.this.permutationCountChanged();
            }
        });
        this.permutationCountChanged();
        
        this.criticalThresholdSpinner.setModel(
                this.criticalThresholdSpinnerModel);
        this.criticalThresholdSpinnerModel.addChangeListener(new ChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                MaanovaTestFinalPanel.this.criticalThresholdChanged();
            }
        });
        this.criticalThresholdChanged();
        
        for(ShuffleMethod method: ShuffleMethod.values())
        {
            this.shufflingMethodComboBox.addItem(method);
        }
        this.shufflingMethodComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    MaanovaTestFinalPanel.this.shuffleMethodChanged();
                }
            }
        });
        this.shuffleMethodChanged();
        
        this.poolCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                MaanovaTestFinalPanel.this.poolValuesChanged();
            }
        });
        this.poolValuesChanged();
        
        this.verboseCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                MaanovaTestFinalPanel.this.verboseOutputChanged();
            }
        });
        this.verboseOutputChanged();
    }

    /**
     * respond to a change in whether or not we should perform permutations
     */
    private void performPermutationsChanged()
    {
        boolean performPerms = this.performPermutationsCheckBox.isSelected();
        
        this.permutationCountSpinner.setEnabled(performPerms);
        this.criticalThresholdLabel.setEnabled(performPerms);
        this.criticalThresholdSpinner.setEnabled(performPerms);
        this.shufflingMethodLabel.setEnabled(performPerms);
        this.shufflingMethodComboBox.setEnabled(performPerms);
        this.poolCheckBox.setEnabled(performPerms);
        
        if(performPerms)
        {
            this.permutationCountChanged();
        }
        else
        {
            this.commandBuilder.setPermutationCount(1);
            this.fireCommandModified();
        }
    }

    /**
     * respond to a change in the verbose output selection
     */
    private void verboseOutputChanged()
    {
        this.commandBuilder.setVerbose(this.verboseCheckBox.isSelected());
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the "pool p values" option
     */
    private void poolValuesChanged()
    {
        this.commandBuilder.setPoolPValues(
                this.poolCheckBox.isSelected());
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the data shuffling method
     */
    private void shuffleMethodChanged()
    {
        ShuffleMethod shuffleMethod =
            (ShuffleMethod)this.shufflingMethodComboBox.getSelectedItem();
        this.commandBuilder.setShuffleMethod(shuffleMethod);
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the threshold
     */
    private void criticalThresholdChanged()
    {
        double criticalThreshold =
            this.criticalThresholdSpinnerModel.getNumber().doubleValue();
        this.commandBuilder.setCriticalThreshold(criticalThreshold);
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in the permutation count
     */
    private void permutationCountChanged()
    {
        int permutationCount =
            this.permutationCountSpinnerModel.getNumber().intValue();
        this.commandBuilder.setPermutationCount(permutationCount);
        
        this.fireCommandModified();
    }

    /**
     * respond to a change in name
     */
    private void nameChanged()
    {
        String madataName = this.commandBuilder.getMadataParameter();
        String newName = this.nameTextField.getText().trim();
        
        if(newName.length() == 0)
        {
            this.commandBuilder.setTestResultDataName(null);
        }
        else
        {
            try
            {
                newName = RUtilities.fromReadableNameToRIdentifier(newName);
                this.commandBuilder.setTestResultDataName(
                        madataName + "." + newName);
            }
            catch(RSyntaxException ex)
            {
                // since we can't convert the name into an R identifier
                // we should reset the name in the command builder
                this.commandBuilder.setTestResultDataName("");
            }
        }
        
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean validateData()
    {
        String message = null;
        
        String name = this.nameTextField.getText().trim();
        if(name.length() == 0)
        {
            message =
                "Please enter a name for the fit result before continuing.";
        }
        else
        {
            message = RUtilities.getErrorMessageForReadableName(name);
        }
        
        if(message == null)
        {
            return true;
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    message,
                    "Validation Failed");
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.commandBuilder.getCommand()};
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        performPermutationsCheckBox = new javax.swing.JCheckBox();
        permutationCountSpinner = new javax.swing.JSpinner();
        criticalThresholdLabel = new javax.swing.JLabel();
        criticalThresholdSpinner = new javax.swing.JSpinner();
        shufflingMethodLabel = new javax.swing.JLabel();
        shufflingMethodComboBox = new javax.swing.JComboBox();
        poolCheckBox = new javax.swing.JCheckBox();
        verboseCheckBox = new javax.swing.JCheckBox();

        nameLabel.setText("Name Your Test Result:");

        performPermutationsCheckBox.setSelected(true);
        performPermutationsCheckBox.setText("Perform Permutations");

        criticalThresholdLabel.setText("Critical F-distribution Quantile:");

        shufflingMethodLabel.setText("Data Shuffling Method:");

        poolCheckBox.setSelected(true);
        poolCheckBox.setText("Use Pooled Permutation F statistics");

        verboseCheckBox.setSelected(true);
        verboseCheckBox.setText("Print Verbose Output");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(poolCheckBox)
                    .add(verboseCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(criticalThresholdLabel)
                            .add(performPermutationsCheckBox)
                            .add(shufflingMethodLabel)
                            .add(nameLabel))
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(shufflingMethodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, criticalThresholdSpinner)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, permutationCountSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)))))
                .addContainerGap(132, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(performPermutationsCheckBox)
                    .add(permutationCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(criticalThresholdLabel)
                    .add(criticalThresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shufflingMethodLabel)
                    .add(shufflingMethodComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(poolCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(verboseCheckBox)
                .addContainerGap(118, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel criticalThresholdLabel;
    private javax.swing.JSpinner criticalThresholdSpinner;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox performPermutationsCheckBox;
    private javax.swing.JSpinner permutationCountSpinner;
    private javax.swing.JCheckBox poolCheckBox;
    private javax.swing.JComboBox shufflingMethodComboBox;
    private javax.swing.JLabel shufflingMethodLabel;
    private javax.swing.JCheckBox verboseCheckBox;
    // End of variables declaration//GEN-END:variables

}
