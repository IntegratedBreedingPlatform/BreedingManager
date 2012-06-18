/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.study.application;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Factor;
import org.generationcp.middleware.pojos.Representation;
import org.generationcp.middleware.pojos.StudyEffect;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.VerticalLayout;
/**
 * 
 * @author Macky
 *
 */
public class StudyEffectComponent extends VerticalLayout {
	
    private final static Logger log = LoggerFactory.getLogger(StudyEffectComponent.class);
	private static final long serialVersionUID = 116672292965099233L;
	
	private StudyDataManager studyDataManager;
	private Tree effectTree;
	
	public StudyEffectComponent(StudyDataManager studyDataManager, int studyId, final Accordion accordion) {
		
		this.studyDataManager = studyDataManager;
		final Accordion studyInfoAccordion = accordion;
		final StudyDataManager managerToPass = studyDataManager;
		final Integer studyIdToPass = studyId;
		
		List<StudyEffect> effects = new ArrayList<StudyEffect>();
		try {
			 effects = studyDataManager.getEffectsByStudyID(studyId);
		} catch (QueryException e) {
			//Put in an application log
			log.error(e.toString() + "\n" + e.getStackTrace());

			//TODO an error window in the UI should pop-up for this
			System.out.println(e);
			e.printStackTrace();
		}
		
		if (effects.isEmpty()) {
			addComponent(new Label("No effects retrieved."));
		} else {
			this.effectTree = new Tree();
			
			for (StudyEffect effect : effects) {
				Integer effectId = effect.getId().getEffectId();
				String effectIdStr = "EFF-" + effectId;
				this.effectTree.addItem(effectIdStr);
				this.effectTree.setItemCaption(effectIdStr, "Effect " + effectId.toString() + " - " + effect.getName());
			}
			
			effectTree.addListener(new Tree.ExpandListener() {
				
				public void nodeExpand(ExpandEvent event) {
					// TODO Auto-generated method stub
					addRepAndFactorNodes(event.getItemId().toString());
				}
			});
			
			effectTree.addListener(new ItemClickEvent.ItemClickListener()
			{
				private static final long serialVersionUID = -3385392211937392824L;

				public void itemClick(ItemClickEvent event)
				{
					if(event.getButton()==ItemClickEvent.BUTTON_LEFT && event.isDoubleClick())
					{
						String itemId = event.getItemId().toString();
						if(itemId.contains("REP-"))
						{
							//respond only to double clicks on representation nodes
							//display dataset for that representation
							Integer repId = Integer.valueOf(itemId.replaceAll("REP-", ""));
							Tree source = (Tree) event.getComponent();
						
							String repName = source.getItemCaption(itemId);
							String tabTitle = "Dataset of " + repName;
							
							if(!Util.isAccordionDatasetExist(accordion,tabTitle)){
								RepresentationDatasetComponent datasetComponent = new RepresentationDatasetComponent(managerToPass, repId, tabTitle, studyIdToPass);
								studyInfoAccordion.addTab(datasetComponent, tabTitle);
								studyInfoAccordion.setSelectedTab(datasetComponent);
							}else{
								// open the representation dataset tab already exist if the user click the same representation dataset 
								for(int i=3;i<studyInfoAccordion.getComponentCount();i++){
									Tab tab=studyInfoAccordion.getTab(i);
									if(tab.getCaption().equals(tabTitle)){
										studyInfoAccordion.setSelectedTab(tab.getComponent());
										break;
										
									}
								}
								
							}
						}
					}
				}
			});
			
			addComponent(this.effectTree);
		}
		
	}
	
	private void addRepAndFactorNodes(String idString) {
		
		if (idString.contains("EFF-")) {
			int effectId = Integer.valueOf(idString.replaceAll("EFF-", ""));
			
			List<Representation> reps = new ArrayList<Representation>();
			
			try {
				reps = studyDataManager.getRepresentationByEffectID(effectId);
			} catch (QueryException e) {
				//Put in an application log
				log.error(e.toString() + "\n" + e.getStackTrace());

				//TODO an error window in the UI should pop-up for this
				System.out.println(e);
				e.printStackTrace();
			}
			
			for (Representation rep : reps) {
				int repId = rep.getId();
				String repIdStr = "REP-" + repId;
				this.effectTree.addItem(repIdStr);
				this.effectTree.setItemCaption(repIdStr, "Representation " + rep.getName());
				this.effectTree.setParent(repIdStr, idString);
			}
		} else if (idString.contains("REP-")) {
			int repId = Integer.valueOf(idString.replaceAll("REP-", ""));
			
			List<Factor> factors = new ArrayList<Factor>();
			
			try {
				factors = studyDataManager.getFactorsByRepresentationId(repId);
			} catch (QueryException e) {
				//Put in an application log
				log.error(e.toString() + "\n" + e.getStackTrace());

				//TODO an error window in the UI should pop-up for this
				System.out.println(e);
				e.printStackTrace();
			}
			
			for (Factor factor : factors) {
				int labelId = factor.getId();
				String labelIdStr = "FAC-" + labelId + idString;
				this.effectTree.addItem(labelIdStr);
				this.effectTree.setItemCaption(labelIdStr, factor.getName());
				this.effectTree.setParent(labelIdStr, idString);
				this.effectTree.setChildrenAllowed(labelIdStr, false);
			}
		}
	}
	
}
