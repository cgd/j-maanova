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

package org.jax.maanova.madata.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jax.maanova.configuration.MaanovaApplicationConfigurationManager;
import org.jax.maanova.madata.AffyJustRMACommandBuilder;
import org.jax.maanova.madata.ReadMicroarrayDataCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.CharacterDelimitedViewDialog;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FileChooserExtensionFilter;

/**
 * A panel for reading in affy data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ReadAffymetrixDataPanel extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6728156957455182944L;
    
    private static final int MAX_ROW_COUNT = 200;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ReadAffymetrixDataPanel.class.getName());

    private static final String BROWSE_ICON_RESOURCE = "/images/action/browse-16x16.png";
    private static final String PREVIEW_ICON_RESOURCE = "/images/action/preview-16x16.png";
    
    private final ReadMicroarrayDataCommandBuilder readMACommandBuilder =
        new ReadMicroarrayDataCommandBuilder();
    
    private final AffyJustRMACommandBuilder affyJustRMACommandBuilder =
        new AffyJustRMACommandBuilder();
    
    private final JDialog parentDialog;
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent dialog to use for popups etc..
     */
    public ReadAffymetrixDataPanel(JDialog parentDialog)
    {
        this.parentDialog = parentDialog;
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Perform the initialization that the netbeans GUI builder doesn't
     * take care of for us
     */
    private void postGuiInit()
    {
        this.readMACommandBuilder.setDataFileIsReallyAnObject(true);
        this.readMACommandBuilder.setMatchDataToDesign(true);
        this.readMACommandBuilder.setProbeIdColumnValid(false);
        
        this.celFilesAreCompressedCheckBox.setSelected(
                this.affyJustRMACommandBuilder.getCelFilesCompressed());
        this.quantileNormalizationCheckBox.setSelected(
                this.affyJustRMACommandBuilder.getDoQuantileNormalization());
        this.backgroundCorrectionCheckBox.setSelected(
                this.affyJustRMACommandBuilder.getDoBackgroundCorrection());
        
        // register all the listeners needed to update our command preview
        // in real time
        ItemListener updateCommandItemListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ReadAffymetrixDataPanel.this.updateRCommand();
            }
        };
        
        this.celFilesAreCompressedCheckBox.addItemListener(updateCommandItemListener);
        this.quantileNormalizationCheckBox.addItemListener(updateCommandItemListener);
        this.backgroundCorrectionCheckBox.addItemListener(updateCommandItemListener);
        
        DocumentListener updateCommandDocListener = new SimplifiedDocumentListener()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void anyUpdate(DocumentEvent e)
            {
                ReadAffymetrixDataPanel.this.updateRCommand();
            }
        };
        
        this.celFilesTextField.getDocument().addDocumentListener(updateCommandDocListener);
        this.cdfFileTextField.getDocument().addDocumentListener(updateCommandDocListener);
        this.designFileTextField.getDocument().addDocumentListener(updateCommandDocListener);
        this.dataNameTextField.getDocument().addDocumentListener(updateCommandDocListener);
        
        // respond to the browse and preview stuff
        this.browseCelFilesButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataPanel.this.browseCelFiles();
            }
        });
        
        this.browseDesignFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataPanel.this.browseDesignFiles();
            }
        });
        
        this.previewDesignFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ReadAffymetrixDataPanel.this.previewDesignFile();
            }
        });
        
        // add some image icons to the buttons
        this.browseCelFilesButton.setIcon(
                new ImageIcon(this.getClass().getResource(BROWSE_ICON_RESOURCE)));
        this.browseDesignFileButton.setIcon(
                new ImageIcon(this.getClass().getResource(BROWSE_ICON_RESOURCE)));
        this.previewDesignFileButton.setIcon(
                new ImageIcon(this.getClass().getResource(PREVIEW_ICON_RESOURCE)));
    }

    private void previewDesignFile()
    {
        CharacterDelimitedViewDialog.viewFlatFile(
                "Microarray Design Preview",
                this.readMACommandBuilder.getDesignFileName(),
                CommonFlatFileFormat.TAB_DELIMITED_UNIX,
                this.readMACommandBuilder.getFilesIncludeHeader(),
                this.parentDialog,
                MAX_ROW_COUNT);
    }

    private void browseDesignFiles()
    {
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Select Microarray Design File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int response = fileChooser.showOpenDialog(this);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile != null)
            {
                this.designFileTextField.setText(
                        selectedFile.getAbsolutePath());
                
                // update the starting directory which is the default location
                // that the file chooser opens to
                File newMicroarrayStartingDirectory =
                    selectedFile.getParentFile();
                manager.setStartingDataDirectory(newMicroarrayStartingDirectory);
            }
        }
    }

    private void browseCelFiles()
    {
        MaanovaApplicationConfigurationManager manager =
            MaanovaApplicationConfigurationManager.getInstance();
        
        JFileChooser fileChooser = new JFileChooser(
                manager.getStartingDataDirectory());
        fileChooser.setDialogTitle("Select Affymetrix CEL File");
        fileChooser.setFileFilter(new FileChooserExtensionFilter(
                "CEL",
                "Affy CEL Files (*.CEL)"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        int response = fileChooser.showOpenDialog(this);
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if(selectedFiles != null)
            {
                String[] fileNames = new String[selectedFiles.length];
                for(int i = 0; i < fileNames.length; i++)
                {
                    fileNames[i] = selectedFiles[i].getAbsolutePath();
                }
                
                this.celFilesTextField.setText(SequenceUtilities.toString(
                        Arrays.asList(fileNames),
                        ", "));
                
                // update the starting directory which is the default location
                // that the file chooser opens to
                if(selectedFiles.length >= 1)
                {
                    File newMicroarrayStartingDirectory =
                        selectedFiles[0].getParentFile();
                    manager.setStartingDataDirectory(newMicroarrayStartingDirectory);
                }
            }
        }
    }

    /**
     * Update the R command to reflect the current state of the GUI
     */
    private void updateRCommand()
    {
        String[] celFiles =
            this.celFilesTextField.getText().split(Pattern.quote(","));
        for(int i = 0; i < celFiles.length; i++)
        {
            celFiles[i] = celFiles[i].trim();
        }
        this.affyJustRMACommandBuilder.setCelFiles(celFiles);
        
        final String cdfFile = this.cdfFileTextField.getText().trim();
        this.affyJustRMACommandBuilder.setCdfFile(cdfFile);
        final boolean isCompressed = this.celFilesAreCompressedCheckBox.isSelected();
        this.affyJustRMACommandBuilder.setCelFilesCompressed(isCompressed);
        
        final boolean doQuantile = this.quantileNormalizationCheckBox.isSelected();
        this.affyJustRMACommandBuilder.setDoQuantileNormalization(doQuantile);
        
        final boolean doBackground = this.backgroundCorrectionCheckBox.isSelected();
        this.affyJustRMACommandBuilder.setDoBackgroundCorrection(doBackground);
        
        this.readMACommandBuilder.setDesignFileName(
                this.designFileTextField.getText().trim());
        
        // update the data object name
        try
        {
            String dataRIdentifier = RUtilities.fromReadableNameToRIdentifier(
                    this.dataNameTextField.getText().trim());
            this.readMACommandBuilder.setMicroarrayDataName(dataRIdentifier);
            
            if(dataRIdentifier.length() >= 1)
            {
                String rmaDataName = dataRIdentifier + ".rma_normalized";
                this.affyJustRMACommandBuilder.setResultObjectName(rmaDataName);
                this.readMACommandBuilder.setDataFileName(rmaDataName);
            }
            else
            {
                this.affyJustRMACommandBuilder.setResultObjectName(null);
                this.readMACommandBuilder.setDataFileName(null);
            }
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.FINE,
                    "can't convert readable name to an R identifier",
                    ex);
        }
        
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {
                this.affyJustRMACommandBuilder.getCommand(),
                this.readMACommandBuilder.getCommand()};
    }
    
    /**
     * Validate that all of the data in this panel is OK
     * @return
     *          true iff it's all valid
     */
    public boolean validateData()
    {
        String invalidMessage = null;
        if(this.celFilesTextField.getText().trim().length() == 0)
        {
            invalidMessage =
                "Please select input CEL files before continuing";
        }
        else if(this.designFileTextField.getText().trim().length() == 0)
        {
            invalidMessage =
                "Please select a design file before continuing";
        }
        else
        {
            String readableDataName = this.dataNameTextField.getText().trim();
            if(readableDataName.length() == 0)
            {
                invalidMessage = "Please fill in the name field before continuing";
            }
            else
            {
                invalidMessage = RUtilities.getErrorMessageForReadableName(
                        readableDataName);
            }
        }
        
        if(invalidMessage == null)
        {
            // its valid
            return true;
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    invalidMessage,
                    "Validation Failed");
            return false;
        }
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

        javax.swing.JLabel celFilesLabel = new javax.swing.JLabel();
        celFilesTextField = new javax.swing.JTextField();
        browseCelFilesButton = new javax.swing.JButton();
        javax.swing.JLabel designFileLabel = new javax.swing.JLabel();
        designFileTextField = new javax.swing.JTextField();
        browseDesignFileButton = new javax.swing.JButton();
        previewDesignFileButton = new javax.swing.JButton();
        cdfFileLabel = new javax.swing.JLabel();
        cdfFileTextField = new javax.swing.JTextField();
        celFilesAreCompressedCheckBox = new javax.swing.JCheckBox();
        quantileNormalizationCheckBox = new javax.swing.JCheckBox();
        backgroundCorrectionCheckBox = new javax.swing.JCheckBox();
        dataNameLabel = new javax.swing.JLabel();
        dataNameTextField = new javax.swing.JTextField();

        celFilesLabel.setText("CEL Files:");

        browseCelFilesButton.setText("Browse ...");

        designFileLabel.setText("Experiment Design:");

        browseDesignFileButton.setText("Browse ...");

        previewDesignFileButton.setText("Preview ...");

        cdfFileLabel.setText("CDF Package (Optional):");

        celFilesAreCompressedCheckBox.setText("CEL Files are Compressed");

        quantileNormalizationCheckBox.setText("Perform Quantile Normalization");

        backgroundCorrectionCheckBox.setText("Perform Background Correction");

        dataNameLabel.setText("Microarray Data Object Name:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(backgroundCorrectionCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(celFilesLabel)
                                    .add(cdfFileLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(celFilesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .add(designFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, cdfFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, designFileLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(browseDesignFileButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(previewDesignFileButton))
                            .add(browseCelFilesButton)))
                    .add(celFilesAreCompressedCheckBox)
                    .add(quantileNormalizationCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(dataNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dataNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 153, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(celFilesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseCelFilesButton)
                    .add(celFilesLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(designFileLabel)
                    .add(browseDesignFileButton)
                    .add(previewDesignFileButton)
                    .add(designFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cdfFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cdfFileLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(celFilesAreCompressedCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(quantileNormalizationCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(backgroundCorrectionCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dataNameLabel)
                    .add(dataNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox backgroundCorrectionCheckBox;
    private javax.swing.JButton browseCelFilesButton;
    private javax.swing.JButton browseDesignFileButton;
    private javax.swing.JLabel cdfFileLabel;
    private javax.swing.JTextField cdfFileTextField;
    private javax.swing.JCheckBox celFilesAreCompressedCheckBox;
    private javax.swing.JTextField celFilesTextField;
    private javax.swing.JLabel dataNameLabel;
    private javax.swing.JTextField dataNameTextField;
    private javax.swing.JTextField designFileTextField;
    private javax.swing.JButton previewDesignFileButton;
    private javax.swing.JCheckBox quantileNormalizationCheckBox;
    // End of variables declaration//GEN-END:variables

}
