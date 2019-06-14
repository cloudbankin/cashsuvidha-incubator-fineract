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
package org.mifosplatform.infrastructure.mail.vo;

import java.io.Serializable;

public class MailDataVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -387461629641602672L;
	private final String mailTemplateType;
	private final String toMail;
	private final String templateParameterString;
	private final String templateConentType;
	private final String fileName;
	private final String msgcontent;

	public MailDataVO(final String mailTemplateType, final String toMail, final String templateParameterString,final String templateConentType,final String fileName,final String msgcontent) {
		this.mailTemplateType = mailTemplateType;
		this.toMail = toMail;
		this.templateParameterString = templateParameterString;
		this.templateConentType = templateConentType;
		this.fileName = fileName;
		this.msgcontent = msgcontent;
	}

	public String getMailTemplateType() {
		return mailTemplateType;
	}

	public String getToMail() {
		return toMail;
	}

	public String getTemplateParameterString() {
		return templateParameterString;
	}

	public String getTemplateConentType() {
		return templateConentType;
	}

	public String getFileName(){
	     return fileName;       
	}
	public String getmsgcontent(){
	     return msgcontent;       
	}
	}


