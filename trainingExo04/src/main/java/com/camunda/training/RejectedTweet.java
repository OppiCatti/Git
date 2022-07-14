package com.camunda.training;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RejectedTweet implements JavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(RejectedTweet.class.getName());

    public void execute(DelegateExecution execution) throws Exception {
    	String content = (String) execution.getVariable("content");

        LOGGER.info("Rejected tweet: " + content);
        BufferedWriter writer = new BufferedWriter(new FileWriter("d:/temp/tweetRejected.txt", true));
        writer.append('\n');
        writer.append(content);
        
        writer.close();
    }
}