package org.mifosplatform.infrastructure.mail.service;

import org.mifosplatform.infrastructure.mail.vo.MailDataVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVONimbus;

public interface MailProcessingService {
	

    boolean sendMailFile(MailDataVO data);
    boolean sendMail(MailDataVO data);

}