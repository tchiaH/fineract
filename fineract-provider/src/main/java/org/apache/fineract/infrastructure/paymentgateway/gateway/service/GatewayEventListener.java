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
package org.apache.fineract.infrastructure.paymentgateway.gateway.service;

import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.infrastructure.paymentgateway.gateway.service.InboundMessageHandler;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.BytesMessage;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GatewayEventListener implements MessageListener {

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
    private final AppUserRepository appUserRepository;
    private final InboundMessageHandler messageHandler;

    @Autowired
    public GatewayEventListener(BasicAuthTenantDetailsService basicAuthTenantDetailsService,
                                InboundMessageHandler messageHandler,
                                     AppUserRepository appUserRepository) {
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
        this.appUserRepository = appUserRepository;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onMessage(Message message)  {
        if (message instanceof BytesMessage) {
            try {
                long length = ((BytesMessage) message).getBodyLength();
                byte[] content = new byte[(int)length];
                int bytesRead= ((BytesMessage)message).readBytes(content, (int)length);
                String s = new String(content);
                messageHandler.handlePayment(s);
            } catch(Exception E) {
                throw new InvalidJsonException();
            }

        } else {
            throw new IllegalArgumentException("Message Error");
        }
    }
}
