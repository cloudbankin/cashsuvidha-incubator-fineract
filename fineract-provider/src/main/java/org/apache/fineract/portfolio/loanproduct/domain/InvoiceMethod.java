/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanproduct.domain;


public enum InvoiceMethod {
    INVOICE_FINANCING(0, "invoiceType.invoice.financing"), OTHERS(1, "invoiceType.others"), INVALID(2, "invoiceType.invalid");

    private final Integer value;
    private final String code;

    private InvoiceMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InvoiceMethod fromInt(final Integer selectedMethod) {

    	InvoiceMethod repaymentMethod = null;
        switch (selectedMethod) {
            case 0:
                repaymentMethod = InvoiceMethod.INVOICE_FINANCING;
            break;
            case 1:
                repaymentMethod = InvoiceMethod.OTHERS;
            break;
            default:
                repaymentMethod = InvoiceMethod.INVALID;
            break;
        }
        return repaymentMethod;
    }

    public boolean isInvoiceFinancing() {
        return this.value.equals(InvoiceMethod.INVOICE_FINANCING.getValue());
    }
}