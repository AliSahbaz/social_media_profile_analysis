/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package profilecrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import user.UserPost;
import user.UserProfile;

/**
 * TwitterProfileCrawlerSelenium has the ability to crawl public profile
 * informations and public posts for given Twitter users. It uses page scripting
 * API called Selenium (v2.43.1). twProfile is an instance of UserProfile that
 * stores public profile informations. twPosts is instance of List<UserPost>
 * that stores list of public posts.
 * CONSUMER_KEY,CONSUMER_KEY_SECRET,ACCESS_TOKEN,ACCESS_TOKEN_SECRET: These can
 * be obtained from developer.twitter.com. create sample app to get these access
 * tokens.
 */
public class TwitterProfileCrawlerSelenium {
    
    private UserProfile twProfile;
    private List<UserPost> twPosts;
    private WebDriver firefoxDriver;

    /**
     * Constructor that initializes necessary attributes.
     */
    public TwitterProfileCrawlerSelenium() {
	twProfile = new UserProfile();

    }

    /**
     * This starts the Firefox browser and go to the Twitter home page. The
     * person who wants to crawl other user's public information, need to enter
     * his/her Twitter login credentials.
     */
    public void startTwWebDriverNode() {

	firefoxDriver = new FirefoxDriver();
	String adminUserID = "", password = "";

	// Open twitter home page in firefox
	getFirefoxDriver().get("https://www.twitter.com/");

	// Login permission
	BufferedReader br2 = new BufferedReader(
		new InputStreamReader(System.in));
	try {
	    System.out.print("\nEnter adminUserid:");
	    adminUserID = br2.readLine();
	    Thread.sleep(500);
	    System.out.print("Enter Password:");
	    password = br2.readLine();

	    getFirefoxDriver().findElement(By.id("signin-email")).sendKeys(
		    adminUserID);
	    getFirefoxDriver().findElement(By.id("signin-password")).sendKeys(
		    password);

	    List<WebElement> buttons = getFirefoxDriver().findElements(
		    By.tagName("button"));
	    for (WebElement button : buttons) {
		if (button.getText().equals("Sign in")) {
		    button.click();
		    break;
		}
	    }

	} catch (IOException ex) {
	    System.out.println(ex);
	} catch (InterruptedException ex) {
	    System.out.println();
	}
    }

    /**
     * This method prints out public profile informations of given twitter user
     * and sets the twProfile with all those information. getTwProfile() can be
     * used to get the information.
     * 
     * @param userName
     *            : user name of a Twitter user to crawl the public profile
     *            informations.
     * @return no return value
     */
    public void startCrawling(String userName) {
	startTwWebDriverNode();
	System.out.println("\n\nTwitter Crawler started...\n\n");

	// Go to twitter user's about page
	getFirefoxDriver().get("https://www.twitter.com/" + userName);
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}

	WebElement nameElement = getFirefoxDriver().findElement(
		By.className("ProfileHeaderCard-name")).findElement(
		By.tagName("a"));
	System.out.println("Profile name: " + nameElement.getText());
	getTwProfile().setUserId(userName);
	getTwProfile().setName(nameElement.getText());
	getTwProfile().setGender("");
	getTwProfile().setDateOfBirth("");
	getTwProfile().setLanguages(new ArrayList<String>());

	try {

	    List<WebElement> currentCityElement = getFirefoxDriver()
		    .findElements(By.className("ProfileHeaderCard-location"));
	    if (currentCityElement.size() != 0) {

		String currentCity = currentCityElement.get(0).getText();

		System.out.println("Current City: " + currentCity);
		getTwProfile().setCurrentLocation(currentCity);
	    } else {
		System.out.println("Current City: Not avaialble");
		getTwProfile().setCurrentLocation("");
	    }

	} catch (NoSuchElementException ex) {
	    System.out.println("Current City: Not avaialble");
	    getTwProfile().setCurrentLocation("");
	}

	getTwProfile().setHomeLocation("");
	Set<String> edus = new TreeSet<String>();
	getTwProfile().setEducation(edus);
	Set<String> works = new TreeSet<String>();
	getTwProfile().setEmployer(works);

	// go to friend list page
	getFirefoxDriver().get(
		"https://www.twitter.com/" + userName + "/following");

	// scroll down the page
	JavascriptExecutor jse = (JavascriptExecutor) getFirefoxDriver();

	System.out.println("____________________________________");
	System.out.println("         Friend List           ");
	System.out.println("====================================");
	System.out.println("Page scrolling in progres...\n");

	int pageScroll = 0;
	double progressPerc = 0.0;
	while (pageScroll < 300) {
	    progressPerc = (pageScroll / 299.0) * 100;
	    System.out.print(progressPerc + " % \r");

	    try {
		jse.executeScript("window.scrollBy(0,1000)", "");
		Thread.sleep(500);
		pageScroll++;

	    } catch (InterruptedException ex) {
		System.out.println(ex);
	    }
	}
	System.out.println("");

	List<WebElement> friendListBox = getFirefoxDriver().findElements(
		By.className("GridTimeline-items"));
	List<String> friends = new ArrayList<String>();

	if (friendListBox.size() != 0) {
	    List<WebElement> friendList = friendListBox.get(0).findElements(
		    By.tagName("a"));

	    for (WebElement friend : friendList) {
		if (!friend.getText().trim().equals("")) {
		    String textColor = friend.getCssValue("color");
		    // System.out.println(textColor);

		    if (textColor.equals("rgba(41, 47, 51, 1)")) {
			System.out.println(friend.getText());
			friends.add(friend.getText());
		    }
		}

	    }

	    getTwProfile().setFriends(friends);
	} else {
	    getTwProfile().setFriends(friends);
	}

	System.out.println("Finish Crawling...");
	closeFbWebDriverNode();
    }

    /**
     * Close the Firefox browser
     */
    private void closeFbWebDriverNode() {
	getFirefoxDriver().close();
    }

    /**
     * This is the wrapper function for retrievePosts(String) in
     * TwitterProfileCrawlerAPI4J class
     * 
     * @param userName
     *            : user name for a Twitter user to crawl the posts
     * @return It returns nothing. It stores the list of post as twPosts. See
     *         UserPost class for attributes.
     */
    public void fetchUserPosts(String userName) {
	TwitterProfileCrawlerAPI4J tw = new TwitterProfileCrawlerAPI4J();
	twPosts = tw.retrievePosts(userName);
    }

    /**
     * @return the twProfile
     */
    public UserProfile getTwProfile() {
	return twProfile;
    }

    /**
     * @return the firefoxDriver
     */
    public WebDriver getFirefoxDriver() {
	return firefoxDriver;
    }

    /**
     * @return the twPost
     */
    public List<UserPost> getTwPosts() {
	return twPosts;
    }

    // main can be used for testing purposes
    public static void main(String[] args) throws IOException {
	TwitterProfileCrawlerSelenium tw = new TwitterProfileCrawlerSelenium();
	tw.startTwWebDriverNode();

	tw.startCrawling("rponnuru");
	tw.fetchUserPosts("rponnuru");

	TwitterProfileCrawlerAPI4J twt = new TwitterProfileCrawlerAPI4J();
	twt.retrievePosts("rponnuru");

    }
}
