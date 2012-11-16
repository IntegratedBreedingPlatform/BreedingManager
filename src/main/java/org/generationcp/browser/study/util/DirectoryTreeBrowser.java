/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/


package org.generationcp.browser.study.util;

import java.io.File;

import org.generationcp.browser.exception.GermplasmStudyBrowserException;
import org.generationcp.browser.study.SaveRepresentationDatasetExcelDialog;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.util.MessageNotifier;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Tree.ExpandEvent;

/**
 * Browsable directory explorer using Vaadin Tree component.
 *
 * Based on http://dev.vaadin.com/svn/versions/6.0/src/com/vaadin/tests/TreeFilesystem.java
 *
 * @author Joyce Avestro
 *
 */
public class DirectoryTreeBrowser extends VerticalLayout {


    private static final long serialVersionUID = 1L;
    
    private final String UP_ONE_FOLDER = ". .";

    private final Panel explorerPanel = new Panel("File System Explorer");
    private final Tree tree = new Tree();

    private SaveRepresentationDatasetExcelDialog caller;
    
    private String path;

    public DirectoryTreeBrowser(SaveRepresentationDatasetExcelDialog caller) {
        this(caller, null);
    }

    public DirectoryTreeBrowser(SaveRepresentationDatasetExcelDialog caller, String path) {
        this.caller = caller;
        this.path = path;
        init();
    }

    @SuppressWarnings("deprecation")
    public void init() {

        explorerPanel.addComponent(tree);
        explorerPanel.setHeight(400);

        tree.addListener(new DirectoryTreeExpandListener()); 
        tree.addListener(new DirectoryClickListener());
        addComponent(explorerPanel);

        createDirectoryTree();

    }

    /**
     * Creates the directory tree. Initial directory is based on (1) input, (2) desktop, (3) application directory
     */
    private void createDirectoryTree() {
        File currentDirectory;
        try {

            if (path != null && !path.equals("")) {
                currentDirectory = Util.getDefaultBrowseDirectory(path);
            } else {
                currentDirectory = Util.getDefaultBrowseDirectory(caller.getMainApplication());
            }
            // populate tree's root node with the current directory
            if (currentDirectory != null) {
                populateNode(currentDirectory.getAbsolutePath(), null);
            }
        } catch (GermplasmStudyBrowserException e) {
            MessageNotifier.showError(caller.getWindow(), "Error", e.getMessage());
            ((SaveRepresentationDatasetExcelDialog) caller).closeFileSystemWindow();
        }
    }
    
    public void recreateDirectoryTree(){
        tree.removeAllItems();
        createDirectoryTree();
        
    }
    /**
     * Populates files to tree as items. In this example items are of String
     * type that consist of file path. New items are added to tree and item's
     * parent and children properties are updated.
     *
     * @param file
     *            path which contents are added to tree
     * @param parent
     *            for added nodes, if null then new nodes are added to root node
     */
    private void populateNode(String file, Object parent) {
        final File subdir = new File(file);
        final File[] files = subdir.listFiles();

        tree.addItem(UP_ONE_FOLDER); // up one directory
        tree.setChildrenAllowed(UP_ONE_FOLDER, false);
        
        String path = "";
        try {
            
            // add the current directory as the root of the tree
            if (subdir.isDirectory() && subdir.canRead()) {
                path = subdir.getCanonicalPath().toString();
                tree.addItem(path);
                tree.setChildrenAllowed(path, true);
            } else {
                tree.setChildrenAllowed(path, false);
                return;
            }

            for (int x = 0; x < files.length; x++) {
                // add new item (String) to tree
                path = files[x].getCanonicalPath().toString();

                // check if item is a directory and read access exists
                if (files[x].isDirectory() && files[x].canRead()) {
                    
                  tree.addItem(path);
                  // set parent if this item has one
                  if (parent != null) {
                      tree.setParent(path, parent);
                  }
                    tree.setChildrenAllowed(path, true);
                } else {
                    tree.setChildrenAllowed(path, false);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    

    /**
     * Handles expand action of directory tree items
     * 
     */    
    private class DirectoryTreeExpandListener implements Tree.ExpandListener{

        private static final long serialVersionUID = 1L;

        /**
         * Handle tree expand event, populate expanded node's childs with new files
         * and directories.
         */
        public void nodeExpand(ExpandEvent event) {
            final Item i = tree.getItem(event.getItemId());
            if (!tree.hasChildren(i)) {
                // populate tree's node which was expanded
                populateNode(event.getItemId().toString(), event.getItemId());
            }
        }
        
    }
    
    
    /**
     * Handles directory item click event
     *
     */
    private class DirectoryClickListener implements ItemClickEvent.ItemClickListener {

        private static final long serialVersionUID = 1L;

        @Override
        public void itemClick(ItemClickEvent event) {
            
            String value = event.getItemId().toString();

            if (event.isDoubleClick()){
                
                if (value.equals(UP_ONE_FOLDER)){   // Up one directory
                    path = Util.getOneFolderUp(path);
                    recreateDirectoryTree();
                    requestRepaintAll();
                } else {                            // Choose the clicked directory as the target folder
                    caller.setDestinationFolderValue(value);
                    caller.closeFileSystemWindow();
                }
                
            }
        }
        
    }

}