package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

	private static final String PROCESS_FILE = "ex_7.bpmn";
	private static final String PROCESS_NAME = "ex7";

	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 7 test - Pierre-Yves " + sdf.format(new Date()));

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
	public void testErrorTwitter() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Network error");

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
		try {
			execute(job);
			// we must not be here: execution should failed
			assert( false );
		} catch(Exception e) {
			
		}
		// here, an error is generated.
		// process instance is still active
		assertThat(processInstance).isActive();
		
		// update the process instance
		runtimeService().setVariable(processInstance.getId(), "content","Exercise 7 test - Pierre-Yves " + sdf.format(new Date()));
		
		// replay the same job now, it should be Ok now
		execute(job);
		
		
		assertThat(processInstance).isEnded();
	}
	
	
	@Test
	@Deployment(resources = PROCESS_FILE)
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 7 test - Pierre-Yves " + sdf.format(new Date()));

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
