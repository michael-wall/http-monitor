**POC of Liferay PaaS HTTP Monitor**

This module can be used to check a set of pages. For each it checks if a specific piece of HTML is present on a specific page. This can be used to ensure a specific widget is being rendered as expected after a Liferay PaaS build has been deployed in the environment.

Although third party HTTP Monitors exist, their requests would go through the environments webserver and loadbalancer so they wouldn't be able to target the individual Liferay service instances of a high availability environment to check the page on each individual Liferay service instance.

The component is to be triggered using the gogo shell from the Liferay PaaS > Liferay Service shell service. It uses absolute urls with `http://localhost:8080` and site friendly URL syntax to bypass the webserver service (and the load balancer etc). This allows it to targetted at each specific Liferay service instance in a high availability Liferay PaaS environment. This means it can be used to verify that the widget is rendering on the page on each individual Liferay service instance.

**Deploying the HTTP Monitor in Liferay PaaS**

The http-monitor module should be copied into the modules folder of the Liferay service folder of an existing Liferay DXP Cloud workspace so it is deployed as part of the Liferay PaaS build deployment process like other OSGi modules.

Refresh the workspace, then do a Gradle > refresh Gradle Project at the liferay service folder level to ensure the module source code is compiling as expected.

Add the module source code to the git repository to trigger a Liferay PaaS build.

Deploy the resulting build to a Liferay PaaS environment

**Configuring the HTTP Monitor (when deployed)**

Login to the Liferay DXP environment as an Administrator and go to Control Panel > Configuration > System Settings > HTTP Monitor > HTTP Monitor Configuration.

For a Liferay PaaS environment leave the Protocol (http), Hostname (localhost) and Port (8080) settings as the defaults.

For each Page to be monitored create a new Page Details item (with the + icon) in the syntax relativePageUrl|expectedPageContent. For example:

`/web/xxx/savings/compare-savings-account-rates|<div class="portlet-boundary portlet-boundary_xxxsavingsfinder_  portlet-static portlet-static-end portlet-decorate  " id="p_p_id_xxxsavingsfinder_INSTANCE_iatm_">`

or 

`/web/xxx/mortgages/mortgage-calculators/mortgage-repayment-calculator|<div class="portlet-boundary portlet-boundary_repaymentcalculator_  portlet-static portlet-static-end portlet-decorate  " id="p_p_id_repaymentcalculator_">`

The relativePageUrl should include the site friendly URL e.g. /web/xxx/savings/compare-savings-account-rates

The expectedPageContent should be a piece of HTML content that is present on the page when the widget is rendering as expected. In these examples it is the main HTML div tag for the widget in question, which is generally present in a similar format for each widget on each page, and can be found by using for example Chrome > Inspect near the top of the target widget while viewing the page. The check is case sensitive and the value should be IDENTICAL to the HTML string from the page, without any additional leading or trailing spaces or carriage returns added etc.

**Avoid using a piece of content with carriage returns as it may not match correctly.**

Click Save or Update when done.

Use the System Settings export to create a com.mw.monitoring.config.HttpMonitorConfiguration.config configuration file that can be used in the Liferay DXP Cloud Workspace to avoid needing configure for each individual environment manually.

**Running the HTTP Monitor (when deployed)**

1. Go to the Liferay PaaS > Liferay service shell, select a Liferay service instance (for a high availability environment).
2. Enter the command `telnet localhost 11311` then press Enter.
3. Enter the command `httpMonitor:checkPages` then press Enter and wait for the command to complete.
4. Review the onscreen output. For each page / widget combination that didn't work, review the details. If the result is 'Expected content not found.' then using the gogo shell command on the same Liferay service instance try stopping and starting the associated OSGi module.
5. Repeat steps 3 to 4 for the same Liferay service instance. At this point all of the monitored pages on the Liferay service instances should be rendering as expected.
6. Repeat the steps for each of the Liferay service instances.
7. At this point all of the monitored pages on all of the Liferay service instances should be rendering as expected.

**Notes**

1. The module has been tested in a standard DXP 7.3 Liferay PaaS environment, a high availability DXP 7.3 Liferay PaaS environment and a self hosted Liferay DXP 7.3 environment, all using Zulu JDK 8 for compile and runtime and Liferay DXP 7.3 U26.
2. All of the pages should be Public pages that allow Guest access.
3. If an instanceable widget is removed and added back to the same page it will be assigned a new instance id - e.g. the esmm part of offsetcalculatornew_INSTANCE_esmm will be different. 
4. Ensure the Gogo shell command is run in the newly provisioned Liferay PaaS Liferay service shells and not the old Liferay services that are to be terminated after a Liferay PaaS build deployment completes.
5. Ensure the connect and readout timeout values are not too low. The Liferay service's various caches will be empty after a Liferay PaaS build deployment. This can mean that each page is slower to load than normal on each Liferay service instance for the first page load after a Liferay PaaS build deployment.
