/**
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
 * "Portions Copyrighted 2011 [name of copyright owner]"
 * 
 */
package com.evolveum.midpoint.test;

import com.evolveum.midpoint.common.QueryUtil;
import com.evolveum.midpoint.common.crypto.Protector;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.test.ldap.OpenDJController;
import com.evolveum.midpoint.test.util.DerbyController;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2.*;
import com.evolveum.prism.xml.ns._public.query_2.QueryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.evolveum.midpoint.test.IntegrationTestTools.display;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Radovan Semancik
 * 
 */
public abstract class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {

	private static final Trace LOGGER = TraceManager.getTrace(AbstractIntegrationTest.class);

	@Autowired(required = true)
	@Qualifier("cacheRepositoryService")
	protected RepositoryService repositoryService;
	protected static Set<Class> initializedClasses = new HashSet<Class>();

	@Autowired(required = true)
	protected TaskManager taskManager;
	
	@Autowired(required = true)
	protected Protector protector;
	
	@Autowired(required = true)
	protected PrismContext prismContext;

	// Controllers for embedded OpenDJ and Derby. The abstract test will configure it, but
	// it will not start
	// only tests that need OpenDJ or derby should start it
	protected static OpenDJController openDJController = new OpenDJController();
	protected static DerbyController derbyController = new DerbyController();

	// We need this complicated init as we want to initialize repo only once.
	// JUnit will
	// create new class instance for every test, so @Before and @PostInit will
	// not work
	// directly. We also need to init the repo after spring autowire is done, so
	// @BeforeClass won't work either.
	@BeforeMethod
	public void initSystemConditional() throws Exception {
		// Check whether we are already initialized
		assertNotNull("Repository is not wired properly", repositoryService);
		assertNotNull("Task manager is not wired properly", taskManager);
		LOGGER.trace("initSystemConditional: {} systemInitialized={}", this.getClass(), isSystemInitialized());
		if (!isSystemInitialized()) {
			DebugUtil.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
			PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
			LOGGER.trace("initSystemConditional: invoking initSystem");
			OperationResult result = new OperationResult(this.getClass().getName() + ".initSystem");
			initSystem(result);
			result.computeStatus();
			IntegrationTestTools.display("initSystem result", result);
			// TODO: check result
			IntegrationTestTools.assertSuccessOrWarning("initSystem failed (result)", result, 1);
			setSystemInitialized();
		}
	}

	protected boolean isSystemInitialized() {
		return initializedClasses.contains(this.getClass());
	}
	
	private void setSystemInitialized() {
		initializedClasses.add(this.getClass());
	}

	abstract public void initSystem(OperationResult initResult) throws Exception;

	protected PrismObject<ObjectType> addObjectFromFile(String filePath, OperationResult result) throws Exception {
		return addObjectFromFile(filePath, ObjectType.class, result);
	}

	protected <T extends ObjectType> PrismObject<T> addObjectFromFile(String filePath, Class<T> type,
			OperationResult parentResult) throws SchemaException, ObjectAlreadyExistsException {
		OperationResult result = parentResult.createSubresult(AbstractIntegrationTest.class.getName()
				+ ".addObjectFromFile");
		result.addParam("file", filePath);
		LOGGER.trace("addObjectFromFile: {}", filePath);
		PrismObject<T> object = prismContext.getPrismDomProcessor().parseObject(new File(filePath), type);
		System.out.println("obj: " + object.getName());
		// OperationResult result = new
		// OperationResult(AbstractIntegrationTest.class.getName() +
		// ".addObjectFromFile");
		if (object.canRepresent(TaskType.class)) {
			Assert.assertNotNull(taskManager, "Task manager is not initialized");
			try {
				taskManager.addTask((PrismObject<TaskType>) object, result);
			} catch (ObjectAlreadyExistsException ex) {
				result.recordFatalError(ex.getMessage(), ex);
				throw ex;
			} catch (SchemaException ex) {
				result.recordFatalError(ex.getMessage(), ex);
				throw ex;
			}
		} else {
			Assert.assertNotNull(repositoryService, "Repository service is not initialized");
			try{
			repositoryService.addObject(object, result);
			} catch(ObjectAlreadyExistsException ex){
				result.recordFatalError(ex.getMessage(), ex);
				throw ex;
			} catch(SchemaException ex){
				result.recordFatalError(ex.getMessage(), ex);
				throw ex;
			}
		}
		result.recordSuccess();
		return object;
	}
	
	protected <T extends ObjectType> T parseObjectTypeFromFile(String fileName, Class<T> clazz) throws SchemaException {
		return parseObjectType(new File(fileName), clazz);
	}
	
	protected <T extends ObjectType> T parseObjectType(File file) throws SchemaException {
		PrismObject<T> prismObject = prismContext.parseObject(file);
		return prismObject.asObjectable();
	}
	
	protected <T extends ObjectType> T parseObjectType(File file, Class<T> clazz) throws SchemaException {
		PrismObject<T> prismObject = prismContext.parseObject(file);
		return prismObject.asObjectable();
	}

	protected static <T> T unmarshallJaxbFromFile(String filePath, Class<T> clazz)
			throws FileNotFoundException, JAXBException, SchemaException {
		return PrismTestUtil.unmarshalObject(new File(filePath), clazz);
	}

	protected static ObjectType unmarshallJaxbFromFile(String filePath) throws FileNotFoundException,
			JAXBException, SchemaException {
		return unmarshallJaxbFromFile(filePath, ObjectType.class);
	}

	protected PrismObject<ResourceType> addResourceFromFile(String filePath, String connectorType, OperationResult result)
			throws FileNotFoundException, JAXBException, SchemaException, ObjectAlreadyExistsException {
		LOGGER.trace("addObjectFromFile: {}, connector type {}", filePath, connectorType);
		PrismObject<ResourceType> resource = prismContext.getPrismDomProcessor().parseObject(new File(filePath), ResourceType.class);
		fillInConnectorRef(resource, connectorType, result);
		display("Adding resource ", resource);
		String oid = repositoryService.addObject(resource, result);
		resource.setOid(oid);
		return resource;
	}

	protected PrismObject<ConnectorType> findConnectorByType(String connectorType, OperationResult result)
			throws SchemaException {
		Document doc = DOMUtil.getDocument();

		Element connectorTypeElement = doc.createElementNS(
				SchemaConstants.C_CONNECTOR_CONNECTOR_TYPE.getNamespaceURI(),
				SchemaConstants.C_CONNECTOR_CONNECTOR_TYPE.getLocalPart());
		connectorTypeElement.setTextContent(connectorType);

		// We have all the data, we can construct the filter now
		Element filter = QueryUtil.createEqualFilter(doc, null, connectorTypeElement);

		QueryType query = new QueryType();
		query.setFilter(filter);

//		System.out.println("Query:\n"+DOMUtil.serializeDOMToString(query.getFilter())+"\n--");
		List<PrismObject<ConnectorType>> connectors = repositoryService.searchObjects(ConnectorType.class, query, null,
				result);
		if (connectors.size() != 1) {
			throw new IllegalStateException("Cannot find connector type " + connectorType + ", got "
					+ connectors);
		}
		return connectors.get(0);
	}

	protected void fillInConnectorRef(PrismObject<ResourceType> resourcePrism, String connectorType, OperationResult result)
			throws SchemaException {
		ResourceType resource = resourcePrism.asObjectable();
		PrismObject<ConnectorType> connectorPrism = findConnectorByType(connectorType, result);
		ConnectorType connector = connectorPrism.asObjectable();
		if (resource.getConnectorRef() == null) {
			resource.setConnectorRef(new ObjectReferenceType());
		}
		resource.getConnectorRef().setOid(connector.getOid());
		resource.getConnectorRef().setType(ObjectTypes.CONNECTOR.getTypeQName());
	}

}
