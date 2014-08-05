package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.pojos.Name;

public class NameNotExistsForGermplasmActionListener implements ImportGermplasmEntryActionListener, ConfirmDialog.Listener {

	public static final String WINDOW_NAME = "New Name";
	public static final String ADD_NAME_TO_GID = "Add name to GID";
	public static final String SEARCH_OR_CREATE_NEW = "Search/create another germplasm record";
	
	private static final long serialVersionUID = 3381474046997205167L;
	
	private  String germplasmName;
	private int germplasmIndex;
	private Integer ibdbUserId;
	private Integer dateIntValue;
	private Integer gid;
	private Integer nameMatchesCount;
	
	private ProcessImportedGermplasmAction source;
	
	public NameNotExistsForGermplasmActionListener(ProcessImportedGermplasmAction source){
		this.source = source;
	}
	
	
	public Integer getGid() {
		return gid;
	}

	public Integer getIbdbUserId() {
		return ibdbUserId;
	}

	public Integer getDateIntValue() {
		return dateIntValue;
	}

	@Override
	public String getGermplasmName() {
		return germplasmName;
	}

	@Override
	public int getGermplasmIndex() {
		return germplasmIndex;
	}

	public Integer getNameMatchesCount() {
		return nameMatchesCount;
	}

	public void setNameMatchesCount(Integer nameMatchesCount) {
		this.nameMatchesCount = nameMatchesCount;
	}

	public void setGermplasmName(String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public void setGermplasmIndex(int germplasmIndex) {
		this.germplasmIndex = germplasmIndex;
	}

	public void setIbdbUserId(Integer ibdbUserId) {
		this.ibdbUserId = ibdbUserId;
	}

	public void setDateIntValue(Integer dateIntValue) {
		this.dateIntValue = dateIntValue;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	@Override
	public void onClose(ConfirmDialog dialog) {
		 if (dialog.isConfirmed()) {
         	addGermplasmName(getGermplasmName(),getGid(), getIbdbUserId(), getDateIntValue());
         } else{
         	source.searchOrAddANewGermplasm(this);
         }
		
	}
	
    private void addGermplasmName(String desig, Integer gid, Integer ibdbUserId, Integer dateIntValue){
		Name name = source.createNameObject(ibdbUserId, dateIntValue, desig);
		
		name.setNid(null);
		name.setNstat(Integer.valueOf(0));
		name.setGermplasmId(gid);
		
		source.addNameToGermplasm(name, gid);
		
		source.removeCurrentListenerAndProcessNextItem(this);
    }
    
    public String getConfirmationMessage(){
    	return "The name \"" + getGermplasmName() + "\" is not recorded as a name of GID " + getGid() + "."
	            + " Do you want to add the name to the GID or search/create another germplasm record?";
    }


}
