
package org.generationcp.breeding.manager.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.pojos.UserDefinedField;

public class UserDefinedFieldTestDataInitializer {

	public static UserDefinedField createUserDefinedField() {
		return UserDefinedFieldTestDataInitializer.createUserDefinedField("LST", "Generic List");
	}

	public static UserDefinedField createUserDefinedField(final String fcode, final String fname) {
		final UserDefinedField udField = new UserDefinedField();
		udField.setFcode(fcode);
		udField.setFname(fname);
		return udField;
	}

	public static List<UserDefinedField> createUserDefinedFieldList() {
		final List<UserDefinedField> udFields = new ArrayList<UserDefinedField>();
		udFields.add(UserDefinedFieldTestDataInitializer.createUserDefinedField());
		return udFields;
	}

	public static List<UserDefinedField> createUserDefinedFieldList(final String fcode, final String fname) {
		final List<UserDefinedField> udFields = new ArrayList<UserDefinedField>();
		udFields.add(UserDefinedFieldTestDataInitializer.createUserDefinedField(fcode, fname));
		return udFields;
	}
}
