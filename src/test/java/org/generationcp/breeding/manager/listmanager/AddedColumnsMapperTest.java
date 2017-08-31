
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.FillColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AddedColumnsMapperTest {

	private static final int ATTRIBUTE_TYPE_ID2 = 2;
	private static final int ATTRIBUTE_TYPE_ID1 = 1;
	private static final String ATTRIBUTE_TYPE_NAME2 = "New Passport Type";
	private static final String ATTRIBUTE_TYPE_NAME1 = "Ipstat";

	private static final String[] STANDARD_COLUMNS =
			{ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(), ColumnLabels.SEED_SOURCE.getName(),
					ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};

	private static final List<String> ADDED_COLUMNS = Arrays.asList(ColumnLabels.PREFERRED_NAME.getName(),
			ColumnLabels.PREFERRED_ID.getName(), ColumnLabels.GERMPLASM_DATE.getName(), ColumnLabels.GERMPLASM_LOCATION.getName(),
			ColumnLabels.BREEDING_METHOD_NAME.getName(), ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
			ColumnLabels.BREEDING_METHOD_NUMBER.getName(), ColumnLabels.BREEDING_METHOD_GROUP.getName(),
			ColumnLabels.CROSS_FEMALE_GID.getName(), ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
			ColumnLabels.CROSS_MALE_GID.getName(), ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@Mock
	private FillColumnSource fillColumnSource;

	@InjectMocks
	private AddedColumnsMapper addedColumnsMapper;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.addedColumnsMapper.setValuesGenerator(this.valuesGenerator);
		this.addedColumnsMapper.setGermplasmDataManager(this.germplasmDataManager);
	}

	@Test
	public void testGenerateValuesForAddedColumnsWhenNoAddedColumns() {
		final boolean attributeTypesIncluded = false;
		this.addedColumnsMapper.generateValuesForAddedColumns(STANDARD_COLUMNS, attributeTypesIncluded);

		Mockito.verifyZeroInteractions(this.valuesGenerator);
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
	}

	@Test
	public void testGenerateValuesForAddedColumnsWhenColumnsAdded() {
		final boolean attributeTypesIncluded = false;
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.addAll(ADDED_COLUMNS);
		this.addedColumnsMapper.generateValuesForAddedColumns(columns.toArray(), attributeTypesIncluded);
		
		Mockito.verify(this.valuesGenerator).setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.verify(this.valuesGenerator).setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
		Mockito.verify(this.valuesGenerator).setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.verify(this.valuesGenerator).setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_GID.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
		Mockito.verify(this.valuesGenerator).setCrossMaleGIDColumnValues(ColumnLabels.CROSS_MALE_GID.getName());
		Mockito.verify(this.valuesGenerator).setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		Mockito.verifyZeroInteractions(this.germplasmDataManager);
	}
	
	@Test
	public void testGenerateValuesForAddedColumnsWhenAtributeColumnsAdded() {
		Mockito.doReturn(this.getAttributeTypes()).when(this.germplasmDataManager).getAllAttributesTypes();
		final boolean attributeTypesIncluded = true;
		final List<String> columns = new ArrayList<>(Arrays.asList(STANDARD_COLUMNS));
		columns.add(ATTRIBUTE_TYPE_NAME1);
		columns.add(ATTRIBUTE_TYPE_NAME2);
		
		this.addedColumnsMapper.generateValuesForAddedColumns(columns.toArray(), attributeTypesIncluded);
		
		// Check that attribute type names are capitalized
		Mockito.verify(this.valuesGenerator).fillWithAttribute(ATTRIBUTE_TYPE_ID1, ATTRIBUTE_TYPE_NAME1.toUpperCase());
		Mockito.verify(this.valuesGenerator).fillWithAttribute(ATTRIBUTE_TYPE_ID2, ATTRIBUTE_TYPE_NAME2.toUpperCase());
	}
	
	private List<UserDefinedField> getAttributeTypes() {
		final UserDefinedField attributeType1 = new UserDefinedField(ATTRIBUTE_TYPE_ID1);
		attributeType1.setFname(ATTRIBUTE_TYPE_NAME1);
		final UserDefinedField attributeType2 = new UserDefinedField(ATTRIBUTE_TYPE_ID2);
		attributeType2.setFname(ATTRIBUTE_TYPE_NAME2);
		final UserDefinedField attributeType3 = new UserDefinedField(3);
		attributeType3.setFname("Grower");
		return Arrays.asList(attributeType1, attributeType2, attributeType3);
	}

}
