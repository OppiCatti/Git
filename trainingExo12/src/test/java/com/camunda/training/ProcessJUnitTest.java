package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.TransitionInstanceDto;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

	private static final String PROCESS_FILE = "ex_12.bpmn";
	private static final String PROCESS_NAME = "ex12";

	@Test
	@Deployment(resources = { PROCESS_FILE, "ex_12_decision.dmn" })
	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 125 test - Pierre-Yves " + sdf.format(new Date()));
		variables.put("email", "jakob.freund@camunda.com");

		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_NAME, variables);
		printCurrentState(processInstance);
		// get the job
		List<Job> jobList = jobQuery().processInstanceId(processInstance.getId()).list();

		// execute the job
		assertThat(jobList).hasSize(1);
		Job job = jobList.get(0);
		execute(job);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

	@Test
	@Deployment(resources = { PROCESS_FILE, "ex_12_decision.dmn" })
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 4 test - Pierre-Yves " + sdf.format(new Date()));
		variables.put("approved", false);

		ProcessInstance processInstance = runtimeService().createProcessInstanceByKey(PROCESS_NAME)
				.setVariables(variables).startAfterActivity(findId("Review Tweet")).execute();

		// printCurrentState(processInstance);

		assertThat(processInstance).isWaitingAt(findId("Notify user of rejection")).externalTask()
				.hasTopicName("notification");
		complete(externalTask());
		// Make assertions on the process instance
		assertThat(processInstance).isEnded().hasPassed(findId("Notify user of rejection"));
	}

	@Test
	@Deployment(resources = { PROCESS_FILE, "ex_12_decision.dmn" })
	public void testCreateMessageSuperUser() {

		ProcessInstance processInstance = runtimeService().createMessageCorrelation("superuserTweet")
				.setVariable("content", "My Exercise 11 Tweet Pierre-Yves " + System.currentTimeMillis())
				.correlateWithResult().getProcessInstance();
		assertThat(processInstance).isStarted();

		try {
			runtimeService().createMessageCorrelation("tweetWithdrawn").correlateWithResult();
		} catch (Exception e) {
			System.out.println("The message [tweetWithdrawn] does not fit any processinstance now");
		}
		// get the job
		List<Job> jobList = jobQuery().processInstanceId(processInstance.getId()).list();

		// execute the job
		assertThat(jobList).hasSize(1);
		Job job = jobList.get(0);
		execute(job);

		assertThat(processInstance).isEnded();
	}

	@Test
	@Deployment(resources = "ex_12_decision.dmn")
	public void testTweetFromJakob() {
		Map<String, Object> variables = withVariables("email", "jakob.freund@camunda.com", "content",
				"this should be published");
		DmnDecisionTableResult decisionResult = decisionService().evaluateDecisionTableByKey("tweetApproval",
				variables);
		assertThat(decisionResult.getFirstResult()).contains(entry("approved", true));
		assertThat(decisionResult.getFirstResult()).containsEntry("approved", true);

	}

	private void printCurrentState(ProcessInstance processInstance) {
		try {
			ActivityInstance activityInstance = runtimeService().getActivityInstance(processInstance.getId());
			ActivityInstanceDto result = ActivityInstanceDto.fromActivityInstance(activityInstance);
			System.out.println("activityName=[" + result.getName() + "](" + result.getActivityType() + ")");
			for (ActivityInstanceDto activity : result.getChildActivityInstances()) {
				System.out.println("activityName=[" + activity.getName() + "](" + activity.getActivityType() + ")");
			}
			for (TransitionInstanceDto transition : result.getChildTransitionInstances()) {
				System.out.println("TransitionName=[" + transition.getActivityName() + "](" + transition.getActivityType() + ")");
			}

		} catch (Exception e) {
			System.out.println("printCurrentState exception " + e.toString());
		}
	}

}
