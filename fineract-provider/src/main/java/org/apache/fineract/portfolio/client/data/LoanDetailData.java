package org.apache.fineract.portfolio.client.data;

import java.math.BigDecimal;
import java.util.Date;

public class LoanDetailData {
	
	private final long openingarrears;
	
	private final long currentdemand;
	
	private final long arrearcollection;
	
	private final long advancecollection;
	
	private final long currentcollection;
	
	private final long closingarrears;

	

	public LoanDetailData(long openingarrears, long currentdemand, long arrearcollection, long advancecollection,
			long currentcollection, long closingarrears) {
		super();
		this.openingarrears = openingarrears;
		this.currentdemand = currentdemand;
		this.arrearcollection = arrearcollection;
		this.advancecollection = advancecollection;
		this.currentcollection = currentcollection;
		this.closingarrears = closingarrears;
		
	}

	public long getOpeningarrears() {
		return openingarrears;
	}

	public long getCurrentdemand() {
		return currentdemand;
	}

	public long getArrearcollection() {
		return arrearcollection;
	}

	public long getAdvancecollection() {
		return advancecollection;
	}

	public long getCurrentcollection() {
		return currentcollection;
	}

	public long getClosingarrears() {
		return closingarrears;
	}

	
	
}