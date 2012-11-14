package engine;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.DefaultResponseParser;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AuthPortalCMCC {
	private final static int RET_OTHER		= -1;
	private final static int RET_UNKNOWN	= -2;
	private final static int RET_ALREADY	= -3;
	
	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	private static String LOGIN_REQUEST_SIGNATURE = "cmcccs|login_req";
	private static String LOGIN_FORM_PATTERN = "<form.*?name=\"loginform\".*?action=\"(.*?)\".*?>(.*?)</form>";
	private static String LOGIN_INPUT_PATTERN =  "<input.*?name=\"(.*?)\".*?value=\"(.*?)\".*?>";
	private static String LOGIN_RESPONSE_CODE_PATTERN = "cmcccs\\|login_res\\|(.*?)\\|";
	private static String LOGOUT_RESPONSE_CODE_PATTERN = "cmcccs\\|offline_res\\|(.*?)\\|";
	
	private static AuthPortalCMCC instance = null;
	private String nextAction = null;
	private String user = "";
	private String password = "";
	private Pattern formPattern = null;
	private Pattern inputPattern = null;
	private Pattern loginCodePattern = null;
	private Pattern logoutCodePattern = null;
	
	private AuthPortalCMCC() {
		formPattern = Pattern.compile(LOGIN_FORM_PATTERN, Pattern.DOTALL);
		inputPattern = Pattern.compile(LOGIN_INPUT_PATTERN, Pattern.DOTALL);
		loginCodePattern = Pattern.compile(LOGIN_RESPONSE_CODE_PATTERN, Pattern.DOTALL);
		logoutCodePattern = Pattern.compile(LOGOUT_RESPONSE_CODE_PATTERN, Pattern.DOTALL);
	}
	
	public static AuthPortalCMCC getInstance() {
		if (instance == null) {
			instance = new AuthPortalCMCC();
		}
		return instance;
	}
	
	public String getDescription(int code) {
		String ret = "未知错误代码" + code;
		switch (code) {
		case RET_OTHER:
			ret = "异常错误，请联系10086";
			break;
		case RET_UNKNOWN:
			ret = "未知网络错误";
			break;
		case RET_ALREADY:
			ret = "已经在线，无需重复登录（是否上次登录未下线？）";
			break;
		case 0:
			ret = "操作成功";
			break;
		case 1:
			ret = "用户未注册该业务";
			break;
		case 2:
			ret = "用户当前处于非正常状态";
			break;
		case 3:
			ret = "用户密码错误";
			break;
		case 7:
			ret = "用户IP地址不匹配";
			break;
		case 8:
			ret = "AC名称不匹配";
			break;
		case 15:
			ret = "用户认证被拒绝，同一用户在线中";
			break;
		case 17:
			ret = "同一用户正在认证中";
			break;
		case 21:
			ret = "账号不存在";
			break;
		case 26:
			ret = "用户与在线用户名不一致";
			break;
		case 40:
			ret = "用户免认证到期或者失败";
			break;
		case 55:
			ret = "同一用户在线中";
			break;
		case 105:
			ret = "登录失败，请联系10086";
			break;
		case 106:
			ret = "认证前踢同一用户下线失败，请联系10086";
			break;
		case 107:
			ret = "您当前在线终端数已经超过3个";
			break;
		}
		Logger.getInstance().writeLog("Get description " + ret + " for code " + code);
		return ret;
	}
	
	private String parseAuthenPage(String output) {
		Matcher formMatcher = formPattern.matcher(output);
		if (formMatcher.find()) {
			StringBuffer action = new StringBuffer();
			action.append(formMatcher.group(1)).append("?USER=").append(user).append("&PWD=").append(password);
			Matcher inputMatcher = inputPattern.matcher(formMatcher.group(2));
			while (inputMatcher.find()) {
				String name = inputMatcher.group(1);
				String value = inputMatcher.group(2);
				if (!name.equals("USER") && !name.equals("PWD")) {
					action.append("&").append(name).append("=").append(value);
				}
			}
			return action.toString();
		}
		return null;
	}
	
	private String parseRedirectPage(String output) {
		Matcher inputMatcher = inputPattern.matcher(output);
		String url = null;
		String acName = null;
		String userIp = null;
		while (inputMatcher.find()) {
			String name = inputMatcher.group(1);
			String value = inputMatcher.group(2);
			if (name.equals("portalurl")) {
				url = value;
			} else if (name.equals("wlanacname")) {
				acName = value;
			} else if (name.equals("wlanuserip")) {
				userIp = value;
			}
		}
		if (url != null && acName != null && userIp != null) {
			StringBuffer action = new StringBuffer();
			action.append(url);
			action.append("?wlanacname=").append(acName);
			action.append("&wlanuserip=").append(userIp);
			return action.toString();
		}
		return null;
	}
	
	public int login(String user, String password, Context context) {
		this.user = user;
		this.password = password;
		try {
			Log.v("========================================", getCurUrl());
			
			
			final HttpParams params = new BasicHttpParams();
			// This line causes CMCC-EDU no response.
			//HttpProtocolParams.setUserAgent(params, "G3WLAN");
			// The following lines are for Guangdong AC bug.
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			MyClientConnManager connectionManager = new MyClientConnManager(params, schemeRegistry);
			
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			HttpConnectionParams.setConnectionTimeout(params, 20000);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(params, 40000);
			HttpClient client = new DefaultHttpClient(connectionManager, params);
			HttpResponse response = null;
			String output = null;
			
			
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String bssid = prefs.getString("lastBssid", null);
			if (bssid != null) {
				if (bssid.equals(((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getBSSID())) {
					nextAction = prefs.getString("lastAction", null);
					response = client.execute(new HttpPost(nextAction));
					output = EntityUtils.toString(response.getEntity(), "GBK");
					Logger.getInstance().writeLog("Http Request:\n" + nextAction);
					Logger.getInstance().writeLog("HTTP Response:\n" + output);
					Matcher codeMatcher = loginCodePattern.matcher(output);
					if (codeMatcher.find()) {
						int code = parseCode(codeMatcher.group(1));
						if (code == 0) {
							nextAction = parseAuthenPage(output);  // prepare action parameters for logout
							Logger.getInstance().writeLog("Fast login success!");
							return code;
						}
						Logger.getInstance().writeLog("Fast login code=" + code);
					}
				}
			}
			
			response = client.execute(new HttpGet(LOGIN_TEST_URL));
			output = EntityUtils.toString(response.getEntity(), "GBK");

			Logger.getInstance().writeLog("Http Request:\n" + LOGIN_TEST_URL);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);
			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("Already loginned!");
				return RET_ALREADY;
			}
			
			if (!output.contains(LOGIN_REQUEST_SIGNATURE)) {
				nextAction = parseRedirectPage(output);
				if (nextAction != null) {
					response = client.execute(new HttpGet(nextAction));
					output = EntityUtils.toString(response.getEntity(), "GBK");
					Logger.getInstance().writeLog("Http Request:\n" + nextAction);
					Logger.getInstance().writeLog("HTTP Response:\n" + output);
				} else {
					Logger.getInstance().writeLog("Can't get redirect page!");
					return RET_UNKNOWN;
				}
			}
			
			if (output.contains(LOGIN_REQUEST_SIGNATURE)) {
				nextAction = parseAuthenPage(output);
				prefs.edit().putString("lastAction", nextAction).putString("lastBssid", ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getBSSID()).commit();
				response = client.execute(new HttpPost(nextAction));
				output = EntityUtils.toString(response.getEntity(), "GBK");
				Logger.getInstance().writeLog("Http Request:\n" + nextAction);
				Logger.getInstance().writeLog("HTTP Response:\n" + output);
				Matcher codeMatcher = loginCodePattern.matcher(output);
				if (codeMatcher.find()) {
					int code = parseCode(codeMatcher.group(1));
					if (code == 0) {
						nextAction = parseAuthenPage(output);  // prepare action parameters for logout
						Logger.getInstance().writeLog("Login success!");
					}
					Logger.getInstance().writeLog("Login code=" + code);
					return code;
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
	
	public int logout() {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpPost(nextAction));
			String output = EntityUtils.toString(response.getEntity(), "GBK");
			Logger.getInstance().writeLog("Http Request:\n" + nextAction);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);
			Matcher codeMatcher = logoutCodePattern.matcher(output);
			if (codeMatcher.find()) {
				int code = parseCode(codeMatcher.group(1));
				if (code == 0) {
					Logger.getInstance().writeLog("Logout success!");
				}
				Logger.getInstance().writeLog("Logout code=" + code);
				return code;
			}
		} catch (Exception e) {
			Logger.getInstance().writeLog(e.toString());
			e.printStackTrace();
		}
		return RET_UNKNOWN;
	}
	
	private int parseCode(String codeString) {
		int code = RET_OTHER;
		if (codeString != null) {
			try {
				code = Integer.parseInt(codeString);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return code;
	}
	
	class MyLineParser extends BasicLineParser {
	    @Override
	    public Header parseHeader(
	            final CharArrayBuffer buffer) throws ParseException {
	        try {
	            return super.parseHeader(buffer);
	        } catch (ParseException ex) {
	            // Suppress ParseException exception
	            return new BasicHeader("invalid", buffer.toString());
	        }
	    }
	}
	
	class MyClientConnection extends DefaultClientConnection {
	    @Override
	    protected HttpMessageParser createResponseParser(
	            final SessionInputBuffer buffer,
	            final HttpResponseFactory responseFactory, 
	            final HttpParams params) {
	        return new DefaultResponseParser(
	                buffer, 
	                new MyLineParser(), 
	                responseFactory, 
	                params);
	    }
	}
	
	class MyClientConnectionOperator extends DefaultClientConnectionOperator {
	    public MyClientConnectionOperator(final SchemeRegistry sr) {
	        super(sr);
	    }
	    @Override
	    public OperatedClientConnection createConnection() {
	        return new MyClientConnection();
	    }
	}
	
	class MyClientConnManager extends SingleClientConnManager {
	    public MyClientConnManager(
	            final HttpParams params,
	            final SchemeRegistry sr) {
	        super(params, sr);
	    }
	    @Override
	    protected ClientConnectionOperator createConnectionOperator(
	            final SchemeRegistry sr) {
	        return new MyClientConnectionOperator(sr);
	    }
	}
	
	
	
	public String getCurUrl() {
		String currentUrl = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(LOGIN_TEST_URL);
		BasicHttpContext context = new BasicHttpContext();
		HttpResponse response;
		try {
			response = httpClient.execute(httpget, context);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
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
		return currentUrl;
	}


}
