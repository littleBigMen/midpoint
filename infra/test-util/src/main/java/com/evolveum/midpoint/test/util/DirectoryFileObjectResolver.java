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
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.test.util;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.opends.server.types.ObjectClass;

import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ObjectResolver;
import com.evolveum.midpoint.util.JAXBUtil;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectType;

/**
 * @author semancik
 *
 */
public class DirectoryFileObjectResolver implements ObjectResolver {

	private File directory;
	
	public DirectoryFileObjectResolver(File directory) {
		super();
		this.directory = directory;
	}

	@Override
	public <T extends ObjectType> T resolve(ObjectReferenceType ref, Class<T> expectedType, String contextDescription, OperationResult result)
			throws ObjectNotFoundException, SchemaException {
		File file = new File( directory, oidToFilename(ref.getOid()));
		if (file.exists()) {
			return (T)PrismTestUtil.parseObject(file).asObjectable();
		} else {
			throw new ObjectNotFoundException("Object "+ref.getOid()+" does not exists");
		}
	}
	
	private String oidToFilename(String oid) {
		return oid+".xml";
	}

}
