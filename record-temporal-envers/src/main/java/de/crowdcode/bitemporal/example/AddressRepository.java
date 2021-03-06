/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.crowdcode.bitemporal.example;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Repository;

@Repository
@Named("addressRepository")
public class AddressRepository {

	@PersistenceContext
	private EntityManager em;

	private AuditReader ar;

	public AuditReader getAuditReader() {
		if (ar == null) {
			return AuditReaderFactory.get(em);
		} else {
			return ar;
		}
	}

	public void setAuditReader(AuditReader ar) {
		this.ar = ar;
	}

	public Address save(Address address) {
		em.persist(address);
		return address;
	}

	public Address update(Address address) {
		em.merge(address);
		return address;
	}

	public Integer getAmount() {
		Query query = em.createQuery("select count(c.id) from AddressImpl c");
		Number amount = (Number) query.getSingleResult();
		return amount.intValue();
	}

	@SuppressWarnings("unchecked")
	public Collection<Address> findAll() {
		Query query = em.createQuery("select c from AddressImpl c");
		return query.getResultList();
	}

	public Address findById(Long id) {
		Query query = em
				.createQuery("select c from AddressImpl c where c.id = :id");
		query.setParameter("id", id);
		return (Address) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public Collection<Address> findAuditedAdressesWithRevision(
			Integer revisionNumber) {
		Collection<Address> auditedAddresses = getAuditReader().createQuery()
				.forEntitiesAtRevision(AddressImpl.class, revisionNumber)
				.getResultList();
		return auditedAddresses;
	}

	public Number findRevisionNumberByAddressIdAndRevisionNumber(
			Long addressId, Number revisionNumber) {
		Number revision = (Number) getAuditReader().createQuery()
				.forRevisionsOfEntity(AddressImpl.class, false, true)
				.addProjection(AuditEntity.revisionNumber().min())
				.add(AuditEntity.id().eq(addressId))
				.add(AuditEntity.revisionNumber().gt(revisionNumber))
				.getSingleResult();
		return revision;
	}

	public List<Object> findEntitiesChangedByRevisionNumber(
			Number revisionNumber) {
		List<Object> modifiedEntities = getAuditReader()
				.getCrossTypeRevisionChangesReader().findEntities(
						revisionNumber);
		return modifiedEntities;
	}
}
