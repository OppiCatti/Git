package com.camunda.training;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class CreateTweetDelegate implements JavaDelegate {
	private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

	public void execute(DelegateExecution execution) throws Exception {
		String content = (String) execution.getVariable("content");

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		content += " at " + sdf.format(new Date());
		LOGGER.info("Publishing tweet: [" + content + "]");
		
		if (content.equals("Network error")) {
			LOGGER.error("error during publishing tweet: [" + content + "]");
			throw new RuntimeException("simulated network error");
		}
		try {
	        AccessToken accessToken = new AccessToken("220324559-CO8TfUmrcoCrvFHP4TacgToN5hLC8cMw4n2EwmHo", "WvVureFv5TBWTGhESgGe3fqZM7XbGMuyIhxB84zgcoUER");
	        Twitter twitter = new TwitterFactory().getInstance();
	        twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");

			twitter.setOAuthAccessToken(accessToken);
			twitter.updateStatus(content);
			LOGGER.info("tweet published: [" + content + "] : ");
		} catch (Exception e) {
			LOGGER.error("error during publishing tweet: [" + content + "] : " + e);
			throw e;
		}
	}
}