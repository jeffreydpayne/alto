package com.frs.alto.hibernate4;

import org.hibernate.stat.spi.StatisticsImplementor;

public interface WarnableStatisticsImplementor extends StatisticsImplementor {
	
	public void setTransactionsWarningsEnabled(boolean b);

}
