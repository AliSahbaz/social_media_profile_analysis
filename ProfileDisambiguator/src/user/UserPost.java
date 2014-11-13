/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package user;

import java.util.Comparator;
import java.util.Date;

/**
 * UserPost is used to model a social network user with their posts published on
 * social network. userID: a social network user name that uniquely identifies a
 * user. postId: a unique identifier for each post placeOfPost: location of
 * published post. postText: post text description. postDate: post date UserPost
 * implements Comparator to override the compare method. This helps to sort the
 * list of UserPost based on post date.
 * */
public class UserPost implements Comparator<UserPost> {

    private String userId, postId, placeOfPost, postText;
    private Date postDate;

    /**
     * @return the userId
     */
    public String getUserId() {
	return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
	this.userId = userId;
    }

    /**
     * @return the placeOfPost
     */
    public String getPlaceOfPost() {
	return placeOfPost;
    }

    /**
     * @param placeOfPost
     *            the placeOfPost to set
     */
    public void setPlaceOfPost(String placeOfPost) {
	this.placeOfPost = placeOfPost;
    }

    /**
     * @return the postText
     */
    public String getPostText() {
	return postText;
    }

    /**
     * @param postText
     *            the postText to set
     */
    public void setPostText(String postText) {
	this.postText = postText;
    }

    /**
     * @return the postDate
     */
    public Date getPostDate() {
	return postDate;
    }

    /**
     * @param postDate
     *            the postDate to set
     */
    public void setPostDate(Date postDate) {
	this.postDate = postDate;
    }

    /**
     * @return the postId
     */
    public String getPostId() {
	return postId;
    }

    /**
     * @param postId
     *            the postId to set
     */
    public void setPostId(String postId) {
	this.postId = postId;
    }

    /**
     * @override the compare method based on post date *
     */
    public int compare(UserPost post1, UserPost post2) {
	return post1.getPostDate().compareTo(post2.getPostDate());
    }

}
