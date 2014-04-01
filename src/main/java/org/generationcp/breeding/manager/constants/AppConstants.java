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
		
		public static final String MARKED_MANDATORY = "marked_mandatory";
		
		public static final String PANEL_GRAY_BACKGROUND = "section_panel_layout";
		
	}
	
	
	public static class Icons {
		
		public static final ThemeResource TRASH_ICON = new ThemeResource("images/trash-icon-blue.png");
		public static final ThemeResource TRASH_ICON_GRAY_BG = new ThemeResource("images/bluetrash-icon-graybg.png");
		public static final String POPUP_VIEW_ICON = "?";
		public static final ThemeResource ICON_TOOLS = new ThemeResource("images/tools.png");
	    public static final ThemeResource ICON_PLUS = new ThemeResource("images/plus_icon.png");
	    public static final ThemeResource ICON_TOOGLE = new ThemeResource("images/toogle_icon.PNG");
	    public static final ThemeResource ICON_BUILD_NEW_LIST = new ThemeResource("images/build-new-list.png");
	    public static final ThemeResource ICON_REVIEW_LIST_DETAILS = new ThemeResource("images/review-list-details.png");
	    public static final ThemeResource ICON_MATCHING_GERMPLASMS = new ThemeResource("images/matching-germplasms.png");
	    public static final ThemeResource ICON_MANAGE_SETTINGS = new ThemeResource("images/manage-settings.png");
	    public static final ThemeResource ICON_ARROW = new ThemeResource("images/arrow_icon.png"); 
	}
	
	
	public static class DB {
		public static final String FOLDER = "FOLDER";
	}

}
