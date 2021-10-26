package com.genpact.fda.service;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class ProcessMgntService {

	private static final Logger logger = LoggerFactory.getLogger(ProcessMgntService.class);

	@Value("${caseMgtPath}")
	private String caseMgtPath;

	@Value("${auditPath}")
	private String auditPath;

	@Value("${platformAdapterPath}")
	private String platformAdapterPath;

	@Value("${businessRulesProcessorPath}")
	private String businessRulesProcessorPath;

	@Value("${outputGenerationPath}")
	private String outputGenerationPath;

	private String logMethod = "inside method : {}()";
	private String errorMethod = "Exception inside method : {}()";

	public void startProcess(String extractedMailDetails) throws Exception{
		String methodName = "startProcess";
		logger.debug(logMethod , methodName);

		try {
			RuntimeService runtimeService = null;

			ProcessEngineConfiguration processEngineConfiguration = ((ProcessEngineConfiguration) ProcessEngineConfiguration
					.createProcessEngineConfigurationFromResourceDefault());

			processEngineConfiguration.getHttpClientConfig().setDisableCertVerify(true);

			ProcessEngine processEngine = processEngineConfiguration
					.createProcessEngineConfigurationFromResourceDefault()
					.buildProcessEngine();
			RepositoryService repositoryService = processEngine.getRepositoryService();
			repositoryService.createDeployment().addClasspathResource("executeCase.bpmn20.xml").deploy();

			runtimeService = processEngine.getRuntimeService();
			Map<String, Object> variables = new HashMap<>();

			variables.put("ExtractedMailDetails", extractedMailDetails);

			variables.put("caseMgtPath", caseMgtPath);
			variables.put("platformAdapterPath", platformAdapterPath);
			variables.put("businessRulesProcessorPath", businessRulesProcessorPath);
			variables.put("outputGenerationPath", outputGenerationPath);

			runtimeService.startProcessInstanceByKey("ProcessFda", variables);
		}catch(Exception e) {
			logger.error(errorMethod, methodName);
			throw e;
		}
	}

	public void auditSaveProcess(String audit) throws Exception {
		String methodName = "auditSaveProcess";
		logger.debug(logMethod , methodName);

		try {
			RuntimeService runtimeService = null;
//			ProcessEngine processEngine = ((ProcessEngineConfiguration) ProcessEngineConfiguration
//					.createProcessEngineConfigurationFromResourceDefault())
//					.buildProcessEngine();
			ProcessEngineConfiguration processEngineConfiguration = ((ProcessEngineConfiguration) ProcessEngineConfiguration
					.createProcessEngineConfigurationFromResourceDefault());
			
			processEngineConfiguration.getHttpClientConfig().setDisableCertVerify(true);
			
			ProcessEngine processEngine = processEngineConfiguration
					.createProcessEngineConfigurationFromResourceDefault()
					.buildProcessEngine();
			RepositoryService repositoryService = processEngine.getRepositoryService();
			repositoryService.createDeployment().addClasspathResource("AuditFda.bpmn20.xml").deploy();

			runtimeService = processEngine.getRuntimeService();
			Map<String, Object> variables = new HashMap<>();

			variables.put("auditDetails", audit);
			variables.put("auditPath", auditPath);

			runtimeService.startProcessInstanceByKey("AuditFda", variables);
		}catch(Exception e) {
			logger.error(errorMethod, methodName);
			throw e;
		}
	}

	public void auditUpdateProcess(String audit) throws Exception {
		String methodName = "auditUpdateProcess";
		logger.debug(logMethod , methodName);

		try {
			RuntimeService runtimeService = null;
			ProcessEngine processEngine = ((ProcessEngineConfiguration) ProcessEngineConfiguration
					.createProcessEngineConfigurationFromResourceDefault())
					.buildProcessEngine();
			RepositoryService repositoryService = processEngine.getRepositoryService();
			repositoryService.createDeployment().addClasspathResource("AuditUpdateFda.bpmn20.xml").deploy();

			runtimeService = processEngine.getRuntimeService();
			Map<String, Object> variables = new HashMap<>();

			variables.put("auditDetails", audit);
			variables.put("auditPath", auditPath);

			runtimeService.startProcessInstanceByKey("AuditUpdateFda", variables);
		}catch(Exception e) {
			logger.error(errorMethod, methodName);
			throw e;
		}
	}
}
