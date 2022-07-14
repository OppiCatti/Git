package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.ArgumentMatchers.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

	private static final String PROCESS_FILE = "ex_8.bpmn";
	private static final String PROCESS_NAME = "ex8";

	@Mock
	private CreateTweetDelegateMock myCreateTweetDelegateMock;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mocks.register("CreateTweetDelegate", myCreateTweetDelegateMock);
	}
	
	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 8 test - Pierre-Yves " + sdf.format(new Date()));

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
		
		try {
		Mockito.verify(myCreateTweetDelegateMock).execute(any(DelegateExecution.class));
		} catch(Exception e) {
			assert(false);
		}
		// Make assertions on the process instance
		assertThat(processInstance).isEnded().hasPassed("publishOnTwitter");
	}

	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 8 test - Pierre-Yves " + sdf.format(new Date()));
		variables.put("approved", false);

		
		
		ProcessInstance processInstance = runtimeService()
		        .createProcessInstanceByKey(PROCESS_NAME)
		        .setVariables(variables)
		        .startAfterActivity(findId("Review Tweet"))
		        .execute();
		
		
		// Make assertions on the process instance
	    assertThat(processInstance).isEnded().hasPassed(findId("Do not publish"));
	}

	public static class CreateTweetDelegateMock implements JavaDelegate {
		private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

	    public void execute(DelegateExecution execution) throws Exception {
	    	LOGGER.info("Tweet Mock");
	    	};
	}
}
