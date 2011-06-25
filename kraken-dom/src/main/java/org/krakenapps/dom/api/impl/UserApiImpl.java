/*
 * Copyright 2011 Future Systems
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.AbstractApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.api.UserExtensionSchema;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Component(name = "dom-user-api")
@Provides
@JpaConfig(factory = "dom")
public class UserApiImpl extends AbstractApi<User> implements UserApi {
	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	private UserExtensionProviderTracker tracker;

	private Map<String, UserExtensionProvider> userExtensionProviders;

	public UserApiImpl(BundleContext bc) {
		tracker = new UserExtensionProviderTracker(bc);
	}

	@Validate
	public void start() {
		userExtensionProviders = new ConcurrentHashMap<String, UserExtensionProvider>();
		tracker.open();
	}

	@Invalidate
	public void stop() {
		tracker.close();
	}

	@Override
	public Map<String, UserExtensionSchema> getExtensionSchemas() {
		Map<String, UserExtensionSchema> schemas = new HashMap<String, UserExtensionSchema>();
		
		for (String name : userExtensionProviders.keySet()) {
			UserExtensionProvider p = userExtensionProviders.get(name);
			schemas.put(name, p.getSchema());
		}
		
		return schemas;
	}

	@Override
	public Collection<UserExtensionProvider> getExtensionProviders() {
		return new ArrayList<UserExtensionProvider>(userExtensionProviders.values());
	}

	@Override
	public UserExtensionProvider getExtensionProvider(String name) {
		return userExtensionProviders.get(name);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(Organization organization) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u WHERE u.organization = ?").setParameter(1, organization).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(OrganizationUnit ou, boolean includeChildren) {
		EntityManager em = entityManagerService.getEntityManager();
		if (!includeChildren)
			return em.createQuery("FROM User u WHERE u.organizationUnit = ?").setParameter(1, ou).getResultList();
		else
			return getChildren(em, ou);
	}

	@SuppressWarnings("unchecked")
	private Collection<User> getChildren(EntityManager em, OrganizationUnit ou) {
		Collection<User> users = new ArrayList<User>();
		Collection<OrganizationUnit> children = em.createQuery("FROM OrganizationUnit o WHERE o.parent = ?")
				.setParameter(1, ou).getResultList();

		users.addAll(em.createQuery("FROM User u WHERE u.organizationUnit = ?").setParameter(1, ou).getResultList());
		for (OrganizationUnit child : children)
			users.addAll(getChildren(em, child));

		return users;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<User> getUsers(String domainController) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM User u WHERE u.domainController = ?").setParameter(1, domainController)
				.getResultList();
	}

	@Transactional
	@Override
	public User getUser(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(User.class, id);
	}

	@Override
	public void createUser(User user) {
		createUserInternal(user);
		fireEntityAdded(user);
	}

	@Transactional
	private void createUserInternal(User user) {
		EntityManager em = entityManagerService.getEntityManager();
		user.setCreateDateTime(new Date());
		user.setUpdateDateTime(new Date());
		em.persist(user);
	}

	@Override
	public void updateUser(User user) {
		updateUserInternal(user);
		fireEntityUpdated(user);
	}

	@Transactional
	private void updateUserInternal(User user) {
		EntityManager em = entityManagerService.getEntityManager();
		if (user.getId() == 0)
			throw new IllegalArgumentException("check user id");

		User u = em.find(User.class, user.getId());
		if (!u.getOrganization().equals(user.getOrganizationUnit().getOrganization()))
			; // TODO
		u.setOrganizationUnit(user.getOrganizationUnit());
		u.setLoginName(user.getLoginName());
		u.setName(user.getName());
		u.setDescription(user.getDescription());
		u.setPassword(hashPassword(user.getPassword()));
		u.setTitle(user.getTitle());
		u.setEmail(user.getEmail());
		u.setPhone(user.getPhone());
		u.setDomainController(user.getDomainController());
		u.setUpdateDateTime(new Date());
		u.setAdmin(user.getAdmin());
		em.merge(u);
	}

	@Override
	public void removeUser(int id) {
		User user = removeUserInternal(id);
		fireEntityRemoved(user);
	}

	@Transactional
	private User removeUserInternal(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		User user = em.find(User.class, id);
		em.remove(user);
		return user;
	}

	@Override
	public String hashPassword(String text) {
		return Sha1.hashPassword(text);
	}

	private class UserExtensionProviderTracker extends ServiceTracker {
		public UserExtensionProviderTracker(BundleContext bc) {
			super(bc, UserExtensionProvider.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			UserExtensionProvider p = (UserExtensionProvider) super.addingService(reference);
			userExtensionProviders.put(p.getName(), p);
			return p;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			UserExtensionProvider p = (UserExtensionProvider) service;
			userExtensionProviders.remove(p.getName());
			super.removedService(reference, service);
		}
	}
}
