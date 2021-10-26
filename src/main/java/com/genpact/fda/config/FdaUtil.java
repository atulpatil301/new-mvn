package com.genpact.fda.config;

import java.util.Iterator;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;

public class FdaUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(FdaUtil.class);
	private static String logMethod = "inside method : {}()";
	private static String errorMethod = "Exception inside method : {}()";
	
	public static JSONObject isValidJsonData(String json, String schema) throws Exception {
		String methodName = "isValidJsonData";
		logger.debug(logMethod , methodName);
		
		JsonNode schemaNode = JsonLoader.fromString(schema);       
		JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		JsonSchema jsonSchema = factory.getJsonSchema(schemaNode);
		JsonNode jsonNode = JsonLoader.fromString(json);
		com.github.fge.jsonschema.core.report.ProcessingReport report = jsonSchema.validate(jsonNode);
		String errorMessage = "";
		JSONObject output = new JSONObject();
		if(!report.isSuccess()) {
			String erroeMsg=getErrorsList(report); 
			Gson	gsonData = new Gson(); 
			try {
					errorMessage = "Input json is not in proper format. Validation failed for value= " + json;
			}catch (Exception e) {
				logger.error(errorMethod, methodName);
				throw e;
			}
		}
		output.put("isValidJson", report.isSuccess());
		output.put("errorMessage", errorMessage);
		return output;
	}

	static String getErrorsList(ProcessingReport report) throws JsonProcessingException {
		String methodName = "getErrorsList";
		logger.debug(logMethod , methodName);
		
		Iterator<ProcessingMessage> i = report.iterator();
		StringBuilder buff = new StringBuilder();
		while (!report.isSuccess() && i != null && i.hasNext()) {
			ProcessingMessage pm = i.next();
			ObjectMapper mapper = new ObjectMapper();
			String prettyPrintJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pm.asJson());
			buff.append(prettyPrintJson).append("\n\n");
		} 
		JSONObject jError = new JSONObject(buff.toString());

		return jError.toString();
	}
}
