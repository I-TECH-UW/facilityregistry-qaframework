package org.facilityregistry.qaframework.automation.page.lab;

import org.facilityregistry.qaframework.automation.page.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This class represents the Login page
 */
public class LoginPage extends Page {
	
	private static final String PATH_LOGIN = "/LoginPage.do";
	
	private static final By FIELD_USERNAME = By.id("loginName");
	
	private static final By FIELD_PASSWORD = By.id("password");
	
	private static final By BUTTON_SUBMIT = By.id("submitButton");
	
	static final String LOGOUT_PATH = "/logout";
	
	private String username;
	
	private String password;
	
	public LoginPage(WebDriver driver) {
		super(driver);
		username = properties.getLabUsername();
		password = properties.getLabPassword();
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public void go() {
		goToLabPage(PATH_LOGIN);
		acceptSelfAssignedCert();
	}
	
	@Override
	public String getPageUrl() {
		return PATH_LOGIN;
	}
	
	public void enterUsername(String username) {
		setTextToFieldNoEnter(FIELD_USERNAME, username);
	}
	
	public void enterPassword(String password) {
		setTextToFieldNoEnter(FIELD_PASSWORD, password);
	}
	
	public WebElement getLoginButton() {
		return findElement(BUTTON_SUBMIT);
	}
	
	public HomePage goToHomePage() {
		go();
		enterUsername(this.username);
		enterPassword(this.password);
		clickOn(BUTTON_SUBMIT);
		return new HomePage(this);
	}
	
	private void acceptSelfAssignedCert() {
		By BUTTON_ADVANCED = By.id("details-button");
		By LINK_PROEED = By.id("proceed-link");
		if (this.hasElementWithoutWait(BUTTON_ADVANCED)) {
			clickOn(BUTTON_ADVANCED);
			clickOn(LINK_PROEED);
		}
	}
}
