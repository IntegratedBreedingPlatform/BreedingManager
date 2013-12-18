package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.pojos.GermplasmInventory;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.report.LotReportRow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListInventoryComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
    private Table listDataInventoryTable;


    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private InventoryDataManager inventoryDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;

    private int germplasmListId;

    private static final String GERMPLASM_INVENTORY_ENTITY_ID = "entityid";
    private static final String GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE = "lotbalance";
    private static final String GERMPLASM_INVENTORY_LOCATION_NAME = "location";
    private static final String GERMPLASM_INVENTORY_SCALE_NAME = "scale";
    private static final String GERMPLASM_INVENTORY_LOT_COMMENT = "lotcomment";

    public ListInventoryComponent(int germplasmListId){
    	this.germplasmListId = germplasmListId;
    }
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<GermplasmListData> listData = new ArrayList<GermplasmListData>();
        List<Integer> EntityIdList=new ArrayList<Integer>();
        long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
        if (listDataCount == 0) {
            addComponent(new Label(messageSource.getMessage(Message.NO_LISTDATA_INVENTORY_RETRIEVED_LABEL))); // "No Germplasm List Data retrieved."
        } else {

            listData = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, (int) listDataCount);

            for(GermplasmListData g:listData){
                EntityIdList.add(g.getGid());
            }

            List<LotReportRow> lotReportRowData = inventoryDataManager.generateReportOnLotsByEntityTypeAndEntityId("GERMPLSM", EntityIdList, 0, listData.size());
            
            if(lotReportRowData.size()==0){
                addComponent(new Label(messageSource.getMessage(Message.NO_LISTDATA_INVENTORY_RETRIEVED_LABEL)));
            }else{

                listDataInventoryTable = new Table("");
                listDataInventoryTable.setColumnCollapsingAllowed(true);
                listDataInventoryTable.setColumnReorderingAllowed(true);
                listDataInventoryTable.setPageLength(15); // number of rows to display in the Table
                listDataInventoryTable.setSizeFull(); // to make scrollbars appear on the Table component
                listDataInventoryTable.setWidth("95%");
                listDataInventoryTable.setHeight("95%");

                listDataInventoryTable.addContainerProperty(GERMPLASM_INVENTORY_ENTITY_ID, Integer.class, "");
                listDataInventoryTable.addContainerProperty(GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE, Long.class, "");
                listDataInventoryTable.addContainerProperty(GERMPLASM_INVENTORY_LOCATION_NAME, String.class, "");
                listDataInventoryTable.addContainerProperty(GERMPLASM_INVENTORY_SCALE_NAME, String.class, "");
                listDataInventoryTable.addContainerProperty(GERMPLASM_INVENTORY_LOT_COMMENT, String.class, "");

                messageSource.setColumnHeader(listDataInventoryTable, GERMPLASM_INVENTORY_ENTITY_ID, Message.ENTITY_ID_HEADER);
                messageSource.setColumnHeader(listDataInventoryTable, GERMPLASM_INVENTORY_ACTUAL_LOT_BALANCE, Message.LOT_BALANCE_HEADER);
                messageSource.setColumnHeader(listDataInventoryTable, GERMPLASM_INVENTORY_LOCATION_NAME, Message.LOCATION_HEADER);
                messageSource.setColumnHeader(listDataInventoryTable, GERMPLASM_INVENTORY_SCALE_NAME, Message.SCALE_HEADER);
                messageSource.setColumnHeader(listDataInventoryTable, GERMPLASM_INVENTORY_LOT_COMMENT, Message.LOT_COMMENT_HEADER);
                
                // sum of lot balancer per germplasm ID
                Map<Integer, GermplasmInventory> inventoryMap = new HashMap<Integer, GermplasmInventory>();
                for (LotReportRow lotReportRow : lotReportRowData) {
                    Integer entityId = lotReportRow.getEntityIdOfLot();
                    if (inventoryMap.containsKey(entityId)){
                    	GermplasmInventory germplasmInventory = inventoryMap.get(entityId);
                    	germplasmInventory.addInventory(lotReportRow.getActualLotBalance());
                    } else {
                    	inventoryMap.put(entityId, new GermplasmInventory(entityId, 
                    			lotReportRow.getActualLotBalance(), 
                    			(lotReportRow.getLocationOfLot() == null) ? null : lotReportRow.getLocationOfLot().getLname(), 
            					lotReportRow.getScaleOfLot() == null ? null : lotReportRow.getScaleOfLot().getName(), 
    							lotReportRow.getCommentOfLot()));
                    }
                }
                
                for (GermplasmInventory germplasmInventory : inventoryMap.values()){
					listDataInventoryTable.addItem(new Object[] {germplasmInventory.getGid(), 
							germplasmInventory.getBalance(), germplasmInventory.getLocation(),
                            germplasmInventory.getScale(), germplasmInventory.getComment()
                        }, germplasmInventory.getGid()
					);
                }
                addComponent(listDataInventoryTable);
            }
        }
		
	}
	
	@Override
    public void attach() {
        super.attach();
        updateLabels();
    }

	
}
