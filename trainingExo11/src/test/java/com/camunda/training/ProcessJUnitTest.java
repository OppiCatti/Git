package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
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

	private static final String PROCESS_FILE = "ex_11.bpmn";
	private static final String PROCESS_NAME = "ex11";

	@Test
	@Deployment(resources = PROCESS_FILE)

	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 5 test - Pierre-Yves " + sdf.format(new Date()));

		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_NAME, variables);

		List<Task> taskList = taskService().createTaskQuery().taskCandidateGroup("management")
				.processInstanceId(processInstance.getId()).list();
		assertThat(taskList).isNotNull();
		assertThat(taskList).hasSize(1);
		Task task = taskList.get(0);
		Map<String, Object> approvedMap = new HashMap<String, Object>();
		approvedMap.put("approved", true);
		taskService().complete(task.getId(), approvedMap);

		List<Job> jobList = jobQuery().processInstanceId(processInstance.getId()).list();
		assertThat(jobList).hasSize(1);
		Job job = jobList.get(0);
		execute(job);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 4 test - Pierre-Yves " + sdf.format(new Date()));
		variables.put("approved", false);

		ProcessInstance processInstance = runtimeService().createProcessInstanceByKey(PROCESS_NAME)
				.setVariables(variables).startAfterActivity(findId("Review Tweet")).execute();

		printCurrentState(processInstance);

		assertThat(processInstance).isWaitingAt(findId("Notify user of rejection")).externalTask()
				.hasTopicName("notification");
		complete(externalTask());
		// Make assertions on the process instance
		assertThat(processInstance).isEnded().hasPassed(findId("Notify user of rejection"));
	}

	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testCreateMessageSuperUser() {

		ProcessInstance processInstance = runtimeService()
				.createMessageCorrelation("superuserTweet")
				.setVariable("content", "My Exercise 11 Tweet Pierre-Yves " + System.currentTimeMillis())
				.correlateWithResult().getProcessInstance();
		assertThat(processInstance).isStarted();

		try {
			runtimeService().createMessageCorrelation("tweetWithdrawn").correlateWithResult();
		} catch (Exception e) {
			System.out.print("The message [tweetWithdrawn] does not fit any processinstance now");
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
		@Deployment(resources = PROCESS_FILE)
	  public void testTweetWithdrawn() {
		Map<String, Object> varMap = new HashMap<>();
	    varMap.put("content", "Test tweetWithdrawn message");
	    
	    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_NAME, varMap);
	    // ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_NAME, "BusinessKey", varMap);
	    
		printCurrentState(processInstance);

	    assertThat(processInstance).isStarted().isWaitingAt(findId("Review Tweet"));
	    runtimeService()
	       .createMessageCorrelation("tweetWithdrawn")
	       .processInstanceVariableEquals("content", "Test tweetWithdrawn message")	       
	       .correlateWithResult();
	    // .processInstanceBusinessKey("BusinessKey")
	    assertThat(processInstance).isEnded();
	  }
	 
	 
	private void printCurrentState(ProcessInstance processInstance) {
		try {
			ActivityInstance activityInstance = runtimeService().getActivityInstance(processInstance.getId());
			ActivityInstanceDto result = ActivityInstanceDto.fromActivityInstance(activityInstance);
			System.out.println("activityName=[" + result.getName() + "](" + result.getActivityType() + ")");
			for (ActivityInstanceDto activity : result.getChildActivityInstances()) {
				System.out.println("activityName=[" + activity.getName() + "](" + activity.getActivityType() + ")");
			}

		} catch (Exception e) {
			System.out.println("printCurrentState exception " + e.toString());
		}
	}

}
