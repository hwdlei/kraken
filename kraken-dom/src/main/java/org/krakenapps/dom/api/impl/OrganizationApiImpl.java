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
package org.krakenapps.dom.api.impl;

import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;

@Component(name = "dom-org-api")
@Provides
@JpaConfig(factory = "dom")
public class OrganizationApiImpl extends AbstractApi<Organization> implements OrganizationApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private ProgramApi programApi;

	@Requires
	private OrganizationParameterApi orgParameterApi;

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<Organization> getOrganizations() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM Organization o").getResultList();
	}

	@Transactional
	@Override
	public Organization getOrganization(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(Organization.class, id);
	}

	@Transactional
	@Override
	public Organization getOrganizationForUser(int userId) {
		EntityManager em = entityManagerService.getEntityManager();
		Admin admin = em.find(Admin.class, userId);
		if (admin == null)
			throw new AdminNotFoundException(userId);

		return admin.getUser().getOrganization();
	}

	@Override
	public Organization createOrganization(Organization organization) {
		Organization org = createOrganizationInternal(organization);
		fireEntityAdded(org);
		return org;
	}

	@Transactional
	private Organization createOrganizationInternal(Organization organization) {
		EntityManager em = entityManagerService.getEntityManager();
		organization.setCreateDateTime(new Date());
		organization.setEnabled(true);
		em.persist(organization);

		ProgramProfile profile = new ProgramProfile();
		profile.setName("default");
		profile.setOrganization(organization);
		programApi.createProgramProfile(profile);

		orgParameterApi.setOrganizationParameter(organization.getId(), "default_program_profile_id",
				String.valueOf(profile.getId()));
		return organization;
	}

	@Override
	public Organization updateOrganization(Organization organization) {
		Organization org = updateOrganizationInternal(organization);
		fireEntityUpdated(org);
		return org;
	}

	@Transactional
	private Organization updateOrganizationInternal(Organization organization) {
		EntityManager em = entityManagerService.getEntityManager();
		if (organization.getId() == 0)
			throw new IllegalArgumentException("check organization id");

		Organization org = em.find(Organization.class, organization.getId());
		org.setName(organization.getName());
		org.setAddress(organization.getAddress());
		org.setPhone(organization.getPhone());
		org.setDescription(organization.getDescription());
		org.setDomainController(organization.getDomainController());
		org.setBackupDomainController(organization.getBackupDomainController());
		em.merge(org);
		return org;
	}

	@Override
	public Organization removeOrganization(int id) {
		Organization org = removeOrganizationInternal(id);
		fireEntityRemoved(org);
		return org;
	}

	@Transactional
	private Organization removeOrganizationInternal(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		Organization organization = em.find(Organization.class, id);
		em.remove(organization);
		return organization;
	}
}