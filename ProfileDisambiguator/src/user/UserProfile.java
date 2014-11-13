/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */
package user;

import java.util.List;
import java.util.Set;

/**
 * UserProfile is used to model a social network user with basic profile
 * informations together with work and education details. userId: a social
 * network user name that uniquely identifies a user. name: name that is
 * displayed on social network. gender: male/female dateOfBirth: user's date of
 * birth. currentLocation: user's current location. homeLocation: user's home
 * town location. education: list of education history. employer: list of work
 * experience. friends: list of user's friends' name.
 * 
 */
public class UserProfile {
    private String userId, name, gender, dateOfBirth, currentLocation,
	    homeLocation;
    private List<String> languages;
    private Set<String> education, employer;
    private List<String> friends;

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
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the gender
     */
    public String getGender() {
	return gender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public void setGender(String gender) {
	this.gender = gender;
    }

    /**
     * @return the languages
     */
    public List<String> getLanguages() {
	return languages;
    }

    /**
     * @param languages
     *            the languages to set
     */
    public void setLanguages(List<String> languages) {
	this.languages = languages;
    }

    /**
     * @return the currentLocation
     */
    public String getCurrentLocation() {
	return currentLocation;
    }

    /**
     * @param currentLocation
     *            the currentLocation to set
     */
    public void setCurrentLocation(String currentLocation) {
	this.currentLocation = currentLocation;
    }

    /**
     * @return the homeLocation
     */
    public String getHomeLocation() {
	return homeLocation;
    }

    /**
     * @param homeLocation
     *            the homeLocation to set
     */
    public void setHomeLocation(String homeLocation) {
	this.homeLocation = homeLocation;
    }

    /**
     * @return the education
     */
    public Set<String> getEducation() {
	return education;
    }

    /**
     * @param education
     *            the education to set
     */
    public void setEducation(Set<String> education) {
	this.education = education;
    }

    /**
     * @return the employer
     */
    public Set<String> getEmployer() {
	return employer;
    }

    /**
     * @param employer
     *            the employer to set
     */
    public void setEmployer(Set<String> employer) {
	this.employer = employer;
    }

    /**
     * @return the dateOfBirth
     */
    public String getDateOfBirth() {
	return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            the dateOfBirth to set
     */
    public void setDateOfBirth(String dateOfBirth) {
	this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the friends
     */
    public List<String> getFriends() {
	return friends;
    }

    /**
     * @param friends
     *            the friends to set
     */
    public void setFriends(List<String> friends) {
	this.friends = friends;
    }

}
