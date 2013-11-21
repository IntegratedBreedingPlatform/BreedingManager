package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.listmanager.SearchResultsComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

public class SearchResultsItemClickListener implements ItemClickListener {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsItemClickListener.class);
	private static final long serialVersionUID = 1267325300705610720L;

	private String resultType;
	private ListManagerDetailsLayout detailsLayout;

    public SearchResultsItemClickListener(String resultType, ListManagerDetailsLayout detailsLayout) {
        this.resultType = resultType;
        this.detailsLayout = detailsLayout;
    }
    
	@Override
	public void itemClick(ItemClickEvent event) {
		Integer itemId = (Integer) event.getItemId();
		
		if(!event.isCtrlKey() && !event.isShiftKey()){
			try {
				
				if (resultType.equals(SearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA)){
					detailsLayout.createGermplasmInfoTab(itemId);
					
				} else if (resultType.equals(SearchResultsComponent.MATCHING_LISTS_TABLE_DATA)){
					detailsLayout.createListInfoFromSearchScreen(itemId);
				}
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
		}

	}

}
