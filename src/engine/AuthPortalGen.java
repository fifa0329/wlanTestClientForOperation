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
		Logger.getInstance().writeLog("��ʱִ���˹��캯��");
		aimUrl = getAimUrl(LOGIN_TEST_URL);
//		aimURL ���� host
		Matcher hostMatcher = hostPattern.matcher(aimUrl);
		hostMatcher.find();
		host=hostMatcher.group(0);
		// ���շ��ص���host��http://www.baidu.com/

	}

	public static AuthPortalGen getInstance() {
		instance = new AuthPortalGen();
		return instance;
	}

	public String getDescription(int code) {
		String ret = "δ֪�������" + code;
		switch (code) {

		case RET_ALREADY:
			ret = "�Ѿ����ߣ������ظ���¼���Ƿ��ϴε�¼δ���ߣ���";
			break;
		case LOGIN_SUCCESS:
			ret = "ͨ����̨��¼�ɹ�";
			break;
		case RET_UNKNOWN:
			ret = "δ֪����";
			break;
		case LOGIN_FAILED:
			ret = "��½ʧ��";
			break;
		case EXCEPTION_FAILED:
			ret = "��������쳣";
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

			// ���Ƚ����½״̬���б�
			mydatabase = new DataBaseHelper(context);
//			���ȶ�λ��꣬����һ��ȡֵ
			cursor_login = mydatabase.getInstance().query(
					SSID,// ��ʱ��������Ի���SSID
					new String[] { "State", "State_Signature", "Next_Action",
							"Next_URL", "Next_Form_Parameters",
							"EXTRA_Parameters", "Next_State", "USER",
							"PASSWORD" }, "State=?",
					new String[] { SSID + "_LOGIN_STATE" }, null, null, null);
			cursor_login.moveToFirst();
			// �жϵ��Ѿ��������������ҳ���޷�ʵ�ֵ�½����
			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("���ʰٶȳɹ���Already loginned!");
				return RET_ALREADY;
			}

			// �ж�ȷ�Ͻ����˵�½ҳ��
			else if (output.contains(cursor_login.getString(1))) {
				Logger.getInstance().writeLog("ȷ�Ͻ����˵�½ҳ��");
				Logger.getInstance().writeLog("HTTP Response:\n" + output);
				// NEXTURL�Ļ�ȡ
				Pattern patternURL = Pattern.compile(cursor_login.getString(3),
						Pattern.DOTALL);
				Matcher matcherURL = patternURL.matcher(output);
				matcherURL.find();
				String NEXT_URL = matcherURL.group(1);
				// ��������ݿ����NEXTURL����
				if (NEXT_URL.charAt(0) == '/') {
					NEXT_URL = NEXT_URL.substring(1);
					NEXT_URL = host + NEXT_URL;
					// e.g: /u/
				}
				if (NEXT_URL.contains("//")) {
					// ��������
				}

				// extras��Mobile=$user;K=$password;isok=1����ʽ
				// extra��hashmap
				ArrayList<HashMap<String, String>> extras = new ArrayList<HashMap<String, String>>();
				String EXTRA = cursor_login.getString(5);
				// ���Ȼ���ɣ������ĸ����ַ���
				String[] strings = EXTRA.split(";");
				for (String string : strings) {
					String[] pairs = string.split("=");
					HashMap<String, String> hashmap = new HashMap<String, String>();
					// keyΪ=ǰ
					// valueΪ=��
					hashmap.put("key", pairs[0]);
					hashmap.put("value", pairs[1]);
					extras.add(hashmap);
				}

				// �ж�NEXT_ACTIONΪ ʲô�������������
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
					/* ������������������� */
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

				// �ҵ������½ʧ�ܵ�tag��Ԥ���ַ���
				Cursor cursor_login_success = mydatabase.getInstance().query(
						SSID,// ��ʱ��������Ի���SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD" }, "State=?",
						new String[] { SSID + "_LOGIN_SUCCESS_STATE" }, null,
						null, null);
				cursor_login_success.moveToFirst();
				String LOGIN_SUCCESS_SIGNATURE = cursor_login_success
						.getString(1);

				// �ҵ������½�ɹ���tag��Ԥ���ַ���
				Cursor cursor_login_failed = mydatabase.getInstance().query(
						SSID,// ��ʱ��������Ի���SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State" }, "State=?",
						new String[] { SSID + "_LOGIN_FAILED_STATE" }, null,
						null, null);
				cursor_login_failed.moveToFirst();
				String LOGIN_FAILED_SIGNATURE = cursor_login_failed
						.getString(1);

				// �����жϣ��Ƿ��̨��½�ɹ�������
				if (output.contains(LOGIN_SUCCESS_SIGNATURE)) {
					Logger.getInstance().writeLog("Login success!");
					return LOGIN_SUCCESS;
				} else if (output.contains(LOGIN_FAILED_SIGNATURE)) {
					Logger.getInstance().writeLog("Login failed!");
					return LOGIN_FAILED;
					// ���ͣ��˺��������
				}

			}
			// ����û���ܹ������½ҳ��
			else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			Logger.getInstance().writeLog("��Ϊ�쳣���µ�½ʧ��" + e.toString());
			e.printStackTrace();
			return EXCEPTION_FAILED;
		}
		Logger.getInstance().writeLog("δ֪ԭ���µ�½ʧ�ܣ�����û��crash��Ȼ��ȴ������");
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
				Logger.getInstance().writeLog("�����һ���ռ�Ŀ��");
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.getInstance().writeLog("Ŀ��urlΪ"+next);
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
					SSID,// ��ʱ��������Ի���SSID
					new String[] { "State", "State_Signature", "Next_Action",
							"Next_URL", "Next_Form_Parameters",
							"EXTRA_Parameters", "Next_State", "USER",
							"PASSWORD" }, "State=?",
					new String[] { SSID + "_GETPWD_STATE" }, null, null, null);
			cursor_getpwd.moveToFirst();
//			�����½ҳ��
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
//					��������
				}
				
//				ע�⣬������ʱû���漰ʲô���Ӳ���������isok=1
//				Ŀǰֻ������(ֻ��Ҫ�����ֻ����뼴�ɻ������)
				
				if (cursor_getpwd.getString(2).equals("POST")) {
					HttpPost httpPost = new HttpPost(NEXT_URL);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair(cursor_getpwd.getString(7), user));
					/* ������������������� */
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
//				Ԥ��
				Cursor cursor_getpwd_success = mydatabase.getInstance().query(
						SSID,// ��ʱ��������Ի���SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD" }, "State=?",
						new String[] { SSID + "_GETPWD_SUCCESS_STATE" }, null,
						null, null);
				cursor_getpwd_success.moveToFirst();
				String PASSWORD_SUCCESS_SIGNATURE = cursor_getpwd_success
						.getString(1);
//				Ԥ��
				Cursor cursor_pwd_failed = mydatabase.getInstance().query(
						SSID,// ��ʱ��������Ի���SSID
						new String[] { "State", "State_Signature",
								"Next_Action", "Next_URL",
								"Next_Form_Parameters", "EXTRA_Parameters",
								"Next_State", "USER", "PASSWORD"}, "State=?",
						new String[] { SSID + "_GETPWD_FAILED_STATE" }, null,
						null, null);
				cursor_pwd_failed.moveToFirst();
				String PASSWORD_FAILED_SIGNATURE = cursor_pwd_failed.getString(1);
//��ʽ�����жϺ�̨��½�ɹ����
				if (output.contains(PASSWORD_SUCCESS_SIGNATURE)) {
					Logger.getInstance().writeLog("�������ɹ�������");
					return GET_PASSWORD_SUCCESS;
				}
				// ��������������ʧ��Ҳ��һ���ķ���ҳ��
				else if (output.contains(PASSWORD_FAILED_SIGNATURE)) {
					Logger.getInstance().writeLog("�������ʧ�ܣ�����");
					return GET_PASSWORD_FAILED;
				}
			} else {
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Logger.getInstance().writeLog("���쳣δ�ܳɹ���ȡ���룡����" + e.toString());
			return GET_PASSWORD_EXCEPTION;
		}
		Logger.getInstance().writeLog("�������꣬ȴ��δ֪ԭ��δ�ܳɹ���ȡ���룡����");
		return GET_PASSWORD_UNKNOWN;
	}

}
