package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTreeTable;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent.FolderSaveMode;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Created by EfficioDaniel on 9/29/2014.
 */
public class ListSelectorComponentTest {

    private SimpleResourceBundleMessageSource messageSource;
    private ListSelectorComponent listSelectorComponent;
    private GermplasmListManager germplasmListManager;


    @Before
    public void setUp(){
        messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
        listSelectorComponent = Mockito.mock(ListSelectorComponent.class);
        germplasmListManager = Mockito.mock(GermplasmListManager.class);
    }
	
    @Test
    public void testReturnTrueIfGermplasmListIdIsFolder() throws MiddlewareQueryException {
        Integer itemId = new Integer(5);
        ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

        GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
        Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
        Mockito.when(germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
        listManagerTreeComponent.setGermplasmListManager(germplasmListManager);
        Assert.assertTrue("Expecting a true when the germplasm list that was retrieve using the item id is a folder ", listManagerTreeComponent.isFolder(itemId));
    }

    @Test
    public void testReturnFalseIfGermplasmListIdIsNotAFolder() throws MiddlewareQueryException {
        Integer itemId = new Integer(5);
        ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

        GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
        Mockito.when(germplasmList.getType()).thenReturn("Not Folder");
        Mockito.when(germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);
        listManagerTreeComponent.setGermplasmListManager(germplasmListManager);
        Assert.assertFalse("Expecting a false when the germplasm list that was retrieve using the item id is not a folder ", listManagerTreeComponent.isFolder(itemId));
    }
    
    @Test 
    public void testDoAddItemReturnTrueIfGermplasmListIsAFolderAndDoShowFoldersOnlyIsFalse() throws MiddlewareQueryException{
    	Integer itemId = new Integer(5);
        ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

        GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
        Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
        Mockito.when(germplasmListManager.getGermplasmListById(itemId)).thenReturn(germplasmList);        
        listManagerTreeComponent.setGermplasmListManager(germplasmListManager);
        Assert.assertTrue("Expecting a true when the germplasm list that was retrieve using the item id is a folder and the setting doShowFolderOnly is false", listManagerTreeComponent.doAddItem(germplasmList));
    }

    @Test
    public void testAddGermplasmListNodeUsingAParentGermplasmListId() throws MiddlewareQueryException{
    	Integer parentGermplasmListId = new Integer(5);
    	Integer childGermplasmListId = new Integer(20);
        ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
        
        UserDataManager userDataManager = Mockito.mock(UserDataManager.class);        
        GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
        
        Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
        Mockito.when(germplasmList.getId()).thenReturn(childGermplasmListId);
        
        List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();
        germplasmListChildren.add(germplasmList);
        Mockito.when(germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, ListSelectorComponent.BATCH_SIZE)).thenReturn(germplasmListChildren);
        
        listManagerTreeComponent.instantiateGermplasmListSourceComponent();
        listManagerTreeComponent.setGermplasmListManager(germplasmListManager);
        listManagerTreeComponent.setUserDataManager(userDataManager);
        listManagerTreeComponent.addGermplasmListNode(parentGermplasmListId);        
        
        Assert.assertNotNull("Returns same child germplasm list for the germplasm list that was added in the list source", listManagerTreeComponent.getGermplasmListSource().getItem(childGermplasmListId));
    }
    
    @Test
    public void testRenameGermplasmListFolderIsSuccess() throws MiddlewareQueryException{
    	String newFolderName = "New Folder Name";
    	Integer germplasmListTreeId = new Integer(5);
        ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();

        TextField folderTextField = new TextField();
        folderTextField.setValue(newFolderName);
        UserDataManager userDataManager = Mockito.mock(UserDataManager.class);        
        GermplasmList germplasmList = Mockito.mock(GermplasmList.class);
        GermplasmListTreeTable treeTable = Mockito.mock(GermplasmListTreeTable.class);
        
        Mockito.when(germplasmList.getType()).thenReturn(AppConstants.DB.FOLDER);
        Mockito.when(germplasmListManager.getGermplasmListById(germplasmListTreeId)).thenReturn(germplasmList);
               
        listManagerTreeComponent.instantiateGermplasmListSourceComponent();
        listManagerTreeComponent.setGermplasmListSource(treeTable);
        listManagerTreeComponent.setGermplasmListManager(germplasmListManager);        
        listManagerTreeComponent.setMessageSource(messageSource);
        listManagerTreeComponent.setUserDataManager(userDataManager);
       
        listManagerTreeComponent.setFolderSaveMode(FolderSaveMode.RENAME);
        listManagerTreeComponent.setFolderTextField(folderTextField);

        listManagerTreeComponent.instantiateComponents();
        listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(germplasmListManager);
        listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(messageSource);
        
        Object[] treeTableInfo = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
        listManagerTreeComponent.getGermplasmListSource().addItem(treeTableInfo, germplasmListTreeId);
        String newName = listManagerTreeComponent.getGermplasmListTreeUtil().renameFolderOrList(germplasmListTreeId, listManagerTreeComponent.getTreeActionsListener(), folderTextField, "Test");
        
        Assert.assertEquals("Returns correct folder name when the user is renaming a folder in the tree", " " + newFolderName, newName);
    }

    @Test
    public void testDeletionOfFolderNodeInTheTree(){
    	Integer germplasmListId = new Integer(5);
    	GermplasmList germplasmList = new GermplasmList();
    	
    	germplasmList.setId(germplasmListId);
    	ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
    	listManagerTreeComponent.setGermplasmListManager(germplasmListManager);
    	listManagerTreeComponent.setMessageSource(messageSource);
        listManagerTreeComponent.instantiateComponents();
        listManagerTreeComponent.getGermplasmListTreeUtil().setGermplasmListManager(germplasmListManager);
        listManagerTreeComponent.getGermplasmListTreeUtil().setMessageSource(messageSource);
                
    	Object[] info = listManagerTreeComponent.generateCellInfo("Test", "Owner", "description", "listType", "25");
        listManagerTreeComponent.getGermplasmListSource().addItem(info, germplasmListId);
    	listManagerTreeComponent.removeListFromTree(germplasmList);
    	Assert.assertNull("Should not return an object since the folder in the tree was deleted already", listManagerTreeComponent.getGermplasmListSource().getItem(germplasmListId));
    }
    
    @Test
    public void testMoveGermplasmListIfSourceIsLocalRootNode(){
    	GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

    	Mockito.when(listSelectorComponent.getWindow()).thenReturn(new Window());
    	
    	treeUtil.setSource(listSelectorComponent);
    	treeUtil.setMessageSource(messageSource);
    	
    	String sourceItemId = ListSelectorComponent.LOCAL;
    	String targetItemId = ListSelectorComponent.CENTRAL;
    	boolean result = treeUtil.setParent(sourceItemId,targetItemId);
    	Assert.assertFalse("Should not be able to move ROOT Local folder", result);
    }
    @Test
    public void testMoveGermplasmListIfSourceIsPublicRootNode(){
    	GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

    	Mockito.when(listSelectorComponent.getWindow()).thenReturn(new Window());
    	
    	treeUtil.setSource(listSelectorComponent);
    	treeUtil.setMessageSource(messageSource);
    	
    	String sourceItemId = ListSelectorComponent.CENTRAL;
    	String targetItemId = ListSelectorComponent.LOCAL;
    	boolean result = treeUtil.setParent(sourceItemId,targetItemId);
    	Assert.assertFalse("Should not be able to move ROOT Public folder", result);
    }
    @Test
    public void testMoveGermplasmListIfSourceIsAChildPublicNode(){
    	GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

    	Mockito.when(listSelectorComponent.getWindow()).thenReturn(new Window());
    	
    	treeUtil.setSource(listSelectorComponent);
    	treeUtil.setMessageSource(messageSource);
    	
    	String sourceItemId = "1";
    	String targetItemId = ListSelectorComponent.LOCAL;
    	boolean result = treeUtil.setParent(sourceItemId,targetItemId);
    	Assert.assertFalse("Should not be able to move Child Public folder", result);
    }
    @Test
    public void testMoveGermplasmListIfSourceIsAChildLocalNode() throws MiddlewareQueryException{
        //start: setup for the scenario
    	GermplasmListTreeUtil treeUtil = new GermplasmListTreeUtil();

    	Integer sourceItemId = new Integer(-11);
    	Integer targetItemId = new Integer(-12);
    	
    	Mockito.when(listSelectorComponent.getWindow()).thenReturn(new Window());
                
        Mockito.when(germplasmListManager.getGermplasmListById(sourceItemId)).thenReturn(Mockito.mock(GermplasmList.class));
        Mockito.when(germplasmListManager.getGermplasmListById(targetItemId)).thenReturn(Mockito.mock(GermplasmList.class));
        
        treeUtil.setTargetListSource(Mockito.mock(GermplasmListSource.class));
        treeUtil.setGermplasmListManager(germplasmListManager);
    	treeUtil.setSource(listSelectorComponent);
    	treeUtil.setMessageSource(messageSource);
        //end: setup for the scenario

    	boolean result = treeUtil.setParent(sourceItemId,targetItemId);
    	
    	Assert.assertTrue("Should be able to move Child to any children local folder", result);
    }
    
}
