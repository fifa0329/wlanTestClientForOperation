package engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
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
import android.util.Log;

public class AuthPortalStar {
	private static final int LOGIN_FAILED = 0;
	private final static int LOGIN_SUCCESS = -1;
	private final static int RET_UNKNOWN = -2;
	private final static int RET_ALREADY = -3;
	private static final int EXCEPTION_FAILED = -4;
	private static final int GET_PASSWORD_SUCCESS = 1;
	private static final int GET_PASSWORD_UNKNOWN = 2;
	private static final int GET_PASSWORD_EXCEPTION = 3;
	private static final int GET_PASSWORD_FAILED = 4                                        ;

//������ڰٶȺ�������һ���ܣ������𣿣���
	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_TEST_SIGNATURE = "news.baidu.com";
	private static String LOGIN_REQUEST_SIGNATURE = "���ǰͿ����������������";
	private static String LOGIN_SUCCESS_SIGNATURE = "�ǰͿ˽��㻦��΢��";
	private static String STARBUCKS_PATTERN = "http://.*?/";
	private static String GET_PASSWORD_ACTION = "ck/";
	private static String SUBMIT_ACTION = "u/";
	private static AuthPortalStar instance = null;
	private static String aimUrl;
	private static Pattern starbucksPattern;
	private String user;
	private String password;

	private AuthPortalStar() {
		starbucksPattern = Pattern.compile(STARBUCKS_PATTERN, Pattern.DOTALL);
	}

	public static AuthPortalStar getInstance() {
		if (instance == null) {
			instance = new AuthPortalStar();
			
		}
		aimUrl=getAimUrl(LOGIN_TEST_URL);
		return instance;
	}

	public String getDescription(int code) {
		String ret = "δ֪�������" + code;
		switch (code) {

		case RET_ALREADY:
			ret = "�Ѿ����ߣ������ظ���¼���Ƿ��ϴε�¼δ���ߣ���";
			break;
		case LOGIN_SUCCESS:
			ret = "ͨ���ǰͿ˺�̨��¼�ɹ�";
			break;
		case RET_UNKNOWN:
			ret = "δ֪����";
			break;
		case LOGIN_FAILED:
			ret="��½ʧ��,�Ƿ���������룿";
			break;
		case EXCEPTION_FAILED:
			ret="��������쳣";
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
			response = client.execute(new HttpGet(aimUrl));
			output = EntityUtils.toString(response.getEntity(), "GBK");

			Logger.getInstance().writeLog("Http Request:\n" + aimUrl);
			Logger.getInstance().writeLog("HTTP Response:\n" + output);

			if (output.contains(LOGIN_TEST_SIGNATURE)) {
				Logger.getInstance().writeLog("Already loginned!");
				return RET_ALREADY;
			}

			else if (output.contains(LOGIN_REQUEST_SIGNATURE) )
			{

				Logger.getInstance().writeLog("HTTP Response:\n" + output);

					Matcher starbucksMatcher = starbucksPattern.matcher(aimUrl);
					starbucksMatcher.find();
					String host = starbucksMatcher.group(0);
					HttpPost httpPost = new HttpPost(host
							+ SUBMIT_ACTION);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("Mobile", user));
					params.add(new BasicNameValuePair("isok", "" + 1));
					params.add(new BasicNameValuePair("K", password));
					/* ������������������� */
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
					response = client.execute(httpPost);
					output = EntityUtils.toString(response.getEntity(), "GBK");
					Logger.getInstance().writeLog("Http Request:\n" + host
							+ SUBMIT_ACTION);
					Logger.getInstance().writeLog(output);
					if (output.contains(LOGIN_SUCCESS_SIGNATURE)) {
						Logger.getInstance().writeLog("Login success!");
						return LOGIN_SUCCESS;
					}
					else if(output.contains(LOGIN_REQUEST_SIGNATURE))
					{
						Logger.getInstance().writeLog("Login failed!");
						return LOGIN_FAILED;
//						���ͣ��˺��������
					}

			}
			else 
			{
				Logger.getInstance().writeLog("Can't get login page!");
			}
		} catch (Exception e) {
			Logger.getInstance().writeLog("��Ϊ�쳣���µ�½ʧ��"+e.toString());
			e.printStackTrace();
			return EXCEPTION_FAILED;
		}
		Logger.getInstance().writeLog("δ֪ԭ���µ�½ʧ��");
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
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) 
			{
//				http://*
				if(response.getFirstHeader("Location").getValue().contains("://"))
				{
					next=MyUrlEncode.URLencoding(response.getFirstHeader("Location").getValue(),"utf-8");
					getAimUrl(next);
				}
//				/abc/*
				else if(response.getFirstHeader("Location").getValue().startsWith("/"))
				{
					HttpHost currentHost = (HttpHost) context
							.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					next=MyUrlEncode.URLencoding(currentHost.toURI() + response.getFirstHeader("Location").getValue(),"utf-8");
					getAimUrl(next);
				}
//				abc/*
				else
				{
					HttpUriRequest currentReq = (HttpUriRequest) context
							.getAttribute(ExecutionContext.HTTP_REQUEST);
					HttpHost currentHost = (HttpHost) context
							.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq
							.getURI().toString() : (currentHost.toURI() + currentReq
							.getURI());
					next=currentUrl.substring(0, currentUrl.lastIndexOf('/'));
					next=MyUrlEncode.URLencoding(next+response.getFirstHeader("Location").getValue(),"utf-8");
					getAimUrl(next);
				}
			}

			else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				Logger.getInstance().writeLog("�����һ���ռ�Ŀ��");
			}

			
			


		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		Matcher starbucksMatcher = starbucksPattern.matcher(next);
		starbucksMatcher.find();
		return starbucksMatcher.group(0);
		*/
		return next;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	public int getDynamicPassword(String user) 
	{
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
				Logger.getInstance().writeLog("Already loginned!Can't get portal page to get password!");
				return RET_ALREADY;
			}
			else if (output.contains(LOGIN_REQUEST_SIGNATURE) )
			{
				Logger.getInstance().writeLog("HTTP Response:\n" + output);


					Matcher starbucksMatcher = starbucksPattern.matcher(aimUrl);
					starbucksMatcher.find();
					String host = starbucksMatcher.group(0);
					HttpPost httpPost = new HttpPost(host + GET_PASSWORD_ACTION);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("Mobile", user));
					/* ������������������� */
					httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			        response = client.execute(httpPost);
			        output = EntityUtils.toString(response.getEntity(), "GBK");
			        Logger.getInstance().writeLog(output);
//			???�����Ҿ������������жϻ�ȡ����ɹ�������
			        if(output.contains(LOGIN_REQUEST_SIGNATURE))
			        {
			        	Logger.getInstance().writeLog("�������ɹ�������");
			        	return GET_PASSWORD_SUCCESS;
			        }
//			        ��������������ʧ��Ҳ��һ���ķ���ҳ��
			        else
			        {
			        	Logger.getInstance().writeLog("�������ʧ�ܣ�����");
			        	return GET_PASSWORD_FAILED;
			        }
			}
			else 
			{
				Logger.getInstance().writeLog("Can't get login page!");
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Logger.getInstance().writeLog("���쳣δ�ܳɹ���ȡ���룡����"+e.toString());
			return GET_PASSWORD_EXCEPTION;
		}
		Logger.getInstance().writeLog("��δ֪ԭ��δ�ܳɹ���ȡ���룡����");
		return GET_PASSWORD_UNKNOWN;
	}

	
	
	
	
		
		
		
		
	

}
