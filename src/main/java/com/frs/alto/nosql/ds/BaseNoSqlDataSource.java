package com.frs.alto.nosql.ds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;

public abstract class BaseNoSqlDataSource implements NoSqlDataSource {
	
	private IdentifierGenerator identifierGenerator = new LocalUUIDGenerator();
	private boolean autoCreateEnabled = false;
	
	private static String ISO_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
	@Override 
	public String nextId(BaseDomainObject domain) {
		return identifierGenerator.generateStringIdentifier(domain);
	}

	@Override
	public boolean isAutoCreateEnabled() {
		return autoCreateEnabled;
	}

	public void setAutoCreateEnabled(boolean autoCreateEnabled) {
		this.autoCreateEnabled = autoCreateEnabled;
	}

	public IdentifierGenerator getIdentifierGenerator() {
		return identifierGenerator;
	}

	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}
	
	private SimpleDateFormat getIsoDateFormatter() {
		
		SimpleDateFormat fmt = new SimpleDateFormat(ISO_TS_FORMAT);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt;
		
	}
	
	public Date parseTimeStamp(String value) {
		
		if (value == null) {
			return null;
		}
		
		try {
			return getIsoDateFormatter().parse(value);
		}
		catch (ParseException ex) {
			return null;
		}
		catch (NumberFormatException ex) {
			return null;
		}


	}

	public String formatTimeStamp(Date date) {
		if (date != null) {
			return getIsoDateFormatter().format(date);
		}
		else {
			return null;
		}
	}

}
