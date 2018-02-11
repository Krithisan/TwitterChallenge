# TwitterChallenge
Salesforce tweet reader displaying latest 10 tweets,with option to search

The main project directory with the source is feedreader, it contains the test class also.
This is a web application built using java back end and jsp front end with lightning design system used for styling.

It uses the TwitterAPI to get the 10 latest tweets from @salesforce user timeline. Retweets are skipped.
It has a tomcat server, when run it opens up the page with description and 2 buttons for fetch and search.

For the first time the user needs to use the button to fetch and therafter it is refreshed every min to show the most recent 10 tweets.
If search is run without a search string or if it returns no results it shows up on the screen to let the user know.
