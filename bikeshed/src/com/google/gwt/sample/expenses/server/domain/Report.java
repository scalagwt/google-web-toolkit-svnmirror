/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.sample.expenses.server.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Version;

/**
 * Models an expense report.
 */
@Entity
public class Report {

  public static long countReports() {
    EntityManager em = entityManager();
    try {
      return ((Number) em.createQuery("select count(o) from Employee o").getSingleResult()).longValue();
    } finally {
      em.close();
    }
  }

  public static final EntityManager entityManager() {
    return EMF.get().createEntityManager();
  }

  @SuppressWarnings("unchecked")
  public static List<Report> findAllReports() {
    EntityManager em = entityManager();
    try {
      List<Report> reportList = em.createQuery("select o from Report o").getResultList();
      // force it to materialize
      reportList.size();
      return reportList;
    } finally {
      em.close();
    }
  }

  public static Report findReport(Long id) {
    if (id == null) {
      return null;
    }
    EntityManager em = entityManager();
    try {
      return em.find(Report.class, id);
    } finally {
      em.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  public static List<Report> findReportEntries(int firstResult, int maxResults) {
    EntityManager em = entityManager();
    try {
      List<Report> reportList = em.createQuery("select o from Report o").setFirstResult(
          firstResult).setMaxResults(maxResults).getResultList();
      // force it to materialize
      reportList.size();
      return reportList;
    } finally {
      em.close();
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Report> findReportsByEmployee(Long employeeId) {
    EntityManager em = entityManager();
    try {
      Query query = em.createQuery("select o from Report o where o.reporterKey =:reporterKey");
      query.setParameter("reporterKey", employeeId);
      List<Report> reportList = query.getResultList();
      // force it to materialize
      reportList.size();
      return reportList;
    } finally {
      em.close();
    }
  }

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(name = "version")
  private Integer version;

  private Date created;
  
  private String notes;

  private String purpose;

  /**
   * Store reporter's key instead of reporter.  See:
   * http://code.google.com/appengine
   * /docs/java/datastore/relationships.html#Unowned_Relationships
   */
  // @JoinColumn
  private Long reporterKey;

  // @JoinColumn
  private Long approvedSupervisorKey;

  public Long getApprovedSupervisorKey() {
    return approvedSupervisorKey;
  }

  public Date getCreated() {
    return this.created;
  }

  public Long getId() {
    return this.id;
  }
  
  public String getNotes() {
    return this.notes;
  }

  public String getPurpose() {
    return this.purpose;
  }

  public Long getReporterKey() {
    return this.reporterKey;
  }

  public Integer getVersion() {
    return this.version;
  }

  public void persist() {
    EntityManager em = entityManager();
    try {
      em.persist(this);
    } finally {
      em.close();
    }
  }

  public void remove() {
    EntityManager em = entityManager();
    try {
      Report attached = em.find(Report.class, this.id);
      em.remove(attached);
    } finally {
      em.close();
    }
  }

  public void setApprovedSupervisorKey(Long approvedSupervisorKey) {
    this.approvedSupervisorKey = approvedSupervisorKey;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  public void setReporterKey(Long reporter) {
    this.reporterKey = reporter;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Id: ").append(getId()).append(", ");
    sb.append("Version: ").append(getVersion()).append(", ");
    sb.append("Created: ").append(getCreated()).append(", ");
    sb.append("Notes: ").append(getNotes()).append(", ");
    sb.append("Purpose: ").append(getPurpose()).append(", ");
    sb.append("Reporter: ").append(getReporterKey()).append(", ");
    sb.append("ApprovedSupervisor: ").append(getApprovedSupervisorKey());
    return sb.toString();
  }
}
