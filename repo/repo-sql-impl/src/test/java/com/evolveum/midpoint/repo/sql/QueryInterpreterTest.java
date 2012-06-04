/*
 * Copyright (c) 2012 Evolveum
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
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.repo.sql.data.common.*;
import com.evolveum.midpoint.repo.sql.query.QueryException;
import com.evolveum.midpoint.repo.sql.query.QueryInterpreter;
import com.evolveum.midpoint.repo.sql.util.HibernateToSqlTranslator;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.File;

/**
 * @author lazyman
 */
@ContextConfiguration(locations = {
        "../../../../../application-context-sql-no-server-mode-test.xml",
        "../../../../../application-context-repository.xml",
        "classpath:application-context-repo-cache.xml",
        "../../../../../application-context-configuration-sql-test.xml"})
public class QueryInterpreterTest extends AbstractTestNGSpringContextTests {

    private static final Trace LOGGER = TraceManager.getTrace(AddGetObjectTest.class);
    private static final File TEST_DIR = new File("./src/test/resources/query");
    @Autowired(required = true)
    RepositoryService repositoryService;
    @Autowired(required = true)
    PrismContext prismContext;
    @Autowired(required = true)
    SessionFactory factory;
    
    @Test
    public void queryGenericLong() throws Exception {
        Session session = open();
        Criteria main = session.createCriteria(RGenericObject.class, "g");

        Criteria extension = main.createCriteria("extension", "e");
        Criteria stringExt = extension.createCriteria("longs", "l");

        //and
        Criterion c1 = Restrictions.eq("name", "generic object");
        //and
        Conjunction c2 = Restrictions.conjunction();
        c2.add(Restrictions.eq("l.value", 123L));
        c2.add(Restrictions.eq("l.name", new QName("http://example.com/p", "intType")));
        c2.add(Restrictions.eq("l.type", new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "integer")));
        
        Conjunction conjunction = Restrictions.conjunction();
        conjunction.add(c1);
        conjunction.add(c2);
        main.add(conjunction);

        String expected = HibernateToSqlTranslator.toSql(main);
        String real = getInterpretedQuery(session, GenericObjectType.class,
                new File(TEST_DIR, "query-and-generic.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);
        
        close(session);
    }

    @Test
    public void queryOrComposite() throws Exception {
        Session session = open();
        Criteria main = session.createCriteria(RAccountShadow.class, "a");

        Criteria attributes = main.createCriteria("attributes", "a1");
        Criteria stringAttr = attributes.createCriteria("strings", "s");

        Criteria extension = main.createCriteria("extension", "e");
        Criteria stringExt = extension.createCriteria("strings", "s1");

        Criteria resourceRef = main.createCriteria("resourceRef", "r");

        //or
        Criterion c1 = Restrictions.eq("accountType", "some account type");
        //or
        Conjunction c2 = Restrictions.conjunction();
        c2.add(Restrictions.eq("s.value", "foo value"));
        c2.add(Restrictions.eq("s.name", new QName("http://midpoint.evolveum.com/blabla", "foo")));
        c2.add(Restrictions.eq("s.type", new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string")));
        //or
        Conjunction c3 = Restrictions.conjunction();
        c3.add(Restrictions.eq("s1.value", "uid=test,dc=example,dc=com"));
        c3.add(Restrictions.eq("s1.name", new QName("http://example.com/p", "stringType")));
        c3.add(Restrictions.eq("s1.type", new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string")));
        //or
        Criterion c4 = Restrictions.eq("r.targetOid", "d0db5be9-cb93-401f-b6c1-86ffffe4cd5e");

        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(c1);
        disjunction.add(c2);
        disjunction.add(c3);
        disjunction.add(c4);
        main.add(disjunction);

        String expected = HibernateToSqlTranslator.toSql(main);
        String real = getInterpretedQuery(session, AccountShadowType.class,
                new File(TEST_DIR, "query-or-composite.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }

    @Test
    public void queryUserByFullName() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserByFullName"});
        Session session = open();

        Criteria main = session.createCriteria(RUser.class, "u");
        main.add(Restrictions.eq("fullName.norm", "cpt jack sparrow"));
        String expected = HibernateToSqlTranslator.toSql(main);

        String real = getInterpretedQuery(session, UserType.class,
                new File(TEST_DIR, "query-user-by-fullName.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }

    @Test
    public void queryUserSubstringFullName() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserSubstringFullName"});
        Session session = open();

        Criteria main = session.createCriteria(RUser.class, "u");
        main.add(Restrictions.like("fullName.norm", "%cpt jack sparrow%"));
        String expected = HibernateToSqlTranslator.toSql(main);

        String real = getInterpretedQuery(session, UserType.class,
                new File(TEST_DIR, "query-user-substring-fullName.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }

    @Test
    public void queryUserByName() throws Exception {
        LOGGER.info("===[{}]===", new Object[]{"queryUserByName"});
        Session session = open();

        Criteria main = session.createCriteria(RUser.class, "u");
        main.add(Restrictions.eq("name", "some name identificator"));
        String expected = HibernateToSqlTranslator.toSql(main);

        String real = getInterpretedQuery(session, UserType.class,
                new File(TEST_DIR, "query-user-by-name.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }

    @Test
    public void queryConnectorByType() throws Exception {
        Session session = open();

        Criteria main = session.createCriteria(RConnector.class, "c");
        main.add(Restrictions.eq("connectorType", "org.identityconnectors.ldap.LdapConnector"));
        String expected = HibernateToSqlTranslator.toSql(main);

        String real = getInterpretedQuery(session, ConnectorType.class,
                new File(TEST_DIR, "query-connector-by-type.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }

    @Test
    public void queryAccountByAttributesAndResourceRef() throws Exception {
        Session session = open();
        Criteria main = session.createCriteria(RAccountShadow.class, "a");

        Criteria resourceRef = main.createCriteria("resourceRef", "r");

        Criteria attributes = main.createCriteria("attributes", "a1");
        Criteria stringAttr = attributes.createCriteria("strings", "s");

        //and
        Criterion c1 = Restrictions.eq("r.targetOid", "aae7be60-df56-11df-8608-0002a5d5c51b");
        //and
        Conjunction c2 = Restrictions.conjunction();
        c2.add(Restrictions.eq("s.value", "uid=jbond,ou=People,dc=example,dc=com"));
        c2.add(Restrictions.eq("s.name", new QName("http://midpoint.evolveum.com/blabla", "foo")));
        c2.add(Restrictions.eq("s.type", new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string")));

        Conjunction conjunction = Restrictions.conjunction();
        conjunction.add(c1);
        conjunction.add(c2);
        main.add(conjunction);

        String expected = HibernateToSqlTranslator.toSql(main);
        String real = getInterpretedQuery(session, AccountShadowType.class,
                new File(TEST_DIR, "query-account-by-attributes-and-resource-ref.xml"));

        LOGGER.info("exp. query>\n{}\nreal query>\n{}", new Object[]{expected, real});
        AssertJUnit.assertEquals(expected, real);

        close(session);
    }
    
    @Test
    public void queryEmail() throws Exception {
        //todo query some Set<String> stuff and embedded stuff and Set<RObjectReference> value
    }

    private <T extends ObjectType> String getInterpretedQuery(Session session, Class<T> type, File file) throws
            QueryException {

        QueryInterpreter interpreter = new QueryInterpreter(session, type, prismContext);

        Document document = DOMUtil.parseFile(file);
        Element filter = DOMUtil.listChildElements(document.getDocumentElement()).get(0);
        Criteria criteria = interpreter.interpret(filter);
        return HibernateToSqlTranslator.toSql(criteria);
    }

    private Session open() {
        Session session = factory.openSession();
        session.beginTransaction();
        return session;
    }

    private void close(Session session) {
        session.getTransaction().commit();
        session.close();
    }
}
