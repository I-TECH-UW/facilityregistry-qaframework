package org.facilityregistry.qaframework.automation.page;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.facilityregistry.qaframework.automation.test.RemoteTestBase;
import org.facilityregistry.qaframework.automation.test.TestProperties;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A superclass for "real" pages. Has lots of handy methods for accessing elements, clicking,
 * filling fields. etc.
 */
public abstract class Page {
	
	protected final TestProperties properties = TestProperties.instance();
	
	protected final WebDriver driver;
	
	protected final WebDriverWait waiter;
	
	private final String contextUrl;
	
	private final String emrServerUrl;
	
	private final String labServerUrl;
	
	private final String facilityServerUrl;
	
	public WebDriver getDriver() {
		return this.driver;
	}
	
	private final ExpectedCondition<Boolean> pageReady = new ExpectedCondition<Boolean>() {
		
		public Boolean apply(WebDriver driver) {
			if (getPageRejectUrl() != null) {
				if (driver.getCurrentUrl().contains(getPageRejectUrl())) {
					return true;
				}
			}
			
			if (!driver.getCurrentUrl().contains(getPageUrl())) {
				if (getPageAliasUrl() != null) {
					if (!driver.getCurrentUrl().contains(getPageAliasUrl())) {
						return false;
					}
				} else {
					return false;
				}
			}
			
			Object readyState = executeScript("return document.readyState;");
			
			if (hasPageReadyIndicator()) {
				return "complete".equals(readyState) && Boolean.TRUE.equals(executeScript("return (typeof "
				        + getPageReadyIndicatorName() + "  !== 'undefined') ? " + getPageReadyIndicatorName() + " : null;"));
			} else {
				return "complete".equals(readyState);
			}
		}
	};
	
	public Page(Page parent, WebElement waitForStaleness) {
		this(parent.driver);
		waitForStalenessOf(waitForStaleness);
	}
	
	public Page(Page parent) {
		this(parent.driver);
		if (this.getClass().isInstance(parent)) {
			throw new RuntimeException("When returning the same page use the two arguments constructor");
		}
	}
	
	public Page(WebDriver driver) {
		this.driver = driver;
		String emrUrl = properties.getEmrUrl();
		emrServerUrl = formatUrl(emrUrl);
		
		String labUrl = properties.getLabUrl();
		labServerUrl = formatUrl(labUrl);
		
		String facilityUrl = properties.getFacilityUrl();
		facilityServerUrl = formatUrl(facilityUrl);
		try {
			contextUrl = new URL(emrServerUrl).getPath();
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException("webapp.url " + properties.getEmrUrl() + " is not a valid URL", e);
		}
		waiter = new WebDriverWait(driver, Duration.ofSeconds(RemoteTestBase.MAX_WAIT_IN_SECONDS));
	}
	
	private String formatUrl(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}
	
	private String appendSlash(String pageUrl) {
		if (!pageUrl.startsWith("/")) {
			pageUrl = "/" + pageUrl;
		}
		return pageUrl;
	}
	
	/**
	 * Override to return true, if a page has the 'pageReady' JavaScript variable.
	 * 
	 * @return true if the page has pageReady indicator, false by default
	 */
	public boolean hasPageReadyIndicator() {
		return false;
	}
	
	/**
	 * @return the page ready JavaScript variable, pageReady by default.
	 */
	public String getPageReadyIndicatorName() {
		return "pageReady";
	}
	
	public Object executeScript(String script) {
		return ((JavascriptExecutor) driver).executeScript(script);
	}
	
	public Page waitForPage() {
		waiter.until(pageReady);
		if (getPageRejectUrl() != null) {
			
		}
		return this;
	}
	
	public String newContextPageUrl(String pageUrl) {
		return contextUrl + appendSlash(pageUrl);
	}
	
	public String newAbsoluteEmrPageUrl(String pageUrl) {
		return emrServerUrl + appendSlash(pageUrl);
	}
	
	public String newAbsoluteLabPageUrl(String pageUrl) {
		return labServerUrl + appendSlash(pageUrl);
	}
	
	public String newAbsoluteFacilityPageUrl(String pageUrl) {
		return facilityServerUrl + appendSlash(pageUrl);
	}
	
	public void goToEmrPage(String address) {
		driver.get(newAbsoluteEmrPageUrl(address));
	}
	
	public void goToLabPage(String address) {
		driver.get(newAbsoluteLabPageUrl(address));
	}
	
	public void goToFacilityPage(String address) {
		driver.get(newAbsoluteFacilityPageUrl(address));
	}
	
	public void go() {
		driver.get(getAbsolutePageUrl());
		waitForPage();
	}
	
	public WebElement findElement(By by) {
		waiter.until(ExpectedConditions.visibilityOfElementLocated(by));
		return driver.findElement(by);
	}
	
	public WebElement findElementWithoutWait(By by) {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try {
			return driver.findElement(by);
		}
		catch (Exception e) {
			return null;
		}
		
	}
	
	public List<WebElement> getElementsIfExisting(By by) {
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		return driver.findElements(by);
	}
	
	public void waitForPageToLoad() {
		ExpectedCondition<Boolean> expectation = driver -> ((JavascriptExecutor) driver)
		        .executeScript("return document.readyState").toString().equals("complete");
		try {
			Thread.sleep(1000);
			waiter.until(expectation);
		}
		catch (Throwable error) {
			Assert.fail("Timeout waiting for Page.");
		}
	}
	
	public WebElement findElementById(String id) {
		return findElement(By.id(id));
	}
	
	public WebElement findElementByName(String name) {
		return findElement(By.name(name));
	}
	
	public String getText(By by) {
		return findElement(by).getText();
	}
	
	public void setText(By by, String text) {
		setText(findElement(by), text);
	}
	
	public void clearText(By by) {
		findElement(by).clear();
	}
	
	public void setText(String id, String text) {
		setText(findElement(By.id(id)), text);
	}
	
	public void setTextToFieldNoEnter(By by, String text) {
		setTextNoEnter(findElement(by), text);
	}
	
	public void setTextToFieldInsideSpan(String spanId, String text) {
		setText(findTextFieldInsideSpan(spanId), text);
	}
	
	private void setText(WebElement element, String text) {
		setTextNoEnter(element, text);
		element.sendKeys(Keys.RETURN);
	}
	
	private void setTextNoEnter(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
	}
	
	public void clickOn(By by) {
		findElement(by).click();
	}
	
	public void clickOnLast(By by) {
		Iterables.getLast(findElements(by)).click();
	}
	
	public void selectFrom(By by, String value) {
		Select droplist = new Select(findElement(by));
		droplist.selectByVisibleText(value);
	}
	
	public void hoverOn(By by) {
		Actions builder = new Actions(driver);
		Actions hover = builder.moveToElement(findElement(by));
		hover.perform();
	}
	
	private WebElement findTextFieldInsideSpan(String spanId) {
		return findElementById(spanId).findElement(By.tagName("input"));
	}
	
	public String title() {
		return getText(By.tagName("title"));
	}
	
	public String getCurrentAbsoluteUrl() {
		return driver.getCurrentUrl();
	}
	
	public List<WebElement> findElements(By by) {
		waiter.until(ExpectedConditions.presenceOfElementLocated(by));
		return driver.findElements(by);
	}
	
	public void waitForStalenessOf(WebElement webElement) {
		waiter.until(ExpectedConditions.stalenessOf(webElement));
	}
	
	/**
	 * @return the page path
	 */
	public abstract String getPageUrl();
	
	public String getPageAliasUrl() {
		return null;
	}
	
	public String getPageRejectUrl() {
		return null;
	}
	
	public String getContextPageUrl() {
		return newContextPageUrl(getPageUrl());
	}
	
	public String getAbsolutePageUrl() {
		return newAbsoluteEmrPageUrl(getPageUrl());
	}
	
	public void clickOnLinkFromHref(String href) throws InterruptedException {
		// We allow use of xpath here because href's tend to be quite stable.
		clickOn(byFromHref(href));
	}
	
	public By byFromHref(String href) {
		return By.xpath("//a[@href='" + href + "']");
	}
	
	public void waitForFocusById(final String id) {
		waiter.until(new ExpectedCondition<Boolean>() {
			
			@Override
			public Boolean apply(WebDriver driver) {
				return hasFocus(id);
			}
		});
	}
	
	public void waitForFocusByCss(final String tag, final String attr, final String value) {
		waiter.until(new ExpectedCondition<Boolean>() {
			
			@Override
			public Boolean apply(WebDriver driver) {
				return hasFocus(tag, attr, value);
			}
		});
	}
	
	boolean hasFocus(String id) {
		return (Boolean) ((JavascriptExecutor) driver).executeScript("return jQuery('#" + id + "').is(':focus')",
		    new Object[] {});
	}
	
	boolean hasFocus(String tag, String attr, String value) {
		return (Boolean) ((JavascriptExecutor) driver)
		        .executeScript("return jQuery('" + tag + "[" + attr + "=" + value + "]').is(':focus')", new Object[] {});
	}
	
	public void waitForJsVariable(final String varName) {
		waiter.until(new ExpectedCondition<Boolean>() {
			
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver)
				        .executeScript("return (typeof " + varName + "  !== 'undefined') ? " + varName + " : null") != null;
			}
		});
	}
	
	public void waitForElementToBeHidden(By by) {
		waiter.until(ExpectedConditions.invisibilityOfElementLocated(by));
	}
	
	public void waitForElementToBeEnabled(By by) {
		waiter.until(ExpectedConditions.elementToBeClickable(by));
	}
	
	public void acceptAlert() {
		waiter.until(ExpectedConditions.alertIsPresent());
		Alert alert = driver.switchTo().alert();
		alert.accept();
	}
	
	public void dismissAlert() {
		waiter.until(ExpectedConditions.alertIsPresent());
		Alert alert = driver.switchTo().alert();
		alert.dismiss();
	}
	
	public Boolean alertPresent() throws InterruptedException {
		Thread.sleep(1000);
		Boolean booelan = false;
		try {
			Alert alert = driver.switchTo().alert();
			booelan = true;
		}
		catch (Exception e) {}
		return booelan;
	}
	
	public Boolean promptPresent() throws InterruptedException {
		Thread.sleep(1000);
		Boolean booelan = false;
		try {
			Alert alert = driver.switchTo().alert();
			booelan = true;
		}
		catch (Exception e) {}
		return booelan;
	}
	
	public void waitForElement(By by) {
		waiter.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	public void waitForTextToBePresentInElement(By by, String text) {
		waiter.until(ExpectedConditions.textToBePresentInElementLocated(by, text));
	}
	
	public void waitForElementWithSpecifiedMaxTimeout(By by, long secs) {
		WebDriverWait waiter = new WebDriverWait(driver, secs);
		waiter.until(pageReady);
		waiter.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	
	public Boolean containsText(String text) {
		return driver.getPageSource().contains(text);
	}
	
	public String getClass(By by) {
		return findElement(by).getAttribute("class");
	}
	
	public String getValue(By by) {
		return findElement(by).getAttribute("value");
	}
	
	public String getValueWithoutWait(By by) {
		if (findElementWithoutWait(by) == null) {
			return "";
		}
		return findElementWithoutWait(by).getAttribute("value");
	}
	
	public List<String> getValidationErrors() {
		List<String> validationErrors = new ArrayList<String>();
		for (WebElement webElement : driver.findElements(By.className("field-error"))) {
			if (StringUtils.isNotBlank(webElement.getText())) {
				validationErrors.add(webElement.getText());
			}
		}
		for (WebElement webElement : driver.findElements(By.className("error"))) {
			if (StringUtils.isNotBlank(webElement.getText())) {
				validationErrors.add(webElement.getText());
			}
		}
		return validationErrors;
	}
	
	public String queryJsForAttribute(String cssHandle, String attribute) {
		return (String) ((JavascriptExecutor) driver)
		        .executeScript(String.format("return document.querySelector('%s').%s", cssHandle, attribute));
	}
	
	public void setAttributeWithJs(String cssHandle, String attribute, String value) {
		((JavascriptExecutor) driver)
		        .executeScript(String.format("document.querySelector('%s').%s = '%s'", cssHandle, attribute, value));
	}
	
	public String getStyle(By by) {
		return findElement(by).getAttribute("style");
	}
	
	public void selectOptionFromDropDown(By by) {
		By FIELD_OPTION = By.tagName("option");
		clickOn(by);
		List<WebElement> options = findElement(by).findElements(FIELD_OPTION);
		int n = 0;
		for (WebElement option : options) {
			if (n == 1) {
				option.click();
				break;
			}
			n = n + 1;
		}
	}
	
	public Boolean dropDownHasOptions(By by) {
		By FIELD_OPTION = By.tagName("option");
		clickOn(by);
		List<WebElement> options = findElement(by).findElements(FIELD_OPTION);
		return options.size() > 0 ? true : false;
	}
	
	public Boolean hasElement(By by) {
		return findElement(by) != null ? true : false;
	}
	
	public Boolean hasElementWithoutWait(By by) {
		return findElementWithoutWait(by) != null ? true : false;
	}
	
	public Boolean isDisabled(By by) {
		Boolean disabled = false;
		String disabledAttribute = findElement(by).getAttribute("disabled");
		if (disabledAttribute != null) {
			if (disabledAttribute.equals("true")) {
				disabled = true;
			}
		}
		return disabled;
	}
	
	public Boolean isChecked(By by) {
		Boolean disabled = false;
		String disabledAttribute = findElement(by).getAttribute("checked");
		if (disabledAttribute != null) {
			if (disabledAttribute.equals("true")) {
				disabled = true;
			}
		}
		return disabled;
	}
	
	/**
	 * Forcefuly selects a React option by Javascript. use name attribute
	 * 
	 * @throws InterruptedException
	 */
	public void selectOptionByJavacript(By by) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebElement element = findElement(by);
		js.executeScript("arguments[0].click();", element);
		Thread.sleep(2000);
		element.sendKeys(Keys.chord(Keys.DOWN, Keys.ENTER, Keys.ENTER));
	}
	
	public void selectOptionByAction(By by, By value) throws InterruptedException {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebElement element = findElement(by);
		Actions action = new Actions(driver);
		action.moveToElement(element).click().build().perform();
		action.click(findElement(value)).build().perform();
	}
	
	public void clickByJavacript(By by) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", findElement(by));
	}
	
	public void refreshPage() {
		driver.navigate().refresh();
	}
}
