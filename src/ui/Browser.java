package ui;

import com.example.testclient.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Browser extends Activity {
	private Button complete;
	private WebView mWebView;
	private static String HTML_HOME = "/wlantest/current/html/";
	ImageView browser_step;
	private String stepstring;
	private int stepint;
	private ImageView back;
	TextView tip;
	TextView show_id;
	TextView show_password;
	Button clip_id;
	Button clip_password;
	MyApplication mApp;
	String user;
	String password;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		init();

	}

	public void init() {
		show_id = (TextView) findViewById(R.id.show_id);
		show_password = (TextView) findViewById(R.id.show_password);
		clip_id = (Button) findViewById(R.id.clip_id);
		clip_password = (Button) findViewById(R.id.clip_password);
		mApp = (MyApplication) getApplication();
		user = mApp.getUser();
		password = mApp.getPassword();

		show_id.setText(user);
		show_password.setText(password);

		clip_id.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(user);

			}
		});

		clip_password.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(password);

			}
		});

		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Browser.this.finish();
			}
		});
		browser_step = (ImageView) findViewById(R.id.browser_step);
		tip = (TextView) findViewById(R.id.tip);

		stepstring = getIntent().getStringExtra("step");
		stepint = Integer.parseInt(stepstring);
		switch (stepint) {
		case 1:
			browser_step.setImageResource(R.drawable.firststep);
			tip.setText("使用浏览器,完成该开放网络的一次上线下线过程,完成“上线——下线”操作后,点击“测试完成”");
			break;
		case 2:
			browser_step.setImageResource(R.drawable.secondstep);
			tip.setText("测试该无线网络通过浏览器登陆是否可行,根据网页提示操作,完成“上线——下线”操作后,点击“测试完成”");
			break;
		case 3:
			browser_step.setImageResource(R.drawable.thirdstep);
			tip.setText("测试该无线网络通过浏览器登陆是否可行,根据网页提示操作,完成“上线——下线”操作后,点击“测试完成”");
			break;
		case 4:
			browser_step.setImageResource(R.drawable.fourthstep);
			tip.setText("测试该无线网络通过浏览器登陆是否可行,根据网页提示操作,完成“上线——下线”操作后,点击“测试完成”");
			break;

		default:
			break;
		}

		stepint = stepint + 1;

		complete = (Button) findViewById(R.id.complete);
		complete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("step", "" + stepint);
				intent.setClass(Browser.this, Report.class);
				startActivity(intent);
			}
		});

		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new HtmlOutJavaScript(), "HTMLOUT");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				new File(Environment.getExternalStorageDirectory() + HTML_HOME)
						.mkdirs();
				mWebView.loadUrl("javascript:window.HTMLOUT.getAll();");
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				new AlertDialog.Builder(view.getContext())
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								}).setCancelable(false).create().show();
				return true;
			};

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				new AlertDialog.Builder(view.getContext())
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.cancel();
									}
								}).create().show();
				return true;
			};
		});
		mWebView.loadUrl("http://www.baidu.com/");
		mWebView.requestFocus();
	}

	private class HtmlOutJavaScript {

		public void getAll() {
			mWebView.loadUrl("javascript:window.HTMLOUT.getHTML(document.location.pathname, document.documentElement.outerHTML);");
			mWebView.loadUrl("javascript:window.HTMLOUT.getScripts('document', document.scripts.length);");
			mWebView.loadUrl("javascript:window.HTMLOUT.getFrames(top.frames.length);");
		}

		public void getFrames(int count) {
			Log.v("WLANEngine", "Frame count:" + count);
			for (int i = 0; i < count; i++) {
				mWebView.loadUrl("javascript:window.HTMLOUT.getHTML(top.frames["
						+ i
						+ "].document.location.pathname, top.frames["
						+ i
						+ "].document.documentElement.outerHTML)");
				mWebView.loadUrl("javascript:window.HTMLOUT.getScripts('top.frames["
						+ i
						+ "].document', top.frames["
						+ i
						+ "].document.scripts.length)");
			}
		}

		public void getScripts(String doc, int count) {
			Log.v("WLANEngine", "Script count:" + count);
			for (int i = 0; i < count; i++) {
				mWebView.loadUrl("javascript:window.HTMLOUT.getScriptSrc("
						+ doc + ".scripts[" + i + "].src);");
			}
		}

		public void getScriptSrc(String src) {
			if (src.equals("")) {
				return;
			}
			Log.v("WLANEngine", "Script src:" + src);

			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(src);
				HttpResponse response = client.execute(get);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					int length = (int) entity.getContentLength();
					InputStream istream = entity.getContent();

					String outputFileName = Environment
							.getExternalStorageDirectory()
							+ HTML_HOME
							+ URLEncoder.encode(src);
					while (new File(outputFileName + ".script").exists()) {
						outputFileName = outputFileName + "_";
					}
					FileOutputStream oStream = new FileOutputStream(
							outputFileName + ".script");

					int ch = -1;
					int count = 0;
					byte[] buf = new byte[1024];
					while ((ch = istream.read(buf)) > 0) {
						oStream.write(buf, 0, ch);
						count += ch;
					}
					oStream.flush();
					oStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void getHTML(String uri, String html) {
			Log.v("WLANEngine", "URI:" + URLEncoder.encode(uri));
			String outputFileName = Environment.getExternalStorageDirectory()
					+ HTML_HOME + URLEncoder.encode(uri);
			while (new File(outputFileName + ".html").exists()) {
				outputFileName = outputFileName + "_";
			}
			try {
				File outputFile = new File(outputFileName + ".html");
				outputFile.createNewFile();
				FileOutputStream oStream = new FileOutputStream(outputFile);
				oStream.write(html.getBytes());
				oStream.flush();
				oStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void showTips() {
		Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("退出测试");
		alertDialog.setMessage("确定退出本次测试？");
		alertDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(Browser.this, MainActivity.class);
						startActivity(intent);
					}
				});
		alertDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		alertDialog.create().show();
		; // 创建对话框
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.showTips();
			return false;
		}
		return false;
	}

}
