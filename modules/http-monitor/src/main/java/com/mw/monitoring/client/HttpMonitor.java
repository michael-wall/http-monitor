package com.mw.monitoring.client;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.mw.monitoring.config.HttpMonitorConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Michael Wall
 */
@Component(
	immediate = true,
	configurationPid = HttpMonitorConfiguration.PID,
	property = {"osgi.command.function=checkPages", "osgi.command.scope=monitor"},
	service = HttpMonitor.class
)
public class HttpMonitor {

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("activating");
		
		httpMonitorConfiguration = ConfigurableUtil.createConfigurable(HttpMonitorConfiguration.class, properties);

		if (_log.isInfoEnabled()) {		
			_log.info("connectTimeout: " + httpMonitorConfiguration.connectTimeout());
			
			_log.info("readTimeout: " + httpMonitorConfiguration.readTimeout());
			
			_log.info("protocol: " + httpMonitorConfiguration.protocol());
			
			_log.info("hostname: " + httpMonitorConfiguration.hostname());
			
			_log.info("port: " + httpMonitorConfiguration.port());
			
			_log.info("pageDetails count: " + httpMonitorConfiguration.pageDetails().length);
		}

		if (_log.isInfoEnabled()) _log.info("activated");
	}	
	
	public void checkPages() {
		long start = System.currentTimeMillis();
		
		String[] checkPages = httpMonitorConfiguration.pageDetails();
		
		int count = 0;
		int validCount = 0;
		int foundCount = 0;
		
		for (int i = 0; i < checkPages.length; i++) {
			count ++;
			
			String checkPage = checkPages[i];
			
			String[] tokens = checkPage.split("\\|");
			
			if (tokens.length == 2) {
				validCount ++;
				
				String relativePageUrl = tokens[0];
				String expectedPageContent = tokens[1];
				
				boolean found = checkPage(i, relativePageUrl, expectedPageContent);
				
				if (found) foundCount ++;
			} else {
				log("[" + i + "]: ERROR: invalid token count in pageDetails.");		
			}
		}
		
		double totalDurationSeconds = Math.ceil((double)(System.currentTimeMillis() - start) / 1000);
		
		log("Count: " + count + ", valid count: " + validCount + ", found count: " + foundCount + ", total duration: " + totalDurationSeconds + " second(s).");
	}
	
	private String getPageUrl(String relativePageUrl) {
		return httpMonitorConfiguration.protocol() + "://" + httpMonitorConfiguration.hostname() + ":" + httpMonitorConfiguration.port() + relativePageUrl;
	}
	
	private boolean checkPage(int count, String pageUrl, String expectedPageContent) {
		
		if (Validator.isNull(pageUrl) || Validator.isNull(expectedPageContent)) return false;
		
		String absoluteUrl = getPageUrl(pageUrl);
		
		log("[" + count + "]: absoluteUrl: " + absoluteUrl);
		log("[" + count + "]: expectedPageContent: " + expectedPageContent);
		
		HttpURLConnection httpURLConnection = null;
		BufferedReader bufferedReader = null;

		try {
			URL url = new URL(absoluteUrl);
			httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
			
			httpURLConnection.setConnectTimeout(httpMonitorConfiguration.connectTimeout());
			httpURLConnection.setReadTimeout(httpMonitorConfiguration.readTimeout());
			
			int status = httpURLConnection.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				log("[" + count + "]: ERROR: Unexpected HTTP Status Code: " + status + " for URL: " + absoluteUrl);
				
				return false;
			}
			
			bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			
			StringBuffer pageContent = new StringBuffer();
			while ((inputLine = bufferedReader.readLine()) != null) {
				pageContent.append(inputLine);
			}
			
			int pos = pageContent.indexOf(expectedPageContent);
			
			if (pos == -1) {
				log("[" + count + "]: ERROR: Expected content not found.");
				
				return false;
			} else {
				log("[" + count + "]: SUCCESS: Expected content found.");
				
				return true;				
			}

		} catch (Exception e) {
			log("[" + count + "]: ERROR: " + e.getMessage() + ", " + e);
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();	
			}
			
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ioe) {}
			}
		}
		
		return false;
	}
	
	private void log(String value) {
		_log.info(value);
		
		System.out.println(value);
	}
	
	private volatile HttpMonitorConfiguration httpMonitorConfiguration;
	
	private static final Log _log = LogFactoryUtil.getLog(HttpMonitor.class);
}