package com.frs.alto.hibernate4;

import java.sql.Types;
import org.hibernate.dialect.MySQL5Dialect;

public class UUIDAwareMySqlDialect extends MySQL5Dialect {
	
	@Override
	protected void registerVarcharTypes() {
		super.registerVarcharTypes();
		for (int i = 2 ; i < 36 ; i++) {
			registerColumnType( Types.VARCHAR, i, "varchar(" + i + ")");
		}
		registerColumnType( Types.VARCHAR, 36, "char(36)");
	}


}
