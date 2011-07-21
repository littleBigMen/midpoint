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
 */

package com.evolveum.midpoint.schema.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Resource Object Definition (Object Class).
 * 
 * Object Class refers to a type of object on the Resource. Unix account, Active
 * Directory group, inetOrgPerson LDAP objectclass or a schema of USERS database
 * table are all Object Classes from the midPoint point of view. Object class
 * defines a set of attribute names, types for each attributes and few
 * additional properties.
 * 
 * This class represents schema definition for resource object (object class).
 * See {@link Definition} for more details.
 * 
 * Resource Object Definition is immutable. TODO: This will probably need to be
 * changed to a mutable object.
 * 
 * @author Radovan Semancik
 * 
 */
public class ResourceObjectDefinition extends PropertyContainerDefinition {

	private Schema schema;
	private Set<ResourceObjectAttributeDefinition> idenitifiers;
	private Set<ResourceObjectAttributeDefinition> secondaryIdenitifiers;
	private ResourceObjectAttributeDefinition descriptionAttribute;
	private ResourceObjectAttributeDefinition displayNameAttribute;
	private ResourceObjectAttributeDefinition namingAttribute;
	private boolean defaultAccountType = false;
	private boolean accountType = false;
	private String nativeObjectClass;

	public ResourceObjectDefinition(Schema schema, QName name, QName defaultName, QName typeName) {
		super(name, defaultName, typeName);
		if (schema == null) {
			throw new IllegalArgumentException("Schema can't be null.");
		}
		this.schema = schema;
	}

	/**
	 * Returns the definition of identifier attributes of a resource object.
	 * 
	 * May return empty set if there are no identifier attributes. Must not
	 * return null.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of model consistency should be done at the time of
	 * schema parsing.
	 * 
	 * @return definition of identifier attributes
	 * @throws IllegalStateException
	 *             if there is no definition for the referenced attributed
	 */
	public Set<ResourceObjectAttributeDefinition> getIdentifiers() {
		if (idenitifiers == null) {
			idenitifiers = new HashSet<ResourceObjectAttributeDefinition>();
		}
		return idenitifiers;
	}

	/**
	 * Returns the definition of secondary identifier attributes of a resource
	 * object.
	 * 
	 * May return empty set if there are no secondary identifier attributes.
	 * Must not return null.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of model consistency should be done at the time of
	 * schema parsing.
	 * 
	 * @return definition of secondary identifier attributes
	 * @throws IllegalStateException
	 *             if there is no definition for the referenced attributed
	 */
	public Set<ResourceObjectAttributeDefinition> getSecondaryIdentifiers() {
		if (secondaryIdenitifiers == null) {
			secondaryIdenitifiers = new HashSet<ResourceObjectAttributeDefinition>();
		}
		return secondaryIdenitifiers;
	}

	/**
	 * Returns the definition of description attribute of a resource object.
	 * 
	 * Returns null if there is no description attribute.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of model consistency should be done at the time of
	 * schema parsing.
	 * 
	 * @return definition of secondary identifier attributes
	 * @throws IllegalStateException
	 *             if there is more than one description attribute. But this
	 *             should never happen.
	 * @throws IllegalStateException
	 *             if there is no definition for the referenced attributed
	 */
	public ResourceObjectAttributeDefinition getDescriptionAttribute() {
		return descriptionAttribute;
	}

	public void setDescriptionAttribute(ResourceObjectAttributeDefinition descriptionAttribute) {
		this.descriptionAttribute = descriptionAttribute;
	}
	
	/**
	 * Specifies which resource attribute should be used as a "technical" name
	 * for the account. This name will appear in log files and other troubleshooting
	 * tools. The name should be a form of unique identifier that can be used to
	 * locate the resource object for diagnostics. It should not contain white chars and
	 * special chars if that can be avoided and it should be reasonable short.
                
	 * It is different from a display name attribute. Display name is intended for a 
	 * common user or non-technical administrator (such as role administrator). The
	 * naming attribute is intended for technical IDM administrators and developers.
	 * 
	 * @return resource attribute definition that should be used as a "technical" name
	 * 					for the account.
	 */
	public ResourceObjectAttributeDefinition getNamingAttribute() {
		return namingAttribute;
	}

	public void setNamingAttribute(ResourceObjectAttributeDefinition namingAttribute) {
		this.namingAttribute = namingAttribute;
	}

	/**
	 * Returns the native object class string for the resource object.
	 * 
	 * Native object class is the name of the Resource Object Definition (Object
	 * Class) as it is seen by the resource itself. The name of the Resource
	 * Object Definition may be constrained by XSD or other syntax and therefore
	 * may be "mangled" to conform to such syntax. The <i>native object
	 * class</i> value will contain unmangled name (if available).
	 * 
	 * Returns null if there is no native object class.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of model consistency should be done at the time of
	 * schema parsing.
	 * 
	 * @return native object class
	 * @throws IllegalStateException
	 *             if there is more than one description attribute.
	 */
	public String getNativeObjectClass() {
		return nativeObjectClass;
	}

	void setNativeObjectClass(String nativeObjectClass) {
		this.nativeObjectClass = nativeObjectClass;
	}

	/**
	 * Indicates whether definition is should be used as account type.
	 * 
	 * If true value is returned then the definition should be used as an
	 * account type definition. This is a way how a resource connector may
	 * suggest applicable object classes (resource object definitions) for
	 * accounts.
	 * 
	 * If no information about account type is present, false should be
	 * returned.
	 * 
	 * @return true if the definition should be used as account type.
	 */
	public boolean isAccountType() {
		return accountType;
	}

	public void setAccountType(boolean accountType) {
		this.accountType = accountType;
		if (!accountType) {
			defaultAccountType = false;
		}
	}

	/**
	 * Indicates whether definition is should be used as default account type.
	 * 
	 * If true value is returned then the definition should be used as a default
	 * account type definition. This is a way how a resource connector may
	 * suggest applicable object classes (resource object definitions) for
	 * accounts.
	 * 
	 * If no information about account type is present, false should be
	 * returned. This method must return true only if isAccountType() returns
	 * true.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of at-most-one value should be done at the time of
	 * schema parsing. The exception may not even be thrown at all if the
	 * implementation is not able to determine duplicity.
	 * 
	 * @return true if the definition should be used as account type.
	 * @throws IllegalStateException
	 *             if more than one default account is suggested in the schema.
	 */
	public boolean isDefaultAccountType() {
		return defaultAccountType;
	}

	public void setDefaultAccountType(boolean defaultAccountType) {
		this.defaultAccountType = defaultAccountType;
		if (defaultAccountType && !accountType) {
			throw new IllegalStateException(
					"Can't be default account type, flat account type (boolean) not set.");
		}
		if (defaultAccountType) {
			Set<Definition> definitions = schema.getDefinitions();
			for (Definition definition : definitions) {
				if (this == definition) {
					continue;
				}
				if (!(definition instanceof ResourceObjectDefinition)) {
					continue;
				}
				ResourceObjectDefinition resourceDef = (ResourceObjectDefinition) definition;
				if (resourceDef.isAccountType() && resourceDef.isDefaultAccountType()) {
					throw new IllegalStateException("Can't have two default account types "
							+ "(ResourceObjectDefinition) in schema (" + this.getName() + ", "
							+ resourceDef.getName() + ").");
				}
			}
		}
	}

	/**
	 * Returns the definition of display name attribute.
	 * 
	 * Display name attribute specifies which resource attribute should be used
	 * as title when displaying objects of a specific resource object class. It
	 * must point to an attribute of String type. If not present, primary
	 * identifier should be used instead (but this method does not handle this
	 * default behavior).
	 * 
	 * Returns null if there is no display name attribute.
	 * 
	 * The exception should be never thrown unless there is some bug in the
	 * code. The validation of model consistency should be done at the time of
	 * schema parsing.
	 * 
	 * @return native object class
	 * @throws IllegalStateException
	 *             if there is more than one display name attribute or the
	 *             definition of the referenced attribute does not exist.
	 */
	public ResourceObjectAttributeDefinition getDisplayNameAttribute() {
		return displayNameAttribute;
	}

	void setDisplayNameAttribute(ResourceObjectAttributeDefinition displayName) {
		this.displayNameAttribute = displayName;
	}
	
	@Override
	public ResourceObject instantiate() {
		return new ResourceObject(getNameOrDefaultName(), this);
	}
	
	public Set<ResourceObjectAttribute> parseAttributes(List<Element> elements) {
		return parseProperties(elements, ResourceObjectAttribute.class);
	}

	public Set<ResourceObjectAttribute> parseIdentifiers(List<Element> elements) {
		return parseProperties(elements, ResourceObjectAttribute.class,getIdentifiers());
	}

	public ResourceObjectAttributeDefinition findAttributeDefinition(QName elementQName) {
		return findPropertyDefinition(elementQName,ResourceObjectAttributeDefinition.class);
	}
}
