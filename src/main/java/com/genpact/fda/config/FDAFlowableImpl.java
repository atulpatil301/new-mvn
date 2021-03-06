package com.genpact.fda.config;

import static org.flowable.bpmn.model.ImplementationType.IMPLEMENTATION_TYPE_CLASS;
import static org.flowable.bpmn.model.ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION;
import static org.flowable.http.ExpressionUtils.getBooleanFromField;
import static org.flowable.http.ExpressionUtils.getIntFromField;
import static org.flowable.http.ExpressionUtils.getStringFromField;
import static org.flowable.http.ExpressionUtils.getStringSetFromField;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowableHttpRequestHandler;
import org.flowable.bpmn.model.FlowableHttpResponseHandler;
import org.flowable.bpmn.model.HttpServiceTask;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.MapExceptionEntry;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.cfg.HttpClientConfig;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.flowable.engine.impl.bpmn.parser.FieldDeclaration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.http.HttpActivityExecutor;
import org.flowable.http.HttpRequest;
import org.flowable.http.bpmn.impl.ProcessErrorPropagator;
import org.flowable.http.bpmn.impl.handler.ClassDelegateHttpHandler;
import org.flowable.http.bpmn.impl.handler.DelegateExpressionHttpHandler;
import org.flowable.http.delegate.HttpRequestHandler;
import org.flowable.http.delegate.HttpResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FDAFlowableImpl extends AbstractBpmnActivityBehavior {

    public static final String HTTP_TASK_REQUEST_FIELD_INVALID = "request fields are invalid";

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FDAFlowableImpl.class);

    // HttpRequest method (GET,POST,PUT etc)
    protected Expression requestMethod;
    // HttpRequest URL (http://flowable.org)
    protected Expression requestUrl;
    // Line separated HTTP body headers(Optional)
    protected Expression requestHeaders;
    // HttpRequest body expression (Optional)
    protected Expression requestBody;
    // Timeout in seconds for the body (Optional)
    protected Expression requestTimeout;
    // HttpRequest retry disable HTTP redirects (Optional)
    protected Expression disallowRedirects;
    // Comma separated list of HTTP body status codes to fail, for example 400,5XX (Optional)
    protected Expression failStatusCodes;
    // Comma separated list of HTTP body status codes to handle, for example 404,3XX (Optional)
    protected Expression handleStatusCodes;
    // Flag to ignore exceptions (Optional)
    protected Expression ignoreException;
    // Flag to save request variables. default is false (Optional)
    protected Expression saveRequestVariables;
    // Flag to save response variables. default is false (Optional)
    protected Expression saveResponseParameters;
    // Variable name for response body
    protected Expression responseVariableName;
    // Prefix for the execution variable names (Optional)
    protected Expression resultVariablePrefix;
    
    protected Expression saveResponseVariableAsJson; 
    protected Expression saveResponseAsJson;
    
    // Exception mapping
    protected List<MapExceptionEntry> mapExceptions;

    protected HttpServiceTask httpServiceTask;
    protected HttpActivityExecutor httpActivityExecutor;

    public FDAFlowableImpl() {
        HttpClientConfig config = CommandContextUtil.getProcessEngineConfiguration().getHttpClientConfig();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        config.setDisableCertVerify(true);
        // https settings
        if (config.isDisableCertVerify()) {
            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                httpClientBuilder.setSSLSocketFactory(
                        new SSLConnectionSocketFactory(builder.build(), new HostnameVerifier() {
                            @Override
                            public boolean verify(String s, SSLSession sslSession) {
                                return true;
                            }
                        }));

            } catch (Exception e) {
                LOGGER.error("Could not configure HTTP client SSL self signed strategy", e);
            }
        }

        // request retry settings
        int retryCount = 0;
        if (config.getRequestRetryLimit() > 0) {
            retryCount = config.getRequestRetryLimit();
        }
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false));

        this.httpActivityExecutor = new HttpActivityExecutor(httpClientBuilder, new ProcessErrorPropagator(), new ObjectMapper());
    }

    @Override
    public void execute(DelegateExecution execution) {

        HttpRequest request = new HttpRequest();

        try {
            request.setMethod(getStringFromField(requestMethod, execution));
            request.setUrl(getStringFromField(requestUrl, execution));
            request.setHeaders(getStringFromField(requestHeaders, execution));
            request.setBody(getStringFromField(requestBody, execution));
            request.setTimeout(getIntFromField(requestTimeout, execution));
            request.setNoRedirects(getBooleanFromField(disallowRedirects, execution));
            request.setIgnoreErrors(getBooleanFromField(ignoreException, execution));
            request.setSaveRequest(getBooleanFromField(saveRequestVariables, execution));
            request.setSaveResponse(getBooleanFromField(saveResponseParameters, execution));
            request.setPrefix(getStringFromField(resultVariablePrefix, execution));
            request.setPrefix(getStringFromField(saveResponseVariableAsJson, execution)); 
            request.setSaveResponseAsJson(true);
            String failCodes = getStringFromField(failStatusCodes, execution);
            String handleCodes = getStringFromField(handleStatusCodes, execution);

            if (failCodes != null) {
                request.setFailCodes(getStringSetFromField(failCodes));
            }
            if (handleCodes != null) {
                request.setHandleCodes(getStringSetFromField(handleCodes));
            }

            if (request.getPrefix() == null) {
                request.setPrefix(execution.getCurrentFlowElement().getId());
            }

            // Save request fields
            if (request.isSaveRequest()) {
                execution.setVariable(request.getPrefix() + ".requestMethod", request.getMethod());
                execution.setVariable(request.getPrefix() + ".requestUrl", request.getUrl());
                execution.setVariable(request.getPrefix() + ".requestHeaders", request.getHeaders());
                execution.setVariable(request.getPrefix() + ".requestBody", request.getBody());
                execution.setVariable(request.getPrefix() + ".requestTimeout", request.getTimeout());
                execution.setVariable(request.getPrefix() + ".disallowRedirects", request.isNoRedirects());
                execution.setVariable(request.getPrefix() + ".failStatusCodes", failCodes);
                execution.setVariable(request.getPrefix() + ".handleStatusCodes", handleCodes);
                execution.setVariable(request.getPrefix() + ".ignoreException", request.isIgnoreErrors());
                execution.setVariable(request.getPrefix() + ".saveRequestVariables", request.isSaveRequest());
                execution.setVariable(request.getPrefix() + ".saveResponseParameters", request.isSaveResponse());
                execution.setVariable(request.getPrefix() + ".saveResponseAsJson", request.isSaveResponseAsJson());
            }

        } catch (Exception e) {
            if (e instanceof FlowableException) {
                throw (FlowableException) e;
            } else {
                throw new FlowableException(HTTP_TASK_REQUEST_FIELD_INVALID + " in execution " + execution.getId(),
                        e);
            }
        }

        httpActivityExecutor.validate(request);

        httpActivityExecutor.execute(request, execution, execution.getId(),
                createHttpRequestHandler(httpServiceTask.getHttpRequestHandler(),
                        CommandContextUtil.getProcessEngineConfiguration()),
                createHttpResponseHandler(httpServiceTask.getHttpResponseHandler(),
                        CommandContextUtil.getProcessEngineConfiguration()),
                getStringFromField(responseVariableName, execution), mapExceptions,
                CommandContextUtil.getProcessEngineConfiguration().getHttpClientConfig().getSocketTimeout(),
                CommandContextUtil.getProcessEngineConfiguration().getHttpClientConfig().getConnectTimeout(),
                CommandContextUtil.getProcessEngineConfiguration().getHttpClientConfig()
                        .getConnectionRequestTimeout());

        leave(execution);
    }

    protected HttpRequestHandler createHttpRequestHandler(FlowableHttpRequestHandler handler,
            ProcessEngineConfigurationImpl processEngineConfiguration) {
        HttpRequestHandler requestHandler = null;

        if (handler != null) {
            if (IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(handler.getImplementationType())) {
                requestHandler = new ClassDelegateHttpHandler(handler.getImplementation(),
                        createFieldDeclarations(handler.getFieldExtensions(), processEngineConfiguration));

            } else if (IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(handler.getImplementationType())) {
                requestHandler = new DelegateExpressionHttpHandler(
                        processEngineConfiguration.getExpressionManager()
                                .createExpression(handler.getImplementation()),
                        createFieldDeclarations(handler.getFieldExtensions(), processEngineConfiguration));
            }
        }
        return requestHandler;
    }

    protected HttpResponseHandler createHttpResponseHandler(FlowableHttpResponseHandler handler,
            ProcessEngineConfigurationImpl processEngineConfiguration) {
        HttpResponseHandler responseHandler = null;

        if (handler != null) {
            if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(handler.getImplementationType())) {
                responseHandler = new ClassDelegateHttpHandler(handler.getImplementation(),
                        createFieldDeclarations(handler.getFieldExtensions(), processEngineConfiguration));

            } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION
                    .equalsIgnoreCase(handler.getImplementationType())) {
                responseHandler = new DelegateExpressionHttpHandler(
                        processEngineConfiguration.getExpressionManager()
                                .createExpression(handler.getImplementation()),
                        createFieldDeclarations(handler.getFieldExtensions(), processEngineConfiguration));
            }
        }
        return responseHandler;
    }

    protected List<FieldDeclaration> createFieldDeclarations(List<FieldExtension> fieldList,
            ProcessEngineConfigurationImpl processEngineConfiguration) {
        List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

        for (FieldExtension fieldExtension : fieldList) {
            FieldDeclaration fieldDeclaration;
            if (StringUtils.isNotEmpty(fieldExtension.getExpression())) {
                fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(),
                        processEngineConfiguration.getExpressionManager()
                                .createExpression(fieldExtension.getExpression()));
            } else {
                fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(),
                        new FixedValue(fieldExtension.getStringValue()));
            }

            fieldDeclarations.add(fieldDeclaration);
        }
        return fieldDeclarations;
    }

    public void setServiceTask(ServiceTask serviceTask) {
        this.httpServiceTask = (HttpServiceTask) serviceTask;
    }

}