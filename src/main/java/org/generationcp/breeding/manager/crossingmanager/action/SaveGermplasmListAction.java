package org.generationcp.breeding.manager.crossingmanager.action;

import java.io.Serializable;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean {
	
	private static final long serialVersionUID = 1L;

	@Autowired
    private GermplasmListManager germplasmListManager;
    
	private GermplasmList germplasmList;
	
    public SaveGermplasmListAction(GermplasmList germplasmList){
    	this.germplasmList = germplasmList;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public GermplasmList saveGermplasmListRecord(GermplasmList germplasmList) throws MiddlewareQueryException {
        int newListId = this.germplasmListManager.addGermplasmList(germplasmList);
        GermplasmList list = this.germplasmListManager.getGermplasmListById(newListId);
        
        return list;
    }
}
