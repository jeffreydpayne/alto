package com.frs.alto.hibernate4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.Service;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.internal.ConcurrentStatisticsImpl;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ThreadStatisticsImpl implements WarnableStatisticsImplementor, Service, ApplicationContextAware {
	
	
	private static Log logger = LogFactory.getLog(ThreadStatisticsImpl.class);
	
	private ThreadLocal<StatisticsImplementor> threadStats = new ThreadLocal<StatisticsImplementor>();
	
	private ThreadLocal<Boolean> threadWarning = new ThreadLocal<Boolean>();
	
	private StatisticsImplementor disabledStatistics = null;
	
	private boolean warnOnExtraTransaction = false;
	
	private boolean statisticsEnabled = false;
	
	private ApplicationContext applicationContext = null;
	
	private String sessionFactoryBeanId = "sessionFactory";
		

	private StatisticsImplementor getThreadStatistics() {
		
		if (!statisticsEnabled) {
			if (disabledStatistics == null) {
				disabledStatistics = new ConcurrentStatisticsImpl(getSessionFactory());
				disabledStatistics.clear();
			}
			return disabledStatistics;
		}
		else {
			StatisticsImplementor stats = threadStats.get();
			if (stats == null) {
				stats = new ConcurrentStatisticsImpl(getSessionFactory());
				stats.setStatisticsEnabled(statisticsEnabled);
				stats.clear();
				threadWarning.set(false);
				threadStats.set(stats);
			}
			return stats;
		}
		
	}
	
	
	

	@Override
	public void setTransactionsWarningsEnabled(boolean b) {
		clear();
		threadWarning.set(b);		
	}




	public String getSessionFactoryBeanId() {
		return sessionFactoryBeanId;
	}




	public void setSessionFactoryBeanId(String sessionFactoryBeanId) {
		this.sessionFactoryBeanId = sessionFactoryBeanId;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		this.applicationContext = applicationContext;
		
	}

	private SessionFactoryImplementor getSessionFactory() {
		return (SessionFactoryImplementor)applicationContext.getBean(getSessionFactoryBeanId());
	}


	@Override
	public void clear() {
		getThreadStatistics().clear();

	}

	@Override
	public EntityStatistics getEntityStatistics(String entityName) {
		return getThreadStatistics().getEntityStatistics(entityName);
	}

	@Override
	public CollectionStatistics getCollectionStatistics(String role) {
		return getThreadStatistics().getCollectionStatistics(role);
	}

	@Override
	public SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
		return getThreadStatistics().getSecondLevelCacheStatistics(regionName);
	}

	@Override
	public NaturalIdCacheStatistics getNaturalIdCacheStatistics(String regionName) {
		return getThreadStatistics().getNaturalIdCacheStatistics(regionName);
	}

	@Override
	public QueryStatistics getQueryStatistics(String queryString) {
		return getThreadStatistics().getQueryStatistics(queryString);
	}

	@Override
	public long getEntityDeleteCount() {
		return getThreadStatistics().getEntityDeleteCount();
	}

	@Override
	public long getEntityInsertCount() {
		return getThreadStatistics().getEntityInsertCount();
	}

	@Override
	public long getEntityLoadCount() {
		return getThreadStatistics().getEntityLoadCount();
	}

	@Override
	public long getEntityFetchCount() {
		return getThreadStatistics().getEntityFetchCount();
	}

	@Override
	public long getEntityUpdateCount() {
		return getThreadStatistics().getEntityUpdateCount();
	}

	@Override
	public long getQueryExecutionCount() {
		return getThreadStatistics().getQueryExecutionCount();
	}

	@Override
	public long getQueryExecutionMaxTime() {
		return getThreadStatistics().getQueryExecutionCount();
	}

	@Override
	public String getQueryExecutionMaxTimeQueryString() {
		return getThreadStatistics().getQueryExecutionMaxTimeQueryString();
	}

	@Override
	public long getQueryCacheHitCount() {
		return getThreadStatistics().getQueryCacheHitCount();
	}

	@Override
	public long getQueryCacheMissCount() {
		return getThreadStatistics().getQueryCacheMissCount();
	}

	@Override
	public long getQueryCachePutCount() {
		return getThreadStatistics().getQueryCachePutCount();
	}

	@Override
	public long getNaturalIdQueryExecutionCount() {
		return getThreadStatistics().getNaturalIdQueryExecutionCount();
	}

	@Override
	public long getNaturalIdQueryExecutionMaxTime() {
		return getThreadStatistics().getNaturalIdQueryExecutionMaxTime();
	}

	@Override
	public String getNaturalIdQueryExecutionMaxTimeRegion() {
		return getThreadStatistics().getNaturalIdQueryExecutionMaxTimeRegion();
	}

	@Override
	public long getNaturalIdCacheHitCount() {
		return getThreadStatistics().getNaturalIdCacheHitCount();
	}

	@Override
	public long getNaturalIdCacheMissCount() {
		return getThreadStatistics().getNaturalIdCacheMissCount();
	}

	@Override
	public long getNaturalIdCachePutCount() {
		return getThreadStatistics().getNaturalIdCachePutCount();
	}

	@Override
	public long getUpdateTimestampsCacheHitCount() {
		return getThreadStatistics().getUpdateTimestampsCacheHitCount();
	}

	@Override
	public long getUpdateTimestampsCacheMissCount() {
		return getThreadStatistics().getUpdateTimestampsCacheMissCount();
	}

	@Override
	public long getUpdateTimestampsCachePutCount() {
		return getThreadStatistics().getUpdateTimestampsCachePutCount();
	}

	@Override
	public long getFlushCount() {
		return getThreadStatistics().getFlushCount();
	}

	@Override
	public long getConnectCount() {
		return getThreadStatistics().getConnectCount();
	}

	@Override
	public long getSecondLevelCacheHitCount() {
		return getThreadStatistics().getSecondLevelCacheHitCount();
	}

	@Override
	public long getSecondLevelCacheMissCount() {
		return getThreadStatistics().getSecondLevelCacheMissCount();
	}

	@Override
	public long getSecondLevelCachePutCount() {
		return getThreadStatistics().getSecondLevelCachePutCount();
	}

	@Override
	public long getSessionCloseCount() {
		return getThreadStatistics().getSessionCloseCount();
	}

	@Override
	public long getSessionOpenCount() {
		return getThreadStatistics().getSessionCloseCount();
	}

	@Override
	public long getCollectionLoadCount() {
		return getThreadStatistics().getCollectionLoadCount();
	}

	@Override
	public long getCollectionFetchCount() {
		return getThreadStatistics().getCollectionFetchCount();
	}

	@Override
	public long getCollectionUpdateCount() {
		return getThreadStatistics().getCollectionUpdateCount();
	}

	@Override
	public long getCollectionRemoveCount() {
		return getThreadStatistics().getCollectionRemoveCount();
	}

	@Override
	public long getCollectionRecreateCount() {
		return getThreadStatistics().getCollectionRecreateCount();
	}

	@Override
	public long getStartTime() {
		return getThreadStatistics().getStartTime();
	}

	@Override
	public void logSummary() {
		getThreadStatistics().logSummary();

	}

	@Override
	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	@Override
	public void setStatisticsEnabled(boolean b) {
		this.statisticsEnabled = b;

	}

	@Override
	public String[] getQueries() {
		return getThreadStatistics().getQueries();
	}

	@Override
	public String[] getEntityNames() {
		return getThreadStatistics().getEntityNames();
	}

	@Override
	public String[] getCollectionRoleNames() {
		return getThreadStatistics().getCollectionRoleNames();
	}

	@Override
	public String[] getSecondLevelCacheRegionNames() {
		return getThreadStatistics().getSecondLevelCacheRegionNames();
	}

	@Override
	public long getSuccessfulTransactionCount() {
		return getThreadStatistics().getSuccessfulTransactionCount();
	}

	@Override
	public long getTransactionCount() {
		return getThreadStatistics().getTransactionCount();
	}

	@Override
	public long getPrepareStatementCount() {
		return getThreadStatistics().getPrepareStatementCount();
	}

	@Override
	public long getCloseStatementCount() {
		return getThreadStatistics().getCloseStatementCount();
	}

	@Override
	public long getOptimisticFailureCount() {
		return getThreadStatistics().getOptimisticFailureCount();
	}

	@Override
	public void openSession() {
		getThreadStatistics().openSession();

	}

	@Override
	public void closeSession() {
		getThreadStatistics().closeSession();

	}

	@Override
	public void flush() {
		getThreadStatistics().flush();
	}

	@Override
	public void connect() {
		getThreadStatistics().connect();
	}

	@Override
	public void prepareStatement() {
		getThreadStatistics().prepareStatement();
	}

	@Override
	public void closeStatement() {
		getThreadStatistics().closeStatement();
	}

	@Override
	public void endTransaction(boolean success) {
		
		
		getThreadStatistics().endTransaction(success);
		if (warnOnExtraTransaction && threadWarning.get().booleanValue() && (getThreadStatistics().getTransactionCount() > 1) ) {
			logger.warn(getThreadStatistics().getTransactionCount() + " transactions this thread!");
		}
	}

	@Override
	public void loadEntity(String entityName) {
		getThreadStatistics().loadEntity(entityName);
	}

	@Override
	public void fetchEntity(String entityName) {
		getThreadStatistics().fetchEntity(entityName);
	}

	@Override
	public void updateEntity(String entityName) {
		getThreadStatistics().updateEntity(entityName);
	}

	@Override
	public void insertEntity(String entityName) {
		getThreadStatistics().insertEntity(entityName);
	}

	@Override
	public void deleteEntity(String entityName) {
		getThreadStatistics().deleteEntity(entityName);
	}

	@Override
	public void optimisticFailure(String entityName) {
		getThreadStatistics().optimisticFailure(entityName);
	}

	@Override
	public void loadCollection(String role) {
		getThreadStatistics().loadCollection(role);
	}

	@Override
	public void fetchCollection(String role) {
		getThreadStatistics().fetchCollection(role);
	}

	@Override
	public void updateCollection(String role) {
		getThreadStatistics().updateCollection(role);
	}

	@Override
	public void recreateCollection(String role) {
		getThreadStatistics().recreateCollection(role);
	}

	@Override
	public void removeCollection(String role) {
		getThreadStatistics().removeCollection(role);
	}

	@Override
	public void secondLevelCachePut(String regionName) {
		getThreadStatistics().secondLevelCachePut(regionName);
	}

	@Override
	public void secondLevelCacheHit(String regionName) {
		getThreadStatistics().secondLevelCacheHit(regionName);
	}

	@Override
	public void secondLevelCacheMiss(String regionName) {
		getThreadStatistics().secondLevelCacheMiss(regionName);
	}

	@Override
	public void naturalIdCachePut(String regionName) {
		getThreadStatistics().naturalIdCachePut(regionName);
	}

	@Override
	public void naturalIdCacheHit(String regionName) {
		getThreadStatistics().naturalIdCacheHit(regionName);
	}

	@Override
	public void naturalIdCacheMiss(String regionName) {
		getThreadStatistics().naturalIdCacheMiss(regionName);
	}

	@Override
	public void naturalIdQueryExecuted(String regionName, long time) {
		getThreadStatistics().naturalIdQueryExecuted(regionName, time);
	}

	@Override
	public void queryCachePut(String hql, String regionName) {
		getThreadStatistics().queryCachePut(hql, regionName);
	}

	@Override
	public void queryCacheHit(String hql, String regionName) {
		getThreadStatistics().queryCacheHit(hql, regionName);
	}

	@Override
	public void queryCacheMiss(String hql, String regionName) {
		getThreadStatistics().queryCacheMiss(hql, regionName);

	}

	@Override
	public void queryExecuted(String hql, int rows, long time) {
		getThreadStatistics().queryExecuted(hql, rows, time);

	}

	@Override
	public void updateTimestampsCacheHit() {
		getThreadStatistics().updateTimestampsCacheHit();
	}

	@Override
	public void updateTimestampsCacheMiss() {
		getThreadStatistics().updateTimestampsCacheMiss();
	}

	@Override
	public void updateTimestampsCachePut() {
		getThreadStatistics().updateTimestampsCachePut();
	}


	public boolean isWarnOnExtraTransaction() {
		return warnOnExtraTransaction;
	}


	public void setWarnOnExtraTransaction(boolean warnOnExtraTransaction) {
		this.warnOnExtraTransaction = warnOnExtraTransaction;
	}
	
	
	
	

}
