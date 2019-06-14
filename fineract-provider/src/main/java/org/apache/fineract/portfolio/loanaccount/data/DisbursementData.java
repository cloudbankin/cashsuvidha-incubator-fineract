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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing disbursement information.
 */
public class DisbursementData implements Comparable<DisbursementData> {

    @SuppressWarnings("unused")
    private final Long id;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final BigDecimal principal;
    @SuppressWarnings("unused")
    private final String loanChargeId;
    private final BigDecimal chargeAmount;
    private final BigDecimal waivedChargeAmount;
    private BigDecimal interestBrokenAmount;

    public DisbursementData(Long id, final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final BigDecimal principalDisbursed, final String loanChargeId, BigDecimal chargeAmount, BigDecimal waivedChargeAmount,BigDecimal interestBrokenAmount) {
        this.id = id;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.principal = principalDisbursed;
        this.loanChargeId = loanChargeId;
        this.chargeAmount = chargeAmount;
        this.waivedChargeAmount = waivedChargeAmount;
        this.interestBrokenAmount = interestBrokenAmount;
    }

    public LocalDate disbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public LocalDate getActualDisbursementDate() {
        LocalDate actualDisbursementDate = null;
        if (this.actualDisbursementDate != null) {
       	actualDisbursementDate = new LocalDate(this.actualDisbursementDate);
        }
      return actualDisbursementDate;
   }
    
    public BigDecimal amount() {
        return this.principal;
    }

    public BigDecimal getChargeAmount() {
        return this.chargeAmount;
    }

    public BigDecimal getInterestBrokenAmount() {
        return this.interestBrokenAmount;
     }

    public void updateinterestBrokenChargeTranche(BigDecimal interestBrokenChargeTranche) {
    	this.interestBrokenAmount = interestBrokenChargeTranche;
    }
    
    public boolean isDisbursed() {
        return this.actualDisbursementDate != null;
    }

    @Override
    public int compareTo(final DisbursementData obj) {
        if (obj == null) { return -1; }

        return obj.expectedDisbursementDate.compareTo(this.expectedDisbursementDate);
    }

    public boolean isDueForDisbursement(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive) {
        final LocalDate dueDate = disbursementDate();
        return occursOnDayFromAndUpToAndIncluding(fromNotInclusive, upToAndInclusive, dueDate);
    }

    private boolean occursOnDayFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive,
            final LocalDate target) {
        return target != null && target.isAfter(fromNotInclusive) && !target.isAfter(upToAndInclusive);
    }

    
    public BigDecimal getWaivedChargeAmount() {
        if(this.waivedChargeAmount == null){
            return BigDecimal.ZERO;
        }
        return this.waivedChargeAmount;
    }

}