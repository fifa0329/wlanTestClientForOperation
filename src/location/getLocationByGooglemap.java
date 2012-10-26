package location;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;


public class getLocationByGooglemap  {
	String result;
	String getAddress(String lat, String lag) {
				
		try {

			URL url = new URL("http://maps.google.cn/maps/geo?key=abcdefg&q="
					+ lat + "," + lag);
			InputStream inputStream = url.openConnection().getInputStream();
			InputStreamReader inputReader = new InputStreamReader(inputStream,
					"utf-8");
			BufferedReader bufReader = new BufferedReader(inputReader);

			String line = "", lines = "";

			while ((line = bufReader.readLine()) != null) {
				lines += line;
			}
			if (!lines.equals("")) {

				JSONObject jsonobject = new JSONObject(lines);
				JSONArray jsonArray = new JSONArray(jsonobject.get("Placemark")
						.toString());
				for (int i = 0; i < jsonArray.length(); i++) {
					result=jsonArray.getJSONObject(i).getString("address");
				}

			}

		} catch (Exception e) {
			;
		}
		return result;

	}
}