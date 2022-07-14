package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.junit5.test.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(ProcessEngineExtension.class)
public class ProcessJUnitTest {

	@Test
	@Deployment(resources = "ex_3a.bpmn")

	public void testHappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		variables.put("approved", true);
		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ex3a", variables);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

	@Test
	@Deployment(resources = "ex_3a.bpmn")
	public void testRejectedPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap();
		variables.put("approved", false);
		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("ex3a", variables);
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

}
