/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package topicmodel;

import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import user.UserPost;

public class TimeBasedPostTopicModel {

    private String path;

    /**
     * Constructor takes only one argument called path and initializes class
     * attribute.
     * 
     * @param path
     *            - path where the topic model needs to be created
     */
    public TimeBasedPostTopicModel(String path) {
	this.path = path;
    }

    /**
     * This method create topic model for given user and given posts segment by
     * segment.
     * 
     * @param sourceName
     *            - represents the source name (facebook, twitter etc.) of given
     *            user.
     * @param userName
     *            - social network user name.
     * @param posts
     *            - list of posts of relevant user.
     * @param noOfSegments
     *            - represents the total number of segments for the posts being
     *            separated.
     * @param segmentTimeLength
     *            - represents the time length of each segments (eg: 30 Days).
     * 
     */
    public void createTopicModel(String sourceName, String userName,
	    List<UserPost> posts, int noOfSegments, int segmentTimeLength) {

	// topic model is not created if there are no posts available
	if (posts.size() == 0)
	    return;

	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	Calendar cal = Calendar.getInstance();

	// Date currentDate = cal.getTime();
	// System.out.println(dateFormat.format(currentDate));

	Date temp;
	for (int i = 0, j = 0; i < noOfSegments; i++) {
	    String segmentedStr = "";

	    cal.add(Calendar.DATE, (-1) * segmentTimeLength);
	    temp = cal.getTime();

	    System.out.println("==========================================");
	    System.out.println("\nDate segment_" + (i + 1) + ": "
		    + dateFormat.format(temp));
	    System.out.println("==========================================");

	    if (j >= posts.size())
		break;

	    Date postDate = posts.get(j).getPostDate();

	    // skip if the post date is not available
	    while (postDate == null) {
		j++;
		postDate = posts.get(j).getPostDate();
	    }
	    dateFormat.format(postDate);

	    // filter the posts into relevant segment according to post date
	    while (postDate.after(temp)) {
		segmentedStr += posts.get(j).getPostText();
		j++;

		if (j >= posts.size())
		    break;
		postDate = posts.get(j).getPostDate();

		// skip if the post date is not available
		while (postDate == null) {
		    j++;
		    if (j >= posts.size())
			break;
		    postDate = posts.get(j).getPostDate();
		}
		if (j >= posts.size())
		    break;
	    }

	    // skip to next segment if the current segment has no texts
	    if (segmentedStr.equals(""))
		continue;

	    // write segmented posts in to binary files
	    byte[] data = segmentedStr.getBytes();

	    String fileName = sourceName + "_segment_" + (i + 1) + ".txt";
	    String filePath = "";

	    String platform = System.getProperty("os.name");
	    if (platform.contains("Windows")) {
		filePath = this.path + sourceName + "\\" + userName
			+ "\\segment_" + (i + 1) + "\\";
	    } else if (platform.contains("Mac")) {
		filePath = this.path + sourceName + "//" + userName
			+ "//segment_" + (i + 1) + "//";
	    }

	    File file = new File(filePath + fileName);

	    try {
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data, 0, data.length);

		fos.flush();
		fos.close();

		// create mallet file using topic modeling
		String malletName = "topic_segment_" + (i + 1) + ".mallet";

		String[] arg = { "--remove-stopwords", "true",
			"--preserve-case", "false", "--input", filePath,
			"--output", (filePath + malletName), "--keep-sequence" };

		Text2Vectors.main(arg);
	    } catch (FileNotFoundException ex) {
		System.out.println(ex);
	    } catch (IOException ex) {
		System.out.println(ex);
	    }

	    System.out.println("==========================================");
	    System.out.println("\nSegment_" + (i + 1) + "_Texts: "
		    + segmentedStr);
	    System.out.println("==========================================");
	}

    }

    /**
     * @param file
     *            - can be any mallet file created using createTopicModel()
     * @param noOfTopics
     *            - number of topics to extract from given topic model.
     * @return the topics as a string. Each topic is comma separated in it.
     */
    public static String getKeywords(File file, int noOfTopics) {
	String topicKeyWords = "";

	ParallelTopicModel lda = new ParallelTopicModel(1);
	InstanceList training = InstanceList.load(file);
	lda.addInstances(training);
	// lda.estimate();
	Object[] str[] = lda.getTopWords(noOfTopics);
	int w = 0;
	for (Object s : str) {
	    Object d[] = (Object[]) s;
	    System.out
		    .println("Topic Words are in sorted order(from high to low) : \n");
	    System.out.print("Topic Number : " + w++ + "\t" + "Topic Words : ");

	    for (Object r : d) {

		System.out.print(" " + r.toString());
		topicKeyWords += r.toString() + ",";
	    }
	    System.out.println("\n");
	}

	return topicKeyWords;
    }

    /**
     * This method extract topics from given topic model file and save them
     * inside \topics directory.
     * 
     * @param filePath
     *            - path to topic model file
     * @param malletName
     *            - name of the topic model file(i.e: .mallet file)
     * @param noOfTopics
     *            - number of topics to extract from given topic model.
     */
    public static void extractTopics(String filePath, String malletName,
	    int noOfTopics) {

	// create mallet file by topic modeling
	String[] arg = { "--remove-stopwords", "true", "--preserve-case",
		"false", "--input", filePath, "--output",
		(filePath + malletName), "--keep-sequence" };

	try {
	    Text2Vectors.main(arg);

	    File mallet = new File(filePath + malletName);

	    String keywords = getKeywords(mallet, noOfTopics);

	    // write the combined keywords into a file
	    String fileName = "Topic_keywords.txt";
	    File output = null;

	    String platform = System.getProperty("os.name");
	    if (platform.contains("Windows")) {
		output = new File(filePath + "\\topics\\" + fileName);
	    } else if (platform.contains("Mac")) {
		output = new File(filePath + "//topics//" + fileName);
	    }

	    if (!output.exists()) {
		output.getParentFile().mkdirs();
		output.createNewFile();
	    }

	    FileOutputStream fos = new FileOutputStream(output);

	    byte[] data = keywords.getBytes();
	    fos.write(data, 0, data.length);
	    fos.close();
	} catch (FileNotFoundException ex) {

	} catch (IOException ex) {

	}

    }

    public static void main(String args[]) {

	String path = "C:\\Users\\diska\\Dropbox\\Karthick_RA@NUS\\Results_Example\\ExtractedTopics\\SelectedPost\\";
	String malletName = "example_1.mallet";
	extractTopics(path, malletName, 30);

    }

}
