package ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.testclient.R;
import com.nullwire.trace.ExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class Browser extends Activity {
    private Button complete;
    private WebView mWebView;
	private MyApplication appState;
    private static String HTML_HOME = "/wlantest/current/html/";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
    	init();
    }
    


    
    
    public void init(){

    	complete=(Button) findViewById(R.id.complete);
    	complete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
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
            	new File(Environment.getExternalStorageDirectory() + HTML_HOME).mkdirs();
            	mWebView.loadUrl("javascript:window.HTMLOUT.getAll();");
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
	                .setMessage(message)
	                .setPositiveButton(android.R.string.ok,
	                    new AlertDialog.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            result.confirm();
	                        }
	                    })
	                .setCancelable(false)
	                .create()
	                .show();
                return true;
            };
            
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, 
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            result.confirm();
	                        }
	                    })
                    .setNegativeButton(android.R.string.cancel, 
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            result.cancel();
	                        }
	                    })
	                .create()
	                .show();
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
	    	for (int i=0; i<count; i++) {
	    		mWebView.loadUrl("javascript:window.HTMLOUT.getHTML(top.frames[" + i + "].document.location.pathname, top.frames[" + i + "].document.documentElement.outerHTML)");
	    		mWebView.loadUrl("javascript:window.HTMLOUT.getScripts('top.frames[" + i + "].document', top.frames[" + i + "].document.scripts.length)");
	    	}
	    }
	    
	    public void getScripts(String doc, int count) {
	    	Log.v("WLANEngine", "Script count:" + count);
	    	for (int i=0; i<count; i++) {
	    		mWebView.loadUrl("javascript:window.HTMLOUT.getScriptSrc(" + doc + ".scripts[" + i + "].src);");
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
		            int length = (int)entity.getContentLength();
		            InputStream istream = entity.getContent();
		            
		            String outputFileName = Environment.getExternalStorageDirectory() + HTML_HOME + URLEncoder.encode(src);
			    	while (new File(outputFileName+ ".script").exists()) {
			    		outputFileName = outputFileName + "_";
			    	}
		            FileOutputStream oStream = new FileOutputStream(outputFileName+ ".script"); 
		            
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
	    	String outputFileName = Environment.getExternalStorageDirectory() + HTML_HOME + URLEncoder.encode(uri);
	    	while (new File(outputFileName+ ".html").exists()) {
	    		outputFileName = outputFileName + "_";
	    	}
	    	try {
		    	File outputFile = new File(outputFileName+ ".html");
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
	

	
	
	/*
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
		}
		return true;
		
	}
	*/
	
    
}
