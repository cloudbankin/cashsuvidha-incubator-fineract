/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.mail.service;


import java.io.IOException;
import javax.mail.*;
import java.net.Proxy;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.mifosplatform.infrastructure.mail.vo.MailDataVO;
import org.mifosplatform.infrastructure.utils.CommonMethodsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.Request;
import com.amazonaws.Response;





@Service
public class MailProcessingServiceImpl implements MailProcessingService {

    private final static Logger logger = LoggerFactory.getLogger(MailProcessingServiceImpl.class);

    
    @Autowired
	private Environment propertyEnv;
    

   
    
    @SuppressWarnings("rawtypes")
	@Override
	public boolean sendMailFile(MailDataVO data) {
    	

		boolean isSuccess = false;
		String templateName = "";

		try {
			
			if (CommonMethodsUtil.isBlank(data.getFileName())) {
				logger.error("Error in sending Mail : File is blank");
				return isSuccess;
			}
			
			boolean isActive = Boolean.valueOf(propertyEnv.getProperty("mail."+data.getMailTemplateType()+".isActive"));
			if (!isActive) {
				logger.error("Error in sending Mail : Mail trigger is inactive");
				return isSuccess;
			}

			
			
		
			    String to = data.getToMail();
			    if(to==null)
			    	logger.error("Error in sending Mail");

			    // Sender's email ID needs to be mentioned
			    final  String from = propertyEnv.getProperty("mail.FromEmail");
			    final String password = propertyEnv.getProperty("mail.EmailPassword");
			    final String contentMessage=data.getmsgcontent();
			    // Assuming you are sending email from localhost
			    String host = propertyEnv.getProperty("mail.Host");

			    // Get system properties
			    Properties properties = System.getProperties();

			    // Setup mail server
			    properties.setProperty("mail.smtp.host", host);
			    properties.setProperty("mail.smtp.port",  propertyEnv.getProperty("mail.TLSPort")); //TLS Port
			    properties.setProperty("mail.smtp.auth",  propertyEnv.getProperty("mail.Authentication")); //enable authentication
			    properties.setProperty("mail.smtp.starttls.enable",  propertyEnv.getProperty("mail.STARTTLS"));
			    // Get the default Session object.
			    
			    Authenticator auth = new Authenticator() {
			    	
			        //override the getPasswordAuthentication method
			        protected PasswordAuthentication getPasswordAuthentication() {
			            return new PasswordAuthentication(from, password);
			        }

					
			    };
			    Session session = Session.getInstance(properties, auth);
			    
//			    Session session = Session.getDefaultInstance(properties);

			  
			       // Create a default MimeMessage object.
			       MimeMessage message = new MimeMessage(session);

			       // Set From: header field of the header.
			       message.setFrom(new InternetAddress(from));

			       // Set To: header field of the header.
			       message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));

			       // Set Subject: header field
			       message.setSubject(data.getTemplateConentType());
			       
			       // Create the message part 
			      // BodyPart messageBodyPart = new MimeBodyPart();
			       MimeBodyPart messageBodyPart = new MimeBodyPart();
			       // Fill the message
			       messageBodyPart.setText(contentMessage);
			       
			       // Create a multipar message
			       Multipart multipart = new MimeMultipart();

			       // Set text message part
			       multipart.addBodyPart(messageBodyPart);

			       // Part two is attachment
			       messageBodyPart = new MimeBodyPart();
			       String filename = data.getFileName();
			       DataSource source = new FileDataSource(filename);
			       messageBodyPart.setDataHandler(new DataHandler(source));
			       messageBodyPart.setFileName(filename);
			       multipart.addBodyPart(messageBodyPart);
			      // messageBodyPart.attachFile(filename, "application/octet-stream", "base64");
			       // Send the complete message parts   
			       
			       message.setContent(multipart );

			       // Send message
			       Transport.send(message);
			       System.out.println("Sent message successfully....");
			   
			       isSuccess = true;
			    
				
				
			   
			}
			
			
			
		 catch (Exception ex) {
			isSuccess = false;
			logger.error(propertyEnv.getProperty("sms.sent.exception") + " - "
							+ ex.getMessage());
			return isSuccess;
		}
		return isSuccess;
	}

   
    
    
    
    @SuppressWarnings("rawtypes")
   	@Override
   	public boolean sendMail(MailDataVO data) {
       	

   		boolean isSuccess = false;
   		String templateName = "";

   		try {
   			
   			if (CommonMethodsUtil.isBlank(data.getMailTemplateType())) {
   				logger.error("Error in sending Mail : Mail template type is blank");
   				return isSuccess;
   			}
   			
   			boolean isActive = Boolean.valueOf(propertyEnv.getProperty("mail."+data.getMailTemplateType()+".isActive"));
   			if (!isActive) {
   				logger.error("Error in sending Mail : Mail trigger is inactive");
   				return isSuccess;
   			}

   			String[] templateArray = data.getTemplateParameterString().split(",");
   			String contentMessage =  propertyEnv.getProperty("mail."+data.getTemplateConentType());
   			for(int i=0;i<templateArray.length;i++){
   				String replace=String.valueOf(i);
   				contentMessage=contentMessage.replace('{'+replace+'}', templateArray[i]);
   			}
   			
   			
   		
   			
   			final  String fromEmail = propertyEnv.getProperty("mail.FromEmail");
		    final String password = propertyEnv.getProperty("mail.EmailPassword");
		 // Assuming you are sending email from localhost
		    String host = propertyEnv.getProperty("mail.Host");

   			
		 // Get system properties
		    Properties properties = System.getProperties();
   	      
   	    System.out.println("TLSEmail Start");
   	// Setup mail server
	    properties.setProperty("mail.smtp.host", host);
	    properties.setProperty("mail.smtp.port",  propertyEnv.getProperty("mail.TLSPort")); //TLS Port
	    properties.setProperty("mail.smtp.auth",  propertyEnv.getProperty("mail.Authentication")); //enable authentication
	    properties.setProperty("mail.smtp.starttls.enable",  propertyEnv.getProperty("mail.STARTTLS"));
   	        //create Authenticator object to pass in Session.getInstance argument
   	    Authenticator auth = new Authenticator() {
   	        //override the getPasswordAuthentication method
   	        protected PasswordAuthentication getPasswordAuthentication() {
   	            return new PasswordAuthentication(fromEmail, password);
   	        }
   	    };
   	    Session session = Session.getInstance(properties, auth);

   	    MimeMessage message = new MimeMessage(session);
   	    message.setFrom(new InternetAddress(fromEmail));
   	 String toMail = data.getToMail();
   	    //multiple recipient
   	   // for( String to :toMail){
   	    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toMail));
   	   // }
   	    System.out.println("Mail Check 2");

   	    /*message.setSubject(prop.setProperty("EmailSubjectText"));//subj
   	*/   
   	    message.setSubject(propertyEnv.getProperty("mail."+data.getMailTemplateType()));//subj
   	    message.setText(contentMessage); //text msg

   	    System.out.println("Mail Check 3");
   	    Transport.send(message);
   	     isSuccess = true;
   	    System.out.println("Mail Sent");   			   
   				
   			    
   				
   				
   			   
   			}
   			
   			
   			
   		 catch (Exception ex) {
   			isSuccess = false;
   			logger.error(propertyEnv.getProperty("sms.sent.exception") + " - "
   							+ ex.getMessage());
   			return isSuccess;
   		}
   		return isSuccess;
   	}

    	

}