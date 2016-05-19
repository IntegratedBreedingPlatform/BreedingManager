
package org.generationcp.breeding.manager.util.awhere;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.util.awhere.json.pojos.SeasonProfile;
import org.generationcp.breeding.manager.util.awhere.json.pojos.SeasonProfileResult;
import org.generationcp.breeding.manager.util.awhere.json.pojos.TenYearAverage;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import com.google.gson.Gson;

@Configurable
@Deprecated
public class AWhereFormComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

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

		this.setWidth("100%");
		this.setHeight("900px");
		this.setMargin(false);

		SimpleDateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");

		Label header = new Label("<h1>AWhere Test Tool</h1>", Label.CONTENT_XHTML);

		this.latitude = new TextField();
		this.latitude.setCaption("Latitude");
		this.latitude.setValue("-1.5089");

		this.longitude = new TextField();
		this.longitude.setCaption("Longitude");
		this.longitude.setValue("37.2948");

		this.plantingDate = new DateField();
		this.plantingDate.setCaption("Planting Date");
		this.plantingDate.setValue(dmyFormat.parse("03-01-2013"));
		this.plantingDate.setDateFormat("dd-MM-yyyy");
		this.plantingDate.setWidth("100px");

		this.harvestDate = new DateField();
		this.harvestDate.setCaption("Harvest Date");
		this.harvestDate.setValue(dmyFormat.parse("15-05-2013"));
		this.harvestDate.setDateFormat("dd-MM-yyyy");
		this.harvestDate.setWidth("100px");

		this.getData = new Button();
		this.getData.setCaption("Get Weather/Season Data");
		this.getData.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				AWhereFormComponent.this.getDataAction();
			}
		});

		this.jsonResult = new TextArea();
		this.jsonResult.setWidth("800px");
		this.jsonResult.setHeight("120px");
		this.jsonResult.setCaption("AWhere JSON Result");

		this.locationName = new TextField();
		this.locationName.setCaption("Location Name");

		this.seasonProfileTable = new Table();
		this.seasonProfileTable.setCaption("Season Profile Table");
		this.seasonProfileTable.setHeight("180px");
		this.seasonProfileTable.addContainerProperty("PERIOD FROM", String.class, "");
		this.seasonProfileTable.addContainerProperty("PERIOD TO", String.class, "");
		this.seasonProfileTable.addContainerProperty("AVG MAX TEMP", Double.class, "");
		this.seasonProfileTable.addContainerProperty("AVG MIN TEMP", Double.class, "");
		this.seasonProfileTable.addContainerProperty("TOTAL RAIN", Double.class, "");
		this.seasonProfileTable.addContainerProperty("AVG SOLAR", Double.class, "");
		this.seasonProfileTable.addContainerProperty("TOTAL GDD", Double.class, "");
		this.seasonProfileTable.addContainerProperty("COUNT", Integer.class, "");

		this.tenYearAverageTable = new Table();
		this.tenYearAverageTable.setCaption("Ten Year Average Table");
		this.tenYearAverageTable.setHeight("180px");
		this.tenYearAverageTable.addContainerProperty("PERIOD FROM", String.class, "");
		this.tenYearAverageTable.addContainerProperty("PERIOD TO", String.class, "");
		this.tenYearAverageTable.addContainerProperty("AVG MAX TEMP", Double.class, "");
		this.tenYearAverageTable.addContainerProperty("AVG MIN TEMP", Double.class, "");
		this.tenYearAverageTable.addContainerProperty("TOTAL RAIN", Double.class, "");
		this.tenYearAverageTable.addContainerProperty("AVG SOLAR", Double.class, "");
		this.tenYearAverageTable.addContainerProperty("TOTAL GDD", Double.class, "");
		this.tenYearAverageTable.addContainerProperty("COUNT", Integer.class, "");

		this.addComponent(header, "top:0px; left:30px;");
		this.addComponent(this.latitude, "top:95px; left: 30px;");
		this.addComponent(this.longitude, "top:95px; left: 200px;");
		this.addComponent(this.plantingDate, "top:95px; left: 390px;");
		this.addComponent(this.harvestDate, "top:95px; left: 500px;");
		this.addComponent(this.getData, "top:95px; left: 650px;");
		this.addComponent(this.jsonResult, "top:160px; left: 30px;");
		this.addComponent(this.locationName, "top:310; left: 30px;");
		this.addComponent(this.seasonProfileTable, "top:360px; left:30px;");
		this.addComponent(this.tenYearAverageTable, "top:570px; left:30px;");

		this.initializeAWhere();

	}

	private void initializeAWhere() throws Exception {
		this.aWhereUtil = new AWhereUtil();
	}

	private void getDataAction() {

		this.jsonResult.setValue("");
		this.locationName.setValue("");
		this.seasonProfileTable.removeAllItems();
		this.tenYearAverageTable.removeAllItems();

		try {
			this.aWhereUtil.authenticate();
			String jsonString =
					this.aWhereUtil.getSeason(Double.valueOf(this.latitude.getValue().toString()),
							Double.valueOf(this.longitude.getValue().toString()), (Date) this.plantingDate.getValue(),
							(Date) this.harvestDate.getValue());

			jsonString = jsonString.trim();

			if (jsonString.charAt(0) == '[' && jsonString.charAt(jsonString.length() - 1) == ']') {
				jsonString = jsonString.substring(1, jsonString.length() - 1);
			}

			this.jsonResult.setValue(jsonString);

			Gson gson = new Gson();
			SeasonProfileResult seasonProfileResult = gson.fromJson(jsonString, SeasonProfileResult.class);

			this.locationName.setValue(seasonProfileResult.getLocationName());

			List<SeasonProfile> seasonProfiles = seasonProfileResult.getSeason_Profile();

			Integer i = 0;
			for (SeasonProfile s : seasonProfiles) {
				this.seasonProfileTable.addItem(
						new Object[] {s.getPeriodFrom(), s.getPeriodTo(), s.getAvg_MaxTemp(), s.getAvg_MinTemp(), s.getTotal_Rain(),
								s.getAvg_Solar(), s.getTotal_GDD(), s.getCount()}, i);
				i++;
			}

			List<TenYearAverage> tenYearAverages = seasonProfileResult.getTenYearAverage();
			i = 0;
			for (TenYearAverage t : tenYearAverages) {
				this.tenYearAverageTable.addItem(new Object[] {t.getPeriodFrom(), t.getPeriodTo(), t.getAvg_MaxTemp(), t.getAvg_MinTemp(),
						t.getTotal_Rain(), t.getAvg_Solar(), t.getTotal_GDD(), t.getCount()}, i);
				i++;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
