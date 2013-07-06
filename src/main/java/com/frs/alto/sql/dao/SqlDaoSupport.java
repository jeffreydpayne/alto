package com.frs.alto.sql.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.frs.alto.cache.CachingDaoSupport;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;

public abstract class SqlDaoSupport<T extends BaseDomainObject> extends CachingDaoSupport<T> implements SqlDao<T>, RowMapper<T> {
	
	private JdbcTemplate jdbcTemplate = null;
	private IdentifierGenerator identifierGenerator = null;
	
	
	
	
	@Override
	public void delete(T anObject) {
		delete(anObject.getObjectIdentifier());
	}

	@Override
	public T findById(String id) {
		
		if (id == null) {
			return null;
		}
		
		T result = readFromCache(id);
		if (result != null) {
			return result;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ");
		sql.append(getTableName());
		sql.append(" where ");
		sql.append(getPrimaryKeyColumn());
		sql.append(" = ?");
		List<T> results = jdbcTemplate.query(sql.toString(), new Object[]{id}, this);
		if (results.size() > 1) {
			throw new IllegalStateException("Too many results for findById()");
		}
		else if (results.size() == 1) {
			return results.get(0);
		}
		else {
			return null;
		}
	}

	@Override
	public Collection<String> findAllIds() {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(getPrimaryKeyColumn());
		sql.append(" from ");
		sql.append(getTableName());
		
		final List<String> results = new ArrayList<String>();
		
		jdbcTemplate.query(sql.toString(), new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				results.add(rs.getString(1));
				
			}
		});
		return results;
	}

	@Override
	public Collection<T> findAll() {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ");
		sql.append(getTableName());
		return jdbcTemplate.query(sql.toString(), this);
	}

	@Override
	public void delete(String hashKey) {
		
		
		
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ");
		sql.append(getTableName());
		sql.append(" where ");
		sql.append(getPrimaryKeyColumn());
		sql.append(" = ?");
		jdbcTemplate.update(sql.toString(), new Object[]{hashKey});
		
		removeFromCache(hashKey);
	}

	@Override
	public void delete(Collection<String> hashKeys) {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ");
		sql.append(getTableName());
		sql.append(" where ");
		sql.append(getPrimaryKeyColumn());
		sql.append(" = ?");
		
		List<Object[]> args = new ArrayList<Object[]>();
		for (String id : hashKeys) {
			removeFromCache(id);
			args.add(new Object[]{id});
		}
		
		jdbcTemplate.batchUpdate(sql.toString(), args);
		
	}

	@Override
	public String save(T domain) {
		
		generateVersionHash(domain);
		
		if (domain.getObjectIdentifier() == null) {
			insert(domain);
		}
		else {
			update(domain);
		}
		
		writeToCache(domain);
		
		return domain.getObjectIdentifier();
	}
	
	protected void insert(final List<T> domains) {
		
		T trialInstance = domains.get(0);
		
		jdbcTemplate.batchUpdate(generateInsertStatement(trialInstance), new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				T domain = domains.get(i);
				domain.setObjectIdentifier(nextId(domain));
				populateUpdateStatement(domain, ps);
			}
			
			@Override
			public int getBatchSize() {
				return domains.size();
			}
		});
		
	}
	
	protected void insert(final T domain) {
		
		domain.setObjectIdentifier(nextId(domain));
		jdbcTemplate.update(generateInsertStatement(domain), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				populateUpdateStatement(domain, ps);
			}
		});
		
	}
	
	protected abstract void populateUpdateStatement(T domain, PreparedStatement ps);
	
	
	protected String generateInsertStatement(T domain) {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(getTableName());
		sql.append(" (");
		String[] columns = getOrderedColumns();
		for (String col : columns) {
			sql.append(col);
			sql.append(",");
		}
		sql.append(getPrimaryKeyColumn());
		sql.append(") VALUES (?");
		for (int i = 0; i < columns.length; i++) {
			sql.append(",?");
		}
		return sql.toString();
		
	}
	
	protected void update(final List<T> domains) {
		
		T trialInstance = domains.get(0);
		
		jdbcTemplate.batchUpdate(generateUpdateStatement(trialInstance), new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				
				populateUpdateStatement(domains.get(i), ps);
			}
			
			@Override
			public int getBatchSize() {
				return domains.size();
			}
		});
		
	}
	
	protected void update(final T domain) {
		jdbcTemplate.update(generateUpdateStatement(domain), new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				populateUpdateStatement(domain, ps);
			}
		});
	}
	
	protected String generateUpdateStatement(T domain) {
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(getTableName());
		sql.append(" set ");
		boolean firstColumn = true;
		String[] columns = getOrderedColumns();
		for (String col : columns) {
			if (!firstColumn) {
				sql.append(",");
			}
			sql.append(col);
			sql.append(" = ?");
			firstColumn = false;
		}
		sql.append(" where ");
		sql.append(getPrimaryKeyColumn());
		sql.append(" = ?")
;		return sql.toString();
		
	}
	

	@Override
	public Collection<String> save(Collection<T> domains) {
		
		List<T> insertQueue = new ArrayList<T>();
		List<T> updateQueue = new ArrayList<T>();
		
		for (T domain : domains) {
			generateVersionHash(domain);
			if (domain.getObjectIdentifier() == null) {
				insertQueue.add(domain);
			}
			else {
				updateQueue.add(domain);
			}
		}
		
		if (insertQueue.size() > 0) {
			if (insertQueue.size() > 1) {
				insert(insertQueue);
			}
			else {
				insert(insertQueue.get(0));
			}
			
		}
		if (updateQueue.size() > 0) {
			if (updateQueue.size() > 1){
				update(updateQueue);
			}
			else {
				update(updateQueue.get(0));
			}
			
		}
		
		Collection<String> results = new ArrayList<String>();
		for (T domain : domains) {
			results.add(domain.getObjectIdentifier());
			writeToCache(domain);
		}
		
		return results;
	}

	@Override
	public String nextId(BaseDomainObject domain) {
		return identifierGenerator.generateStringIdentifier(domain);
	}


	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}

	public void setDataSource(DataSource ds) {
		jdbcTemplate = new JdbcTemplate(ds);
	}
		
	protected abstract String getTableName();
	protected String getPrimaryKeyColumn() {
		return "id";
	}
	protected abstract String[] getOrderedColumns();

}
