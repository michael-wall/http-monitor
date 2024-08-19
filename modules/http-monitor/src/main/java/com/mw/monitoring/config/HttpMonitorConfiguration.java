/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.mw.monitoring.config;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

@ExtendedObjectClassDefinition(category = "http-monitor")
@Meta.OCD(
	id = HttpMonitorConfiguration.PID,
	localization = "content/Language",
	name = "configuration.http-monitor.name",
	description = "configuration.http-monitor.desc"
)
public interface HttpMonitorConfiguration {
	public static final String PID = "com.mw.monitoring.config.HttpMonitorConfiguration";
	
	@Meta.AD(deflt = "http", name = "field.protocol.name", description = "field.protocol.desc", required = false)
	public String protocol();

	@Meta.AD(deflt = "localhost", name = "field.hostname.name", description = "field.hostname.desc", required = false)
	public String hostname();
	
	@Meta.AD(deflt = "8080", name = "field.port.name", description = "field.port.desc", required = false)
	public int port();

	@Meta.AD(deflt = "15000", name = "field.connectTimeout.name", description = "field.connectTimeout.desc", required = false)
	public int connectTimeout();

	@Meta.AD(deflt = "10000", name = "field.readTimeout.name", description = "field.readTimeout.desc", required = false)
	public int readTimeout();

	@Meta.AD(deflt = "", name = "field.pageDetails.name", description = "field.pageDetails.desc", required = false)
	public String[] pageDetails();	
}