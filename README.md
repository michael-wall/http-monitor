**POC of Liferay PaaS HTTP Monitor**

This module can be used to check if a particular string is present on a page e.g. after a build deployment. This can be used to ensure a specific widget is being rendered as expected.

The component is triggered using the gogo shell from the Liferay PaaS > Liferay Service shell service, and it uses absolute urls with `http://localhost:8080` and site friendly URL syntax as it bypasses the webserver service so it can be targetted at a specific node in a high availability Liferay PaaS environment.

**Configuring the HTTP Monitor**

Go to System Settings > HTTP Monitor > HTTP Monitor Configuration.

For a Liferay PaaS environment leave the Protocol, Hostname and Port settings as the defaults.

For each Page to be monitored create a new Page Details item (with the + icon) in the syntax relativePageUrl|expectedPageContent. 

For example:

`/web/xxx/savings/compare-savings-account-rates|<div class="portlet-boundary portlet-boundary_xxxsavingsfinder_  portlet-static portlet-static-end portlet-decorate  " id="p_p_id_xxxsavingsfinder_INSTANCE_iatm_">`

or 

`/web/xxx/mortgages/mortgage-calculators/mortgage-repayment-calculator|<div class="portlet-boundary portlet-boundary_repaymentcalculator_  portlet-static portlet-static-end portlet-decorate  " id="p_p_id_repaymentcalculator_">`

The relativePageUrl should include the site friendly URL e.g. /web/xxx/savings/compare-savings-account-rates

The expectedPageContent should be a piece of HTML content that is present on the page when the widget is rendering as expected. In this example it is the main HTML div tag for the target widget, which is generally present in a similar format for each widget on each page, and can be found by using for example Chrome > Inspect near the top of the target widget while viewing the page. It should be identical to the HTML from the page, without and additional leading or trailing spaces or carriage returns added etc.

**Running the HTTP Monitor**

1. Go to the Liferay PaaS > Liferay service shell, select a Liferay service instance (for a high availability environment).
2. Enter the command `telnet localhost 11311` then press Enter.
3. Enter the command `httpMonitor:checkPages` then press Enter and wait for the command to complete.
4. Review the onscreen output. For each page / widget combination that didn't work, review the details. If the result is 'Expected content not found.' then using the gogo shell command on the same Liferay service instance try stopping and starting the associated OSGi module.
5. Repeat steps 3 to 4 for the same Liferay service instance. At this point all of the monitored pages on the Liferay service instances should be rendering as expected.
6. Repeat the steps for each of the Liferay service instances.
7. At this point all of the monitored pages on all of the Liferay service instances should be rendering as expected.

**Notes**

1. The module has been tested in a standard DXP 7.3 Liferay PaaS environment, a high availability DXP 7.3 Liferay PaaS environment and a self hosted Liferay DXP 7.4 environment, all using Zulu JDK 8 for compile and runtime.
2. All of the pages should be Public pages that allow Guest access.
3. If an instanceable widget is removed and added back to the same page it will be assigned a new instance id - e.g. the esmm part of offsetcalculatornew_INSTANCE_esmm will be different. 
4. Ensure the Gogo shell command is run in the newly provisioned Liferay PaaS Liferay service shells and not the old Liferay services that are to be terminated after a Liferay PaaS build deployment completes.
5. Ensure the connect and readout timeout values are not too low. The Liferay service's various caches will be empty after a Liferay PaaS build deployment. This can mean that each page is slower to load than normal on each Liferay service instance for the first page load after a Liferay PaaS build deployment.
