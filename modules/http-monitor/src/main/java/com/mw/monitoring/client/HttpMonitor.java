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
	property = {"osgi.command.function=checkPages", "osgi.command.scope=httpMonitor"},
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
		
		int pageDetailsCount = 0;
		int pos = 0;
		int pageFoundCount = 0;
		int pageContentFoundCount = 0;
		
		if (checkPages.length == 0) {
			log("No Page Details defined. Please review the Configuration.");
			
			return;
		}
		
		for (int i = 0; i < checkPages.length; i++) {
			pageDetailsCount ++;
			pos ++;
			
			boolean isValidPageDetail = validatePageDetail(pos, checkPages[i]);
			
			if (isValidPageDetail) {
				String checkPage = checkPages[i];
				
				String[] tokens = checkPage.split("\\|");
				
				String relativePageUrl = tokens[0].trim();
				String expectedPageContent = tokens[1].trim();
				
				boolean[] checkPageResult = checkPage(pos, relativePageUrl, expectedPageContent);
				
				// response boolean[] is {pageFound, pageContentFound}
				if (checkPageResult[0]) pageFoundCount ++;
				if (checkPageResult[1]) pageContentFoundCount ++;
			}
		}
		
		double totalDurationSeconds = Math.ceil((double)(System.currentTimeMillis() - start) / 1000);
		
		log("Page Details count: " + pageDetailsCount + ", page found count: " + pageFoundCount + ", page content found count: " + pageContentFoundCount + ", total duration: " + totalDurationSeconds + " second(s).");
	}
	
	private boolean validatePageDetail(int pos, String pageDetail) {
		if (Validator.isNull(pageDetail)) {
			log("[" + pos + "]: CONFIG ERROR: invalid pageDetails value.");
		} else {
			String[] tokens = pageDetail.split("\\|");
			
			if (tokens.length == 2) {
				String relativePageUrl = tokens[0].trim();
				String expectedPageContent = tokens[1].trim();
				
				if (Validator.isNull(relativePageUrl) || Validator.isNull(expectedPageContent)) {
					log("[" + pos + "]: CONFIG ERROR: invalid pageDetails token(s).");		
				} else if (!relativePageUrl.startsWith("/")) {
					log("[" + pos + "]: CONFIG ERROR: relativePageUrl must start with a / character.");	
				} else {
					return true;					
				}
			} else {
				log("[" + pos + "]: CONFIG ERROR: invalid token count in pageDetails.");		
			}				
		}		
		
		return false;
	}
	
	private String getPageUrl(String relativePageUrl) {
		return httpMonitorConfiguration.protocol() + "://" + httpMonitorConfiguration.hostname() + ":" + httpMonitorConfiguration.port() + relativePageUrl;
	}
	
	private boolean[] checkPage(int pos, String pageUrl, String expectedPageContent) {
		String absolutePageUrl = getPageUrl(pageUrl);
		
		log("[" + pos + "]: URL: " + absolutePageUrl);
		log("[" + pos + "]: expectedPageContent: " + expectedPageContent);
		
		HttpURLConnection httpURLConnection = null;
		BufferedReader bufferedReader = null;

		try {
			URL url = new URL(absolutePageUrl);
			httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setRequestProperty("Content-Type", "text/html;charset=UTF-8");
			
			httpURLConnection.setConnectTimeout(httpMonitorConfiguration.connectTimeout());
			httpURLConnection.setReadTimeout(httpMonitorConfiguration.readTimeout());
			
			int status = httpURLConnection.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				log("[" + pos + "]: FAILURE: Unexpected HTTP Status Code: " + status + " for URL: " + absolutePageUrl);
				
				// response boolean[] is {pageFound, pageContentFound}
				boolean[] response = { false, false };
				
				return response;
			}
			
			bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			
			StringBuffer pageContent = new StringBuffer();
			while ((inputLine = bufferedReader.readLine()) != null) {
				pageContent.append(inputLine);
			}
			
			int contentPosition = pageContent.indexOf(expectedPageContent);
			
			if (contentPosition == -1) {
				log("[" + pos + "]: FAILURE: Expected content not found.");
				
				// response boolean[] is {pageFound, pageContentFound}
				boolean[] response = { true, false };
				
				return response;	
			} else {
				log("[" + pos + "]: SUCCESS: Expected content found.");
				
				// response boolean[] is {pageFound, pageContentFound}
				boolean[] response = { true, true };
				
				return response;				
			}

		} catch (Exception e) {
			log("[" + pos + "]: FAILURE: " + e.getMessage() + ", " + e);
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
		
		// response boolean[] is {pageFound, pageContentFound}
		boolean[] response = { false, false };
		
		return response;
	}
	
	private void log(String value) {
		_log.info(value);
		
		System.out.println(value);
	}
	
	private volatile HttpMonitorConfiguration httpMonitorConfiguration;
	
	private static final Log _log = LogFactoryUtil.getLog(HttpMonitor.class);
}