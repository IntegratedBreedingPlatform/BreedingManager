
package org.generationcp.breeding.manager.util.awhere;

import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

@Ignore
@Deprecated
public class AWhereTest {

	private AWhereUtil aWhereUtil;

	@Test
	public void authenticate() {

		// Valid credentials
		String username = "nzlabs@efficio.us.com";
		String password = "9ZBKreXb";

		// Invalid credentials
		// String username = "boola@loo.com";
		// String password = "!#V$@%)VM$#T*M@C)#C@*MR";

		this.aWhereUtil = new AWhereUtil();
		try {
			if (!this.aWhereUtil.authenticate(username, password)) {
				Assert.fail("Authentication failed. " + this.aWhereUtil.getLastResponseCode() + " - "
						+ this.aWhereUtil.getLastResponseMessage());
			} else {
				System.out.println("Login successful!");
			}
		} catch (ConnectException e) {
			Assert.fail("Connection error. Unable to connect to AWhere API.");
			e.printStackTrace();
		} catch (Exception e) {
			Assert.fail("Unknown error: " + e);
		}

	}

	@Test
	public void getData() {
		this.authenticate();
		SimpleDateFormat dmyFormat = new SimpleDateFormat("dd-MM-yyyy");
		try {
			String jsonString = this.aWhereUtil.getSeason(-1.5089, 37.2948, dmyFormat.parse("03-01-2013"), dmyFormat.parse("15-05-2013"));
			if (this.aWhereUtil.getLastResponseCode() != 200) {
				Assert.fail("Call failed. " + this.aWhereUtil.getLastResponseCode() + " - " + this.aWhereUtil.getLastResponseMessage());
			} else {
				System.out.println("Successful call: " + jsonString);
			}
		} catch (ParseException e) {
			Assert.fail("ParseException: " + e);
		} catch (Exception e) {
			Assert.fail("Unknow Error: " + e);
		}
	}

}
