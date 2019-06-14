package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;



@Entity
@Table(name = "hab_loan_repayment_schedule")
public class HabLoanRepaymentSchedule extends AbstractPersistableCustom<Long> {
	
	 @ManyToOne(optional = false)
	 @JoinColumn(name = "transaction_id", nullable = false)
	 private LoanTransaction loanTransaction;
	
	@Column(name = "is_payment_charges", nullable = false)
	private boolean PaymentCharges;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "loan_repayment_id", nullable = false)
	private LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment;
	
	@Column(name = "is_repayment_charges", nullable = false)
	private boolean isRepaymentCharges;
	
	public static HabLoanRepaymentSchedule createNew(LoanTransaction loanTransaction, final boolean isPaymentCharges, LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment, final boolean isRepaymentCharges) {
		
		//final String repaymentScheduleId = command.stringValueOfParameterNamed(LoanApiConstants.repaymentScheduleId);
		//final boolean isPaymentCharges = command.booleanPrimitiveValueOfParameterNamed(LoanApiConstants.ispaymentcharges);
		
		return new HabLoanRepaymentSchedule(loanTransaction, isPaymentCharges, loanRepaymentScheduleInstallment, isRepaymentCharges);
	}
	
	private HabLoanRepaymentSchedule(LoanTransaction loanTransaction, boolean isPaymentCharges, LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment, final boolean isRepaymentCharges) {
		
		  this.loanTransaction = loanTransaction;
		  this.PaymentCharges = isPaymentCharges;
		  this.loanRepaymentScheduleInstallment = loanRepaymentScheduleInstallment;
		  this.isRepaymentCharges = isRepaymentCharges;
	}

	public boolean isPaymentCharges() {
		return this.PaymentCharges;
	}

	public LoanRepaymentScheduleInstallment getLoanRepaymentScheduleInstallment() {
		return loanRepaymentScheduleInstallment;
	}

	public boolean isRepaymentCharges() {
		return isRepaymentCharges;
	}
	
	
	
}


