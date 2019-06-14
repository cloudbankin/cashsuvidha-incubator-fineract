
--
-- Table Columns for Tax Module
--

ALTER TABLE m_loan_charge ADD tax_amount DECIMAL(19,6) NULL DEFAULT 0.000000 AFTER is_active;

ALTER TABLE m_loan_charge ADD tax_percentage DECIMAL(19,6) NULL DEFAULT 0.000000 AFTER tax_amount;

ALTER TABLE m_loan_installment_charge ADD tax_amount DECIMAL(19,6) NULL DEFAULT 0.000000;

ALTER TABLE m_loan_installment_charge ADD tax_percentage DECIMAL(19,6)  NULL DEFAULT 0.000000;
