package engine;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.database.Cursor;

public class AuthPortalGen {
	private static final int LOGIN_FAILED = 0;
	private final static int LOGIN_SUCCESS = -1;
	private final static int RET_UNKNOWN = -2;
	private final static int RET_ALREADY = -3;
	private static final int EXCEPTION_FAILED = -4;
	private static final int GET_PASSWORD_SUCCESS = 1;
	private static final int GET_PASSWORD_UNKNOWN = 2;
	private static final int GET_PASSWORD_EXCEPTION = 3;
	private static final int GET_PASSWORD_FAILED = 4;
	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	private static String HOST_PATTERN = "http://.*?/";
	private static AuthPortalGen instance = null;
	private static String aimUrl;
	private static Pattern hostPattern;
	private String user = null;
	private String password = null;
	private DataBaseHelper mydatabase;
	private Cursor cursor_login;
	private String host;

	private AuthPortalGen() {
		hostPattern = Pattern.compile(HOST_PATTERN, Pattern.DOTALL);
		Logger.getInstance().writeLog("此时执行了构造函数");
		aimUrl = getAimUrl(LOGIN_TEST_URL);
//		aimURL 就是 host
		Matcher hostMatcher = hostPattern.matcher(aimUrl);
		hostMatcher.find();
		host=hostMatcher.group(0);
		// 最终返回的是host，http://www.baidu.com/

	}

	public static AuthPortalGen getInstance() {
		instance = new AuthPortalGen();
		return instance;
	}

	public String getDescription(int code) {
		String ret = "未知错误代码" + code;
		switch (code) {

		case RET_ALREADY:
			ret = "已经在线，无需重复登录（是否上次登录未下线？）";
			break;
		case LOGIN_SUCCESS:
			ret = "通过后台登录成功";
			break;
		case RET_UNKNOWN:
			ret = "未知错误";
			break;
		case LOGIN_FAILED:
			ret = "登陆失败";
			break;
		case EXCEPTION_FAILED:
			ret = "程序出现异常";
			break;
		}
		Logger.getInstance().writeLog(
				"Get description " + ret + " for code " + code);
		return ret;
	}

	public int login(String user, String password, Context context, String SSID) {
		this.user = user;
		this.password = password;
		try {

			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			String output = null;
			response = client.execute(new HttpGet(aimUrl));
			output = EntityUtils.toString(response.getEntity(), "GBK");

			Logger.getInstance().writeLog("Http Request:\n" + aimUrl);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);

			// 首先进入登陆状态的判别
			mydatabase = new DataBaseHelper(context);
//			首先定位光标，方便一会取值
			cursor_login = mydatabase.getInstance().query(
					SSID,// 到时候这里可以换成SSID
					new String[] { "State", "State_Signature", "Next_Action",
							"Next_URL", "Next_Form_Parameters",
							"EXTRA_Parameters", "Next_State", "USER",
							"PASSWORD" }, "State=?",
					new String[] { SSID + "_LOGIN_STATE" }, null, null, null);
			cursor_login.moveToFirst();
			// 判断得已经可以正常浏览网页，无法实现登陆过程
			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("访问百度成功，Already loginned!");
				return RET_ALREADY;
			}

			// 判断确认进入了登陆页面
			else if (output.contains(cursor_login.getString(1))) {
				Logger.getInstance().writeLog("确认进入了登陆页面");
				Logger.getInstance().writeLog("HTTP Response:\n" + output);
				// NEXTURL的获取
				Pattern patternURL = Pattern.compile(cursor_login.getString(3),
						Pattern.DOTALL);
				Matcher matcherURL = patternURL.matcher(output);
				matcherURL.find();
				String NEXT_URL = matcherURL.group(1);
				// 获得了数据库里的NEXTURL内容
				if (NEXT_URL.charAt(0) == '/') {
					NEXT_URL = NEXT_URL.substring(1);
					NEXT_URL = host + NEXT_URL;
					// e.g: /u/
				}
				if (NEXT_URL.contains("//")) {
					// 不作处理
				}

				// extras是Mobile=$user;K=$password;isok=1的形式
				// extra是hashmap
				ArrayList<HashMap<String, String>> extras = new ArrayList<HashMap<String, String>>();
				String EXTRA = cursor_login.getString(5);
				// 首先获得由；隔开的各个字符串
				String[] strings = EXTRA.split(";");
				for (String string : strings) {
					String[] pairs = string.split("=");
					HashMap<String, String> hashmap = new HashMap<String, String>();
					// key为=前
					// value为=后
					hashmap.put("key", pairs[0]);
					hashmap.put("value", pairs[1]);
					extras.add(hashmap);
				}

				// 判断NEXT_ACTION为 什么，觉得请求策略
				if (cursor_login.getString(2).equals("POST")) {
					HttpPost httpPost = new HttpPost(NEXT_URL);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					for (HashMap<String, String> extra : extras) {
						params.add(new BasicNameValuePair(extra.get("key"),
								extra.get("value")));
					}
					String localuser = cursor_login.getString(7);
					String localpassword = cursor_login.getString(8);
					params.add(new BasicNameValuePair(localuser, user));
					params.add(new BasicNameValuePair(localpassword, password));
					/* 添加请求参数到请求对象 */
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					response = client.execute(httpPost);
				} else {
					StringBuilder build = new StringBuilder();
					build.append(cursor_login.getString(7) + "=" + user);
					build.append("&" + cursor_login.getString(8) + "="
							+ password);
					for (HashMap<String, String> extra : extras) {
						build.append("&" + extra.get("key") + "="
								+ extra.get("value"));
					}
					HttpGet httpget = new HttpGet(NEXT_URL + "?"
							+ build.toString());
					response = client.execute(httpget);
				}
				output = EntityUtils.toString(response.getEntity(), "GBK");
				Logger.getInstance().writeLog("Http Request:\n" + NEXT_URL);
				Logger.getInstance().writeLog(output);

				// 找到代表登陆失败的tag，预存字符串
				Cursor cursor_login_success = mydatabase.getInstance().query(
						SSID,// 到时候这里可以换成SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD" }, "State=?",
						new String[] { SSID + "_LOGIN_SUCCESS_STATE" }, null,
						null, null);
				cursor_login_success.moveToFirst();
				String LOGIN_SUCCESS_SIGNATURE = cursor_login_success
						.getString(1);

				// 找到代表登陆成功的tag，预存字符串
				Cursor cursor_login_failed = mydatabase.getInstance().query(
						SSID,// 到时候这里可以换成SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State" }, "State=?",
						new String[] { SSID + "_LOGIN_FAILED_STATE" }, null,
						null, null);
				cursor_login_failed.moveToFirst();
				String LOGIN_FAILED_SIGNATURE = cursor_login_failed
						.getString(1);

				// 进行判断，是否后台登陆成功？？？
				if (output.contains(LOGIN_SUCCESS_SIGNATURE)) {
					Logger.getInstance().writeLog("Login success!");
					return LOGIN_SUCCESS;
				} else if (output.contains(LOGIN_FAILED_SIGNATURE)) {
					Logger.getInstance().writeLog("Login failed!");
					return LOGIN_FAILED;
					// 典型：账号密码输错
				}

			}
			// 发现没有能够进入登陆页面
			else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			Logger.getInstance().writeLog("因为异常导致登陆失败" + e.toString());
			e.printStackTrace();
			return EXCEPTION_FAILED;
		}
		Logger.getInstance().writeLog("未知原因导致登陆失败，程序没有crash，然后却走完了");
		return RET_UNKNOWN;
	}

	public static String getAimUrl(String next) {
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

		HttpGet httpget = new HttpGet(next);
		BasicHttpContext context = new BasicHttpContext();
		HttpResponse response;
		try {
			response = httpClient.execute(httpget, context);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
				// http://*
				if (response.getFirstHeader("Location").getValue()
						.contains("://")) {
					next = MyUrlEncode.URLencoding(
							response.getFirstHeader("Location").getValue(),
							"utf-8");
					getAimUrl(next);
				}
				// /abc/*
				else if (response.getFirstHeader("Location").getValue()
						.startsWith("/")) {
					HttpHost currentHost = (HttpHost) context
							.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					next = MyUrlEncode.URLencoding(currentHost.toURI()
							+ response.getFirstHeader("Location").getValue(),
							"utf-8");
					getAimUrl(next);
				}
				// abc/*
				else {
					HttpUriRequest currentReq = (HttpUriRequest) context
							.getAttribute(ExecutionContext.HTTP_REQUEST);
					HttpHost currentHost = (HttpHost) context
							.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq
							.getURI().toString()
							: (currentHost.toURI() + currentReq.getURI());
					next = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
					next = MyUrlEncode.URLencoding(next
							+ response.getFirstHeader("Location").getValue(),
							"utf-8");
					getAimUrl(next);
				}
			}

			else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Logger.getInstance().writeLog("获得了一次终极目标");
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.getInstance().writeLog("目标url为"+next);
		return next;

	}

	public int getDynamicPassword(String user, Context context, String SSID) {
		this.user = user;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			String output = null;
			response = client.execute(new HttpGet(aimUrl));
			output = EntityUtils.toString(response.getEntity(), "GBK");
			Logger.getInstance().writeLog("Http Request:\n" + aimUrl);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);

			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance()
						.writeLog(
								"Already loginned!Can't get portal page to get password!");
				return RET_ALREADY;
			}
			mydatabase = new DataBaseHelper(context);
			Cursor cursor_getpwd = mydatabase.getInstance().query(
					SSID,// 到时候这里可以换成SSID
					new String[] { "State", "State_Signature", "Next_Action",
							"Next_URL", "Next_Form_Parameters",
							"EXTRA_Parameters", "Next_State", "USER",
							"PASSWORD" }, "State=?",
					new String[] { SSID + "_GETPWD_STATE" }, null, null, null);
			cursor_getpwd.moveToFirst();
//			进入登陆页面
			if (output.contains(cursor_getpwd.getString(1))) {
				Pattern pattern = Pattern.compile(cursor_getpwd.getString(3),
						Pattern.DOTALL);
				Matcher matcher = pattern.matcher(output);
				matcher.find();
				String NEXT_URL = matcher.group(1);
				if (NEXT_URL.charAt(0) == '/') {
					NEXT_URL = NEXT_URL.substring(1);
					NEXT_URL =host+NEXT_URL;
				}
				if(NEXT_URL.contains("//"))
				{
//					不作处理
				}
				
//				注意，这里暂时没有涉及什么附加参数，比如isok=1
//				目前只考虑了(只需要填入手机号码即可获得密码)
				
				if (cursor_getpwd.getString(2).equals("POST")) {
					HttpPost httpPost = new HttpPost(NEXT_URL);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair(cursor_getpwd.getString(7), user));
					/* 添加请求参数到请求对象 */
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					response = client.execute(httpPost);
				} else {
					HttpGet httpget = new HttpGet(NEXT_URL
							+ "?" + cursor_getpwd.getString(7) + user);
					response = client.execute(httpget);
				}
				output = EntityUtils.toString(response.getEntity(), "GBK");
				Logger.getInstance().writeLog(output);
//				预存
				Cursor cursor_getpwd_success = mydatabase.getInstance().query(
						SSID,// 到时候这里可以换成SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD" }, "State=?",
						new String[] { SSID + "_GETPWD_SUCCESS_STATE" }, null,
						null, null);
				cursor_getpwd_success.moveToFirst();
				String PASSWORD_SUCCESS_SIGNATURE = cursor_getpwd_success
						.getString(1);
//				预存
				Cursor cursor_pwd_failed = mydatabase.getInstance().query(
						SSID,// 到时候这里可以换成SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD"}, "State=?",
						new String[] { SSID + "_GETPWD_FAILED_STATE" }, null,
						null, null);
				cursor_pwd_failed.moveToFirst();
				String PASSWORD_FAILED_SIGNATURE = cursor_pwd_failed.getString(1);
//正式进行判断后台登陆成功与否
				if (output.contains(PASSWORD_SUCCESS_SIGNATURE)) {
					Logger.getInstance().writeLog("获得密码成功！！！");
					return GET_PASSWORD_SUCCESS;
				}
				// 不出意外获得密码失败也是一样的返回页面
				else if (output.contains(PASSWORD_FAILED_SIGNATURE)) {
					Logger.getInstance().writeLog("获得密码失败！！！");
					return GET_PASSWORD_FAILED;
				}
			} else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Logger.getInstance().writeLog("因异常未能成功获取密码！！！" + e.toString());
			return GET_PASSWORD_EXCEPTION;
		}
		Logger.getInstance().writeLog("程序走完，却因未知原因未能成功获取密码！！！");
		return GET_PASSWORD_UNKNOWN;
	}

}
