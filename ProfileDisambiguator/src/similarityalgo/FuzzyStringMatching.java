/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package similarityalgo;

public class FuzzyStringMatching {

    /**
     * This function calculates the similarity of two strings. It handles even
     * reverse order of strings.
     * 
     * @param str1
     *            : String
     * @param str2
     *            : String
     * @return the similarity score of two strings. (Best match => 1, no match
     *         => 0) Eg: str1 = "John Vicky", str2 = "Vicky" => 0.5 str1 =
     *         "John Vicky", str2 = "Vicky John" => 1.0
     */
    public static double getMatchingScore(String str1, String str2) {
	int matches = 0;
	double similarityScore;
	double partilaScore = 0;
	int maxSubStrMatch = 0;

	if (str1.length() == 0 || str2.length() == 0)
	    return 0.0;
	else if (str1.equalsIgnoreCase(str2))
	    return 1;

	else {
	    String[] words1 = str1.split("\\s+");
	    String[] words2 = str2.split("\\s+");

	    for (String word1 : words1) {
		for (String word2 : words2) {
		    if (word1.equalsIgnoreCase(word2)) {
			matches++;
		    } else {
			int minWordLenth, maxWordLength;
			if (word1.length() < word2.length()) {
			    minWordLenth = word1.length();
			    maxWordLength = word2.length();
			}

			else {
			    minWordLenth = word2.length();
			    maxWordLength = word1.length();
			}

			for (int i = 0; i < minWordLenth; i++) {
			    if (word1.substring(0, i + 1).equalsIgnoreCase(
				    word2.substring(0, i + 1))) {
				if (maxSubStrMatch < (i + 1)) {
				    maxSubStrMatch = i + 1;
				    partilaScore = (double) maxSubStrMatch
					    / maxWordLength;
				}

			    }

			}

		    }
		}
	    }
	    if (words1.length > words2.length)
		similarityScore = (matches + partilaScore) / words1.length;
	    else
		similarityScore = (matches + partilaScore) / words2.length;

	    if (similarityScore == 1)
		return 1;

	}
	return similarityScore;
    }

}
