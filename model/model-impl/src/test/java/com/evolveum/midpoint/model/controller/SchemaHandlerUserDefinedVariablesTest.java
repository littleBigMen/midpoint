/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.model.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.File;

import javax.xml.bind.JAXBElement;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.evolveum.midpoint.common.result.OperationResult;
import com.evolveum.midpoint.model.test.util.ModelTUtil;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.util.JAXBUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_1.AccountShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceObjectShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.UserType;

/**
 * 
 * @author sleepwalker
 * @author lazyman
 * 
 */
@ContextConfiguration(locations = { "classpath:application-context-model.xml",
		"classpath:application-context-model-unit-test.xml", 
		"classpath:application-context-configuration-test-no-repo.xml",
		"classpath:application-context-task.xml" })
public class SchemaHandlerUserDefinedVariablesTest extends AbstractTestNGSpringContextTests {

	private static final Trace LOGGER = TraceManager.getTrace(SchemaHandlerUserDefinedVariablesTest.class);
	@Autowired
	private SchemaHandler schemaHandler;
	@Autowired
	private RepositoryService repositoryService;

	@BeforeMethod
	public void before() {
		Mockito.reset(repositoryService);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testApplyOutboundSchemaHandlingWithUserDefinedVariablesOnAccount() throws Exception {
		ObjectType object = ((JAXBElement<ObjectType>) JAXBUtil.unmarshal(new File(
				"src/test/resources/generic-object-my-config.xml"))).getValue();
		when(
				repositoryService.getObject(any(Class.class), eq(object.getOid()),
						any(PropertyReferenceListType.class), any(OperationResult.class))).thenReturn(object);

		AccountShadowType account = ((JAXBElement<AccountShadowType>) JAXBUtil.unmarshal(new File(
				"src/test/resources/account-resource-schema-handling-custom-variables.xml"))).getValue();

		UserType user = ((JAXBElement<UserType>) JAXBUtil.unmarshal(new File(
				"src/test/resources/user-new.xml"))).getValue();

		OperationResult result = new OperationResult(
				"testApplyOutboundSchemaHandlingWithUserDefinedVariablesOnAccount");
		ObjectModificationType changes = schemaHandler.processOutboundHandling(user, account, result);
		LOGGER.info(result.dump());

		// TODO: test changes object
		ResourceObjectShadowType appliedAccountShadow = ModelTUtil.patchXml(changes, account,
				AccountShadowType.class);

		assertEquals(2, appliedAccountShadow.getAttributes().getAny().size());
		assertEquals("l", ((Element)appliedAccountShadow.getAttributes().getAny().get(1)).getLocalName());
		assertEquals("Here", ((Element)appliedAccountShadow.getAttributes().getAny().get(1)).getTextContent());
	}
}
