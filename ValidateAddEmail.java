package com.ibm.groceries;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import com.ibm.groceriespages.MailListPage;
import com.ibm.groceriespages.PageDashboard;
import com.ibm.groceriespages.PageLogin;
import com.ibm.groceriespages.SendMailPage;
import com.ibm.initialization.WebDriverLaunch;
import com.ibm.utilities.DatabaseConnection;
import com.ibm.utilities.GetScreenshot;

public class ValidateAddEmail extends WebDriverLaunch {

	@Test(priority = 1, testName = "ValidateEmailAdd", groups = "low")

	public void validateAddEmailDbase() throws IOException, InterruptedException, SQLException {
		String url = data.get("url");
		String userName = data.get("username");
		String password = data.get("password");
		String email = data.get("customerEmail");
		String subject = data.get("subject");
		String message = data.get("message");
		String expMessage = data.get("emailMsg");
		String mailHeader = data.get("mailHeader");
		String expsendmailPageTitle = data.get("sendmailPageTitle");
		String expAddressMessage = data.get("errorAddressMessage");
		String emailTablename = data.get("emailTable");
		// Launching the web site for atozgroceries
		driver.get(url);
		GetScreenshot screen = new GetScreenshot();

		PageLogin login = new PageLogin(driver, wait);
		// To enter email address and password and clickon login button
		login.enterEmailAddress(userName);
		login.enterPassword(password);
		screen.takeScreenshot(driver);
		login.clickOnLogin();
		// Checking logout link is displayed
		Assert.assertTrue(driver.findElement(By.partialLinkText("Logout")).isDisplayed());

		PageDashboard dashboard = new PageDashboard(driver, wait);
		// To click on Catalog
		dashboard.clickOnMarketing();

		// To click on Mail link
		dashboard.clickOnMail();
		screen.takeScreenshot(driver);

		// Verify the presence of mail header
		Assert.assertTrue(driver.getPageSource().contains(mailHeader));

		MailListPage maillistObj = new MailListPage(driver, wait);
		// Calling method to click on Add email button
		maillistObj.clickOnAdd();
		screen.takeScreenshot(driver);
		// Verifying the page title of Send Mail page
		Assert.assertEquals(driver.getTitle(), expsendmailPageTitle);

		SendMailPage addemailObj = new SendMailPage(driver, wait);
		// Calling method to poplate subject
		String pageSource = addemailObj.populateSubject(subject);
		screen.takeScreenshot(driver);
		// Checking the expected error message is displayed
		Assert.assertTrue(pageSource.contains(expAddressMessage));

		// Calling method to enter email,subject
		pageSource = addemailObj.populateMandatory(email, message);
		screen.takeScreenshot(driver);

		if (pageSource.contains(expMessage + email)) {
			Reporter.log(expMessage + email);
			System.out.println(expMessage + email);
			// checking whether success message is displayed or not
			Assert.assertTrue(pageSource.contains(expMessage + email));

			// Checking the presence of record in the admin panel table
			Assert.assertEquals(
					driver.findElement(By.xpath("//table[@id='dataTableExample2']/tbody/tr[1]/td[2]")).getText(),
					email);
			Assert.assertEquals(
					driver.findElement(By.xpath("//table[@id='dataTableExample2']/tbody/tr[1]/td[3]")).getText(),
					subject);

			DatabaseConnection dbaseutil = new DatabaseConnection();
			Statement st = dbaseutil.connectDatabase();

			ResultSet rs = st.executeQuery("select *from " + emailTablename + " where to_mail=" + "'" + email + "'");

			// To verify the presence of record in DB table
			if (rs.next()) {
				System.out.println("Added Record in email table:");
				System.out.println(rs.getString("message"));
				Assert.assertEquals(rs.getString("message"), message);
				System.out.println(rs.getString("subject"));
				Assert.assertEquals(rs.getString("subject"), subject);
				System.out.println(rs.getString("to_mail"));
				Assert.assertEquals(rs.getString("to_mail"), email);

			}

		}

	}

}
