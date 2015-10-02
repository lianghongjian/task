/* 
* Copyright (C) 2003-2015 eXo Platform SAS.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see http://www.gnu.org/licenses/ .
*/
package org.exoplatform.task.dao.jpa;

import static org.exoplatform.task.dao.condition.Conditions.TASK_COWORKER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_MANAGER;
import static org.exoplatform.task.dao.condition.Conditions.TASK_PARTICIPATOR;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.task.dao.OrderBy;
import org.exoplatform.task.dao.TaskHandler;
import org.exoplatform.task.dao.TaskQuery;
import org.exoplatform.task.dao.condition.AggregateCondition;
import org.exoplatform.task.dao.condition.Condition;
import org.exoplatform.task.dao.condition.SingleCondition;
import org.exoplatform.task.domain.Label;
import org.exoplatform.task.domain.Status;
import org.exoplatform.task.domain.Task;
import org.exoplatform.task.domain.TaskLog;
import org.exoplatform.task.exception.EntityNotFoundException;

/**
 * Created by The eXo Platform SAS
 * Author : Thibault Clement
 * tclement@exoplatform.com
 * 4/8/15
 */
public class TaskDAOImpl extends CommonJPADAO<Task, Long> implements TaskHandler {

  public TaskDAOImpl() {
  }

  @Override
  public List<Task> findByUser(String user) {

    List<String> memberships = new ArrayList<String>();
    memberships.add(user);

    return  findAllByMembership(user, memberships);
  }

  public List<Task> findAllByMembership(String user, List<String> memberships) {

    Query query = getEntityManager().createNamedQuery("Task.findByMemberships", Task.class);
    query.setParameter("userName", user);
    query.setParameter("memberships", memberships);

    return cloneEntities(query.getResultList());
  }

  @Override
  public ListAccess<Task> findTasks(TaskQuery query) {
    return findTasks(query.getCondition(), query.getOrderBy());
  }

  @Override
  public <T> List<T> selectTaskField(TaskQuery query, String fieldName) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery q = cb.createQuery();

    Root<Task> task = q.from(Task.class);

    //List<Predicate> predicates = this.buildPredicate(query, task, cb);
    Predicate predicate = this.buildQuery(query.getCondition(), task, cb, q);

    if(predicate != null) {
      q.where(predicate);
    }

    //
    Path path = null;
    if (fieldName.indexOf('.') != -1) {
      String[] strs = fieldName.split("\\.");
      Join join = null;
      for (int i = 0; i < strs.length - 1; i++) {
        String s = strs[i];
        if (join == null) {
          join = task.join(s);
        } else {
          join = join.join(s);
        }
      }
      path = join.get(strs[strs.length - 1]);
    } else {
      path = task.get(fieldName);
    }
    q.select(path).distinct(true);

    if(query.getOrderBy() != null && !query.getOrderBy().isEmpty()) {
      List<OrderBy> orderBies = query.getOrderBy();
      List<Order> orders = new ArrayList<Order>();
      for(OrderBy orderBy : orderBies) {
        if (!orderBy.getFieldName().equals(fieldName)) {
          continue;
        }
        Path p = task.get(orderBy.getFieldName());
        orders.add(orderBy.isAscending() ? cb.asc(p) : cb.desc(p));
      }
      if (!orders.isEmpty()) {
        q.orderBy(orders);
      }
    }

    final TypedQuery<T> selectQuery = em.createQuery(q);
    return cloneEntities(selectQuery.getResultList());
  }

  @Override
  public Task findTaskByActivityId(String activityId) {
    if (activityId == null || activityId.isEmpty()) {
      return null;
    }
    EntityManager em = getEntityManager();
    Query query = em.createNamedQuery("Task.findTaskByActivityId", Task.class);
    query.setParameter("activityId", activityId);
    try {
      return cloneEntity((Task) query.getSingleResult());
    } catch (PersistenceException e) {
      return null;
    }
  }

  @Override
  public void updateTaskOrder(long currentTaskId, Status newStatus, long[] orders) {
      int currentTaskIndex = -1;
      for (int i = 0; i < orders.length; i++) {
          if (orders[i] == currentTaskId) {
              currentTaskIndex = i;
              break;
          }
      }
      if (currentTaskIndex == -1) {
          return;
      }

      Task currentTask = find(currentTaskId);
      Task prevTask = null;
      Task nextTask = null;
      if (currentTaskIndex < orders.length - 1) {
          prevTask = find(orders[currentTaskIndex + 1]);
      }
      if (currentTaskIndex > 0) {
          nextTask = find(orders[currentTaskIndex - 1]);
      }

      int oldRank = currentTask.getRank();
      int prevRank = prevTask != null ? prevTask.getRank() : 0;
      int nextRank = nextTask != null ? nextTask.getRank() : 0;
      int newRank = prevRank + 1;
      if (newStatus != null && currentTask.getStatus().getId() != newStatus.getId()) {
          oldRank = 0;
          currentTask.setStatus(newStatus);
      }

      EntityManager em = getEntityManager();
      StringBuilder sql = null;

      if (newRank == 1 || oldRank == 0) {
          int increment = 1;
          StringBuilder exclude = new StringBuilder();
          if (nextRank == 0) {
              for (int i = currentTaskIndex - 1; i >= 0; i--) {
                  Task task = find(orders[i]);
                  if (task.getRank() > 0) {
                    break;
                  }
                  task.setRank(newRank + currentTaskIndex - i);
                  update(task);
                  if (exclude.length() > 0) {
                      exclude.append(',');
                  }
                  exclude.append(task.getId());
                  increment++;
              }
          }
          //Update rank of tasks have rank >= newRank with rank := rank + increment
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank + ").append(increment)
                                .append(" WHERE ta.rank >= ").append(newRank);
          if (exclude.length() > 0) {
              sql.append(" AND ta.id NOT IN (").append(exclude.toString()).append(")");
          }

      } else if (oldRank < newRank) {
          //Update all task where oldRank < rank < newRank: rank = rank - 1
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank - 1")
                                .append(" WHERE ta.rank > ").append(oldRank)
                                .append(" AND ta.rank < ").append(newRank);
          newRank --;
      } else if (oldRank > newRank) {
          //Update all task where newRank <= rank < oldRank: rank = rank + 1
          sql = new StringBuilder("UPDATE Task as ta SET ta.rank = ta.rank + 1")
                  .append(" WHERE ta.rank >= ").append(newRank)
                  .append(" AND ta.rank < ").append(oldRank);
          newRank ++;
      }

      if (sql != null && sql.length() > 0) {
          // Add common condition
          sql.append(" AND ta.completed = FALSE AND ta.status.id = ").append(currentTask.getStatus().getId());

          //TODO: This block code is temporary workaround because the update is require transaction
          EntityTransaction trans = em.getTransaction();
          boolean active = false;
          if (!trans.isActive()) {
            trans.begin();
            active = true;
          }

          em.createQuery(sql.toString()).executeUpdate();

          if (active) {
            trans.commit();
          }
      }
      currentTask.setRank(newRank);
      update(currentTask);
  }

  @Override
  public List<Task> findTasksByLabel(long labelId, List<Long> projectIds, String username, OrderBy orderBy) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Task> query = cb.createQuery(Task.class);
    From task = query.from(Task.class);
    //
    Join<Task, Label> label = task.join("labels", JoinType.INNER);
    Predicate labelPred;
    if (labelId > 0) {
      labelPred = cb.equal(label.get("id"), labelId);
    } else {
      labelPred = cb.equal(label.get("username"), username);
    }
    //
    Predicate projectPred = null;
    if (projectIds != null && !projectIds.isEmpty()) {      
      projectPred = cb.in(task.join("status", JoinType.LEFT).get("project").get("id")).value(projectIds);
    }
    query.select(task).distinct(true);
    if (projectPred == null) {
      query.where(labelPred);
    } else {
      query.where(cb.and(labelPred, projectPred));      
    }

    if (orderBy != null) {
      Order order = orderBy.isAscending() ? cb.asc(task.get(orderBy.getFieldName())) : cb.desc(task.get(orderBy.getFieldName()));
      query.orderBy(order);
    }

    try {
      return em.createQuery(query).getResultList();
    } catch (PersistenceException e) {
      return Collections.emptyList();
    }
  }  
   
  public TaskLog addTaskLog(long taskId, TaskLog taskLog) throws EntityNotFoundException {
    Task task = getEntityManager().find(Task.class, taskId);
    task.getTaskLogs().add(taskLog);
    this.update(task);
    return taskLog;
  }

  @Override
  public ListAccess<TaskLog> getTaskLogs(long taskId) {
    Task task = getEntityManager().find(Task.class, taskId);
    if (task.getTaskLogs() == null) {
      return new ListAccessImpl<TaskLog>(TaskLog.class, Collections.<TaskLog>emptyList());
    }
    List<TaskLog> taskLogs = new ArrayList<TaskLog>(task.getTaskLogs().size());
    for (TaskLog log : task.getTaskLogs()) {
      taskLogs.add(log.clone());
    }
    return new ListAccessImpl<TaskLog>(TaskLog.class, taskLogs);
  }

  private ListAccess<Task> findTasks(Condition condition, List<OrderBy> orderBies) {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery q = cb.createQuery();
    q.distinct(true);
    Root<Task> task = q.from(Task.class);

    Predicate predicate = buildQuery(condition, task, cb, q);
    if (predicate != null) {
      q.where(predicate);
    }

    //
    q.select(cb.count(task));
    final TypedQuery<Long> countQuery = em.createQuery(q);

    //
    q.select(task);

    if(orderBies != null && !orderBies.isEmpty()) {
      Order[] orders = new Order[orderBies.size()];
      for(int i = 0; i < orders.length; i++) {
        OrderBy orderBy = orderBies.get(i);
        Path p = task.get(orderBy.getFieldName());
        orders[i] = orderBy.isAscending() ? cb.asc(p) : cb.desc(p);
      }
      q.orderBy(orders);
    }

    final TypedQuery<Task> selectQuery = em.createQuery(q);

    return new JPAQueryListAccess<Task>(Task.class, countQuery, selectQuery);
  }

  private Predicate buildQuery(Condition condition, Root<Task> task, CriteriaBuilder cb, CriteriaQuery query) {
    if (condition == null) {
      return null;
    }
    if (condition instanceof SingleCondition) {
      return buildSingleCondition((SingleCondition)condition, task, cb, query);
    } else if (condition instanceof AggregateCondition) {
      AggregateCondition agg = (AggregateCondition)condition;
      String type = agg.getType();
      List<Condition> cds = agg.getConditions();
      Predicate[] ps = new Predicate[cds.size()];
      for (int i = 0; i < ps.length; i++) {
        ps[i] = buildQuery(cds.get(i), task, cb, query);
      }

      if (ps.length == 1) {
        return ps[0];
      }

      if (AggregateCondition.AND.equals(type)) {
        return cb.and(ps);
      } else if (AggregateCondition.OR.equals(type)) {
        return cb.or(ps);
      }
    }
    return null;
  }

  private <T> Predicate buildSingleCondition(SingleCondition<T> condition, Root<Task> task, CriteriaBuilder cb, CriteriaQuery query) {
    String type = condition.getType();
    String field = condition.getField();
    T value = condition.getValue();

    Join join = null;
    if (field.indexOf('.') > 0) {
      String[] arr = field.split("\\.");
      for (int i = 0; i < arr.length - 1; i++) {
        String s = arr[i];
        if (join == null) {
          join = task.join(s, JoinType.INNER);
        } else {
          join = join.join(s, JoinType.INNER);
        }
      }
      field = arr[arr.length - 1];
    }
    Path path = join == null ? task.get(field) : join.get(field);

    if (TASK_COWORKER.equals(field)) {
      path = task.join(field, JoinType.LEFT);
    } else if (TASK_MANAGER.equals(condition.getField())) {
      path = join.join("manager", JoinType.LEFT);
    } else if (TASK_PARTICIPATOR.equals(condition.getField())) {
      path = join.join("participator", JoinType.LEFT);
    }

    if (SingleCondition.EQ.equals(condition.getType())) {
      return cb.equal(path, value);
    } else if (SingleCondition.LT.equals(condition.getType())) {
      return cb.lessThan((Path<Comparable>) path, (Comparable) value);
    } else if (SingleCondition.GT.equals(condition.getType())) {
      return cb.greaterThan((Path<Comparable>) path, (Comparable) value);
    } else if (SingleCondition.LTE.equals(condition.getType())) {
      return cb.lessThanOrEqualTo((Path<Comparable>)path, (Comparable)value);
    } else if (SingleCondition.GTE.equals(condition.getType())) {
      return cb.greaterThanOrEqualTo((Path<Comparable>)path, (Comparable) value);
    } else if (SingleCondition.IS_NULL.equals(type)) {
      return path.isNull();
    } else if (SingleCondition.NOT_NULL.equals(type)) {
      return path.isNotNull();
    } else if (SingleCondition.LIKE.equals(type)) {
      return cb.like(path, String.valueOf(value));
    } else if (SingleCondition.IN.equals(type)) {
      return path.in((Collection) value);
    } else if (SingleCondition.IS_TRUE.equals(type)) {
      return cb.isTrue(path);
    } else if (SingleCondition.IS_FALSE.equals(type)) {
      return cb.isFalse(path);
    }

    throw new RuntimeException("Condition type " + type + " is not supported");
  }

  private static final ListAccess<Task> EMPTY = new ListAccess<Task>() {
    @Override
    public Task[] load(int index, int length) throws Exception, IllegalArgumentException {
      return new Task[0];
    }

    @Override
    public int getSize() throws Exception {
      return 0;
    }
  };
}

