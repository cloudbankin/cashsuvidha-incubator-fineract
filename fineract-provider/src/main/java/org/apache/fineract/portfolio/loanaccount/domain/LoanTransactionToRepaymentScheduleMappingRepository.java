package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanTransactionToRepaymentScheduleMappingRepository extends JpaRepository<LoanTransactionToRepaymentScheduleMapping, Loan> , JpaSpecificationExecutor<LoanTransactionToRepaymentScheduleMapping>{
	
	//@Query("select loanTransactionToRepaymentScheduleMapping from LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping where loanTransactionToRepaymentScheduleMapping.loanTransaction.id in :loanTransaction")
	//Collection<LoanTransactionToRepaymentScheduleMapping> findByLoanTransactionToRepaymentScheduleMapping(@Param("loanTransaction") Long  loanTransactionId);
	
	/*@Query("select * from m_loan_transaction_repayment_schedule_mapping  where loan_transaction_id in loanTransaction ")
	Collection<LoanTransactionToRepaymentScheduleMapping> findByLoanTransactionToRepaymentScheduleMapping(@Param("loanTransaction") Long  loanTransactionId);*/
}
	