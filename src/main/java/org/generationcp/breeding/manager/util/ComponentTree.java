package org.generationcp.breeding.manager.util;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class ComponentTree extends CssLayout {

	private static final long serialVersionUID = 740158017459466809L;
	private final ThemeResource ICON_COLLAPSED = new ThemeResource("img/componenttree-node-collapsed.png");
	private final ThemeResource ICON_EXPANDED = new ThemeResource("img/componenttree-node-expanded.png");

	public ComponentTree () {
		super();
	}
	
	public ComponentTreeItem addChild (Component c) {
		ComponentTreeItem i = new ComponentTreeItem(c);
		addComponent(i);
		return i;
	}
	
	public class ComponentTreeItem extends CssLayout implements ClickListener {
		
		private static final long serialVersionUID = -1436990419814629056L;
		private Button expander;
		private boolean expanded = false;
		private CssLayout children;
		
		public ComponentTreeItem (Component c) {
			super();
			this.expander = new Button();
			this.expander.setIcon(ICON_COLLAPSED);
			this.expander.addStyleName(Button.STYLE_LINK);
			this.expander.addStyleName("componenttree-expander");
			this.expander.addListener(this);
			this.expander.setVisible(false);
			
			this.children = new CssLayout();
			this.children.setVisible(false);
			this.children.setStyleName("componenttree-children");
			
			this.setStyleName("componenttree-child");
			this.addComponent(this.expander);
			this.addComponent(c);
			this.addComponent(this.children);
		}
		
		public ComponentTreeItem addChild (Component c) {
			ComponentTreeItem i = new ComponentTreeItem(c);
			this.expander.setVisible(true);
			this.children.addComponent(i);
			this.setStyleName("componenttree-parent");
			return i;
		}
		
		public void removeChild (ComponentTreeItem c) {
			this.children.removeComponent(c);
		}
		
		public void buttonClick(ClickEvent event) {
			this.expanded = !this.expanded;
			this.expander.setIcon(this.expanded ? ICON_EXPANDED : ICON_COLLAPSED);
			this.children.setVisible(this.expanded);
		}
		
		public void setExpanded(boolean expanded){
			this.expanded = expanded;
		}
		
		public boolean getExpanded(){
			return this.expanded;
		}
		
		public void showChild(){
			this.expanded = !this.expanded;
			this.expander.setIcon(this.expanded ? ICON_EXPANDED : ICON_COLLAPSED);
			this.children.setVisible(this.expanded);
		}
		
	}
}
