package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

	private static final String PROCESS_FILE = "ex_6.bpmn";
	private static final String PROCESS_NAME = "ex6";

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

		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_NAME, variables);

		List<Task> taskList = taskService().createTaskQuery().taskCandidateGroup("management")
				.processInstanceId(processInstance.getId()).list();
		assertThat(taskList).isNotNull();
		assertThat(taskList).hasSize(1);
		Task task = taskList.get(0);
		Map<String, Object> approvedMap = new HashMap<String, Object>();
		approvedMap.put("approved", false);
		taskService().complete(task.getId(), approvedMap);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

}
