package feedreader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/TweetReaderServlet")
public class TweetReaderServlet extends HttpServlet {
	/**
	 * Servlet performs the function based on the button click- fetch, search actions.
	 */
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("unused")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		TwitterFactory tf = new TwitterFactory();
	    Twitter twitter = tf.getInstance();
	    String action = request.getParameter("action");
	    String searchString = request.getParameter("input");
	    List<Status> statuses = new ArrayList<Status>();
	   
	    if(action == null) action = "fetch";
	       
		try {
			User user = twitter.verifyCredentials();
	        List<TweetVO> tweetList = new ArrayList<TweetVO>();
	        Paging page = new Paging (1, 50);
	        
	        //Get the latest tweets when action is fetch , or when search with a valid search string
	        if(action.equalsIgnoreCase("fetch") || (action.equalsIgnoreCase("search") && searchString != null)) {
	        	statuses = twitter.getUserTimeline("salesforce",page);
	        }
	        
	        //When fetch, populate the tweetVO and send back the list to display
			if(action.equalsIgnoreCase("fetch")) {
				tweetList = populateTweet(statuses);
				request.setAttribute("tweetlist", tweetList);
			}
			
			//When action is search, populate the latest tweets into list and search based on search string to filter
			//send back the filtered tweet list
	        else if(action.equalsIgnoreCase("search")) {
	        	List<TweetVO> filteredTweetlist = new ArrayList<TweetVO>();
	        	if(searchString != null && !searchString.isEmpty()) {
	        	List<TweetVO> list = populateTweet(statuses);
	        	
	        		for(TweetVO t : list) {
	        			if(t.getTweetContent().contains(searchString)) {
	        				filteredTweetlist.add(t);
	        			}
	        		}
	        	}
	        	else
	        		request.setAttribute("noSearchString", "true");
	        	if(filteredTweetlist.isEmpty()) request.setAttribute("noResults", "true");
	        	request.setAttribute("tweetlist", filteredTweetlist);
	        }
	        request.getRequestDispatcher("index.jsp").forward(request, response);
		}
	    catch (TwitterException e) {
		// TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	
	}

	protected static String parseTweet(String tweetText) {
		 
		 //check if test contains http, https and convert the into url links
		if(tweetText != null) {
			
			if(tweetText.contains("https:") || tweetText.contains("http:")) {
				int endPoint = 0;
				String tweet = tweetText;	
				while(endPoint < tweet.length()) {
					int indexOfHttp = tweet.indexOf("http:") ;
					int indexOfHttps = tweet.indexOf("https:");
					int index = 0;
					if(indexOfHttp > indexOfHttps && indexOfHttps != -1) index = indexOfHttps;
					else if(indexOfHttps > indexOfHttp && indexOfHttp != -1) index = indexOfHttp;
					else if(indexOfHttp == -1) index = indexOfHttps;
					else index = indexOfHttp;
					endPoint = (tweet.indexOf(' ', index) != -1) ? tweet.indexOf(' ', index) : tweet.length();
			        String url = tweet.substring(index, endPoint);
			        tweetText = tweetText.replace(url, replaceLinks(url));
			        boolean resetEndPoint = false;
			        if(endPoint < tweet.length()) resetEndPoint = true;
			        tweet = tweet.substring(endPoint);    
			        if(resetEndPoint) endPoint = 0;
				}
			}
		    String patternStr = "(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)";
		    Pattern pattern = Pattern.compile(patternStr);
		    Matcher matcher = pattern.matcher(tweetText);
		    String result = "";
		 
		    // Checking for Hashtags
		    while (matcher.find()) {
		        result = matcher.group();
		        result = result.replace(" ", "");
		        String search = result.replace("#", "");
		        String searchHTML="<a href='https://twitter.com/hashtag/" + search + "?src=hash'>"+result+"</a>";
		        tweetText = tweetText.replace(result,searchHTML);
		    }
		 
		    // Checking for @screenname
		    patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		    pattern = Pattern.compile(patternStr);
		    matcher = pattern.matcher(tweetText);
		    while (matcher.find()) {
		        result = matcher.group();
		        result = result.replace(" ", "");
		        String rawName = result.replace("@", "");
		        String userHTML="<a href='https://twitter.com/"+rawName+"'>" + result + "</a>";
		        tweetText = tweetText.replace(result,userHTML);
		    }
		    return tweetText;
		}
		else  return "";
	 }
	
	 /**
	  * 
	  * @param statuses
	  * @return list of tweets
	  * only returns the direct tweets by @salesforce, skips retweets.
	  */
	 protected static List<TweetVO> populateTweet(List<Status> statuses) {
		 	
		 	List<TweetVO> tweetlist = new ArrayList<TweetVO>();
			int count = 1;
			
			for(ListIterator<Status> iter = statuses.listIterator(); iter.hasNext() && count <=10;) {
				TweetVO t = new TweetVO();
				Status s = iter.next();
				if (s.isRetweet())
	                continue;
				count++;
				t.setUserName(s.getUser().getName());
				t.setScreenName(s.getUser().getScreenName());
				t.setProfilePic(s.getUser().getProfileImageURL());
				t.setTimesRetweeted(s.getRetweetCount());			
				t.setTweetContent(parseTweet(s.getText()));
				List<String> urls = new ArrayList<String>();
			
				MediaEntity[] media = s.getMediaEntities(); //get the images from the tweet if any
				for(MediaEntity m : media){ 
				    System.out.println(m.getMediaURL()); 
				    urls.add(m.getMediaURL());
				}
				if(!urls.isEmpty()) {
					
					int size = urls.size();
					String[] images = new String[size];
					for(int i=0;i<size;i++) {
						images[i] = urls.get(i);
					}
					t.setTweetImages(images);
				}
				
				t.setTweetDate(s.getCreatedAt());
				tweetlist.add(t);
			}
			return tweetlist;
	 }
	 
	protected static String replaceLinks(String linkText) {
		String targetUrlHtml = "<a href="+linkText+" target='_blank'>"+linkText+"</a>";   
		return targetUrlHtml;
	 }

}
