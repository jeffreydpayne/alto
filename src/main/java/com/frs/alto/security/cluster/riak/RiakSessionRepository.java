package com.frs.alto.security.cluster.riak;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.frs.alto.dao.riak.RiakDaoSupport;
import com.frs.alto.security.cluster.SessionMetaData;
import com.frs.alto.security.cluster.SessionRepository;

@Component("riak-session-repository")
public class RiakSessionRepository extends RiakDaoSupport<SessionMetaData> implements SessionRepository {

	@Override
	public Class<SessionMetaData> getDomainClass() {
		return SessionMetaData.class;
	}

	@Override
	public Collection<SessionMetaData> findByIds(Collection<String> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
