package com.camunda.training;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RejectedTweet implements JavaDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(RejectedTweet.class.getName());

    public void execute(DelegateExecution execution) throws Exception {
    	String content = (String) execution.getVariable("content");

        LOGGER.info("Rejected tweet: [" + content+"]");
        BufferedWriter writer = new BufferedWriter(new FileWriter("d:/temp/tweetRejected.txt", true));
        writer.append('\n');
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        writer.append("Rejected ["+content+"] at " + sdf.format(new Date()) );
        
        writer.close();
    }
}