package location;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class Lacci2Lonlat {
	String latitude;
	String longitude;
	    
	
	void getLonlat(int cid,int lac,int mcc,int mnc){
		try{
		// 组装JSON查询字符串
		JSONObject holder = new JSONObject();
		holder.put("version", "1.1.0");
		holder.put("host", "maps.google.com");
		// holder.put("address_language", "zh_CN");
		holder.put("request_address", true);

		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();
		data.put("cell_id", cid); // 25070
		data.put("location_area_code", lac);// 4474
		data.put("mobile_country_code", mcc);// 460
		data.put("mobile_network_code", mnc);// 0
		array.put(data);
		holder.put("cell_towers", array);

		// 创建连接，发送请求并接受回应
		DefaultHttpClient client = new DefaultHttpClient();

		HttpPost post = new HttpPost(
				"http://www.google.com/loc/json");

		StringEntity se = new StringEntity(holder.toString());

		post.setEntity(se);
		HttpResponse resp = client.execute(post);

		HttpEntity entity = resp.getEntity();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(entity.getContent()));
		StringBuffer sb = new StringBuffer();
		String result = br.readLine();

		while (result != null) {

			sb.append(result);
			result = br.readLine();
		}
		JSONObject jsonObject = new JSONObject(sb.toString());

		JSONObject jsonObject1 = new JSONObject(jsonObject
				.getString("location"));

		latitude=jsonObject1.getString("latitude");
		longitude=jsonObject1.getString("longitude");
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public String getlat(){
		return latitude;
	}
	
	
	
	public String getlon(){
		return longitude;
	}
	
	
	
	
	
	
}
