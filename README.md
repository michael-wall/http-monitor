**POC of HTTP Monitor**

This module can be used to check if a particular string is present on a page e.g. after a build deployment.

The component is triggered using the gogo shell from the Liferay PaaS > Liferay Service shell service, and it uses absolute urls with `http://localhost:8080` and site friendly URL syntax as it bypasses the webserver Service so it can be targetted at a specific node in a High Availability environment.

**Configuring the HTTP Monitor**

Go to System Settings > HTTP Monitor > HTTP Monitor Configuration.

For a Liferay PaaS environment leave the Protocol, Hostname and Port settings as the defaults.

For each Page to be monitored create a new Page Details item (with the + icon) in the syntax relativePageUrl|expectedPageContent. 

For example:

`/web/xxx/savings/compare-savings-account-rates|<div class="portlet-boundary portlet-boundary_xxxsavingsfinder_  portlet-static portlet-static-end portlet-decorate  " id="p_p_id_xxxsavingsfinder_INSTANCE_iatm_">`

The relativePageUrl should include the site friendly URL e.g. /web/xxx/savings/compare-savings-account-rates
The expectedPageContent should be a piece of HTML content that is present on the page when the widget is rendering as expected.

**Running the HTTP Monitor**

1. Go to the Liferay PaaS > Liferay service shell, select a Liferay service instance (for a High Availability environment).
2. Entry command `telnet localhost 11311` and press Enter.
3. Enter command `monitor:checkPages` and wait for the command to complete.
4. Review the onscreen output. For each page / widget combination that didn't work, review the details. If the result is 'Expected content not found.' then using the gogo shell command on the same Liferay service instance try stopping and starting the associated OSGi module.
5. Repeat steps 3 to 4 for the same Liferay service instance. At this point all of the monitored pages on the Liferay service instances should be rendering as expected.
6. Repeat the steps for each of the Liferay service instances.
7. At this point all of the monitored pages on all of the Liferay service instances should be rendering as expected.

**Notes**

If an instanceable widget is removed and added back to the same page it will be assigned a new instance id - e.g. the esmm part of offsetcalculatornew_INSTANCE_esmm will be different. 
