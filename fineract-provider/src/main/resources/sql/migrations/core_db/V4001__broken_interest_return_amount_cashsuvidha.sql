
--
-- Table Columns for Broken Interest
--

ALTER TABLE m_loan ADD total_interest_broken_due_at_disbursement_derived DECIMAL(19,6) NULL DEFAULT 0.000000 AFTER total_charges_due_at_disbursement_derived;

ALTER TABLE m_loan_charge ADD interest_broken_charge DECIMAL(19,6)  NULL DEFAULT 0.000000 AFTER amount;  

ALTER TABLE m_loan_disbursement_detail ADD interest_broken_charge DECIMAL(19,6)  NULL DEFAULT 0.000000;

--
-- Table Columns for Amount Return
--

ALTER TABLE m_loan ADD amount_return DECIMAL(19,6)  NULL DEFAULT 0.000000  AFTER interest_calculated_from_date; 
ALTER TABLE m_product_loan ADD invoice_method_enum SMALLINT(5)  NOT NULL AFTER interest_method_enum;
ALTER TABLE m_loan ADD invoice_method_enum SMALLINT(5) NOT  NULL AFTER interest_method_enum;
ALTER TABLE m_product_loan_configurable_attributes ADD invoice_method_enum TINYINT(4) NOT NULL DEFAULT '1' AFTER interest_method_enum;