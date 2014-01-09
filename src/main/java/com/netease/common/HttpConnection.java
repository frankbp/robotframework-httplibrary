package com.netease.common;

import org.apache.commons.lang.StringUtils;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.annotation.ArgumentNames;

import java.util.HashMap;
import java.util.Map;

@RobotKeywords
public class HttpConnection {
	
	private static final String CHARACTER_ENCODING = "UTF-8";
    private static int connectionTimeout = 10 * 1000;
    private static int readTimeout = 10 * 1000;
    private static Header[] initHeaders = {new BasicHeader("Content-Type", "application/x-www-form-urlencoded"),
                                             new BasicHeader("Accept-Charset", CHARACTER_ENCODING)};
    private static Header[] headers = initHeaders.clone();
    private static DefaultHttpClient client = createDefaultHttpClient();
    private static CookieStore cookieStore = HttpConnection.client.getCookieStore();

    @RobotKeyword("This keyword sets HTTP cookies\n\n"
            + "| Options  | Man. | Description |\n"
            + "| cookies  | Yes  | Cookies with format 'name=xxx;value=xxx;domain=xxx;path=xxx' |\n"
            + "Note: Following are the valid cookie attributes, 'name' and 'value' are the mandatory ones. "
            + "It will not fail even the format is invalid but only gives the warning messages\n"
            + "- name: cookie name\n"
            + "- value: cookie value\n"
            + "- domain: cookie affected domain\n"
            + "- path: cookie affected path\n"
            + "Examples:\n"
            + "| Set HTTP Cookies | key=company;value=NetEase |\n"
            + "| Set HTTP Cookies | key=company;value=NetEase | key=department;value=R&D |\n")
    @ArgumentNames({"*cookies"})
    public void setHttpCookies(String... cookies) {
        for(String cookie:cookies) {
            try {
                BasicClientCookie clientCookie = generateCookie(cookie);
                HttpConnection.cookieStore.addCookie(clientCookie);}
            catch (Exception e) {
                System.out.println("*WARN* Cookie " + cookie + " is invalid");
                System.out.println("*WARN* " + e);
            }
        }
        System.out.println("*DEBUG* Cookie is " + HttpConnection.cookieStore);

        HttpConnection.client.setCookieStore(HttpConnection.cookieStore);

        System.out.println("*DEBUG* Client cookie is " + HttpConnection.client.getCookieStore());

    }

    private BasicClientCookie generateCookie(String cookie) {
        Map<String, String> cookieMap = generateCookieMap(cookie);

        BasicClientCookie clientCookie = new BasicClientCookie(cookieMap.get("name"), cookieMap.get("value"));
        if (cookieMap.containsKey("domain")) {
            clientCookie.setDomain(cookieMap.get("domain"));
        }
        if (cookieMap.containsKey("path")) {
            clientCookie.setPath(cookieMap.get("path"));
        }
        if (cookieMap.containsKey("version")) {
            clientCookie.setVersion(Integer.parseInt(cookieMap.get("version")));
        }
        /* TODO
        if (cookieMap.containsKey("expiry")) {
            clientCookie.setExpiryDate(cookieMap.get("expiry"));
        }
        */

        return clientCookie;
    }

    private Map<String, String> generateCookieMap(String cookie) {
        Map<String, String> cookieMap = new HashMap<String, String>();
        String[] cookieInfo = cookie.split(";");
        for (int i = 0; i < cookieInfo.length; i++) {
            String[] keyValue = cookieInfo[i].split("=");
            cookieMap.put(keyValue[0], keyValue[1]);
        }

        return cookieMap;
    }

    @RobotKeyword("This keyword resets HTTP cookies to empty\n\n"
            + "Examples:\n"
            + "| Reset HTTP Cookies |\n")
    @ArgumentNames({"*cookies"})
    public void resetHttpCookies() {
        HttpConnection.cookieStore.clear();
        HttpConnection.client.setCookieStore(HttpConnection.cookieStore);
    }

    @RobotKeyword("This keyword sets HTTP connection timeout, "
                + "and it will return the original connection timeout value\n\n"
                + "| Options  | Man. | Description |\n"
                + "| timeout  | Yes  | Timeout in ms |\n\n"
                + "Examples:\n"
                + "| Set HTTP Connection Timeout | 15000 |\n"
                + "| ${original_connection_timeout} | Set HTTP Connection Timeout | 15000 |\n"
                + "| Set HTTP Connection Timeout | ${original_connection_timeout} |\n")
    @ArgumentNames({"timeout"})
    public int setHttpConnectionTimeout(String timeout) throws Exception {
        HttpParams httpParams = HttpConnection.client.getParams();
        int originalConnectionTimeout = HttpConnectionParams.getConnectionTimeout(httpParams);

        try {
            HttpConnectionParams.setConnectionTimeout(httpParams, Integer.parseInt(timeout));
            return originalConnectionTimeout;
        } catch (Exception e) {
            System.out.println("*DEBUG* Exception: " + e);
            throw new Exception("Convert timeout " + timeout + "to integer failed");
        }
    }

    @RobotKeyword("This keyword sets HTTP read timeout, "
                + "and it will return the original read timeout value\n"
                + "| Options  | Man. | Description |\n"
                + "| timeout  | Yes  | Timeout in ms |\n\n"
                + "Examples:\n"
                + "| Set HTTP Read Timeout | 15000 |\n"
                + "| ${original_read_timeout} | Set HTTP Read Timeout | 15000 |\n"
                + "| Set HTTP Read Timeout | ${original_read_timeout} |\n")
    @ArgumentNames({"timeout"})
    public int setHttpReadTimeout(String timeout) throws Exception {
        HttpParams httpParams = HttpConnection.client.getParams();
        int originalReadTimeout = HttpConnectionParams.getSoTimeout(httpParams);

        try {
            HttpConnectionParams.setSoTimeout(httpParams, Integer.parseInt(timeout));
            return originalReadTimeout;
        } catch (Exception e) {
            System.out.println("*DEBUG* Exception: " + e);
            throw new Exception("Convert timeout " + timeout + "to integer failed");
        }
    }

    @RobotKeywordOverload
    @ArgumentNames({})
    public int setHttpReadTimeout() throws Exception {
        int originalReadTimeout = HttpConnection.readTimeout;
        setHttpReadTimeout(String.valueOf(HttpConnection.connectionTimeout));

        return originalReadTimeout;
    }

    @RobotKeyword("This keyword sets HTTP headers\n"
                + "The headers can be given with 'key=value' format and can be multiple"
                + "| Options  | Man. | Description |\n"
                + "| headers  | Yes  | HTTP headers |\n\n"
                + "Examples:\n"
                + "| Set HTTP Headers | a=1 |\n"
                + "| Set HTTP Headers | a=1 | b=2 |\n"
                + "| ${original_http_headers} | Set HTTP Headers | c=3 |\n"
                + "| Set HTTP Headers | ${original_http_headers} |\n")
    @ArgumentNames({"*headers"})
    public Header[] setHttpHeaders(String... headers) {
        Header[] originalHttpHeaders = HttpConnection.headers;
        HttpConnection.headers = new Header[headers.length];
        int index = 0;
        for(String header:headers) {
            try {
                String[] headerNameValuePair = header.split("=");
                HttpConnection.headers[index] = new BasicHeader(headerNameValuePair[0], headerNameValuePair[1]);
                index ++;
            } catch (Exception e) {
                System.out.println("*WARN* HTTP header " + header + " is invalid");
                System.out.println("*WARN* Exception is " + e.toString());
            }
        }

        return originalHttpHeaders;
    }

    @RobotKeyword("This keyword resets HTTP headers to default value\n"
                + "The default HTTP header is \"Content-Type\" = \"application/x-www-form-urlencoded\" and "
                + "\"Accept-Charset\" =  " + CHARACTER_ENCODING + "\n\n"
                + "Examples:\n"
                + "| Reset HTTP Headers |\n")
    @ArgumentNames({})
    public static void resetHttpHeaders() {
        HttpConnection.headers = HttpConnection.initHeaders.clone();
    }
	
	@RobotKeyword("This keyword sends HTTP message via POST method\n\n"
			    + "It returns HttpResponseResult object and following attributes can be directly accessed\n"
			    + "- statusCode: HTTP response code\n"
                + "- headers: HTTP response headers, it is an array\n"
			    + "- rawBody: HTTP response body\n"
			    + "- jsonBody: HTTP response body, but with JSON format\n"
			    + "| Options  | Man. | Description |\n"
			    + "| url      | Yes  | URL |\n"
			    + "| data     | Yes  | Message body |\n\n"
			    + "Examples:\n"
			    + "| POST | http://1.2.3.4:5678 | ${EMPTY} |\n"
			    + "| POST | http://1.2.3.4:5678 | name=yixin&id=123 |\n"
			    + "| ${resp} | POST | http://1.2.3.4:5678 | {\"message\":\"test\"} |\n"
			    + "| Should Be Equal As Strings | ${resp.statusCode} | 200 |\n"
			    + "| Should Be Equal As Strings | ${resp.jsonBody[\"code\"] | 1 |")
	@ArgumentNames({"uri", "data"})
	public static HttpResponseResult post(String uri, String data) throws Exception {
		StringEntity stringEntity = new StringEntity(data, CHARACTER_ENCODING);

		HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeaders(HttpConnection.headers);
		httpPost.setEntity(stringEntity);
		
		System.out.println("*INFO* Request: POST " + uri + " " + data);
        PrintHttpClientInformation();

		try {
		    HttpResponse httpResponse = HttpConnection.client.execute(httpPost);
		    return new HttpResponseResult(httpResponse);
		}
		finally {
			httpPost.releaseConnection();
		}
	}
	
	@RobotKeyword("This keyword sends HTTP message via GET method\n\n"
			    + "It returns HttpResponseResult object and following attributes can be directly accessed\n"
			    + "- statusCode: HTTP response code\n"
                + "- headers: HTTP response headers, it is an array\n"
			    + "- rawBody: HTTP response body\n"
			    + "- jsonBody: HTTP response body, but with JSON format\n"
			    + "| Options  | Man. | Description |\n"
			    + "| url      | Yes  | URL |\n\n"
			    + "Examples:\n"
			    + "| Get | http://1.2.3.4:5678 |\n"
			    + "| ${resp} | Get | http://1.2.3.4:5678?name=yixin&id=1 |\n"
			    + "| Should Be Equal As Strings | ${resp.statusCode} | 200 |\n"
			    + "| Should Be Equal As Strings | ${resp.jsonBody[\"code\"] | 1 |")
	@ArgumentNames({"uri"})
	public static HttpResponseResult get(String uri) throws Exception {
		HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeaders(HttpConnection.headers);
		
	    System.out.println("*INFO* Request: GET " + uri);
        PrintHttpClientInformation();

		try {
			HttpResponse httpResponse = HttpConnection.client.execute(httpGet);
			return new HttpResponseResult(httpResponse);
		}
		finally {
			httpGet.releaseConnection();
		}
	}

    private static DefaultHttpClient createDefaultHttpClient() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, HttpConnection.connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, HttpConnection.readTimeout);

        return new DefaultHttpClient(httpParams);
    }

    private static void PrintHttpClientInformation() {
        System.out.println("*INFO* Request Headers: " + StringUtils.join(HttpConnection.headers, " | "));
        System.out.println("*INFO* Request Cookies: " + HttpConnection.client.getCookieStore());

    }
}