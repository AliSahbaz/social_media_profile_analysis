<h3>User Identification Across Different Social Networks</h3>
<hr>__________________________________________________</hr>


<p> An application developed in java (Eclipse maven 3.1) </p>

<h4> Introduction </h4>

* Social networks allow users to create virtual identities online, typically following advertising based business model. Advertising schemes on social networks could be more effective if networks could see the full set of information an individual posts across several platforms, which is challenging because users may not identify themselves in exactly the same way on each platform. This requires matching user identities across platforms. This is an identity matching system that can be used for user identification, using static profile characteristics (eg: name, location and gender etc.) as well as topic modeling approach using dynamic post data. It uses data analysis techniques together with machine learning approaches for this purpose.
* So, this repository consists of work done to identify users when the user ids for different social network platforms such as facebook, twitter and google+ are feeded.
* It performs analysis pairwise (eg: facebook-twitter, facebook-google+ and so on).
        
<h4> Setup and Pre-requesties</h4>
* java SDK 1.7
* Firefox 31 need to be installed to run this application.
* svm model should be located inside the "/output/svm/model" directory

<h4> Setup Credentials for API Calls </h4>
* This application deals with Facebook Graph API, Twitter API and Google+ API to retrieve data.
* It is important to setup api keys in order to system work properly. 
<h5> Create Twitter API Credentials </h5>
* Go to https://dev.twitter.com/overview/documentation and create sample application to get all the access tokens.(See this step by step guide http://www.prophoto.com/support/twitter-api-credentials/)
* Once you get your all credentials, edit the TwitterProfileCrawlerAPI4J.java ("src/profilecrawler/") to replace all "SET_THE_KEY_HERE" with those 4 keys.
<h5> Create Google API Credentials </h5>
* Go to https://console.developers.google.com/project and create a sample project to get reqired credentials.
* Once you create the project, enable APIs such as Geocoding API, Distance Matrix API and Google+ API under APIs & auth.
* Navigate to credentials section and then note down the API Key under Public API access -> Key for server applications.
* Edit the ProfileDisambiguator.java("src/profiledisambiguator/"), replace "SET_THE_KEY_HERE" inside main() with the key obtained.
<h5> Create Facebook API Credentials </h5>
* Go to https://developers.facebook.com/tools/explorer/ and get the access token by clicking the relevant button. You need to key in this token when application prompts to run.
 
##### NOTE: All the outputs will be generated and saved inside "/output/" directory.

<hr>__________________________________________________</hr>
<p> © NUS, 2014. @Developer: Karthick (karthyuom@gmail.com) </p>
