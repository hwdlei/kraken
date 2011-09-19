/*
 * Copyright 2011 Future Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dom.api;

import java.util.Collection;
import java.util.Map;

import org.krakenapps.dom.model.Application;
import org.krakenapps.dom.model.ApplicationVersion;
import org.krakenapps.dom.model.Vendor;

public interface ApplicationApi extends EntityEventProvider<Application> {
	Collection<Vendor> getVendors();

	Vendor getVendor(String guid);

	Vendor createVendor(String name);

	Vendor updateVendor(String guid, String name);

	Vendor removeVendor(String guid);

	Collection<Application> getApplications();

	Collection<Application> getApplications(String vendorGuid);

	Application getApplication(String guid);

	Application getApplication(String vendorName, String name);

	Application createApplication(String name, String platform, Map<String, String> props);

	Application createApplication(String vendorGuid, String name, String platform, Map<String, String> props);

	Application updateApplication(String guid, String name, Map<String, String> props);

	Application removeApplication(String guid);

	Collection<ApplicationVersion> getApplicationVersions(String vendorName, String appName);

	ApplicationVersion createApplicationVersion(String vendorName, String appName, String version);

	ApplicationVersion updateApplicationVersion(String guid, String version);

	ApplicationVersion removeApplicationVersion(String guid);
}
