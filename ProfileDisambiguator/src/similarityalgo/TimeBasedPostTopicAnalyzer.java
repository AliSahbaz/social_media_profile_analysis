/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package similarityalgo;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class has the ability to calculate the similarity based on topics
 * extracted. It can deal only with .mallet topic model files that is created
 * using TimeBasedPostTopicModel class.
 * 
 * @param path
 *            - this represents the path to the topic model file.
 */
public class TimeBasedPostTopicAnalyzer {
    private String path;

    /**
     * Constructor that initializes class attribute.
     * 
     * @param path
     *            - path to topic model file.
     */
    public TimeBasedPostTopicAnalyzer(String path) {
	this.path = path;
    }

    /**
     * This method get the topic model file (.mallet) and returns the topics
     * from that. It uses LDA topic model.
     * 
     * @param file
     *            - It should be .mallet file
     * @param noOfTopics
     *            - number of topics to extract from mallet file.
     * @return the topics as a string. Each topics are separated by a ','
     */
    public String getTopics(File file, int noOfTopics) {
	String topicKeyWords = "";

	ParallelTopicModel lda = new ParallelTopicModel(1);
	InstanceList training = InstanceList.load(file);
	lda.addInstances(training);
	// lda.estimate();

	Object[] str[] = lda.getTopWords(noOfTopics);

	for (Object s : str) {
	    Object d[] = (Object[]) s;
	    for (Object r : d) {
		topicKeyWords += r.toString() + ",";
	    }
	}

	return topicKeyWords;
    }

    /**
     * This method calculate the similarity between topics that comes from each
     * source(e.g: facebook, twitter) for each segment and then calculates the
     * overall similarity for all segments that is created already in
     * TimeBasedPostTopicAnalyzer.
     * 
     * @param sourceName1
     *            - represents the social network name that belongs to the
     *            userId1 (e.g: "facebook"). It should be same as the sourceName
     *            that is used in createTopicModel() in TimeBasedPostTopicModel.
     * @param sourceName2
     *            - represents the social network name that belongs to the
     *            userId2 (e.g: "twitter"). It should be same as the sourceName
     *            that is used in createTopicModel() in TimeBasedPostTopicModel.
     * @param userId1
     *            - represents the user name of a social network user.
     * @param userId2
     *            - represents the user name of a another social network user.
     * @param noOfSegments
     *            - number of segments that is used in createTopicModel in
     *            TimeBasedPostTopicModel.
     * @return the similarity score based on user posts in each social network.
     */
    public double getOverallTopicSimilarity(String sourceName1,
	    String sourceName2, String userId1, String userId2, int noOfSegments) {

	double score = -1.0;
	int totalMatchingKeys = 0;
	int keywords = 50;
	int segmentsCreated = 0;

	for (int i = 0; i < noOfSegments; i++) {
	    int matchingKeys = 0;

	    String fullPath = "";

	    String platform = System.getProperty("os.name");
	    if (platform.contains("Windows")) {
		fullPath = this.path + "topic_models\\" + sourceName1 + "\\"
			+ userId1 + "\\segment_" + (i + 1) + "\\"
			+ "topic_segment_" + (i + 1) + ".mallet";
	    } else if (platform.contains("Mac")) {
		fullPath = this.path + "topic_models//" + sourceName1 + "//"
			+ userId1 + "//segment_" + (i + 1) + "//"
			+ "topic_segment_" + (i + 1) + ".mallet";
	    }

	    File topicModel1 = new File(fullPath);
	    if (!topicModel1.exists()) {
		System.out.println(sourceName1 + " Segment_" + (i + 1)
			+ " not found");
		continue;
	    }
	    String topicsNetwork1 = getTopics(topicModel1, keywords);

	    File topicModel2 = new File(path + "topic_models\\" + sourceName2
		    + "\\" + userId2 + "\\segment_" + (i + 1) + "\\"
		    + "topic_segment_" + (i + 1) + ".mallet");
	    if (!topicModel2.exists()) {
		System.out.println(sourceName2 + " Segment_" + (i + 1)
			+ " not found");
		continue;
	    }

	    String topicsNetwork2 = getTopics(topicModel2, keywords);

	    // write combined keywords into a file
	    String fileName = "keywords_" + (i + 1) + ".txt";
	    File output = null;

	    if (platform.contains("Windows")) {
		output = new File(this.path + "topic_models\\keywords\\"
			+ userId1 + "_" + userId2 + "\\Keywords_segment_"
			+ (i + 1) + "\\" + fileName);
	    } else if (platform.contains("Mac")) {
		output = new File(this.path + "topic_models//keywords//"
			+ userId1 + "_" + userId2 + "//Keywords_segment_"
			+ (i + 1) + "//" + fileName);
	    }

	    if (!output.exists()) {
		output.getParentFile().mkdirs();
	    }

	    try {
		output.createNewFile();
		FileOutputStream fos = new FileOutputStream(output);

		// calculate the matching keywords
		String[] topics1 = topicsNetwork1.split(",");
		String[] topics2 = topicsNetwork2.split(",");

		for (String topic1 : topics1) {
		    for (String topic2 : topics2) {
			if (topic1.equalsIgnoreCase(topic2)) {
			    matchingKeys++;
			}
		    }
		}

		System.out.println("segment_" + (i + 1)
			+ " Matching keywords: " + matchingKeys);

		byte[] data = ("Facebook Keywords:\t" + topicsNetwork1
			+ "\n Twitter Keywords:\t" + topicsNetwork2
			+ "\nMatching keys: " + Integer.toString(matchingKeys))
			.getBytes();

		fos.write(data, 0, data.length);
		fos.flush();
		fos.close();

	    } catch (FileNotFoundException ex) {
		System.out.println(ex);
	    } catch (IOException ex) {
		System.out.println(ex);
	    }
	    totalMatchingKeys += matchingKeys;
	    segmentsCreated = (i + 1);
	    segmentsCreated++;

	}

	if (segmentsCreated == 0) {
	    System.out.println("No Common segments found");
	    return 0.0;
	}

	score = (((double) totalMatchingKeys) / (segmentsCreated * keywords));

	if (score > 1)
	    return 1.0;
	else
	    return score;

    }

}
