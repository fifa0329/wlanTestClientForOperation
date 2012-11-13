package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import engine.AuthPortalCMCC.MyClientConnManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AuthPortalStar {
	private final static int RET_OTHER = -1;
	private final static int RET_UNKNOWN = -2;
	private final static int RET_ALREADY = -3;

	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	private static String LOGIN_REQUEST_SIGNATURE = "星巴克";
	private static String LOGIN_FORM_PATTERN = "<form.*?name=\"loginform\".*?action=\"(.*?)\".*?>(.*?)</form>";
	private static String LOGIN_INPUT_PATTERN = "<input.*?name=\"(.*?)\".*?value=\"(.*?)\".*?>";
	private static String GET_PASSWORD_ACTION = "/ck/";
	private static String SUBMIT_ACTION = "/u/";

	private static AuthPortalStar instance = null;
	private String nextAction = null;
	private String user = "";
	private String password = "";
	private Pattern formPattern = null;
	private Pattern inputPattern = null;
	private Pattern loginCodePattern = null;

	private AuthPortalStar() {
		formPattern = Pattern.compile(LOGIN_FORM_PATTERN, Pattern.DOTALL);
		inputPattern = Pattern.compile(LOGIN_INPUT_PATTERN, Pattern.DOTALL);
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
		Logger.getInstance().writeLog(
				"Get description " + ret + " for code " + code);
		return ret;
	}

	private String parseAuthenPage(String output) {
		Matcher formMatcher = formPattern.matcher(output);
		if (formMatcher.find()) {
			StringBuffer action = new StringBuffer();
			action.append(formMatcher.group(1)).append("?USER=").append(user)
					.append("&PWD=").append(password);
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


	
	
	

	public int login(String user, String password, Context context) {
		this.user = user;
		this.password = password;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			String output = null;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			response = client.execute(new HttpGet(LOGIN_TEST_URL));
			output = EntityUtils.toString(response.getEntity(), "GBK");

			Logger.getInstance().writeLog("Http Request:\n" + LOGIN_TEST_URL);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);
			
			
			
			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("Already loginned!");
				return RET_ALREADY;
			}



			if (output.contains(LOGIN_REQUEST_SIGNATURE)) {
				nextAction = parseAuthenPage(output);
				prefs.edit()
						.putString("lastAction", nextAction)
						.putString(
								"lastBssid",
								((WifiManager) context
										.getSystemService(Context.WIFI_SERVICE))
										.getConnectionInfo().getBSSID())
						.commit();
				response = client.execute(new HttpPost(nextAction));
				output = EntityUtils.toString(response.getEntity(), "GBK");
				Logger.getInstance().writeLog("Http Request:\n" + nextAction);
				Logger.getInstance().writeLog("HTTP Response:\n" + output);



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
	
	
		
	
	
	public void getDynamicPassword(String user,String curUrl) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost=new HttpPost(curUrl+GET_PASSWORD_ACTION);
	        List <NameValuePair> params =new ArrayList <NameValuePair>(); 
	        params.add(new BasicNameValuePair("Mobile", user)); 
	          /* 添加请求参数到请求对象*/
	        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); 
			HttpResponse response = client.execute(httpPost);
			String output = stream2String(response.getEntity().getContent());
			Log.v("=======================================================", output);
			} 
	        catch (Exception e) 
		{
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

}
