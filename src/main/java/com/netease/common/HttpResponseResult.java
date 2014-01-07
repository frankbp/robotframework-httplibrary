package com.netease.common;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

public class HttpResponseResult {
	
	private static final String CHARACTER_ENCODING = "UTF-8";
	
	private int statusCode;
    private Header[] headers;
    private String rawBody;
    private JSONObject jsonBody;
	
	public HttpResponseResult(HttpResponse httpResponse) throws Exception {
        StatusLine statusLine = httpResponse.getStatusLine();
		System.out.println("*DEBUG* Response StatusLine: " + statusLine);
	    this.statusCode = statusLine.getStatusCode();

        this.headers = httpResponse.getAllHeaders();
        System.out.println("*DEBUG* Response Headers: " + StringUtils.join(headers, " | "));

	    HttpEntity httpEntity = httpResponse.getEntity();
	    String httpBody = EntityUtils.toString(httpEntity, CHARACTER_ENCODING);
		this.rawBody = httpBody;
		System.out.println("*DEBUG* Response Body: " + httpBody);
		
		try {
		    this.jsonBody = JSONObject.fromObject(httpBody);
		    System.out.println("*INFO* Response MessageBody:");
		    System.out.println("*INFO* " + this.jsonBody.toString(2));
		} catch (Exception e) {
			System.out.println("*DEBUG* Cannot covert to JSON object, message body is: " + httpBody);
	    }
    }

	public int getStatusCode() {
		return statusCode;
	}
	
	public String getRawBody() {
		return rawBody;
	}
	
	public JSONObject getJsonBody() {
		return jsonBody;
	}

    public Header[] getHeaders() { return headers; }
}