/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package profilecrawler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import user.UserPost;
import user.UserProfile;


/**
 * TwitterProfileCrawlerAPI4J has the ability to crawl public profile
 * informations and public posts for given Twitter users. It uses twitter4j API
 * for this purpose. twProfile is an instance of UserProfile that stores public
 * profile informations. twPosts is instance of List<UserPost> that stores list
 * of public posts. twitter is an instance of Twitter defined in twitter4j API.
 * CONSUMER_KEY,CONSUMER_KEY_SECRET,ACCESS_TOKEN,ACCESS_TOKEN_SECRET: These can
 * be obtained from developer.twitter.com. create sample App to get these access
 * tokens.
 */
public class TwitterProfileCrawlerAPI4J {

    private final static String CONSUMER_KEY = "SET_THE_KEY_HERE";
    private final static String CONSUMER_KEY_SECRET = "SET_THE_KEY_HERE";
    private final static String ACCESS_TOKEN = "SET_THE_KEY_HERE";
    private final static String ACCESS_TOKEN_SECRET = "SET_THE_KEY_HERE";
    
    private UserProfile twProfile;
    private List<UserPost> twPosts;

    private Twitter twitter;

    /**
     * This is the constructor that initializes necessary attributes of the
     * class
     */
    public TwitterProfileCrawlerAPI4J() {
	twProfile = new UserProfile();
	twPosts = new ArrayList<UserPost>();

	twitter = new TwitterFactory().getInstance();
	twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
	twitter.setOAuthAccessToken(new AccessToken(ACCESS_TOKEN,
		ACCESS_TOKEN_SECRET));
    }

    /**
     * This function takes the user name of a twitter user and crawl posts
     * 
     * @param userName
     *            : user name for a Twitter user to crawl the posts
     * @return It returns the list of UserPost. See UserPost class for
     *         attributes.
     */
    public List<UserPost> retrievePosts(String userName) {
	User user = null;

	try {
	    user = twitter.showUser(userName);
	} catch (TwitterException ex) {
	    System.out.println("Invalid user: " + ex);
	    return null;
	}

	System.out
		.println("==========================================================");
	System.out.println("                        Tweets");
	System.out
		.println("==========================================================");
	System.out.println("TweetsCount: " + user.getStatusesCount());

	int tweetCount = 0;
	int pageNo = 0;

	if (user.getStatusesCount() == 0) {
	    UserPost twPost = new UserPost();
	    twPost.setUserId(userName);
	    twPost.setPostText("");
	    twPost.setPostId("");
	    twPost.setPostDate(new Date());
	    twPost.setPlaceOfPost("");

	    getTwPosts().add(twPost);
	}
	while (tweetCount < user.getStatusesCount()) {
	    pageNo++;
	    Paging paging;
	    List<Status> tweets = null;
	    try {
		paging = new Paging(pageNo, 200);
		tweets = twitter.getUserTimeline(userName, paging);
	    } catch (TwitterException ex) {
		System.err.println("\nCan't retrieve tweets: " + ex);
		System.out
			.println("\nProgram is still running waiting for resetCount...: ");
		break;
	    }

	    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	    for (Status tweet : tweets) {

		if (tweet != null) {
		    UserPost twPost = new UserPost();
		    twPost.setUserId(userName);
		    tweetCount++;
		    System.out.println("                       ________");
		    System.out.println("                       Tweet_"
			    + tweetCount);
		    System.out.println("                       --------");
		    System.out.println("Tweet: " + tweet.getText());
		    twPost.setPostText(tweet.getText());
		    System.out.println("Tweet Id: " + tweet.getId());
		    twPost.setPostId(Long.toString(tweet.getId()));
		    System.out.println("Tweet location: "
			    + tweet.getGeoLocation());
		    System.out.println("Language: " + tweet.getLang());

		    Date tweetDate = tweet.getCreatedAt();
		    dateFormat.format(tweetDate);
		    System.out.println("Tweet time: " + tweetDate.toString());
		    twPost.setPostDate(tweetDate);
		    if (user.getStatus().getPlace() != null) {
			System.out.println("Place of tweet: "
				+ tweet.getPlace().getFullName());
			twPost.setPlaceOfPost(tweet.getPlace().getFullName());
		    } else {
			System.out.println("Place of tweet: null");
			twPost.setPlaceOfPost("");
		    }
		    getTwPosts().add(twPost);

		}

	    }

	}
	return getTwPosts();
    }

    /**
     * This methods print outs public profile informations of given twitter user
     * sets the twProfile with all those informations. getTwProfile() can be
     * used to get the profile informations.
     * 
     * @param userName
     *            : user name of a Twitter user to crawl the public profile
     *            informations.
     * @return no return value
     */
    public void startCrawling(String userName) {

	List<String> langs = new ArrayList<String>();
	Set<String> edus = new TreeSet<String>();
	Set<String> works = new TreeSet<String>();

	System.out.println("\nTwitter Profile Crawler Started...\n\n");

	User user = null;

	try {
	    user = twitter.showUser(userName);
	} catch (TwitterException ex) {
	    System.out.println("Invalid user name: " + ex);
	}

	if (user == null) {
	    System.out.println("No user found");
	    return;
	}

	System.out
		.println("==========================================================");
	System.out.println("                        Profile");
	System.out
		.println("==========================================================");
	System.out.println("Username: " + user.getScreenName());
	twProfile.setUserId(user.getScreenName());
	System.out.println("ScreenName: " + user.getName());
	twProfile.setName(user.getName());
	twProfile.setGender("");
	twProfile.setDateOfBirth("");
	System.out.println("Language: " + user.getLang());
	langs.add(user.getLang());
	twProfile.setLanguages(langs);
	System.out.println("Location: " + user.getLocation());
	twProfile.setCurrentLocation(user.getLocation());
	twProfile.setHomeLocation("");
	twProfile.setEducation(edus);
	twProfile.setEmployer(works);

    }

    /**
     * @return the twProfile
     */
    public UserProfile getTwProfile() {
	return twProfile;
    }

    /**
     * @return the twPosts
     */
    public List<UserPost> getTwPosts() {
	return twPosts;
    }

}
