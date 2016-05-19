
package org.generationcp.breeding.manager.germplasm;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.breeding.manager.germplasm.inventory.InventoryViewComponent;
import org.generationcp.breeding.manager.germplasm.pedigree.GermplasmPedigreeGraphComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ComponentTree;
import org.generationcp.commons.vaadin.ui.ComponentTreeItem;
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
public class GermplasmDetailsComponentTree extends VerticalLayout implements InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = -424140540302043647L;

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
	private InventoryViewComponent inventoryViewComponent;
	private GermplasmListComponent listsComponent;
	private GermplasmStudyInfoComponent studiesComponent;
	private GermplasmGenerationHistoryComponent generationHistoryComponent;
	private GermplasmManagementNeighborsComponent managementNeighborsComponent;
	private GermplasmDerivativeNeighborhoodComponent derivativeNeighborhoodComponent;
	private GermplasmMaintenanceNeighborhoodComponent maintenanceNeighborhoodComponent;
	private GermplasmGroupRelativesComponent groupRelativesComponent;

	private final Integer gid;
	private final GermplasmQueries germplasmQueries;
	private GermplasmDetailModel germplasmDetailModel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public GermplasmDetailsComponentTree(Integer gid, GermplasmQueries germplasmQueries) {
		this.gid = gid;
		this.germplasmQueries = germplasmQueries;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	private void initializeComponents() {

		this.componentTree = new ComponentTree();
		this.componentTree.setSizeFull();
		this.componentTree.addStyleName("component-tree");
		this.componentTree.addStyleName("germplasm-details");

		this.germplasmDetailModel = this.germplasmQueries.getGermplasmDetails(this.gid);
		this.basicDetailsComponent = new GermplasmCharacteristicsComponent(this.germplasmDetailModel);
		this.basicDetailsTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.CHARACTERISTICS_LABEL)));
		this.basicDetailsTreeItem.showChild();
		this.basicDetailsTreeItem.addChild(this.basicDetailsComponent);

		this.attributesTreeItem =
				this.componentTree
						.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource.getMessage(Message.ATTRIBUTES_LABEL)));
		this.tempAttributesChild = this.attributesTreeItem.addChild(new Label());
		this.pedigreeTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.PEDIGREE_TREE_LABEL)));
		this.tempPedigreeTreeItemChild = this.pedigreeTreeItem.addChild(new Label());
		this.namesTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource.getMessage(Message.NAMES_LABEL)));
		this.tempNamesChild = this.namesTreeItem.addChild(new Label());
		this.inventoryInformationTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.INVENTORY_INFORMATION_LABEL)));
		this.tempInventoryChild = this.inventoryInformationTreeItem.addChild(new Label());
		this.listsTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource.getMessage(Message.LISTS_LABEL)));
		this.tempListsTreeItemChild = this.listsTreeItem.addChild(new Label());
		this.studiesTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource.getMessage(Message.STUDIES)));
		this.tempStudiesTreeItemChild = this.studiesTreeItem.addChild(new Label());
		this.generationHistoryTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.GENERATION_HISTORY_LABEL)));
		this.tempGenerationHistoryTreeItemChild = this.generationHistoryTreeItem.addChild(new Label());
		this.managementNeighborsTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.MANAGEMENT_NEIGHBORS_LABEL)));
		this.tempManagementNeighborsTreeItemChild = this.managementNeighborsTreeItem.addChild(new Label());
		this.derivativeNeighborhoodTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.DERIVATIVE_NEIGHBORHOOD_LABEL)));
		this.tempDerivativeNeighborhoodTreeItemChild = this.derivativeNeighborhoodTreeItem.addChild(new Label());
		this.maintenanceNeighborhoodTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.MAINTENANCE_NEIGHBORHOOD_LABEL)));
		this.tempMaintenanceNeighborhoodTreeItemChild = this.maintenanceNeighborhoodTreeItem.addChild(new Label());
		this.groupRelativesTreeItem =
				this.componentTree.addChild(ComponentTreeItem.createHeaderComponent(this.messageSource
						.getMessage(Message.GROUP_RELATIVES_LABEL)));
		this.tempGroupRelativesTreeItemChild = this.groupRelativesTreeItem.addChild(new Label());
	}

	private void initializeValues() {
		this.attributesComponent = null;
		this.pedigreeTreeComponent = null;
		this.namesComponent = null;
		this.inventoryViewComponent = null;
		this.listsComponent = null;
		this.studiesComponent = null;
		this.generationHistoryComponent = null;
		this.managementNeighborsComponent = null;
		this.derivativeNeighborhoodComponent = null;
		this.maintenanceNeighborhoodComponent = null;
		this.groupRelativesComponent = null;
	}

	private void addListeners() {
		this.basicDetailsTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.basicDetailsTreeItem.toggleChild();
				}
			}
		});

		this.attributesTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showAttributes();
					GermplasmDetailsComponentTree.this.attributesTreeItem.toggleChild();
				}
			}
		});

		this.attributesTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showAttributes();
			}
		});

		this.pedigreeTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showPedigreeTree();
					GermplasmDetailsComponentTree.this.pedigreeTreeItem.toggleChild();
				}
			}
		});

		this.pedigreeTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showPedigreeTree();
			}
		});

		this.namesTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showNames();
					GermplasmDetailsComponentTree.this.namesTreeItem.toggleChild();
				}
			}
		});

		this.namesTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showNames();
			}
		});

		this.inventoryInformationTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showInventoryInformation();
					GermplasmDetailsComponentTree.this.inventoryInformationTreeItem.toggleChild();
				}
			}
		});

		this.inventoryInformationTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showInventoryInformation();
			}
		});

		this.listsTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showLists();
					GermplasmDetailsComponentTree.this.listsTreeItem.toggleChild();
				}
			}
		});

		this.listsTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showLists();
			}
		});

		this.studiesTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showStudies();
					GermplasmDetailsComponentTree.this.studiesTreeItem.toggleChild();
				}
			}
		});

		this.studiesTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showStudies();
			}
		});

		this.generationHistoryTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showGenerationHistory();
					GermplasmDetailsComponentTree.this.generationHistoryTreeItem.toggleChild();
				}
			}
		});

		this.generationHistoryTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showGenerationHistory();
			}
		});

		this.managementNeighborsTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showManagementNeighbors();
					GermplasmDetailsComponentTree.this.managementNeighborsTreeItem.toggleChild();
				}
			}
		});

		this.managementNeighborsTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showManagementNeighbors();
			}
		});

		this.derivativeNeighborhoodTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showDerivativeNeighborhood();
					GermplasmDetailsComponentTree.this.derivativeNeighborhoodTreeItem.toggleChild();
				}
			}
		});

		this.derivativeNeighborhoodTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showDerivativeNeighborhood();
			}
		});

		this.maintenanceNeighborhoodTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showMaintenanceNeighborhood();
					GermplasmDetailsComponentTree.this.maintenanceNeighborhoodTreeItem.toggleChild();
				}
			}
		});

		this.maintenanceNeighborhoodTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showMaintenanceNeighborhood();
			}
		});

		this.groupRelativesTreeItem.addListener(new LayoutClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getRelativeY() < GermplasmDetailsComponentTree.TOGGABLE_Y_COORDINATE) {
					GermplasmDetailsComponentTree.this.showGroupRelatives();
					GermplasmDetailsComponentTree.this.groupRelativesTreeItem.toggleChild();
				}
			}
		});

		this.groupRelativesTreeItem.addExpanderClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6108554806619975288L;

			@Override
			public void buttonClick(ClickEvent event) {
				GermplasmDetailsComponentTree.this.showGroupRelatives();
			}
		});
	}

	private void showAttributes() {
		if (this.attributesComponent == null) {
			this.attributesComponent =
					new GermplasmAttributesComponent(new GermplasmIndexContainer(this.germplasmQueries), this.germplasmDetailModel);
			this.attributesTreeItem.removeChild(this.tempAttributesChild);
			this.attributesTreeItem.addChild(this.attributesComponent);
		}
	}

	private void showPedigreeTree() {
		if (this.pedigreeTreeComponent == null) {
			this.pedigreeTreeComponent = new GermplasmPedigreeTreeContainer(this.gid, this.germplasmQueries, this);
			this.pedigreeTreeItem.removeChild(this.tempPedigreeTreeItemChild);
			this.pedigreeTreeItem.addChild(this.pedigreeTreeComponent);
		}
	}

	private void showNames() {
		if (this.namesComponent == null) {
			this.namesComponent =
					new GermplasmNamesComponent(new GermplasmIndexContainer(this.germplasmQueries), this.germplasmDetailModel);
			this.namesTreeItem.removeChild(this.tempNamesChild);
			this.namesTreeItem.addChild(this.namesComponent);
		}
	}

	private void showInventoryInformation() {
		if (this.inventoryViewComponent == null) {
			this.inventoryViewComponent = new InventoryViewComponent(null, null, this.gid);
			this.inventoryInformationTreeItem.removeChild(this.tempInventoryChild);
			this.inventoryInformationTreeItem.addChild(this.inventoryViewComponent);
		}
	}

	private void showLists() {
		if (this.listsComponent == null) {
			this.listsComponent = new GermplasmListComponent(this.gid, true);
			this.listsTreeItem.removeChild(this.tempListsTreeItemChild);
			this.listsTreeItem.addChild(this.listsComponent);
		}
	}

	private void showStudies() {
		if (this.studiesComponent == null) {
			this.studiesComponent =
					new GermplasmStudyInfoComponent(new GermplasmIndexContainer(this.germplasmQueries), this.germplasmDetailModel, true);
			this.studiesTreeItem.removeChild(this.tempStudiesTreeItemChild);
			this.studiesTreeItem.addChild(this.studiesComponent);
		}
	}

	private void showGenerationHistory() {
		if (this.generationHistoryComponent == null) {
			this.generationHistoryComponent =
					new GermplasmGenerationHistoryComponent(new GermplasmIndexContainer(this.germplasmQueries), this.germplasmDetailModel);
			this.generationHistoryTreeItem.removeChild(this.tempGenerationHistoryTreeItemChild);
			this.generationHistoryTreeItem.addChild(this.generationHistoryComponent);
		}
	}

	private void showManagementNeighbors() {
		if (this.managementNeighborsComponent == null) {
			this.managementNeighborsComponent = new GermplasmManagementNeighborsComponent(this.gid);
			this.managementNeighborsTreeItem.removeChild(this.tempManagementNeighborsTreeItemChild);
			this.managementNeighborsTreeItem.addChild(this.managementNeighborsComponent);
		}
	}

	private void showDerivativeNeighborhood() {
		if (this.derivativeNeighborhoodComponent == null) {
			this.derivativeNeighborhoodComponent =
					new GermplasmDerivativeNeighborhoodComponent(this.gid, this.germplasmQueries, new GermplasmIndexContainer(
							this.germplasmQueries), null, null);
			this.derivativeNeighborhoodTreeItem.removeChild(this.tempDerivativeNeighborhoodTreeItemChild);
			this.derivativeNeighborhoodTreeItem.addChild(this.derivativeNeighborhoodComponent);
		}
	}

	private void showMaintenanceNeighborhood() {
		if (this.maintenanceNeighborhoodComponent == null) {
			this.maintenanceNeighborhoodComponent =
					new GermplasmMaintenanceNeighborhoodComponent(this.gid, this.germplasmQueries, new GermplasmIndexContainer(
							this.germplasmQueries), null, null);
			this.maintenanceNeighborhoodTreeItem.removeChild(this.tempMaintenanceNeighborhoodTreeItemChild);
			this.maintenanceNeighborhoodTreeItem.addChild(this.maintenanceNeighborhoodComponent);
		}
	}

	private void showGroupRelatives() {
		if (this.groupRelativesComponent == null) {
			this.groupRelativesComponent = new GermplasmGroupRelativesComponent(this.gid);
			this.groupRelativesTreeItem.removeChild(this.tempGroupRelativesTreeItemChild);
			this.groupRelativesTreeItem.addChild(this.groupRelativesComponent);
		}
	}

	private void layoutComponents() {
		this.setSizeFull();
		this.addComponent(this.componentTree);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void showPedigreeGraphWindow() {
		Window pedigreeGraphWindow = new BaseSubWindow("Pedigree Graph");
		pedigreeGraphWindow.setModal(true);
		pedigreeGraphWindow.setWidth("100%");
		pedigreeGraphWindow.setHeight("620px");
		pedigreeGraphWindow.setName("Pedigree Graph");
		pedigreeGraphWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		pedigreeGraphWindow.addComponent(new GermplasmPedigreeGraphComponent(this.gid, this.germplasmQueries));
		this.getWindow().addWindow(pedigreeGraphWindow);
	}
}
