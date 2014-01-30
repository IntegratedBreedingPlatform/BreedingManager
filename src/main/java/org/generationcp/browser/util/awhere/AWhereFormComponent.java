package org.generationcp.browser.util.awhere;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.generationcp.browser.util.awhere.json.pojos.SeasonProfile;
import org.generationcp.browser.util.awhere.json.pojos.SeasonProfileResult;
import org.generationcp.browser.util.awhere.json.pojos.TenYearAverage;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.gson.Gson;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

@Configurable
public class AWhereFormComponent extends AbsoluteLayout  implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private AWhereUtil aWhereUtil;
	
	private TextField latitude;
	private TextField longitude;
	private DateField plantingDate;
	private DateField harvestDate;
	private Button getData;
	
	private TextArea jsonResult;
	private TextField locationName;
	private Table seasonProfileTable;
	private Table tenYearAverageTable;
	
	@Override
	public void updateLabels() {
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		setWidth("100%");
		setHeight("900px");
		setMargin(false);
		
		SimpleDateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		Label header = new Label("<h1>AWhere Test Tool</h1>", Label.CONTENT_XHTML);
		
		latitude = new TextField();
		latitude.setCaption("Latitude");
		latitude.setValue("-1.5089");

		longitude = new TextField();
		longitude.setCaption("Longitude");
		longitude.setValue("37.2948");
		
		plantingDate = new DateField();
		plantingDate.setCaption("Planting Date");
		plantingDate.setValue(dmyFormat.parse("03-01-2013"));
		plantingDate.setDateFormat("dd-MM-yyyy");
		plantingDate.setWidth("100px");
		
		harvestDate = new DateField();
		harvestDate.setCaption("Harvest Date");
		harvestDate.setValue(dmyFormat.parse("15-05-2013"));
		harvestDate.setDateFormat("dd-MM-yyyy");
		harvestDate.setWidth("100px");
		
		getData = new Button();
		getData.setCaption("Get Weather/Season Data");
		getData.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				getDataAction();
			}
		});
	
		jsonResult = new TextArea();
		jsonResult.setWidth("800px");
		jsonResult.setHeight("120px");
		jsonResult.setCaption("AWhere JSON Result");

		locationName = new TextField();
		locationName.setCaption("Location Name");
		
		seasonProfileTable = new Table();
		seasonProfileTable.setCaption("Season Profile Table");
		seasonProfileTable.setHeight("180px");
		seasonProfileTable.addContainerProperty("PERIOD FROM", String.class, "");
		seasonProfileTable.addContainerProperty("PERIOD TO", String.class, "");
		seasonProfileTable.addContainerProperty("AVG MAX TEMP", Double.class, "");
		seasonProfileTable.addContainerProperty("AVG MIN TEMP", Double.class, "");
		seasonProfileTable.addContainerProperty("TOTAL RAIN", Double.class, "");
		seasonProfileTable.addContainerProperty("AVG SOLAR", Double.class, "");
		seasonProfileTable.addContainerProperty("TOTAL GDD", Double.class, "");
		seasonProfileTable.addContainerProperty("COUNT", Integer.class, "");
		
		tenYearAverageTable = new Table();
		tenYearAverageTable.setCaption("Ten Year Average Table");
		tenYearAverageTable.setHeight("180px");
		tenYearAverageTable.addContainerProperty("PERIOD FROM", String.class, "");
		tenYearAverageTable.addContainerProperty("PERIOD TO", String.class, "");
		tenYearAverageTable.addContainerProperty("AVG MAX TEMP", Double.class, "");
		tenYearAverageTable.addContainerProperty("AVG MIN TEMP", Double.class, "");
		tenYearAverageTable.addContainerProperty("TOTAL RAIN", Double.class, "");
		tenYearAverageTable.addContainerProperty("AVG SOLAR", Double.class, "");
		tenYearAverageTable.addContainerProperty("TOTAL GDD", Double.class, "");
		tenYearAverageTable.addContainerProperty("COUNT", Integer.class, "");
		
		addComponent(header, "top:0px; left:30px;");
		addComponent(latitude, "top:95px; left: 30px;");
		addComponent(longitude, "top:95px; left: 200px;");
		addComponent(plantingDate, "top:95px; left: 390px;");
		addComponent(harvestDate, "top:95px; left: 500px;");
		addComponent(getData, "top:95px; left: 650px;");
		addComponent(jsonResult, "top:160px; left: 30px;");
		addComponent(locationName, "top:310; left: 30px;");
		addComponent(seasonProfileTable, "top:360px; left:30px;");
		addComponent(tenYearAverageTable, "top:570px; left:30px;");
		
		
		initializeAWhere();
		
	}

	
	private void initializeAWhere() throws Exception {
		aWhereUtil = new AWhereUtil();
		aWhereUtil.authenticate();
	}
	
	private void getDataAction() {
		
		jsonResult.setValue("");
		locationName.setValue("");
		seasonProfileTable.removeAllItems();
		tenYearAverageTable.removeAllItems();
		
		try {
			String jsonString = aWhereUtil.getSeason(Double.valueOf(latitude.getValue().toString()), 
													 Double.valueOf(longitude.getValue().toString()), 
													 (Date) plantingDate.getValue(), 
													 (Date) harvestDate.getValue());
			
			jsonString = jsonString.trim();
			
			if(jsonString.charAt(0)=='[' && jsonString.charAt(jsonString.length()-1)==']')
				jsonString = jsonString.substring(1, jsonString.length()-1);
			
			jsonResult.setValue(jsonString);
			
			Gson gson = new Gson();
			SeasonProfileResult seasonProfileResult = gson.fromJson(jsonString, SeasonProfileResult.class);
			
			locationName.setValue(seasonProfileResult.getLocationName());
			
			List<SeasonProfile> seasonProfiles = seasonProfileResult.getSeason_Profile();
			
			Integer i = 0;
			for(SeasonProfile s : seasonProfiles){
				seasonProfileTable.addItem(new Object[] {s.getPeriodFrom(), s.getPeriodTo(), s.getAvg_MaxTemp(), s.getAvg_MinTemp(), s.getAvg_Rain(), s.getAvg_Solar(), s.getTotal_GDD(), s.getCount()}, i);
				i++;
			}
			
			List<TenYearAverage> tenYearAverages = seasonProfileResult.getTenYearAverage();
			i = 0;
			for(TenYearAverage t : tenYearAverages){
				tenYearAverageTable.addItem(new Object[] {t.getPeriodFrom(), t.getPeriodTo(), t.getAvg_MaxTemp(), t.getAvg_MinTemp(), t.getAvg_Rain(), t.getAvg_Solar(), t.getTotal_GDD(), t.getCount()}, i);
				i++;
			}
			
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

