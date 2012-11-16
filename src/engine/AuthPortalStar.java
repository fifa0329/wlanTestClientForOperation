package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

public class AuthPortalStar {
	private final static int RET_OTHER = -1;
	private final static int RET_UNKNOWN = -2;
	private final static int RET_ALREADY = -3;
	private final static int SUCCESS = 1;

	private static String LOGIN_TEST_URL = "http://172.13.0.1:80/s_1/";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	private static String LOGIN_REQUEST_SIGNATURE = "在星巴克享受免费无线上网";
	private static String LOGIN_SUCCESS_SIGNATURE = "星巴克江浙沪的微博";
	private static String STARBUCKS_PATTERN = "http://.*?/(.*?)";
	private static String GET_PASSWORD_ACTION = "ck/";
	private static String SUBMIT_ACTION = "u/";
	private static AuthPortalStar instance = null;
	private Pattern starbucksPattern;
	private String user;
	private String password;

	private AuthPortalStar() {
		starbucksPattern = Pattern.compile(STARBUCKS_PATTERN, Pattern.DOTALL);
	}

	public static AuthPortalStar getInstance() {
		if (instance == null) {
			instance = new AuthPortalStar();
		}
		return instance;
	}

	public String getDescription(int code) {
		String ret = "未知错误代码" + code;
		switch (code) {

		case RET_ALREADY:
			ret = "已经在线，无需重复登录（是否上次登录未下线？）";
			break;
		case SUCCESS:
			ret = "通过星巴克后台登录成功";
			break;
		case RET_UNKNOWN:
			ret = "未知错误";
			break;

		}
		Logger.getInstance().writeLog(
				"Get description " + ret + " for code " + code);
		return ret;
	}

	public int login(String user, String password) {
		this.user = user;
		this.password = password;
		try {

			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			String output = null;
			response = client.execute(new HttpGet(LOGIN_TEST_URL));
			output = EntityUtils.toString(response.getEntity(), "GBK");

			Logger.getInstance().writeLog("Http Request:\n" + LOGIN_TEST_URL);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);

			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("Already loginned!");
				return RET_ALREADY;
			}

			if (output.contains(LOGIN_REQUEST_SIGNATURE)) {

				Logger.getInstance().writeLog("HTTP Response:\n" + output);

				try {

					HttpPost httpPost = new HttpPost(getCurUrl()
							+ SUBMIT_ACTION);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("Mobile", user));
					params.add(new BasicNameValuePair("isok", "" + 1));
					params.add(new BasicNameValuePair("K", password));
					/* 添加请求参数到请求对象 */
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					response = client.execute(httpPost);
					output = EntityUtils.toString(response.getEntity(), "GBK");
					Log.v("================================================================",
							output);
					if (output.contains(LOGIN_SUCCESS_SIGNATURE)) {
						Logger.getInstance().writeLog("Login success!");
						return SUCCESS;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			Logger.getInstance().writeLog(e.toString());
			e.printStackTrace();
		}
		return RET_UNKNOWN;
	}

	public String getCurUrl() {
		String currentUrl = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.setRedirectHandler(new RedirectHandler() {

			@Override
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				return null;
			}
		});

		HttpGet httpget = new HttpGet(LOGIN_TEST_URL);
		BasicHttpContext context = new BasicHttpContext();
		HttpResponse response;
		try {
			response = httpClient.execute(httpget, context);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) 
			{
				String eurl = URLEncoder.encode(response.getFirstHeader("Location").getValue(),"utf-8"); 
				eurl=eurl.replace("+", "%20");
				HttpGet httpget_redirect = new HttpGet(eurl);
				

			}

			else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException(response.getStatusLine().toString());
			
			
			HttpUriRequest currentReq = (HttpUriRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq
					.getURI().toString() : (currentHost.toURI() + currentReq
					.getURI());

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Matcher starbucksMatcher = starbucksPattern.matcher(currentUrl);
		starbucksMatcher.find();
		String Url = starbucksMatcher.group(0);
		return Url;
	}

	public void getDynamicPassword(String user) {
		this.user = user;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(getCurUrl() + GET_PASSWORD_ACTION);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Mobile", user));
			/* 添加请求参数到请求对象 */
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = client.execute(httpPost);
			String output = stream2String(response.getEntity().getContent());
			Logger.getInstance().writeLog(output);
			Log.v("================================================================",
					output);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String stream2String(InputStream istream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(istream));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line);
		}
		return total.toString();
	}
	
	
	
	public static String IsOK()
	{
		
		
		
		
		
		
		
		
		
		
		
		
		return null;
		
	}

}
