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

package com.evolveum.midpoint.web.component.prism;

import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.PrismPropertyDefinition;
import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PropertyPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.xml.ns._public.common.common_2.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyman
 */
public class PropertyWrapper implements ItemWrapper, Serializable {

    private ContainerWrapper container;
    private PrismProperty property;
    private ValueStatus status;
    private List<ValueWrapper> values;
    private String displayName;

    public PropertyWrapper(ContainerWrapper container, PrismProperty property, ValueStatus status) {
        Validate.notNull(property, "Property must not be null.");
        Validate.notNull(status, "Property status must not be null.");

        this.container = container;
        this.property = property;
        this.status = status;

        PropertyPath passwordPath = new PropertyPath(SchemaConstantsGenerated.C_CREDENTIALS,
                CredentialsType.F_PASSWORD);
        if (passwordPath.equals(container.getPath())
                && PasswordType.F_PROTECTED_STRING.equals(property.getName())) {
            displayName = "prismPropertyPanel.name.credentials.password";
        }
    }

    ContainerWrapper getContainer() {
        return container;
    }

    @Override
    public String getDisplayName() {
        if (StringUtils.isNotEmpty(displayName)) {
            return displayName;
        }
        return ContainerWrapper.getDisplayNameFromItem(property);
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ValueStatus getStatus() {
        return status;
    }

    public List<ValueWrapper> getValues() {
        if (values == null) {
            values = createValues();
        }
        return values;
    }

    @Override
    public PrismProperty getItem() {
        return property;
    }

    private List<ValueWrapper> createValues() {
        List<ValueWrapper> values = new ArrayList<ValueWrapper>();

        for (PrismPropertyValue prismValue : (List<PrismPropertyValue>) property.getValues()) {
            values.add(new ValueWrapper(this, prismValue, ValueStatus.NOT_CHANGED));
        }

        PrismPropertyDefinition definition = property.getDefinition();
        if (values.isEmpty()) {
            PrismPropertyValue value;
            if (ProtectedStringType.COMPLEX_TYPE.equals(definition.getTypeName())) {
                values.add(new ValueWrapper(this, new PrismPropertyValue(new ProtectedStringType()),
                        new PrismPropertyValue(new ProtectedStringType()), ValueStatus.ADDED));
            } else if (SchemaConstants.T_POLY_STRING_TYPE.equals(definition.getTypeName())) {
                values.add(new ValueWrapper(this, new PrismPropertyValue(new PolyString(null)),
                        new PrismPropertyValue(new PolyString(null)), ValueStatus.ADDED));
            } else if (isThisPropertyActivationEnabled()) {
                value = new PrismPropertyValue(true);
                values.add(new ValueWrapper(this, value, new PrismPropertyValue(null), ValueStatus.ADDED));
            } else {
                value = new PrismPropertyValue(null);
                values.add(new ValueWrapper(this, value, ValueStatus.ADDED));
            }
        }

        return values;
    }

    private boolean isThisPropertyActivationEnabled() {
        if (!new PropertyPath(UserType.F_ACTIVATION).equals(container.getPath())) {
            return false;
        }

        if (!ActivationType.F_ENABLED.equals(property.getName())) {
            return false;
        }

        return true;
    }

    boolean hasChanged() {
        for (ValueWrapper value : getValues()) {
            switch (value.getStatus()) {
                case DELETED:
                    return true;
                case ADDED:
                case NOT_CHANGED:
                    if (value.hasValueChanged()) {
                        return true;
                    }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getDisplayName());
        builder.append(", ");
        builder.append(status);
        builder.append("\n");
        for (ValueWrapper wrapper : getValues()) {
            builder.append("\t");
            builder.append(wrapper.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
