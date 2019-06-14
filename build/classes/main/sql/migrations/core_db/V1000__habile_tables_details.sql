-- This is table use for Loan Repayment

CREATE TABLE IF NOT EXISTS `hab_loan_repayment_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `transaction_id` bigint(20) NOT NULL,
	`loan_repayment_id` bigint(20) NOT NULL,
  `is_payment_charges` TINYINT(1) NOT NULL DEFAULT '0',
   `is_repayment_charges` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_hab_loan_repayment_schedule` (`transaction_id`),
  CONSTRAINT `FK_hab_loan_repayment_schedule` FOREIGN KEY (`transaction_id`) REFERENCES `m_loan_transaction` (`id`),
  KEY `FK_loan_repayment_schedule_ID` (`loan_repayment_id`),
  CONSTRAINT `FK_loan_repayment_schedule_ID` FOREIGN KEY (`loan_repayment_id`) REFERENCES `m_loan_repayment_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
