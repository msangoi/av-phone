package makeit.airvantage.monitoring;

import net.airvantage.utils.AirVantageClient;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthorizationActivity extends Activity {

	public static final String TOKEN = "token";
	public static final String AUTHORIZATION_CODE = "auth_code";
	public static final int REQUEST_AUTHORIZATION = 1;

	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authorization);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String serverHost = prefs.getString(this.getString(R.string.pref_server_key), null);
        String clientId = prefs.getString(this.getString(R.string.pref_client_id_key), null);

		webview = (WebView) findViewById(R.id.authorization_webview);
		webview.getSettings().setJavaScriptEnabled(true);
		// attach WebViewClient to intercept the callback url
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				// check for our custom callback protocol otherwise use default
				// behavior
				if (url.startsWith("oauth")) {

					Log.d(AuthorizationActivity.class.getName(), "Callback URL: " + url);
					Uri uri = Uri.parse(url);
					// Extract code param
					String code = uri.getQueryParameter("code");
					// Extract token from fragment
					String fragment = uri.getFragment();
					String access_token = null;
					if (fragment != null)
					{
						String paramName = "access_token=";
						int start = fragment.indexOf(paramName);
						int end = fragment.indexOf("&");
						access_token = fragment.substring(start + paramName.length(), end);
					}
					Log.d(AuthorizationActivity.class.getName(), "Access token: " + access_token);
					Log.d(AuthorizationActivity.class.getName(), "OAuth code: " + code);

					// host airvantage detected from callback
					// oauth://airvantage
					if (uri.getHost().equals("airvantage")) {
						if (code != null) {
							sendAuthorizationCode(code);
						}
						else if (access_token != null) {
							sendToken(access_token);
						}
					}

					return true;
				}

				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		String authUrl = AirVantageClient.buildImplicitFlowURL(serverHost, clientId);
		Log.d(AuthorizationActivity.class.getName(), "Auth URL: " + authUrl);
		webview.loadUrl(authUrl);
	}

	private void sendAuthorizationCode(String code) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(AUTHORIZATION_CODE, code);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	private void sendToken(String token) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(TOKEN, token);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
