package org.mifosplatform.infrastructure.sms.service;

import org.mifosplatform.infrastructure.sms.vo.SMSDataVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVONimbus;

public interface SmsProcessingService {
	
   //Eduwize
    boolean sendSMS(SMSDataVO data);
  //Nimbus
    boolean sendSMSNimbus(SMSDataVONimbus data);

}