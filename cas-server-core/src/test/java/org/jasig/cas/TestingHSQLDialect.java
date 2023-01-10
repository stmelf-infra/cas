package org.jasig.cas;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

public class TestingHSQLDialect extends HSQLDialect {

	public TestingHSQLDialect() {
		super();
		registerColumnType(Types.CLOB, "clob");
		registerColumnType(Types.BLOB, "blob");
	}
}
