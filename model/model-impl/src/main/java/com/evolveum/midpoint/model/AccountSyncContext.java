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
package com.evolveum.midpoint.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.common.refinery.ResourceAccountType;
import com.evolveum.midpoint.common.valueconstruction.ValueConstruction;
import com.evolveum.midpoint.schema.processor.ChangeType;
import com.evolveum.midpoint.schema.processor.MidPointObject;
import com.evolveum.midpoint.schema.processor.ObjectDelta;
import com.evolveum.midpoint.schema.processor.PropertyDelta;
import com.evolveum.midpoint.schema.util.DebugUtil;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.xml.ns._public.common.common_1.AccountShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceAccountTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.UserType;

/**
 * @author semancik
 *
 */
public class AccountSyncContext implements Dumpable, DebugDumpable {
	
	private ResourceAccountType resourceAccountType;
	private String oid;
	private MidPointObject<AccountShadowType> accountOld;
	private MidPointObject<AccountShadowType> accountNew;
	private ObjectDelta<AccountShadowType> accountPrimaryDelta;
	private ObjectDelta<AccountShadowType> accountSecondaryDelta; 
	private Map<QName, DeltaSetTriple<ValueConstruction>> attributeValueDeltaSetTripleMap;
	private ResourceType resource;
	private boolean isAssigned;
	private PolicyDecision policyDecision;
	
	AccountSyncContext(ResourceAccountType resourceAccountType) {
		this.resourceAccountType = resourceAccountType;
		this.attributeValueDeltaSetTripleMap = new HashMap<QName, DeltaSetTriple<ValueConstruction>>();
		this.isAssigned = false;
	}
	
	public ResourceAccountType getResourceAccountType() {
		return resourceAccountType;
	}

	public MidPointObject<AccountShadowType> getAccountOld() {
		return accountOld;
	}
	public void setAccountOld(MidPointObject<AccountShadowType> accountOld) {
		this.accountOld = accountOld;
	}
	public MidPointObject<AccountShadowType> getAccountNew() {
		return accountNew;
	}
	public void setAccountNew(MidPointObject<AccountShadowType> accountNew) {
		this.accountNew = accountNew;
	}
	
	public ObjectDelta<AccountShadowType> getAccountPrimaryDelta() {
		return accountPrimaryDelta;
	}
	public void setAccountPrimaryDelta(ObjectDelta<AccountShadowType> accountPrimaryDelta) {
		this.accountPrimaryDelta = accountPrimaryDelta;
	}
	
	public ObjectDelta<AccountShadowType> getAccountSecondaryDelta() {
		return accountSecondaryDelta;
	}
	public void setAccountSecondaryDelta(ObjectDelta<AccountShadowType> accountSecondaryDelta) {
		this.accountSecondaryDelta = accountSecondaryDelta;
	}
	
	public void addToSecondaryDelta(PropertyDelta accountPasswordDelta) {
		if (accountSecondaryDelta == null) {
			accountSecondaryDelta = new ObjectDelta<AccountShadowType>(AccountShadowType.class, ChangeType.MODIFY);
			accountSecondaryDelta.setOid(oid);
		}
		accountSecondaryDelta.swallow(accountPasswordDelta);
	}
	
	public ObjectDelta<AccountShadowType> getAccountDelta() {
		return ObjectDelta.union(accountPrimaryDelta, accountSecondaryDelta);
	}
	
	public ResourceType getResource() {
		return resource;
	}
	public void setResource(ResourceType resource) {
		this.resource = resource;
	}
	
	public String getOid() {
		return oid;
	}
	
	public void setOid(String oid) {
		this.oid = oid;
		if (accountPrimaryDelta != null) {
			accountPrimaryDelta.setOid(oid);
		}
		if (accountSecondaryDelta != null) {
			accountSecondaryDelta.setOid(oid);
		}
		if (accountNew != null) {
			accountNew.setOid(oid);
		}
	}
	
	public boolean isAssigned() {
		return isAssigned;
	}

	public void setAssigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
	}
	
	public PolicyDecision getPolicyDecision() {
		return policyDecision;
	}

	public void setPolicyDecision(PolicyDecision policyDecision) {
		this.policyDecision = policyDecision;
	}

	public Map<QName, DeltaSetTriple<ValueConstruction>> getAttributeValueDeltaSetTripleMap() {
		return attributeValueDeltaSetTripleMap;
	}
	
	public ResourceAccountTypeDefinitionType getResourceAccountTypeDefinitionType() {
		return ResourceTypeUtil.getResourceAccountTypeDefinitionType(resource, resourceAccountType.getAccountType());
	}
	
	/**
	 * Assuming that oldAccount is already set (or is null if it does not exist)
	 */
	public void recomputeAccountNew() {
		ObjectDelta<AccountShadowType> accDelta = getAccountDelta();
		if (accDelta == null) {
			// No change
			accountNew = accountOld;
			return;
		}
		accountNew = accDelta.computeChangedObject(accountOld);
	}	
	
	@Override
	public String debugDump() {
		return debugDump(0);
	}
	
	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("OID: ").append(oid);
		sb.append(", assigned=").append(isAssigned);
		sb.append(", decision=").append(policyDecision);
		sb.append("\n");
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ACCOUNT old:");
		if (accountOld == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			sb.append(accountOld.debugDump(indent+1));
		}
		
		sb.append("\n");
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ACCOUNT new:");
		if (accountNew == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			sb.append(accountNew.debugDump(indent+1));
		}

		sb.append("\n");
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ACCOUNT primary delta:");
		if (accountPrimaryDelta == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			sb.append(accountPrimaryDelta.debugDump(indent+1));
		}

		sb.append("\n");
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ACCOUNT secondary delta:");
		if (accountSecondaryDelta == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			sb.append(accountSecondaryDelta.debugDump(indent+1));
		}

		sb.append("\n");
		DebugUtil.indentDebugDump(sb, indent);
		sb.append("ACCOUNT attribute DeltaSetTriple map:");
		if (attributeValueDeltaSetTripleMap == null) {
			sb.append(" null");
		} else {
			sb.append("\n");
			DebugUtil.debugDumpMapSingleLine(sb, attributeValueDeltaSetTripleMap, indent+1);
		}

		return sb.toString();
	}
	
	@Override
	public String dump() {
		return debugDump();
	}
		
}
