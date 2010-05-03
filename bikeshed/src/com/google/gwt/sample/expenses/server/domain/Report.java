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

import java.util.Collections;
import java.util.Comparator;
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

  /**
   * Comparator for the created field.
   */
  private static final FieldComparator<Date> CREATED_COMPARATOR_ASC = new FieldComparator<Date>(false) {
    @Override
    Date getField(Report r) {
      return r.getCreated();
    }
  };

  /**
   * Comparator for the created field.
   */
  private static final FieldComparator<Date> CREATED_COMPARATOR_DESC = new FieldComparator<Date>(true) {
    @Override
    Date getField(Report r) {
      return r.getCreated();
    }
  };

  /**
   * Comparator for the notes field.
   */
  private static final FieldComparator<String> NOTES_COMPARATOR_ASC = new FieldComparator<String>(false) {
    @Override
    String getField(Report r) {
      return r.getNotes();
    }
  };

  /**
   * Comparator for the notes field.
   */
  private static final FieldComparator<String> NOTES_COMPARATOR_DESC = new FieldComparator<String>(true) {
    @Override
    String getField(Report r) {
      return r.getNotes();
    }
  };

  /**
   * Comparator for the purpose field.
   */
  private static final FieldComparator<String> PURPOSE_COMPARATOR_ASC = new FieldComparator<String>(false) {
    @Override
    String getField(Report r) {
      return r.getPurpose();
    }
  };

  /**
   * Comparator for the purpose field.
   */
  private static final FieldComparator<String> PURPOSE_COMPARATOR_DESC = new FieldComparator<String>(true) {
    @Override
    String getField(Report r) {
      return r.getPurpose();
    }
  };
  
  /**
   * An {@link Comparator} used to compare fields in this class.
   * 
   * @param <C> the comparable within the report to compare
   */
  private abstract static class FieldComparator<C extends Comparable<C>> implements Comparator<Report> {
  
    private static int NEXT = 0;
    private final int id = NEXT++;
    private final boolean descending; 
    
    FieldComparator(boolean descending) {
      this.descending = descending;
    }
    
    public final int compare(Report o1, Report o2) {
      // Compare the fields.
      C c1 = (o1 == null) ? null : getField(o1);
      C c2 = (o2 == null) ? null : getField(o2);
      if (c1 == null && c2 == null) {
        return 0;
      } else if (c1 == null) {
        return descending ? -1 : 1;
      } else if (c2 == null) {
        return descending ? 1 : -1;
      }
      return descending ? c2.compareTo(c1) : c1.compareTo(c2);
    }
    
    public final int getId() {
      return id;
    }
    
    abstract C getField(Report r);
  }

  public static long countReports() {
    EntityManager em = entityManager();
    try {
      return ((Number) em.createQuery("select count(o) from Report o").getSingleResult()).longValue();
    } finally {
      em.close();
    }
  }

  public static long countReportsBySearch(Long employeeId, String startsWith) {
    EntityManager em = entityManager();
    try {
      Query query = queryReportsBySearch(em, employeeId, startsWith, true);
      return ((Number) query.getSingleResult()).longValue();
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
  public static List<Report> findReportEntriesBySearch(Long employeeId,
      String startsWith, String orderBy, int descending, int firstResult,
      int maxResults) {
    // TODO(jlabanca): Switch descending to a boolean when it works.
    EntityManager em = entityManager();
    try {
      Query query = queryReportsBySearch(em, employeeId, startsWith, false);
      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);
      List<Report> reportList = query.getResultList();
      // force it to materialize
      reportList.size();

      // Order the results.
      // We have to sort manually because app engine only supports an inequality
      // check on one field, and we already use it for startsWith.
      if (orderBy != null) {
        // TODO(jlabanca): Can we do full data ordering and search?
        if (orderBy.equals("purpose")) {
          Collections.sort(reportList, descending == 0 ? PURPOSE_COMPARATOR_ASC
              : PURPOSE_COMPARATOR_DESC);
        } else if (orderBy.equals("notes")) {
          Collections.sort(reportList, descending == 0 ? NOTES_COMPARATOR_ASC
              : NOTES_COMPARATOR_DESC);
        } else if (orderBy.equals("created")) {
          Collections.sort(reportList, descending == 0 ? CREATED_COMPARATOR_ASC
              : CREATED_COMPARATOR_DESC);
        }
      }

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

  /**
   * Query for reports based on the search parameters.
   * 
   * @param employeeId the employee id
   * @param startsWith the start substring
   * @param isCount true to query on the count
   * @return the query
   */
  private static Query queryReportsBySearch(EntityManager em, Long employeeId,
      String startsWith, boolean isCount) {
    // Construct a query string.
    boolean isFirstStatement = true;
    boolean hasEmployee = employeeId != null && employeeId >= 0;
    boolean hasStartsWith = startsWith != null && startsWith.length() > 0;
    String retValue = isCount ? "count(o)" : "o";
    String queryString = "select " + retValue + " from Report o";
    if (hasEmployee) {
      queryString += isFirstStatement ? " WHERE" : " AND";
      isFirstStatement = false;
      queryString += " o.reporterKey =:reporterKey";
    }
    if (hasStartsWith) {
      queryString += isFirstStatement ? " WHERE" : " AND";
      isFirstStatement = false;
      queryString += " o.purposeLowerCase >=:startsWith";
      queryString += " AND o.purposeLowerCase <=:startsWithZ";
    }

    // Construct the query;
    Query query = em.createQuery(queryString);
    if (hasEmployee) {
      query.setParameter("reporterKey", employeeId);
    }
    if (hasStartsWith) {
      query.setParameter("startsWith", startsWith);
      query.setParameter("startsWithZ", startsWith + "zzzzzz");
    }
    return query;
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
   * Store a lower case version of the purpose for searching.
   */
  @SuppressWarnings("unused")
  private String purposeLowerCase;

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
    this.purposeLowerCase = purpose == null ? "" : purpose.toLowerCase();
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
