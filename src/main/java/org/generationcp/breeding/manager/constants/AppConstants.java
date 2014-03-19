package org.generationcp.breeding.manager.constants;

import com.vaadin.terminal.ThemeResource;

/**
 * Contains constants used in the Breeding Manager project
 *
 */
public final class AppConstants {
	
	private AppConstants(){
		//make static by making non-instantiable
	}
	
	public static class CssStyles {
		
		public static final String GRAY_PANEL_WITH_BORDER = "list-manager-search-bar";
		public static final String GRAY_ROUNDED_BORDER = "gray-rounded-border";
		public static final String HORIZONTAL_GROUP = "horizontalgroup";
		public static final String ITALIC = "italic";
		public static final String BOLD = "bold";
		public static final String POPUP_VIEW = "gcp-popup-view";
		
		public static final String TREE_ROOT_NODE = "listManagerTreeRootNode";
		public static final String TREE_REGULAR_PARENT_NODE = "listManagerTreeRegularParentNode";
		public static final String TREE_REGULAR_CHILD_NODE = "listManagerTreeRegularChildNode";
		
	}
	
	public static class Icons {
		
		public static final ThemeResource TRASH_ICON = new ThemeResource("images/trash-icon-blue.png");
		public static final ThemeResource TRASH_ICON_GRAY_BG = new ThemeResource("images/bluetrash-icon-graybg.png");
		public static final String POPUP_VIEW_ICON = "?";
		
	}
	
	public static class DB {
		public static final String FOLDER = "FOLDER";
	}

}
