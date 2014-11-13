/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package profilecrawler;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import user.UserPost;
import user.UserProfile;

/**
 * GooglePlusProfileCrawler has the ability to crawl public profile informations
 * and public posts for given Twitter users. It uses Google Plus Json API
 * together with page scripting API called Selenium (v2.43.1) to crawl public
 * profile informations and public posts. plusProfile is an instance of
 * UserProfile that stores public profile informations. plusPosts is instance of
 * List<UserPost> that stores list of public posts. API_KEY : These can be
 * obtained from developer.google.com. create sample project and go to project
 * dash board to get this key.
 */

public class GooglePlusProfileCrawler {
    private UserProfile plusProfile;
    private List<UserPost> plusPosts;

    private static String API_KEY;
    private static WebDriver firefoxDriver;

    /**
     * This constructor initializes necessary attributes of the class.
     * 
     * @param API_KEY
     *            : key that is retrieved from google developer dash board.
     */
    public GooglePlusProfileCrawler(String API_KEY) {
	plusProfile = new UserProfile();
	plusPosts = new ArrayList<UserPost>();
	GooglePlusProfileCrawler.API_KEY = API_KEY;
    }

    /**
     * This methods print outs public posts informations of given user sets the
     * plusPosts with all those information. getPlusPosts() can be used to get
     * the information.
     * 
     * @param userName
     *            : user name for a Google plus user to crawl the posts
     * @return no return value.
     */
    public void fetchUserPosts(String userName) {

	try {
	    InputStream is = new URL(
		    "https://www.googleapis.com/plus/v1/people/" + userName
			    + "/activities/public?key=" + API_KEY).openStream();

	    JsonReader rdr = Json.createReader(is);
	    JsonObject obj = rdr.readObject();

	    UserPost userPost = new UserPost();
	    userPost.setUserId(userName);

	    JsonArray results = obj.getJsonArray("items");
	    if (results != null) {
		System.out.println("====================================");
		System.out.println("         Posts");
		System.out.println("====================================");
		for (JsonObject result : results.getValuesAs(JsonObject.class)) {
		    String postTitle = result.getString("title");
		    String postID = result.getString("id");
		    String postDate = result.getString("published");

		    System.out.println("\nPost ID: " + postID);
		    System.out.println("Post Title: " + postTitle);
		    System.out.println("Post Date: " + postDate);

		    userPost.setPostId(postID);
		    userPost.setPostText(postTitle);

		    SimpleDateFormat formatter = new SimpleDateFormat(
			    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		    Date parsedDate = formatter.parse(postDate);

		    userPost.setPostDate(parsedDate);
		    userPost.setPlaceOfPost("");
		    getPlusPosts().add(userPost);
		    System.out.println("___________________________________");

		}
	    }
	} catch (java.text.ParseException ex) {
	    System.out.println(ex);
	} catch (IOException ex) {
	    System.out.println(ex);
	}

    }

    /**
     * This methods print outs public profile informations of given user sets
     * the plusProfile with all those information. getPlusProfile() can be used
     * to get the information.
     * 
     * @param userName
     *            : user name of a Google plus user to crawl the public profile
     *            informations.
     * @return no return value
     */
    public void startCrawling(String userName) {
	try {

	    InputStream is = new URL(
		    "https://www.googleapis.com/plus/v1/people/" + userName
			    + "?key=" + API_KEY).openStream();

	    JsonReader rdr = Json.createReader(is);
	    JsonObject obj = rdr.readObject();

	    // retrieve user's profile information

	    System.out.println("\nGoogle+ Profile crawler started...\n");
	    System.out.println("____________________________________");
	    System.out.println("         Profile");
	    System.out.println("====================================");
	    String displayName = obj.getJsonString("displayName").getString();
	    System.out.println("Name: " + displayName);
	    getPlusProfile().setName(displayName);

	    String user_id = obj.getJsonString("id").getString();
	    System.out.println("ID: " + user_id);
	    // getPlusProfile().setUserId(user_id);//number format user id
	    getPlusProfile().setUserId(userName);

	    if (obj.containsKey("gender")) {
		String gender = obj.getJsonString("gender").getString();
		System.out.println("Gender: " + gender);
		getPlusProfile().setGender(gender);
	    } else {
		System.out.println("Gender: " + "");
		getPlusProfile().setGender("");
	    }

	    getPlusProfile().setDateOfBirth("");
	    JsonString occupationJson = obj.getJsonString("occupation");
	    if (occupationJson != null) {
		String occupation = obj.getJsonString("occupation").getString();
		System.out.println("Occupation: " + occupation);
	    }

	    // retrieve user's work and education information
	    JsonArray results = obj.getJsonArray("placesLived");
	    if (results != null) {
		System.out.println("____________________________________");
		System.out.println("         Locations Lived");
		System.out.println("====================================");
		int i = 0;
		for (JsonObject result : results.getValuesAs(JsonObject.class)) {
		    String location = result.getString("value");
		    System.out.println(location);
		    if (i == 0)
			getPlusProfile().setCurrentLocation(location);
		    else
			getPlusProfile().setHomeLocation(location);

		    i++;
		}
		if (i == 1)
		    getPlusProfile().setHomeLocation(
			    getPlusProfile().getCurrentLocation());

	    } else {
		System.out.println("Location info NOT AVAILABLE");
		getPlusProfile().setCurrentLocation("");
		getPlusProfile().setHomeLocation("");
	    }

	    // retrieve user's work and education information
	    Set<String> wrkEdu = new TreeSet<String>();
	    results = obj.getJsonArray("organizations");
	    if (results != null) {
		System.out.println("____________________________________");
		System.out.println("         Work and Education");
		System.out.println("====================================");
		for (JsonObject result : results.getValuesAs(JsonObject.class)) {

		    if (result.containsKey("name")) {
			String workEdu = result.getString("name");
			System.out.println(workEdu);
		    }

		    else {
			wrkEdu.add("");
			System.out.println("Company name NOT AVAILABLE");
		    }

		}

		getPlusProfile().setEmployer(wrkEdu);
		getPlusProfile().setEducation(wrkEdu);
	    } else {
		System.out.println("Work and Education info NOT AVAILABLE");
		getPlusProfile().setEmployer(wrkEdu);
		getPlusProfile().setEducation(wrkEdu);
	    }
	    List<String> languages = new ArrayList<String>();
	    getPlusProfile().setLanguages(languages);

	    firefoxDriver = new FirefoxDriver();
	    firefoxDriver.get("https://plus.google.com/" + userName + "/about");
	    Thread.sleep(3000);

	    // get list of circles
	    List<WebElement> circle = firefoxDriver.findElements(By
		    .className("bkb"));
	    Set<String> circles = new TreeSet<String>();

	    if (circle.size() != 0) {
		String circleText = circle.get(0).getText();
		System.out.println(circleText);
		String circleCountstr = circleText.split("\\s+")[0];
		if (circleCountstr.contains(",")) {
		    circleCountstr = circleCountstr.replace(",", "");
		}
		int circleCount = Integer.parseInt(circleCountstr);
		System.out.println("Circle Count:" + circleCount);

		WebElement circleLink = firefoxDriver
			.findElement(By.xpath("//span[contains(text(),'"
				+ circleText + "')]"));
		circleLink.click();
		Thread.sleep(3000);

		WebElement friendListBox = firefoxDriver.findElement(By
			.className("G-q-B"));

		Robot robot = new Robot();

		int loop = 0;
		while (loop < circleCount / 10) {
		    if (circles.size() == circleCount)
			break;

		    Thread.sleep(3000);

		    List<WebElement> friendList = friendListBox.findElements(By
			    .tagName("a"));

		    for (WebElement friend : friendList) {
			System.out.println(friend.getText());
			circles.add(friend.getText());
		    }

		    int count = 0;
		    while (count < 500) {
			robot.keyPress(java.awt.event.KeyEvent.VK_DOWN);
			count++;
		    }

		    loop++;
		}
	    }
	    List<String> friends = new ArrayList<String>();
	    for (String friend : circles) {
		friends.add(friend);
	    }
	    getPlusProfile().setFriends(friends);

	    System.out
		    .println("_________________________________________________________________________________________");

	    System.out.println("Finish Crawling...");
	    closePlusWebDriverNode();

	} catch (AWTException ex) {
	    System.out.println("Problem in key press:" + ex);
	} catch (IOException ex) {
	    System.out.println(ex);
	} catch (InterruptedException ex) {
	    System.out.println();
	}

    }

    /**
     * Close the Firefox browser
     */
    private void closePlusWebDriverNode() {
	firefoxDriver.close();
    }

    /**
     * @return the plusProfile
     */
    public UserProfile getPlusProfile() {
	return plusProfile;
    }

    /**
     * @return the plusPosts
     */
    public List<UserPost> getPlusPosts() {
	return plusPosts;
    }

    public static void main(String args[]) {

	String API_KEY = "AIzaSyBztOQ-dXgY6v5fBzcNRlBw_AOSbwlknxg";

	GooglePlusProfileCrawler plusCrawler = new GooglePlusProfileCrawler(
		API_KEY);
	plusCrawler.startCrawling("105705103265648027714");
	plusCrawler.fetchUserPosts("105705103265648027714");

    }

}
