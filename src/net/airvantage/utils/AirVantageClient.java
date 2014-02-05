package net.airvantage.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.airvantage.model.AccessToken;
import net.airvantage.model.Alert;
import net.airvantage.model.AlertsList;
import net.airvantage.model.Datapoint;
import net.airvantage.model.OperationResult;
import net.airvantage.model.SystemsList;
import net.airvantage.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;

public class AirVantageClient {
	
	private static final String APIS = "/api/v1";

	private final String access_token;

	private final String server;

	public static String buildAuthorizationURL(String server, String clientId) {
		return "https://" + server + "/api/oauth/authorize?client_id="
				+ clientId
				+ "&response_type=code&redirect_uri=oauth://airvantage";
	}
	
	public static String buildImplicitFlowURL(String server, String clientId) {
		
		return "https://" + server + "/api/oauth/authorize?client_id="
				+ clientId
				+ "&response_type=token&redirect_uri=oauth://airvantage";
	}

	public static AirVantageClient build(final String server, final String clientId,
			final String clientSecret, final String code) throws IOException {
		OkHttpClient client = new OkHttpClient();

		// Create request for remote resource.
		HttpURLConnection connection = client.open(new URL("https://" + server
				+ "/api/oauth/token?grant_type=authorization_code&code=" + code
				+ "&client_id=" + clientId + "&client_secret=" + clientSecret
				+ "&redirect_uri=oauth://airvantage"));
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);

		// Deserialize HTTP response to concrete type.
		Gson gson = new Gson();
		AccessToken token = gson.fromJson(isr, AccessToken.class);

		return new AirVantageClient(server, token.access_token);
	}

	private String buildEndpoint(String api) {
		return "https://" + server + APIS + api + "?access_token="
				+ access_token;
	}

	public AirVantageClient(String server, String token) {
		this.server = server;
		this.access_token = token;
	}

	public User getCurrentUser() throws IOException {
		OkHttpClient client = new OkHttpClient();

		// Create request for remote resource.
		HttpURLConnection connection = client.open(new URL(
				buildEndpoint("/users/current")));
		InputStream is = connection.getInputStream();
		Log.d(AirVantageClient.class.getName(), "User URL: "
				+ buildEndpoint("/users/current"));

		InputStreamReader isr = new InputStreamReader(is);

		// Deserialize HTTP response to concrete type.
		Gson gson = new Gson();
		return gson.fromJson(isr, User.class);
	}

	public void expire() throws IOException {
		InputStream is = null;
		try {
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(
					server + "/api/oauth/expire?access_token="
							+ access_token));
			is = connection.getInputStream();
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public net.airvantage.model.System getSystem(String uid)
			throws IOException {
		InputStream is = null;
		try {
			Gson gson = new Gson();
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client
					.open(new URL(
							buildEndpoint("/systems")
									+ "&fields=uid,name,commStatus,lastCommDate,data&uid="
									+ uid));
			connection.addRequestProperty("Cache-Control", "no-cache");

			Log.d(AirVantageClient.class.getName(), "Systems URL: "
					+ buildEndpoint("/systems") + "&fields=uid,name,data&uid="
					+ uid);
			is = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(is);

			// Deserialize HTTP response to concrete type.
			List<net.airvantage.model.System> items = gson.fromJson(isr,
					SystemsList.class).items;
			if (items.size() > 0)
				return items.get(0);
			else
				return null;
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public List<Alert> getUnacknowledgedAlerts(String systemUid)
			throws IOException {
		InputStream is = null;
		try {
			Gson gson = new Gson();
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(
					buildEndpoint("/privates/alerts/groups")
							+ "&acknowledged=false&target=" + systemUid));
			connection.addRequestProperty("Cache-Control", "no-cache");

			Log.d(AirVantageClient.class.getName(), "Systems URL: "
					+ buildEndpoint("/privates/alerts/groups") + "&target="
					+ systemUid);
			is = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(is);

			// Deserialize HTTP response to concrete type.
			return gson.fromJson(isr, AlertsList.class).items;
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public List<Datapoint> getLast24Hours(String systemUid, String data)
			throws IOException {
		InputStream is = null;
		try {
			Gson gson = new Gson();
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client
					.open(new URL(
							buildEndpoint("/systems/" + systemUid + "/data/"
									+ data + "/aggregated")
									+ "&fn=mean&from="
									+ (System.currentTimeMillis() - 23 * 60 * 60 * 1000)
									+ "&to="
									+ (System.currentTimeMillis() + 1 * 60 * 60 * 1000)));
			connection.addRequestProperty("Cache-Control", "no-cache");

			Log.d(AirVantageClient.class.getName(),
					"Systems URL: "
							+ buildEndpoint("/systems/" + systemUid + "/data/"
									+ data + "/aggregated")
							+ "fn=mean&from="
							+ (System.currentTimeMillis() - 23 * 60 * 60 * 1000)
							+ "&to="
							+ (System.currentTimeMillis() + 1 * 60 * 60 * 1000));
			is = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(is);

			// Deserialize HTTP response to concrete type.
			Type collectionType = new TypeToken<List<Datapoint>>() {
			}.getType();
			return gson.fromJson(isr, collectionType);
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public Map<String, Integer> getLast24HoursOccurences(String systemUid,
			String data) throws IOException {
		InputStream is = null;
		try {
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client
					.open(new URL(
							buildEndpoint("/systems/" + systemUid + "/data/"
									+ data + "/aggregated")
									+ "&fn=occ&interval=24hour&from="
									+ (System.currentTimeMillis() - 23 * 60 * 60 * 1000)
									+ "&to="
									+ (System.currentTimeMillis() + 1 * 60 * 60 * 1000)));
			connection.addRequestProperty("Cache-Control", "no-cache");

			Log.d(AirVantageClient.class.getName(), "Systems URL: "
					+ buildEndpoint("/systems/" + systemUid + "/data/" + data
							+ "/aggregated") + "&fn=occ&interval=24hour&from="
					+ (System.currentTimeMillis() - 23 * 60 * 60 * 1000)
					+ "&to="
					+ (System.currentTimeMillis() + 1 * 60 * 60 * 1000));
			is = connection.getInputStream();

			Map<String, Integer> values = new HashMap<String, Integer>();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String line = null;

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				is.close();
				JSONArray json = new JSONArray(sb.toString());
				JSONObject jsonValues = json.getJSONObject(0).getJSONObject(
						"value");
				for (int i = 0; i < jsonValues.names().length(); i++) {
					values.put(jsonValues.names().getString(i).trim(),
							jsonValues.getInt(jsonValues.names().getString(i)));
				}
			} catch (JSONException e) {
				Log.e(AirVantageClient.class.getName(), "Error in json", e);
			}

			return values;
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public List<net.airvantage.model.System> getSystems()
			throws IOException {
		InputStream is = null;
		try {
			Gson gson = new Gson();
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(
					buildEndpoint("/systems")
							+ "&fields=uid,name,commStatus,lastCommDate,data"));
			connection.addRequestProperty("Cache-Control", "no-cache");

			Log.d(AirVantageClient.class.getName(), "Systems URL: "
					+ buildEndpoint("/systems") + "&fields=uid,name,data");
			is = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(is);

			// Deserialize HTTP response to concrete type.
			return gson.fromJson(isr, SystemsList.class).items;
		} finally {
			// Clean up.
			if (is != null)
				is.close();
		}
	}

	public String reboot(String systemUid) throws IOException {

		OutputStream out = null;
		InputStream in = null;
		try {
			Gson gson = new Gson();
			OkHttpClient client = new OkHttpClient();
			String body = "{\"requestConnection\" : \"true\", \"systems\" : {\"uids\" : [\""
					+ systemUid + "\"]}}";

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(
					buildEndpoint("/operations/systems/reboot")));
			connection.addRequestProperty("Cache-Control", "no-cache");
			connection.addRequestProperty("Content-Type", "application/json");
			// Write the request.
			connection.setRequestMethod("POST");
			out = connection.getOutputStream();
			out.write(body.getBytes());
			out.close();

			// Read the response.
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Unexpected HTTP response: "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}
			in = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(in);

			// Deserialize HTTP response to concrete type.
			return gson.fromJson(isr, OperationResult.class).operationUid;
		} finally {
			// Clean up.
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}
	}

	public net.airvantage.model.System create(net.airvantage.model.System system) throws IOException {

		OutputStream out = null;
		InputStream in = null;
		try {
			Gson gson = new Gson();
			String body = gson.toJson(system);
			OkHttpClient client = new OkHttpClient();

			// Create request for remote resource.
			HttpURLConnection connection = client.open(new URL(
					buildEndpoint("/systems")));
			connection.addRequestProperty("Cache-Control", "no-cache");
			connection.addRequestProperty("Content-Type", "application/json");
			// Write the request.
			connection.setRequestMethod("POST");
			out = connection.getOutputStream();
			out.write(body.getBytes());
			out.close();

			// Read the response.
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Unexpected HTTP response: "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}
			in = connection.getInputStream();

			InputStreamReader isr = new InputStreamReader(in);

			// Deserialize HTTP response to concrete type.
			return gson.fromJson(isr, net.airvantage.model.System.class);
		} finally {
			// Clean up.
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}
	}
}
