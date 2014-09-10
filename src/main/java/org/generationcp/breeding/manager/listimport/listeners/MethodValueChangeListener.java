package org.generationcp.breeding.manager.listimport.listeners;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;

/**
 * Created with IntelliJ IDEA.
 * User: Efficio.Daniel
 * Date: 8/15/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MethodValueChangeListener implements  Property.ValueChangeListener{
	private static final long serialVersionUID = 1L;
	
	ComboBox comboBox;
    Map<String, String> comboBoxMap = new HashMap<String,String>();
    public MethodValueChangeListener(ComboBox comboBox, Map<String,String> comboBoxMap){
        this.comboBox = comboBox;
        this.comboBoxMap = comboBoxMap;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
        comboBox.setDescription(comboBoxMap.get(event.getProperty().toString()));

    }
}
