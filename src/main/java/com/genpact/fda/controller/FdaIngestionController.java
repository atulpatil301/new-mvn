package com.genpact.fda.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.genpact.fda.config.FdaUtil;
import com.genpact.fda.pojo.Response;
import com.genpact.fda.service.ProcessMgntService;

import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/fda")
@PropertySource({ "classpath:application.properties" })
public class FdaIngestionController {

	private static final Logger logger = LoggerFactory.getLogger(FdaIngestionController.class);

	private String apiInfo = "Called API : {}" ;
	private String apiError = "Exception occurred in api {}. Reason is : {}" ;
	private String logMethod = "inside method : {}()";
	
	@Autowired
	private ProcessMgntService fdaService;

	private String JSON_SCHEMA_FILE = "schema.json";
	
	@ApiOperation(value = "Ingests data into Fda service", notes = "This is initial api which ingests data into Fda service.\n"
			+ " The input to this api is ExtractedMailDetails.")
	
	@PostMapping("/ingestion")
	public Response ingestMailDetails(@RequestBody String extractedMailDetails) {
		String apiName = "/fda/ingestion";
		logger.info(apiInfo , apiName);
		
		try {
			fdaService.startProcess(extractedMailDetails);
			return new Response(true,"ingested mail details" , HttpStatus.OK.toString(), extractedMailDetails, 0L);
		}
		catch(Exception e) {
			logger.error(apiError, apiName, e.getCause());
			logger.error(ExceptionUtils.getStackTrace(e));
			return new Response(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), extractedMailDetails, 0L);
		}

	}
	
	@ApiOperation(value = "Audits response data of each Fda service", notes = "This api audits response data of each Fda service.\n"
			+ " The input to this api is Response.")
	
	@PostMapping("/audit/save")
	public Response auditSaveFdaServices(@RequestBody String audit) {
		String apiName = "/fda/audit/save";
		logger.info(apiInfo , apiName);
		
		boolean isValid = false;
		String errorMsg = null;
		try {
			String jsonSchema = getSchema();
			JSONObject jSONObject = FdaUtil.isValidJsonData(audit, jsonSchema);
			if (jSONObject!=null) {
				isValid = jSONObject.getBoolean("isValidJson");
				if(!isValid) {
					errorMsg = jSONObject.getString("errorMessage");
				}
			}
			if(isValid) {
				fdaService.auditSaveProcess(audit);
				return new Response(true,"Saved audit details" , HttpStatus.OK.toString() , null, 0L);
			}else {
				return new Response(false, errorMsg, HttpStatus.BAD_REQUEST.toString() , null, 0L);
			}
		}
		catch(Exception e) {
			logger.error(apiError, apiName, e.getCause());
			logger.error(ExceptionUtils.getStackTrace(e));
			return new Response(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString() , null, 0L);
		}
	}
	
	@ApiOperation(value = "Audits response data of each Fda service", notes = "This api audits response data of each Fda service.\n"
			+ " The input to this api is Response.")
	
	@PostMapping("/audit/update")
	public Response auditUpdateFdaServices(@RequestBody String audit) {
		String apiName = "/fda/audit/update";
		logger.info(apiInfo , apiName);
		
		boolean isValid = false;
		String errorMsg = null;
		try {
			String jsonSchema = getSchema();
			JSONObject jSONObject = FdaUtil.isValidJsonData(audit, jsonSchema);
			if (jSONObject!=null) {
				isValid = jSONObject.getBoolean("isValidJson");
				errorMsg = jSONObject.getString("errorMessage");
			}
			if(isValid) {
				fdaService.auditUpdateProcess(audit);
				return new Response(true,"Saved audit details" , HttpStatus.OK.toString() , "200", 0L);
			}else {
				return new Response(false, errorMsg, HttpStatus.BAD_REQUEST.toString() , null, 0L);
			}
		}
		catch(Exception e) {
			logger.error(apiError, apiName, e.getCause());
			logger.error(ExceptionUtils.getStackTrace(e));
			return new Response(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString() , null, 0L);
		}
		
	}

	private String getSchema() throws Exception{
		logger.debug(logMethod , "getSchema");
		
		InputStream inputStream = null;
		inputStream = FdaIngestionController.class.getClassLoader().getResourceAsStream(JSON_SCHEMA_FILE);
		String resultSchema = null;
		try {
			resultSchema = IOUtils.toString(inputStream);
		} catch (IOException e) {
			logger.error("Exception occurred while getting schema. Reason is : {}", e.getCause());
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			if(inputStream != null) {
				inputStream.close();	
			}
		}
		return resultSchema;
	}
}


