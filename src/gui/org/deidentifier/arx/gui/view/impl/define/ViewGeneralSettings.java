/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.define;

import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.Model;
import org.deidentifier.arx.gui.model.ModelCriterion;
import org.deidentifier.arx.gui.model.ModelEvent;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.def.IView;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolder;
import org.deidentifier.arx.gui.view.impl.common.ComponentTitledFolderButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This view displays general settings regarding data transformation
 * @author Fabian Prasser
 */
public class ViewGeneralSettings implements IView {

    /** Static settings*/
    private static final int                     LABEL_WIDTH  = 50;
    /** Static settings*/
    private static final int                     LABEL_HEIGHT = 20;

    /** Controller */
    private final Controller                     controller;
    /** Model */
    private Model                                model;

    /** View */
    private Scale                                sliderOutliers;
    /** View */
    private Label                                labelOutliers;
    /** View */
    private Button                               buttonPracticalMonotonicity;
    /** View */
    private Button                               buttonProtectSensitiveAssociations;
    /** View */
    private Composite                            root;
    /** View */
    private ComponentTitledFolder                folder;
    /** View */
    private ComponentTitledFolder                folder2;
    /** View */
    private ToolItem                             enable;
    /** View */
    private ToolItem                             push;
    /** View */
    private ToolItem                             pull;
    /** View */
    private ViewCriteriaList                     clv;

    /**
     * Creates a new instance
     * @param parent
     * @param controller
     */
    public ViewGeneralSettings(final Composite parent,
                                   final Controller controller) {

        this.controller = controller;
        this.controller.addListener(ModelPart.MODEL, this);
        this.controller.addListener(ModelPart.METRIC, this);
        this.controller.addListener(ModelPart.INPUT, this);
        this.controller.addListener(ModelPart.SELECTED_ATTRIBUTE, this);
        this.controller.addListener(ModelPart.ATTRIBUTE_TYPE, this);
        this.controller.addListener(ModelPart.CRITERION_DEFINITION, this);
        this.root = build(parent);
    }

    @Override
    public void dispose() {
        controller.removeListener(this);
        clv.dispose();
    }
    
    @Override
    public void reset() {

        sliderOutliers.setSelection(0);
        labelOutliers.setText("0"); //$NON-NLS-1
        buttonPracticalMonotonicity.setSelection(false);
        SWTUtil.disable(root);
    }

    @Override
    public void update(final ModelEvent event) {
        if (event.part == ModelPart.MODEL) {
            model = (Model) event.data;
            root.setRedraw(false);
            sliderOutliers.setSelection(SWTUtil.doubleToSlider(0d, 0.999d, model.getInputConfig().getAllowedOutliers()));
            labelOutliers.setText(String.valueOf(model.getInputConfig().getAllowedOutliers()));
            buttonPracticalMonotonicity.setSelection(model.getInputConfig().isPracticalMonotonicity());
            buttonProtectSensitiveAssociations.setSelection(model.getInputConfig().isProtectSensitiveAssociations());
            
            updateControlls();
            root.setRedraw(true);
        } else if (event.part == ModelPart.INPUT) {
            SWTUtil.enable(root);
            updateControlls();
        } else if (event.part == ModelPart.SELECTED_ATTRIBUTE ||
                   event.part == ModelPart.ATTRIBUTE_TYPE ||
                   event.part == ModelPart.CRITERION_DEFINITION ||
                   event.part == ModelPart.METRIC) {
            
            if (model != null){
                updateControlls();
            }
        }
    }

    private Composite build(final Composite parent) {

        // Create input group
        Composite group = new Composite(parent, SWT.NONE);
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(2));

        // Add tab folder for criteria
        GridData gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        gd1.grabExcessHorizontalSpace = true;
        gd1.horizontalSpan = 2;
        
        ComponentTitledFolderButton bar = new ComponentTitledFolderButton("id-80");
        bar.add(Resources.getMessage("CriterionDefinitionView.59"), 
                controller.getResources().getImage("cross.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionEnable(getSelectedCriterion());
                    }
                });
        bar.add(Resources.getMessage("CriterionDefinitionView.57"), 
                controller.getResources().getImage("bullet_arrow_up.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionPush(getSelectedCriterion());
                    }
                });
        
        bar.add(Resources.getMessage("CriterionDefinitionView.58"), 
                controller.getResources().getImage("bullet_arrow_down.png"),
                new Runnable() {
                    @Override
                    public void run() {
                        controller.actionCriterionPull(getSelectedCriterion());
                    }
                });

        folder = new ComponentTitledFolder(group, controller, bar, null);
        folder.setLayoutData(gd1);
        Composite item1 = folder.createItem(Resources.getMessage("CriterionDefinitionView.19"), controller.getResources().getImage("symbol_k.png"));        
        new ViewCriterionKAnonymity(item1, controller, model);
        Composite item2 = folder.createItem(Resources.getMessage("CriterionDefinitionView.60"), controller.getResources().getImage("symbol_d.png"));
        new ViewCriterionDPresence(item2, controller, model);
        Composite item3 = folder.createItem(Resources.getMessage("CriterionDefinitionView.20"), controller.getResources().getImage("symbol_l.png"));
        new ViewCriterionLDiversity(item3, controller, model);
        Composite item4 = folder.createItem(Resources.getMessage("CriterionDefinitionView.21"), controller.getResources().getImage("symbol_t.png"));
        new ViewCriterionTCloseness(item4, controller, model);
        folder.setSelection(0);
        
        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                updateControlls();
            }
        });
        
        enable = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.59"));
        push = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.57"));
        pull = folder.getButtonItem(Resources.getMessage("CriterionDefinitionView.58"));
        
        enable.setEnabled(false);
        push.setEnabled(false);
        pull.setEnabled(false);
      
        // Add general view
        gd1 = SWTUtil.createFillGridData();
        gd1.grabExcessVerticalSpace = false;
        gd1.horizontalSpan = 2;
        folder2 = new ComponentTitledFolder(parent, controller, null, "id-60");
        folder2.setLayoutData(gd1);
        
        // Create general tab
        group = folder2.createItem(Resources.getMessage("CriterionDefinitionView.61"), null);  //$NON-NLS-1$
        group.setLayoutData(SWTUtil.createFillGridData());
        group.setLayout(SWTUtil.createGridLayout(3, false));

        // Create outliers slider
        final Label sLabel = new Label(group, SWT.PUSH);
        sLabel.setText(Resources.getMessage("CriterionDefinitionView.11")); //$NON-NLS-1$

        labelOutliers = new Label(group, SWT.BORDER | SWT.CENTER);
        GridData d2 = new GridData();
        d2.minimumWidth = LABEL_WIDTH;
        d2.widthHint = LABEL_WIDTH;
        d2.heightHint = LABEL_HEIGHT;
        labelOutliers.setLayoutData(d2);
        labelOutliers.setText("0"); //$NON-NLS-1$

        sliderOutliers = new Scale(group, SWT.HORIZONTAL);
        sliderOutliers.setLayoutData(SWTUtil.createFillHorizontallyGridData());
        sliderOutliers.setMaximum(SWTUtil.SLIDER_MAX);
        sliderOutliers.setMinimum(0);
        sliderOutliers.setSelection(0);
        sliderOutliers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setAllowedOutliers(SWTUtil.sliderToDouble(0d,
                                                            1d,
                                                            sliderOutliers.getSelection()));
                labelOutliers.setText(String.valueOf(model.getInputConfig()
                                                             .getAllowedOutliers()));
                if (model.getInputConfig().getAllowedOutliers() != 0) {
                    buttonPracticalMonotonicity.setEnabled(true);
                } else {
                    buttonPracticalMonotonicity.setSelection(false);
                    buttonPracticalMonotonicity.setEnabled(false);
                    model.getInputConfig().setPracticalMonotonicity(false);
                }
            }
        });

        // Build approximate button
        final Label m2Label = new Label(group, SWT.PUSH);
        m2Label.setText(Resources.getMessage("CriterionDefinitionView.31")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        m2Label.setLayoutData(d2);

        final GridData d82 = SWTUtil.createFillHorizontallyGridData();
        d82.horizontalSpan = 2;
        buttonPracticalMonotonicity = new Button(group, SWT.CHECK);
        buttonPracticalMonotonicity.setText(Resources.getMessage("CriterionDefinitionView.53")); //$NON-NLS-1$
        buttonPracticalMonotonicity.setSelection(false);
        buttonPracticalMonotonicity.setEnabled(false);
        buttonPracticalMonotonicity.setLayoutData(d82);
        buttonPracticalMonotonicity.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig()
                     .setPracticalMonotonicity(buttonPracticalMonotonicity.getSelection());
            }
        });

        // Build protect sensitive associations button
        final Label m3Label = new Label(group, SWT.PUSH);
        m3Label.setText(Resources.getMessage("CriterionDefinitionView.54")); //$NON-NLS-1$
        d2 = new GridData();
        d2.heightHint = LABEL_HEIGHT;
        d2.minimumHeight = LABEL_HEIGHT;
        m3Label.setLayoutData(d2);

        final GridData d83 = SWTUtil.createFillHorizontallyGridData();
        d83.horizontalSpan = 2;
        buttonProtectSensitiveAssociations = new Button(group, SWT.CHECK);
        buttonProtectSensitiveAssociations.setText(Resources.getMessage("CriterionDefinitionView.55")); //$NON-NLS-1$
        buttonProtectSensitiveAssociations.setSelection(true);
        buttonProtectSensitiveAssociations.setEnabled(false);
        buttonProtectSensitiveAssociations.setLayoutData(d83);
        buttonProtectSensitiveAssociations.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                model.getInputConfig().
                     setProtectSensitiveAssociations(buttonProtectSensitiveAssociations.getSelection());
            }
        });
        
        // Create metrics tab
        Composite composite1 = folder2.createItem(Resources.getMessage("CriterionDefinitionView.66"), null);  //$NON-NLS-1$
        composite1.setLayout(new FillLayout());
        new ViewMetric(composite1, controller, folder2);
        
        // Create overview tab
        Composite c = folder2.createItem(Resources.getMessage("CriterionDefinitionView.62"), null);  //$NON-NLS-1$
        c.setLayout(new FillLayout());
        clv = new ViewCriteriaList(c, controller);
        
        // Select first and finish
        folder2.setSelection(0);
        return group;
    }

    /**
     * Returns the currently selected criterion
     * @return
     */
    private ModelCriterion getSelectedCriterion() {
        ModelCriterion mc = null;
        if (folder.getSelectionIndex()==0){
            mc = model.getKAnonymityModel();
        } else if (folder.getSelectionIndex()==1){
            mc = model.getDPresenceModel();
        } else if (folder.getSelectionIndex()==2){
            mc = model.getLDiversityModel().get(model.getSelectedAttribute());
        } else if (folder.getSelectionIndex()==3){
            mc = model.getTClosenessModel().get(model.getSelectedAttribute());
        }
        return mc;
    }
    
    /**
     * This method adjusts the toolbar attached to the folder with criteria
     * according to the current state of the model
     */
    private void updateControlls(){

        root.setRedraw(false);
        ModelCriterion mc = null;
        
        // K-Anonymity
        if (folder.getSelectionIndex()==0){
            push.setEnabled(false);
            pull.setEnabled(false);
            mc = model.getKAnonymityModel();
            
        // D-Presence
        } else if (folder.getSelectionIndex()==1){
            push.setEnabled(false);
            pull.setEnabled(false);
            mc = model.getDPresenceModel();
            
        // L-Diversity
        } else if (folder.getSelectionIndex()==2){
            mc = model.getLDiversityModel().get(model.getSelectedAttribute());
            if (mc != null && mc.isActive() && mc.isEnabled()){
                push.setEnabled(true);
                pull.setEnabled(true);
            } else {
                push.setEnabled(false);
                pull.setEnabled(false);
            }
            
        // T-Closeness
        } else if (folder.getSelectionIndex()==3){
            mc = model.getTClosenessModel().get(model.getSelectedAttribute());
            push.setEnabled(true);
            pull.setEnabled(true);
            if (mc != null && mc.isActive() && mc.isEnabled()){
                push.setEnabled(true);
                pull.setEnabled(true);
            } else {
                push.setEnabled(false);
                pull.setEnabled(false);
            }
        }
        
        if (mc == null){
            root.setRedraw(true);
            return;
        }
        
        if (mc.isActive()) {
            enable.setEnabled(true);
            if (mc.isEnabled()) {
                enable.setImage(controller.getResources().getImage("tick.png")); //$NON-NLS-1
            } else {
                enable.setImage(controller.getResources().getImage("cross.png")); //$NON-NLS-1
            }
        } else {
            enable.setEnabled(false);
            enable.setImage(controller.getResources().getImage("cross.png")); //$NON-NLS-1
        }
        
        root.setRedraw(true);
    }
}