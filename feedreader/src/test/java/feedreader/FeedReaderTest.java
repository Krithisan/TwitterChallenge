package feedreader;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Test;
import feedreader.TweetReaderServlet;
import twitter4j.*;

import junit.framework.TestCase;

public class FeedReaderTest extends TestCase {

	
	@Test
	public void testparse() {
		String expected = "checking <a href=https: target='_blank'>https:</a> and <a href=http: target='_blank'>http:</a>";
		String actual = TweetReaderServlet.parseTweet("checking https: and http:");
		assertEquals(expected, actual);
		
		String unexpected = "checking the change of hashtags <a href='https://twitter.com/hashtag/#this?src=hash'>#this</a>";
		expected = "checking the change of hashtags <a href='https://twitter.com/hashtag/this?src=hash'>#this</a>";
		actual = TweetReaderServlet.parseTweet("checking the change of hashtags #this");
		assertNotEquals(unexpected, actual);
		assertEquals(expected, actual);
		
		unexpected = "checking the change of screenname <a href='https://twitter.com/@name'>@name</a>";
		expected = "checking the change of screenname <a href='https://twitter.com/name'>@name</a>";
		actual = TweetReaderServlet.parseTweet("checking the change of screenname @name");
		assertNotEquals(unexpected, actual);
		assertEquals(expected, actual);
	}
	
	@Test
	public void testReplaceLinks() {
		String expected = "<a href=does this get relaced target='_blank'>does this get relaced</a>";
		String actual = TweetReaderServlet.replaceLinks("does this get relaced");
		assertEquals(expected, actual);
	}
	
	@Test
	public void testPopulate() {
		TwitterFactory tf = new TwitterFactory();
	    Twitter twitter = tf.getInstance();
		List<Status> list = null;
		
		try {
			list = twitter.getHomeTimeline();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(TweetReaderServlet.populateTweet(list));
	}

}
