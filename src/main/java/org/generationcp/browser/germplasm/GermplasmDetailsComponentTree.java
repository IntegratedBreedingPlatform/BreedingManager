package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.pedigree.GermplasmPedigreeGraphComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ComponentTree;
import org.generationcp.commons.vaadin.ui.ComponentTreeItem;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmDetailsComponentTree extends VerticalLayout implements
		InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = -424140540302043647L;
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmDetailsComponentTree.class);
	public static final int TOGGABLE_Y_COORDINATE = 30;
	
	private ComponentTree componentTree;
	private ComponentTreeItem basicDetailsTreeItem;
	private ComponentTreeItem attributesTreeItem;
	private ComponentTreeItem pedigreeTreeItem;
	private ComponentTreeItem namesTreeItem;
	private ComponentTreeItem inventoryInformationTreeItem;
	private ComponentTreeItem listsTreeItem;
	private ComponentTreeItem studiesTreeItem;
	private ComponentTreeItem generationHistoryTreeItem;
	private ComponentTreeItem managementNeighborsTreeItem;
	private ComponentTreeItem derivativeNeighborhoodTreeItem;
	private ComponentTreeItem maintenanceNeighborhoodTreeItem;
	private ComponentTreeItem groupRelativesTreeItem;
	
	private ComponentTreeItem tempAttributesChild;
	private ComponentTreeItem tempPedigreeTreeItemChild;
	private ComponentTreeItem tempNamesChild;
	private ComponentTreeItem tempInventoryChild;
	private ComponentTreeItem tempListsTreeItemChild;
	private ComponentTreeItem tempStudiesTreeItemChild;
	private ComponentTreeItem tempGenerationHistoryTreeItemChild;
	private ComponentTreeItem tempManagementNeighborsTreeItemChild;
	private ComponentTreeItem tempDerivativeNeighborhoodTreeItemChild;
	private ComponentTreeItem tempMaintenanceNeighborhoodTreeItemChild;
	private ComponentTreeItem tempGroupRelativesTreeItemChild;
	
	private GermplasmCharacteristicsComponent basicDetailsComponent;
	private GermplasmAttributesComponent attributesComponent;
	private GermplasmPedigreeTreeContainer pedigreeTreeComponent;
	private GermplasmNamesComponent namesComponent;
	private InventoryInformationComponent inventoryComponent;
	private GermplasmListComponent listsComponent;
	private GermplasmStudyInfoComponent studiesComponent;
	private GermplasmGenerationHistoryComponent generationHistoryComponent;
	private GermplasmManagementNeighborsComponent managementNeighborsComponent;
	private GermplasmDerivativeNeighborhoodComponent derivativeNeighborhoodComponent;
	private GermplasmMaintenanceNeighborhoodComponent maintenanceNeighborhoodComponent;
	private GermplasmGroupRelativesComponent groupRelativesComponent;
	
	private Integer gid;
	private GermplasmQueries germplasmQueries;
	private GermplasmDetailModel germplasmDetailModel;
	
	@Autowired
    private GermplasmDataManager germplasmManager;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public GermplasmDetailsComponentTree(Integer gid, GermplasmQueries germplasmQueries){
		this.gid = gid;
		this.germplasmQueries = germplasmQueries;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		initializeComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	private void initializeComponents(){
		
		componentTree = new ComponentTree();
		componentTree.setWidth("100%");
		componentTree.setHeight("390px");
		componentTree.addStyleName("overflow_y_scroll");
		componentTree.addStyleName("component-tree");
		
		germplasmDetailModel = germplasmQueries.getGermplasmDetails(gid);
		basicDetailsComponent = new GermplasmCharacteristicsComponent(germplasmDetailModel);
		basicDetailsTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.CHARACTERISTICS_LABEL)));
		basicDetailsTreeItem.showChild();
		basicDetailsTreeItem.addChild(basicDetailsComponent);
		
		attributesTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.ATTRIBUTES_LABEL)));
		tempAttributesChild = attributesTreeItem.addChild(new Label());
		pedigreeTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.PEDIGREE_TREE_LABEL)));
		tempPedigreeTreeItemChild = pedigreeTreeItem.addChild(new Label());
		namesTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.NAMES_LABEL)));
		tempNamesChild = namesTreeItem.addChild(new Label());
		inventoryInformationTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.INVENTORY_INFORMATION_LABEL)));
		tempInventoryChild = inventoryInformationTreeItem.addChild(new Label());
		listsTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.LISTS_LABEL)));
		tempListsTreeItemChild = listsTreeItem.addChild(new Label());
		studiesTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.STUDIES)));
		tempStudiesTreeItemChild = studiesTreeItem.addChild(new Label());
		generationHistoryTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.GENERATION_HISTORY_LABEL)));
		tempGenerationHistoryTreeItemChild = generationHistoryTreeItem.addChild(new Label());
		managementNeighborsTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.MANAGEMENT_NEIGHBORS_LABEL)));
		tempManagementNeighborsTreeItemChild = managementNeighborsTreeItem.addChild(new Label());
		derivativeNeighborhoodTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.DERIVATIVE_NEIGHBORHOOD_LABEL)));
		tempDerivativeNeighborhoodTreeItemChild = derivativeNeighborhoodTreeItem.addChild(new Label());
		maintenanceNeighborhoodTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.MAINTENANCE_NEIGHBORHOOD_LABEL)));
		tempMaintenanceNeighborhoodTreeItemChild = maintenanceNeighborhoodTreeItem.addChild(new Label());
		groupRelativesTreeItem = componentTree.addChild(ComponentTreeItem.createHeaderComponent(messageSource.getMessage(Message.GROUP_RELATIVES_LABEL)));
		tempGroupRelativesTreeItemChild = groupRelativesTreeItem.addChild(new Label());
	}
	
	private void initializeValues(){
		attributesComponent = null;
		pedigreeTreeComponent = null;
		namesComponent = null;
		inventoryComponent = null;
		listsComponent = null;
		studiesComponent = null;
		generationHistoryComponent = null;
		managementNeighborsComponent = null;
		derivativeNeighborhoodComponent = null;
		maintenanceNeighborhoodComponent = null;
		groupRelativesComponent = null;
	}
	
	private void addListeners(){
		basicDetailsTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					basicDetailsTreeItem.toggleChild();
				}
			}
        });
		
		attributesTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showAttributes();
					attributesTreeItem.toggleChild();
				}
			}
        });
		
		attributesTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showAttributes();
			}
		});
		
		pedigreeTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showPedigreeTree();
					pedigreeTreeItem.toggleChild();
				}
			}
        });
		
		pedigreeTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showPedigreeTree();
			}
		});
		
		namesTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showNames();
					namesTreeItem.toggleChild();
				}
			}
        });
		
		namesTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showNames();
			}
		});

		inventoryInformationTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showInventoryInformation();
					inventoryInformationTreeItem.toggleChild();
				}
			}
        });
		
		inventoryInformationTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showInventoryInformation();
			}
		});
		
		listsTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showLists();
					listsTreeItem.toggleChild();
				}
			}
        });
		
		listsTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showLists();
			}
		});

		studiesTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showStudies();
					studiesTreeItem.toggleChild();
				}
			}
        });
		
		studiesTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showStudies();
			}
		});
		
		generationHistoryTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showGenerationHistory();
					generationHistoryTreeItem.toggleChild();
				}
			}
        });
		
		generationHistoryTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showGenerationHistory();
			}
		});
		
		managementNeighborsTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showManagementNeighbors();
					managementNeighborsTreeItem.toggleChild();
				}
			}
        });
		
		managementNeighborsTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showManagementNeighbors();
			}
		});
		
		derivativeNeighborhoodTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showDerivativeNeighborhood();
					derivativeNeighborhoodTreeItem.toggleChild();
				}
			}
        });
		
		derivativeNeighborhoodTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showDerivativeNeighborhood();
			}
		});
		
		maintenanceNeighborhoodTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showMaintenanceNeighborhood();
					maintenanceNeighborhoodTreeItem.toggleChild();
				}
			}
        });
		
		maintenanceNeighborhoodTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showMaintenanceNeighborhood();
			}
		});
		
		groupRelativesTreeItem.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				if(event.getRelativeY()< TOGGABLE_Y_COORDINATE){
					showGroupRelatives();
					groupRelativesTreeItem.toggleChild();
				}
			}
        });
		
		groupRelativesTreeItem.addExpanderClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				showGroupRelatives();
			}
		});
	}
	
	private void showAttributes(){
		if(attributesComponent == null){
			attributesComponent = new GermplasmAttributesComponent(new GermplasmIndexContainer(germplasmQueries), germplasmDetailModel);
			attributesTreeItem.removeChild(tempAttributesChild);
			attributesTreeItem.addChild(attributesComponent);
		}
	}
	
	private void showPedigreeTree(){
		if(pedigreeTreeComponent == null){
			pedigreeTreeComponent = new GermplasmPedigreeTreeContainer(gid, germplasmQueries, this);
			pedigreeTreeItem.removeChild(tempPedigreeTreeItemChild);
			pedigreeTreeItem.addChild(pedigreeTreeComponent);
		}
	}
	
	private void showNames(){
		if(namesComponent == null){
			namesComponent = new GermplasmNamesComponent(new GermplasmIndexContainer(germplasmQueries), germplasmDetailModel);
			namesTreeItem.removeChild(tempNamesChild);
			namesTreeItem.addChild(namesComponent);
		}
	}
	
	private void showInventoryInformation(){
		if(inventoryComponent == null){
			inventoryComponent = new InventoryInformationComponent(new GermplasmIndexContainer(germplasmQueries), germplasmDetailModel);
			inventoryInformationTreeItem.removeChild(tempInventoryChild);
			inventoryInformationTreeItem.addChild(inventoryComponent);
		}
	}
	
	private void showLists(){
		if(listsComponent == null){
			listsComponent = new GermplasmListComponent(gid, true);
			listsTreeItem.removeChild(tempListsTreeItemChild);
			listsTreeItem.addChild(listsComponent);
		}
	}
	
	private void showStudies(){
		if(studiesComponent == null){
			studiesComponent = new GermplasmStudyInfoComponent(new GermplasmIndexContainer(germplasmQueries), germplasmDetailModel, true);
			studiesTreeItem.removeChild(tempStudiesTreeItemChild);
			studiesTreeItem.addChild(studiesComponent);
		}
	}
	
	private void showGenerationHistory(){
		if(generationHistoryComponent == null){
			generationHistoryComponent = new GermplasmGenerationHistoryComponent(new GermplasmIndexContainer(germplasmQueries), germplasmDetailModel);
			generationHistoryTreeItem.removeChild(tempGenerationHistoryTreeItemChild);
			generationHistoryTreeItem.addChild(generationHistoryComponent);
		}
	}
	
	private void showManagementNeighbors(){
		if(managementNeighborsComponent == null){
			managementNeighborsComponent = new GermplasmManagementNeighborsComponent(gid);
			managementNeighborsTreeItem.removeChild(tempManagementNeighborsTreeItemChild);
			managementNeighborsTreeItem.addChild(managementNeighborsComponent);
		}
	}
	
	private void showDerivativeNeighborhood(){
		if(derivativeNeighborhoodComponent == null){
			derivativeNeighborhoodComponent = new GermplasmDerivativeNeighborhoodComponent(gid, germplasmQueries, new GermplasmIndexContainer(germplasmQueries), null, null);
			derivativeNeighborhoodTreeItem.removeChild(tempDerivativeNeighborhoodTreeItemChild);
			derivativeNeighborhoodTreeItem.addChild(derivativeNeighborhoodComponent);
		}
	}
	
	private void showMaintenanceNeighborhood(){
		if(maintenanceNeighborhoodComponent == null){
			maintenanceNeighborhoodComponent = new GermplasmMaintenanceNeighborhoodComponent(gid, germplasmQueries, new GermplasmIndexContainer(germplasmQueries), null, null);
			maintenanceNeighborhoodTreeItem.removeChild(tempMaintenanceNeighborhoodTreeItemChild);
			maintenanceNeighborhoodTreeItem.addChild(maintenanceNeighborhoodComponent);
		}
	}
	
	private void showGroupRelatives(){
		if(groupRelativesComponent == null){
			groupRelativesComponent = new GermplasmGroupRelativesComponent(gid);
			groupRelativesTreeItem.removeChild(tempGroupRelativesTreeItemChild);
			groupRelativesTreeItem.addChild(groupRelativesComponent);
		}
	}
	
	private void layoutComponents(){
		addComponent(componentTree);
	}
	
	@Override
	public void updateLabels() {
	
	}
	
	public void showPedigreeGraphWindow() {
		Window pedigreeGraphWindow = new Window("Pedigree Graph");
		pedigreeGraphWindow.setModal(true);
		pedigreeGraphWindow.setWidth("100%");
		pedigreeGraphWindow.setHeight("620px");
		pedigreeGraphWindow.setName("Pedigree Graph");
		pedigreeGraphWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		pedigreeGraphWindow.addComponent(new GermplasmPedigreeGraphComponent(gid, germplasmQueries));
		getWindow().addWindow(pedigreeGraphWindow);
    }
}
