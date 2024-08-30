package com.mw.js.resolved.module;

import com.liferay.frontend.js.loader.modules.extender.internal.npm.builtin.BaseBuiltInJSModuleServlet;
import com.liferay.frontend.js.loader.modules.extender.internal.npm.builtin.ResourceDescriptor;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.MimeTypes;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Servlet;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.servlet.name=MW Serve Package Servlet",
		"osgi.http.whiteboard.context.path=/",
		"osgi.http.whiteboard.servlet.pattern=/mw-resolved-module/*",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 100)
	},
	service = {MWBuiltInJSResolvedModuleServlet.class, Servlet.class}
)
public class MWBuiltInJSResolvedModuleServlet extends BaseBuiltInJSModuleServlet {
	
	@Activate
	protected void activate(Map<String, Object> properties) {
		if (_log.isInfoEnabled()) _log.info("activated...");
	}

	@Override
	protected MimeTypes getMimeTypes() {
		return _mimeTypes;
	}

	@Override
	protected ResourceDescriptor getResourceDescriptor(String pathInfo) {
		String identifier = pathInfo.substring(1);
		
		_log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		Collection<JSPackage> jsPackages = _npmRegistry.getResolvedJSPackages();
		
		for (JSPackage jsPackageMW : jsPackages) {
			_log.debug("resolvedId: " + jsPackageMW.getResolvedId() + ", name: " + jsPackageMW.getName() + ", id: " + jsPackageMW.getId() + ", modules.size: " + jsPackageMW.getJSModules().size());
		}
		
		_log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		_log.info("pathInfo: " + pathInfo);
		_log.info("identifier: " + identifier);
		
		String packageName = ModuleNameUtil.getPackageName(identifier);
		
		_log.info("packageName: " + packageName);
		
		String jsPackageId = _jsPackageIdsCache.get(packageName);
		
		_log.info("jsPackageId: " + jsPackageId);
		
		JSPackage jsPackage = _getJSPackage(packageName);

		if (jsPackage == null) {
			_log.info("jsPackage null for packageName: " + packageName + ", identifier: " + identifier);
			
			return null;
		} else {
			_log.info("jsPackage.getId: " + jsPackage.getId());
			_log.info("jsPackage.getVersion: " + jsPackage.getVersion());
			
			_log.info("jsPackage.getName: " + jsPackage.getName());
			_log.info("jsPackage.getResolvedId: " + jsPackage.getResolvedId());
			_log.info("jsPackage.getMainModuleName: " + jsPackage.getMainModuleName());
			_log.info("jsPackage.getJSBundle.getName: " + jsPackage.getJSBundle().getName());
			
			_log.info("jsPackage.getJSModules.size: " + jsPackage.getJSModules().size());
		}
		
		ResourceDescriptor resourceDescriptor = new ResourceDescriptor(jsPackage, ModuleNameUtil.getPackagePath(identifier));

		_log.info("resourceDescriptor: getJsPackage().getName(): " + resourceDescriptor.getJsPackage().getName() + ", getPackagePath(): " + resourceDescriptor.getPackagePath());

		return resourceDescriptor;
	}

	private JSPackage _getJSPackage(String packageName) {
		String jsPackageId = _jsPackageIdsCache.get(packageName);

		if (jsPackageId != null) {
			JSPackage jsPackage = _npmRegistry.getJSPackage(jsPackageId);

			if (jsPackage != null) {
				return jsPackage;
			}

			_jsPackageIdsCache.remove(packageName);
		}

		Collection<JSPackage> jsPackages = _npmRegistry.getResolvedJSPackages();

		for (JSPackage jsPackage : jsPackages) {
			if (packageName.equals(jsPackage.getResolvedId())) {
				_jsPackageIdsCache.put(packageName, jsPackage.getId());

				return jsPackage;
			}
		}

		return null;
	}

	private static final long serialVersionUID = 2647715401054034600L;

	private final LinkedHashMap<String, String> _jsPackageIdsCache =
		new LinkedHashMap<String, String>() {

			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				Collection<JSPackage> jsPackages =
					_npmRegistry.getResolvedJSPackages();

				if (size() > jsPackages.size()) {
					return true;
				}

				return false;
			}

		};
		
	private static final Log _log = LogFactoryUtil.getLog(MWBuiltInJSResolvedModuleServlet.class);	

	@Reference
	private MimeTypes _mimeTypes;

	@Reference
	private NPMRegistry _npmRegistry;

}