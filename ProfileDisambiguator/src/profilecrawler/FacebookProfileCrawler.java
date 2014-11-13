/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */

package profilecrawler;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.Post;
import com.restfb.types.User;

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
 * FacebookProfileCrawler has the ability to crawl public profile informations
 * and public posts for given Facebook users fbProfile is an instance of
 * UserProfile that stores public profile informations. It uses restfb API
 * together with page scripting API called Selenium (v2.43.1) for the purpose of
 * crawling. fbPosts is instance of List<UserPost> that stores list of public
 * posts firefoxDriver is instance of Selenium WebDriver. ACCESS_TOKEN: This can
 * be obtained from developer.facebook.com. go to Graph API Explorer to get the
 * access token. Also, this token tends to expire after sometimes. So you need
 * to get the new token once it is expired.
 */

public class FacebookProfileCrawler {

    private UserProfile fbProfile;
    private List<UserPost> fbPosts;
    private WebDriver firefoxDriver;
    private static String ACCESS_TOKEN;

    /**
     * @param accessToken
     *            : This can be obtained from developer.facebook.com. go to
     *            Graph API Explorer to get the access token.
     */
    public FacebookProfileCrawler(String accessToken) {
	fbProfile = new UserProfile();
	fbPosts = new ArrayList<UserPost>();
	ACCESS_TOKEN = accessToken;
    }

    /**
     * @param userName
     *            : user name for a Facebook user to crawl the posts
     */
    public void fetchUserPosts(String userName) {

	User user = null;
	FacebookClient fbClient = new DefaultFacebookClient(ACCESS_TOKEN);

	if (fbClient != null) {

	    try {
		user = fbClient.fetchObject(userName,
			com.restfb.types.User.class);
	    } catch (com.restfb.exception.FacebookOAuthException ex) {
		System.out.println("Access token has expired: " + ex);

	    }
	}

	if (fbProfile != null) {
	}
	fbProfile.setName(user.getName());

	Connection<Post> feed = fbClient.fetchConnection(user.getUsername()
		+ "/feed" + "/", Post.class);

	int i = 0;
	for (List<Post> page : feed) {
	    for (Post post : page) {
		UserPost userPost = new UserPost();

		userPost.setUserId(user.getUsername());
		if ((post.getFrom().getName().equals(user.getName()))
		/*
		 * && (post .getType ( ).equals ( "status" ))
		 */
		) {
		    i++;

		    try {

			System.out
				.println("==========================================================");
			System.out.println("                        Post_" + i);
			System.out
				.println("==========================================================");
			userPost.setPostId(post.getId());
			System.out.println("Type of post: " + post.getType());
			System.out.println("Post name: " + post.getName());
			System.out.println("Post from: "
				+ post.getFrom().getName());
			System.out.println("Description: "
				+ post.getDescription());
			if (post.getDescription() != null)
			    userPost.setPostText(post.getDescription());
			else if (post.getMessage() != null)
			    userPost.setPostText(post.getMessage());
			else
			    userPost.setPostText("");// continue;

			System.out.println("Message: " + post.getMessage());
			System.out.println("Created time: "
				+ post.getCreatedTime().toString());
			userPost.setPostDate(post.getCreatedTime());
			System.out.println("Post link: " + post.getLink());
			System.out.println("Link for picture: "
				+ post.getPicture());
			System.out.println("Likes: " + post.getLikes());
			System.out.println("Status type: "
				+ post.getStatusType());

			if (post.getPlace() != null) {
			    String postPlace = post.getPlace().getName()
				    + ","
				    + post.getPlace().getLocation().getCity()
				    + ","
				    + post.getPlace().getLocation()
					    .getCountry();
			    System.out.println("Place of post: " + postPlace);
			    userPost.setPlaceOfPost(postPlace);
			} else {
			    System.out.println("");
			    userPost.setPlaceOfPost("");
			}
		    } catch (NullPointerException ex) {
			System.out.println(ex);
		    }

		    fbPosts.add(userPost);
		}
	    }
	}

    }

    /**
     * This starts the Firefox browser and go to the Facebook home page. The
     * person who wants to crawl other user's public information, need to enter
     * his/her Facebook login credentials.
     */
    private void startFbWebDriverNode() {

	firefoxDriver = new FirefoxDriver();
	String adminUserID = "", password = "";

	// Open the facebook home page in firefox
	getFirefoxDriver().get("https://www.facebook.com/");

	// Login permission
	BufferedReader br2 = new BufferedReader(
		new InputStreamReader(System.in));
	try {
	    System.out.print("\nEnter adminUserid:");
	    adminUserID = br2.readLine();
	    Thread.sleep(500);
	    System.out.print("Enter Password:");
	    password = br2.readLine();

	    getFirefoxDriver().findElement(By.id("email"))
		    .sendKeys(adminUserID);
	    getFirefoxDriver().findElement(By.id("pass")).sendKeys(password);
	    getFirefoxDriver().findElement(By.id("loginbutton")).submit();

	} catch (IOException ex) {
	    System.out.println(ex);
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}
    }

    /**
     * Close the Firefox browser
     */
    private void closeFbWebDriverNode() {
	getFirefoxDriver().close();
    }

    /**
     * This method compatibles with Facebook view changed from July - 2014 This
     * method prints out public profile informations of given user and sets the
     * fbProfile with all those information. getFbProfile() can be used to get
     * the information.
     * 
     * @param userName
     *            : user name of a Facebook user to crawl the public profile
     *            informations.
     * @return no return value
     */
    public void startCrawling(String userName) {
	startFbWebDriverNode();

	System.out.println("\n\nFacebook Profile Crawler started...\n\n");
	getFirefoxDriver().get("https://www.facebook.com/" + userName);
	String currentUrl = getFirefoxDriver().getCurrentUrl();
	String[] tokensUrl = currentUrl.split("/");
	String userNameStr = tokensUrl[3];
	System.out.println("User Name: " + userNameStr);

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}
	;

	// Go to the user's about page
	getFirefoxDriver().get(
		"https://www.facebook.com/" + userNameStr + "/about");
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}
	WebElement nameElement = getFirefoxDriver().findElement(
		By.id("fbProfileCover")).findElement(By.tagName("a"));
	System.out.println("Profile name: " + nameElement.getText());
	getFbProfile().setUserId(userNameStr);
	getFbProfile().setName(nameElement.getText());
	getFbProfile().setGender("");
	getFbProfile().setDateOfBirth("");
	List<String> langs = new ArrayList<String>();
	getFbProfile().setLanguages(langs);

	// Retrieve Work and Education details
	System.out.println("____________________________________");
	System.out.println("         Work and Education           ");
	System.out.println("====================================");

	try {

	    getFirefoxDriver().get(
		    "https://www.facebook.com/" + userNameStr
			    + "/about?section=education");
	    Thread.sleep(3000);

	} catch (NoSuchElementException ex) {
	    System.out.println("Work and Education: Not avaialble");
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}

	List<WebElement> workandEduList = getFirefoxDriver().findElements(
		By.id("pagelet_eduwork"));

	if (workandEduList.size() != 0) {
	    List<WebElement> workandEdu = workandEduList.get(0).findElements(
		    By.tagName("a"));
	    Set<String> wrkEdu = new TreeSet<String>();

	    for (WebElement element : workandEdu) {
		System.out.println(element.getText());
		wrkEdu.add(element.getText());
	    }
	    getFbProfile().setEmployer(wrkEdu);
	    getFbProfile().setEducation(wrkEdu);
	    System.out.println("____________________________________");
	}

	String[] tokens;
	try {

	    getFirefoxDriver().get(
		    "https://www.facebook.com/" + userNameStr
			    + "/about?section=living");
	    Thread.sleep(3000);

	    List<WebElement> currentCityElement = getFirefoxDriver()
		    .findElements(By.id("current_city"));
	    if (currentCityElement.size() != 0) {

		String currentCity = currentCityElement.get(0).getText();
		tokens = currentCity.split("\n");
		for (String s : tokens) {
		    System.out.println("Current City: " + s);
		    getFbProfile().setCurrentLocation(s);
		    break;
		}
	    } else {
		System.out.println("Current City: Not avaialble");
		getFbProfile().setCurrentLocation("");
	    }

	} catch (NoSuchElementException ex) {
	    System.out.println("Current City: Not avaialble");
	    getFbProfile().setCurrentLocation("");
	} catch (InterruptedException ex) {
	    System.out.println(ex);
	}

	try {
	    List<WebElement> homeElement = getFirefoxDriver().findElements(
		    By.id("hometown"));
	    if (homeElement.size() != 0) {
		String homeTown = homeElement.get(0).getText();
		tokens = homeTown.split("\n");
		for (String s : tokens) {
		    System.out.println("Home Town: " + s);
		    getFbProfile().setHomeLocation(s);
		    if (getFbProfile().getCurrentLocation().equals(""))
			getFbProfile().setCurrentLocation(s);
		    break;
		}
	    } else {
		System.out.println("Home Town: Not avaialble");
		getFbProfile().setHomeLocation("");
	    }

	} catch (NoSuchElementException ex) {
	    System.out.println("Home Town: Not avaialble");
	    getFbProfile().setHomeLocation("");
	}

	// go to friend listing page
	getFirefoxDriver().get(
		"https://www.facebook.com/" + userNameStr + "/friends");

	// scroll down the page
	JavascriptExecutor jse = (JavascriptExecutor) getFirefoxDriver();

	System.out.println("____________________________________");
	System.out.println("         Friend List           ");
	System.out.println("====================================");
	System.out.println("Page scrolling in progres...\n");

	int pageScroll = 0;
	double progressPerc = 0.0;
	while (pageScroll < 200) {
	    progressPerc = (pageScroll / 199.0) * 100;
	    System.out.print(progressPerc + " % \r");

	    try {
		jse.executeScript("window.scrollBy(0,1000)", "");
		Thread.sleep(500);
		pageScroll++;

		List<WebElement> bottomPage = getFirefoxDriver().findElements(
			By.id("pagelet_timeline_medley_likes"));

		if (bottomPage.size() == 0) {
		    continue;
		} else {
		    break;
		}

	    } catch (NoSuchElementException ex) {
		System.out.println("Expanding page..." + pageScroll);
	    } catch (InterruptedException ex) {
		System.out.println(ex);
	    }
	}
	System.out.println("");

	WebElement friendListBox = getFirefoxDriver().findElement(
		By.id("pagelet_timeline_medley_friends"));
	List<WebElement> friendList = friendListBox.findElements(By
		.tagName("a"));
	List<String> friends = new ArrayList<String>();

	for (WebElement friend : friendList) {
	    if (!friend.getText().trim().equals("")) {
		String textColor = friend.getCssValue("color");
		// System.out.println(textColor);
		if (friend.getText().equals("All Friends")
			|| friend.getText().equals("Recently Added")
			|| friend.getText().equals("Followers")) {
		    continue;
		}
		if (textColor.equals("rgba(59, 89, 152, 1)")) {
		    System.out.println(friend.getText());
		    friends.add(friend.getText());
		}

	    }
	}

	getFbProfile().setFriends(friends);
	closeFbWebDriverNode();
	System.out.println("Finish Crawling...");
    }

    /**
     * @return the fbProfile
     */
    public UserProfile getFbProfile() {
	return fbProfile;
    }

    /**
     * @return the fbPosts
     */
    public List<UserPost> getFbPosts() {
	return fbPosts;
    }

    /**
     * @return the firefoxDriver
     */
    public WebDriver getFirefoxDriver() {
	return firefoxDriver;
    }

    // main can be used for testing purpose
    public static void main(String[] args) throws IOException {

	// This is the Facebook Graph API access token
	String accessToken = "CAACEdEose0cBADNFCHZBnQhZAD2qKERAqs30e13gWv033Mym7YB9YwPZBkgVAZAwENiRFd3Nlscx1rVS4HLeXiSenV1wPaEegA7Q6FC0aVjmJ338gXl3vNqYervMlUPEinu1dvU9vLz2rz70K5ZCDuZBwjs21UJXPMqP6HwRdkmZCGoXEeQzJP1RPOhm59JUzeZAc8oTFtpGQgDMECXGTKZBv";

	FacebookProfileCrawler fbCrawler = new FacebookProfileCrawler(
		accessToken);

	// fbCrawler.startCrawling_v2("alexwilliams93");
	fbCrawler.fetchUserPosts("alexwilliams93");

    }
}
