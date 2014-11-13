/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package profiledisambiguator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import profilecrawler.FacebookProfileCrawler;
import profilecrawler.GooglePlusProfileCrawler;
import profilecrawler.TwitterProfileCrawlerSelenium;
import similarityalgo.Location;
import similarityalgo.TimeBasedPostTopicAnalyzer;
import similarityalgo.FuzzyStringMatching;
import svm.SVMClassification;
import topicmodel.TimeBasedPostTopicModel;
import user.UserPost;
import user.UserProfile;
import util.ExcelDoc;

public class ProfileDisambiguator {

    private static final String MODEL_NAME = "svm_last_linear_80_dist.train";
    private static ExcelDoc excel;

    /**
     * This method calculates the similarity between friend list for each social
     * network platform.
     * 
     * @param profile1
     *            - instance of UserProfile for one social network that keeps
     *            all the static profile information such as name, location and
     *            friend list etc.
     * @param profile2
     *            - instance of UserProfile for another social network that
     *            keeps all the static profile information such as name,
     *            location and friend list etc.
     * @return the similarity score of friend list matching
     */
    public double getFriendListSimilarity(UserProfile profile1,
	    UserProfile profile2) {

	double alpha = 70.0;
	double scoreNetwork1 = 0.0;
	double scoreNetwork2 = 0.0;
	double score = 0.0;
	int matchingCount = 0;

	List<String> friendsNetwork1, friendsNetwork2;
	friendsNetwork1 = profile1.getFriends();
	friendsNetwork2 = profile2.getFriends();

	for (String friend1 : friendsNetwork1) {
	    double sim = 0.0;

	    for (String friend2 : friendsNetwork2) {

		if (friend1.equalsIgnoreCase(friend2)) {
		    sim = 1.0;
		    matchingCount++;
		    break;
		}

	    }
	    scoreNetwork1 += sim;
	}

	for (String friend2 : friendsNetwork2) {
	    double sim = 0.0;

	    for (String friend1 : friendsNetwork1) {
		if (friend1.equalsIgnoreCase(friend2)) {
		    sim = 1.0;
		    break;
		}
	    }
	    scoreNetwork2 += sim;
	}

	score = alpha * (scoreNetwork1 * scoreNetwork2)
		/ (friendsNetwork1.size() * friendsNetwork2.size());

	if (score > 1)
	    return 1.0;

	System.out.println("Friends matching count: " + matchingCount);

	return score;
    }

    /**
     * This method analyzes two profiles derived from facebook and twitter.
     * 
     * @param path
     *            - path to output location.
     * @param fbAccessToken
     *            - Facebook Graph API access token that is used for api
     *            requests.
     * @param googleApiKey
     *            - Google API key that is used for api calls such as distance
     *            measure, google+ profile information and posts retrieval.
     * @param fbUserName
     *            - user name of the facebook user.
     * @param twUserName
     *            - user name of the twitter user.
     * @param excel
     *            - instance of ExcelDoc that logs all the profile reports.
     */
    public void realTimeProfileAnalyzerFbTw(String path, String fbAccessToken,
	    String googleApiKey, String fbUserName, String twUserName,
	    ExcelDoc excel) {

	double features[] = new double[6];
	String pathToTopicModel = "";
	String pathToSvm = "";

	String platform = System.getProperty("os.name");
	if (platform.contains("Windows")) {
	    pathToTopicModel = path + "\\topic_models\\";
	    pathToSvm = path + "\\svm\\";
	} else if (platform.contains("Mac")) {
	    pathToTopicModel = path + "//topic_models//";
	    pathToSvm = path + "//svm//";
	}

	FacebookProfileCrawler fb = new FacebookProfileCrawler(fbAccessToken);

	fb.startCrawling(fbUserName);
	fb.fetchUserPosts(fbUserName);

	// create time based topic modeling for facebook user
	TimeBasedPostTopicModel topicModel = new TimeBasedPostTopicModel(
		pathToTopicModel);

	topicModel.createTopicModel("facebook", fb.getFbProfile().getUserId(),
		fb.getFbPosts(), 10, 37);

	TwitterProfileCrawlerSelenium tw = new TwitterProfileCrawlerSelenium();
	tw.startCrawling(twUserName);
	tw.fetchUserPosts(twUserName);

	// create time based topic modeling for twitter user
	topicModel.createTopicModel("twitter", tw.getTwProfile().getUserId(),
		tw.getTwPosts(), 10, 37);

	// write all profile analysis report into excel file
	writeProfileReport(path, "facebook", "twitter", tw.getTwProfile(),
		fb.getFbProfile());

	// prediction of two profiles using svm model
	SVMClassification svmTraining = new SVMClassification(pathToSvm,
		MODEL_NAME);

	features = calculateFeatureScores(path, googleApiKey, "facebook",
		"twitter", fb.getFbProfile(), tw.getTwProfile(),
		fb.getFbPosts(), tw.getTwPosts());

	svmTraining.predict(fbUserName, twUserName, excel, features);
	System.out
		.println("\n======================================================");

    }

    /**
     * This method analyzes two profiles derived from facebook and google+.
     * 
     * @param path
     *            - path to output location.
     * @param fbAccessToken
     *            - Facebook Graph API access token that is used for api
     *            requests.
     * @param googleApiKey
     *            - Google API key that is used for api calls such as distance
     *            measure, google+ profile information and posts retrieval.
     * @param fbUserName
     *            - user name of the facebook user.
     * @param gPlusUSerName
     *            - user name of the google+ user.
     * @param excel
     *            - instance of ExcelDoc that logs all the profile reports.
     */
    public void realTimeProfileAnalyzerGPlusFb(String path,
	    String fbAccessToken, String googleApiKey, String fbUserName,
	    String gPlusUSerName, ExcelDoc excel) {

	double features[] = new double[6];

	String pathToTopicModel = "";
	String pathToSvm = "";

	String platform = System.getProperty("os.name");
	if (platform.contains("Windows")) {
	    pathToTopicModel = path + "\\topic_models\\";
	} else if (platform.contains("Mac")) {
	    pathToSvm = path + "\\svm\\";
	}

	FacebookProfileCrawler fb = new FacebookProfileCrawler(fbAccessToken);

	fb.startCrawling(fbUserName);
	fb.fetchUserPosts(fbUserName);

	// create topic models for facebook user
	TimeBasedPostTopicModel topicModel = new TimeBasedPostTopicModel(
		pathToTopicModel);

	topicModel.createTopicModel("facebook", fb.getFbProfile().getUserId(),
		fb.getFbPosts(), 10, 37);

	GooglePlusProfileCrawler gPlus = new GooglePlusProfileCrawler(
		googleApiKey);

	gPlus.startCrawling(gPlusUSerName);
	gPlus.fetchUserPosts(gPlusUSerName);

	// create topic models for google+ suer
	topicModel.createTopicModel("googleplus", gPlus.getPlusProfile()
		.getUserId(), gPlus.getPlusPosts(), 10, 37);

	// write all profile analysis report into excel file
	writeProfileReport(path, "facebook", "googleplus",
		gPlus.getPlusProfile(), fb.getFbProfile());

	// prediction of two profiles using svm model
	SVMClassification svmTraining = new SVMClassification(pathToSvm,
		MODEL_NAME);

	features = calculateFeatureScores(path, googleApiKey, fbUserName,
		gPlusUSerName, fb.getFbProfile(), gPlus.getPlusProfile(),
		fb.getFbPosts(), gPlus.getPlusPosts());

	svmTraining.predict(fbUserName, gPlusUSerName, excel, features);
	System.out
		.println("\n======================================================");
    }

    /**
     * This method analyzes two profiles derived from twitter and google+.
     * 
     * @param path
     *            - path to output location.
     * @param googleApiKey
     *            - Google API key that is used for api calls such as distance
     *            measure, google+ profile information and posts retrieval.
     * @param twUserName
     *            - user name of the twitter user.
     * @param gPlusUSerName
     *            - user name of the google+ user.
     * @param excel
     *            - instance of ExcelDoc that logs all the profile reports.
     */
    public void realTimeProfileAnalyzerTwGPlus(String path,
	    String googleApiKey, String twUserName, String gPlusUSerName,
	    ExcelDoc excel) {

	double features[] = new double[6];

	String pathToTopicModel = "";
	String pathToSvm = "";

	String platform = System.getProperty("os.name");
	if (platform.contains("Windows")) {
	    pathToTopicModel = path + "\\topic_models\\";
	} else if (platform.contains("Mac")) {
	    pathToSvm = path + "\\svm\\";
	}

	TwitterProfileCrawlerSelenium tw = new TwitterProfileCrawlerSelenium();
	tw.startCrawling(twUserName);
	tw.fetchUserPosts(twUserName);

	// create topic models for twitter user
	TimeBasedPostTopicModel topicModel = new TimeBasedPostTopicModel(
		pathToTopicModel);

	topicModel.createTopicModel("twitter", tw.getTwProfile().getUserId(),
		tw.getTwPosts(), 10, 37);

	GooglePlusProfileCrawler gPlus = new GooglePlusProfileCrawler(
		googleApiKey);
	gPlus.startCrawling(gPlusUSerName);// "118122914829503351606"
	gPlus.fetchUserPosts(gPlusUSerName);

	// create topic models for google+ user
	topicModel.createTopicModel("googleplus", gPlus.getPlusProfile()
		.getUserId(), gPlus.getPlusPosts(), 10, 37);

	// write all profile analysis report into excel file
	writeProfileReport(path, "twitter", "googleplus", tw.getTwProfile(),
		gPlus.getPlusProfile());

	// prediction of two profiles using svm model
	SVMClassification svmTraining = new SVMClassification(pathToSvm,
		MODEL_NAME);

	features = calculateFeatureScores(path, googleApiKey, twUserName,
		gPlusUSerName, tw.getTwProfile(), gPlus.getPlusProfile(),
		tw.getTwPosts(), gPlus.getPlusPosts());

	svmTraining.predict(twUserName, gPlusUSerName, excel, features);
	System.out
		.println("\n======================================================");
    }

    /**
     * @param path
     *            - path to output location
     * @param googleApiKey
     *            - Google API key that is used for api calls such as distance
     *            measure, google+ profile information and posts retrieval.
     * @param sourceName1
     *            - social network name of profile1 (e.g: "facebook")
     * @param sourceName2
     *            - social network name of profile2 (e.g: "twitter")
     * @param profile1
     *            - instance of UserProfile for one social network that keeps
     *            all the static profile information such as name, location and
     *            friend list etc.
     * @param profile2
     *            - instance of UserProfile for another social network that
     *            keeps all the static profile information such as name,
     *            location and friend list etc.
     * @param post1
     *            - instance of UserPost for sourceName1 that keeps all the
     *            dynamic post information
     * @param post2
     *            - instance of UserPost for sourceName2 that keeps all the
     *            dynamic post information
     * @return the feature vector derived from two profiles.
     */
    public double[] calculateFeatureScores(String path, String googleApiKey,
	    String sourceName1, String sourceName2, UserProfile profile1,
	    UserProfile profile2, List<UserPost> post1, List<UserPost> post2) {

	double features[] = new double[6]; // features in svm model
	double nameSimilarity; // similarity between users' names
	double currentDistance = -1.0; // distance between users' current
				       // location
	double homeDistance = -1.0; // distance between users' home location
	double bestDistance = -1; // minimum distance chosen from homeDistance &
				  // bestDistance
	double overallTopicSimilarity = 0.0; // similarity between users' posts
	double friendListSimilarity = 0.0; // similarity of users' friend list

	TimeBasedPostTopicAnalyzer topicAnalyzer = new TimeBasedPostTopicAnalyzer(
		path);
	Location twLocationStr1 = null;
	Location fbUserHomeLocationStr = null;
	Location fbCurrentLocationStr = null;

	// calculate the name similarity
	nameSimilarity = FuzzyStringMatching.getMatchingScore(
		profile2.getName(), profile1.getName());

	// calculate the best distance
	String twUserLocation = profile2.getCurrentLocation().replace(',', '+')
		.replace(' ', '+');
	String twUserLocation1 = "", twUserLocation2 = "";
	if (twUserLocation.contains("iPhone:")) {
	    String temp[] = twUserLocation.split("iPhone:");
	    System.out.println("Tw user loc: " + temp[1]);
	    twLocationStr1 = new Location(googleApiKey, temp[1]);
	} else if (twUserLocation.contains("&")) {
	    String locs[] = twUserLocation.split("&");
	    twUserLocation1 = locs[0];
	    twUserLocation2 = locs[1];
	}
	if (!twUserLocation1.equals("") && !twUserLocation2.equals("")) {
	    System.out.println("Tw user loc: " + twUserLocation1);
	    twLocationStr1 = new Location(googleApiKey, twUserLocation1);
	} else if (twLocationStr1 == null) {
	    System.out.println("Tw user loc: " + twUserLocation);
	    twLocationStr1 = new Location(googleApiKey, twUserLocation);
	}

	String fbUserCurrentLocation = profile1.getCurrentLocation()
		.replace(',', '+').replace(' ', '+');
	System.out.println("Fb user current loc: " + fbUserCurrentLocation);

	fbCurrentLocationStr = new Location(googleApiKey, fbUserCurrentLocation);

	String fbUserHomeLocation = profile1.getHomeLocation()
		.replace(',', '+').replace(' ', '+');
	System.out.println("Fb user home loc: " + fbUserHomeLocation);
	fbUserHomeLocationStr = new Location(googleApiKey, fbUserHomeLocation);

	if (twUserLocation.equalsIgnoreCase(fbUserCurrentLocation)) {
	    currentDistance = 0.0;
	}

	else if (twUserLocation.equalsIgnoreCase(fbUserHomeLocation)) {
	    homeDistance = 0.0;
	}

	else {
	    currentDistance = twLocationStr1
		    .getEuclideanDistance(fbCurrentLocationStr);
	    homeDistance = twLocationStr1
		    .getEuclideanDistance(fbUserHomeLocationStr);

	    bestDistance = currentDistance < homeDistance ? currentDistance
		    : homeDistance;

	}

	// calculate the post topics similarity
	if (post1.isEmpty() || post2.isEmpty()) {
	    overallTopicSimilarity = -1.0;
	} else {

	    overallTopicSimilarity = topicAnalyzer.getOverallTopicSimilarity(
		    sourceName1, sourceName2, profile1.getUserId(),
		    profile2.getUserId(), 10);
	}

	// calculate the friend list similarity
	if (profile2.getFriends().isEmpty() || profile1.getFriends().isEmpty()) {
	    friendListSimilarity = 0.0;
	} else {
	    friendListSimilarity = getFriendListSimilarity(profile2, profile1);
	}

	features[0] = -1; // label is initialized as unknown
	features[1] = 0;
	features[2] = overallTopicSimilarity;
	features[3] = nameSimilarity;
	features[4] = friendListSimilarity;
	features[5] = bestDistance;

	System.out
		.println("\n\n=================PROFILE REPORT=======================");
	System.out.println("\nName simialrity: " + nameSimilarity);
	System.out.println("Distance between current locations: "
		+ currentDistance);
	System.out.println("Distance between home locations: " + homeDistance);
	System.out.println("Post Topic Similarity: " + overallTopicSimilarity);
	System.out.println("Friend list simialrity: " + friendListSimilarity);

	return features;
    }

    /**
     * @return the instance of ExcelDoc
     */
    public ExcelDoc getExcel() {
	return excel;
    }

    /**
     * This method writes all the profile information such as user id, name and
     * lcoation for each profile.
     * 
     * @param path
     *            - path to output location.
     * @param sourceName1
     *            - social network name of profile1 (e.g: "facebook")
     * @param sourceName2
     *            - social network name of profile1 (e.g: "twitter")
     * @param profile1
     *            - instance of UserProfile for one social network that keeps
     *            all the static profile information such as name, location and
     *            friend list etc.
     * @param profile2
     *            - instance of UserProfile for another social network that
     *            keeps all the static profile information such as name,
     *            location and friend list etc.
     */
    public void writeProfileReport(String path, String sourceName1,
	    String sourceName2, UserProfile profile1, UserProfile profile2) {

	ExcelDoc excel = new ExcelDoc(path, "Profile_" + sourceName1 + "_"
		+ sourceName2 + ".xls");

	List<String> colNames = new ArrayList<String>();
	colNames.add(sourceName1 + "_id");
	colNames.add(sourceName2 + "_id");
	colNames.add(sourceName1 + "_name");
	colNames.add(sourceName2 + "_name");
	colNames.add(sourceName1 + "_current_location");
	colNames.add(sourceName2 + "_current_location");
	colNames.add(sourceName1 + "_home_location");
	colNames.add(sourceName2 + "_home_location");

	excel.createSheet("profile", colNames);

	excel.createNextRow(profile2.getUserId(), profile1.getUserId(),
		profile2.getName(), profile1.getName(),
		profile2.getCurrentLocation(), profile1.getCurrentLocation(),
		profile2.getHomeLocation(), profile1.getHomeLocation());

    }

    public static void testFbTwPair(String fbAccessToken, String googleApiKey) {
	String fbUserName = "", twUserName = "";
	String path = "";
	ExcelDoc excel = null;

	try {
	    System.out.print("\nEnter Facebook user name:");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));
	    fbUserName = br.readLine();
	    System.out.print("Enter Twitter user name:");
	    twUserName = br.readLine();
	} catch (IOException ex) {
	    System.out.println(ex);
	}

	System.out.println("\nFacebook Twitter analysis in progress...");

	String platform = System.getProperty("os.name");

	if (platform.contains("Windows")) {
	    path = System.getProperty("user.dir") + "\\output\\";
	    excel = new ExcelDoc(path + "\\svm\\",
		    "svm_prediction_output_facebook_twitter.xls");
	}

	else if (platform.contains("Mac")) {
	    path = System.getProperty("user.dir") + "//output//";
	    excel = new ExcelDoc(path + "//svm//",
		    "svm_prediction_output_facebook_twitter.xls");
	}

	List<String> colNames = new ArrayList<String>();
	colNames.add("Fb Id");
	colNames.add("Tw Id");
	colNames.add("Probability of '1'");
	colNames.add("Probability of '0'");
	colNames.add("Prediction");

	excel.createSheet("prediction", colNames);

	new ProfileDisambiguator().realTimeProfileAnalyzerFbTw(path,
		fbAccessToken, googleApiKey, fbUserName, twUserName, excel);
    }

    public static void testGPlusFbPair(String fbAccessToken, String googleApiKey) {

	String fbUserName = "", gPlusUserName = "";
	String path = "";
	ExcelDoc excel = null;

	try {
	    System.out.print("Enter Facebook user name:");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));
	    fbUserName = br.readLine();
	    System.out.print("Enter GooglePlus user name:");
	    gPlusUserName = br.readLine();
	} catch (IOException ex) {
	    System.out.println(ex);
	}

	System.out.println("Facebook Google+ analysis in progress...");

	String platform = System.getProperty("os.name");

	if (platform.contains("Windows")) {
	    path = System.getProperty("user.dir") + "\\output\\";
	    excel = new ExcelDoc(path + "\\svm\\",
		    "svm_prediction_output_facebook_google.xls");
	}

	else if (platform.contains("Mac")) {
	    path = System.getProperty("user.dir") + "//output//";
	    excel = new ExcelDoc(path + "//svm//",
		    "svm_prediction_output_facebook_google.xls");
	}

	List<String> colNames = new ArrayList<String>();
	colNames.add("Fb Id");
	colNames.add("GooglePlus Id");
	colNames.add("Probability of '1'");
	colNames.add("Probability of '0'");
	colNames.add("Prediction");

	excel.createSheet("prediction", colNames);

	new ProfileDisambiguator().realTimeProfileAnalyzerGPlusFb(path,
		fbAccessToken, googleApiKey, fbUserName, gPlusUserName, excel);
    }

    public static void testGPlusTWPair(String googleApiKey) {
	String twUserName = "", gPlusUserName = "";
	String path = "";
	ExcelDoc excel = null;

	try {
	    System.out.print("Enter Twitter user name:");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));
	    twUserName = br.readLine();
	    System.out.print("Enter GooglePlus user name:");
	    gPlusUserName = br.readLine();
	} catch (IOException ex) {
	    System.out.println(ex);
	}

	System.out.println("Twitter Google+ analysis in progress...");

	String platform = System.getProperty("os.name");

	if (platform.contains("Windows")) {
	    path = System.getProperty("user.dir") + "\\output\\";
	    excel = new ExcelDoc(path + "\\svm\\",
		    "svm_prediction_output_twitter_google.xls");
	} else if (platform.contains("Mac")) {
	    path = System.getProperty("user.dir") + "//output//";
	    excel = new ExcelDoc(path + "//svm//",
		    "svm_prediction_output_twitter_google.xls");
	}

	List<String> colNames = new ArrayList<String>();
	colNames.add("Tw Id");
	colNames.add("GooglePlus Id");
	colNames.add("Probability of '1'");
	colNames.add("Probability of '0'");
	colNames.add("Prediction");

	excel.createSheet("prediction", colNames);

	new ProfileDisambiguator().realTimeProfileAnalyzerTwGPlus(path,
		googleApiKey, twUserName, gPlusUserName, excel);

    }

    public static void main(String[] args) throws IOException {

	String googleApiKey = "SET_THE_KEY_HERE";

	int option = 0;

	while (true) {

	    System.out.println("\n__________________________");
	    System.out.println("What do you want to do ");
	    System.out.println("==========================");
	    System.out.println("1. Facebook-Twitter user identification");
	    System.out.println("2. Facebook-Google+ user identification");
	    System.out.println("3. Twitter-Google+ user identification");
	    System.out.println("4. Exit");
	    System.out.println("__________________________");
	    System.out.print("\nEnter your option(1-4): ");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    System.in));
	    option = Integer.parseInt(br.readLine());

	    if (option == 4) {
		break;
	    }

	    switch (option) {
	    case 1:
		System.out.print("\nEnter Facebook Access Token: ");
		String accessToken = "";
		accessToken = br.readLine();
		testFbTwPair(accessToken, googleApiKey);
		break;
	    case 2:
		System.out.print("\nEnter Facebook Access Token: ");
		accessToken = br.readLine();
		testGPlusFbPair(accessToken, googleApiKey);
		break;
	    case 3:
		testGPlusTWPair(googleApiKey);
		break;
	    }
	}

    }

}
