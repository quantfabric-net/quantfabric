/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quantfabric.util.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.util.PropertiesViewer;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;

public class EmailUtils
{
	private static final Map<Properties, Session> sessionPool = Collections.synchronizedMap(new HashMap<Properties, Session>());
	
	public static boolean sendEmail(Session session, MimeMessage message) throws MessagingException
	{
		Transport.send(message);
		return true;
	}
	
	public static boolean sendEmail(Session session, 
			String from, String to, String subject, String text) throws MessagingException
	{
		return sendEmail(session, createMessage(session, from, to, subject, text));
	}
	
	public static boolean sendEmail(Properties sessionProperties, 
			String from, String to, String subject, String text) throws MessagingException, NotSpecifiedProperty
	{
		return sendEmail(createSession(sessionProperties), from, to, subject, text);
	}
	
	public static boolean sendEmail(String pathToProperites, 
			String from, String to, String subject, String text) throws MessagingException, NotSpecifiedProperty, IOException
	{
		Properties sessionProperties = new Properties();
		File propertiesFile = new File(
				new File(pathToProperites).exists() ? pathToProperites : QuantfabricRuntime.getAbsolutePath(pathToProperites));
		try(FileInputStream fInput = new FileInputStream(propertiesFile)) {
			sessionProperties.load(fInput);
		}
		return sendEmail(createSession(sessionProperties), from, to, subject, text);
	}
	
	public static boolean sendEmail(String pathToProperites, 
			 String to, String subject, String text) throws MessagingException, NotSpecifiedProperty, IOException
	{
		return sendEmail(pathToProperites, null, to, subject, text);
	}
	
	private static Session createSession(Properties sessionProperties) throws NotSpecifiedProperty
	{		
		Session session = getPooledSeession(sessionProperties);
		
		if (session == null)
		{		
			final String Username = PropertiesViewer.getProperty(sessionProperties, "mail.smtp.auth.username");
			final String Password = PropertiesViewer.getProperty(sessionProperties, "mail.smtp.auth.password");
			
			session = Session.getInstance(sessionProperties,
					  new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(Username, Password);
						}
					  });
			
			addPooledSeesion(sessionProperties, session);
		}
		
		return session; 
	}
	
	private static void addPooledSeesion(Properties sessionProperties,
			Session session)
	{
		sessionPool.put(sessionProperties, session);		
	}

	private static Session getPooledSeession(Properties sessionProperties)
	{
		return sessionPool.get(sessionProperties);
	}

	private static MimeMessage createMessage(Session session, String from, String to, String subject, String text) throws MessagingException
	{
		 MimeMessage message = new MimeMessage(session);
				 
		 if (from != null && from.equals(""))
         message.setFrom(new InternetAddress(from));
         message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
         message.setSubject(subject);
         message.setText(text);
         
         return message;
	}

}
