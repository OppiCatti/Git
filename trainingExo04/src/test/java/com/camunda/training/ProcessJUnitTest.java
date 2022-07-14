package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {

	
	 
	@Test
	@Deployment(resources = "ex_4.bpmn")

	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 4 test - Pierre-Yves " + sdf.format(new Date()));

		variables.put("approved", true);
		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ex4", variables);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

	@Test
	@Deployment(resources = "ex_4.bpmn")
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		variables.put("content", "Exercise 4 test - Pierre-Yves " + sdf.format(new Date()));

		variables.put("approved", false);
		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ex4", variables);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

}
