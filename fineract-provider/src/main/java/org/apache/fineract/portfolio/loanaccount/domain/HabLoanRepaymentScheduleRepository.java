package org.apache.fineract.portfolio.loanaccount.domain;

import org.apache.fineract.portfolio.client.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HabLoanRepaymentScheduleRepository extends JpaRepository<HabLoanRepaymentSchedule, Long>, JpaSpecificationExecutor<HabLoanRepaymentSchedule> {

}
