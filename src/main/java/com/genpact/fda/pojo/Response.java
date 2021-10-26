package com.genpact.fda.pojo;

public class Response{

	private boolean success;
	
	private String message;
	
	private String status_code;
	
	private Object data;
	
	private Long wqId;
	
	public Response() {
		super();
	}

	public Response(boolean success, String message, String status_code, Object data, Long wqId) {
		super();
		this.success = success;
		this.message = message;
		this.status_code = status_code;
		this.data = data;
		this.wqId = wqId;
	}
	
	public Response(boolean success, String message, String status_code) {
		super();
		this.success = success;
		this.message = message;
		this.status_code = status_code;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus_code() {
		return status_code;
	}

	public void setStatus_code(String status_code) {
		this.status_code = status_code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Long getWqId() {
		return wqId;
	}

	public void setWqId(Long wqId) {
		this.wqId = wqId;
	}

}
