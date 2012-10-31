package engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

public class AuthPortalCMCC {
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
		formPattern = Pattern.compile(LOGIN_FORM_PATTERN);
		inputPattern = Pattern.compile(LOGIN_INPUT_PATTERN);
		loginCodePattern = Pattern.compile(LOGIN_RESPONSE_CODE_PATTERN);
		logoutCodePattern = Pattern.compile(LOGOUT_RESPONSE_CODE_PATTERN);
	}
	
	public static AuthPortalCMCC getInstance() {
		if (instance == null) {
			instance = new AuthPortalCMCC();
		}
		return instance;
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
	
	public boolean login(String user, String password) {
		this.user = user;
		this.password = password;
		try {
			final HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setUserAgent(params, "G3WLAN");
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			MyClientConnManager connectionManager = new MyClientConnManager(params, schemeRegistry); 
			HttpClient client = new DefaultHttpClient(connectionManager, params);
			
			HttpResponse response = client.execute(new HttpGet(LOGIN_TEST_URL));
			String output = EntityUtils.toString(response.getEntity());
			Logger.getInstance().writeLog("Http Request:\n" + LOGIN_TEST_URL);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);
			
			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("Already loginned!");
				return true;
			}
			
			if (!output.contains(LOGIN_REQUEST_SIGNATURE)) {
				nextAction = parseRedirectPage(output);
				if (nextAction != null) {
					response = client.execute(new HttpGet(nextAction));
					output = EntityUtils.toString(response.getEntity());
					Logger.getInstance().writeLog("Http Request:\n" + nextAction);
					Logger.getInstance().writeLog("HTTP Response:\n" + output);
				} else {
					Logger.getInstance().writeLog("Can't get redirect page!");
					return false;
				}
			}
			
			if (output.contains(LOGIN_REQUEST_SIGNATURE)) {
				nextAction = parseAuthenPage(output);
				response = client.execute(new HttpPost(nextAction));
				output = EntityUtils.toString(response.getEntity());
				Logger.getInstance().writeLog("Http Request:\n" + nextAction);
				Logger.getInstance().writeLog("HTTP Response:\n" + output);
				Matcher codeMatcher = loginCodePattern.matcher(output);
				if (codeMatcher.find()) {
					int code = parseCode(codeMatcher.group(1));
					if (code == 0) {
						nextAction = parseAuthenPage(output);  // prepare action parameters for logout
						Logger.getInstance().writeLog("Login success!");
						return true;
					}
				}
			} else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean logout() {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpPost(nextAction));
			String output = EntityUtils.toString(response.getEntity());
			Logger.getInstance().writeLog("Http Request:\n" + nextAction);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);
			Matcher codeMatcher = logoutCodePattern.matcher(output);
			if (codeMatcher.find()) {
				int code = parseCode(codeMatcher.group(1));
				if (code == 0) {
					Logger.getInstance().writeLog("Logout success!");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private int parseCode(String codeString) {
		int code = -1;
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
}
