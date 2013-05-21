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
 * Portions Copyrighted 2013 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.common.embedded.RActivation;
import com.evolveum.midpoint.repo.sql.data.common.other.RAssignmentOwner;
import com.evolveum.midpoint.repo.sql.data.common.other.RReferenceOwner;
import com.evolveum.midpoint.repo.sql.data.common.type.RAccountRef;
import com.evolveum.midpoint.repo.sql.util.ContainerIdGenerator;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.FocusType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Where;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lazyman
 */
@Entity
@ForeignKey(name = "fk_focus")
@org.hibernate.annotations.Table(appliesTo = "m_focus",
        indexes = {@Index(name = "iFocusAdministrative", columnNames = "administrativeStatus"),
                @Index(name = "iFocusEffective", columnNames = "effectiveStatus")})
public abstract class RFocus extends RObject {

    private Set<RObjectReference> linkRef;
    private Set<RAssignment> assignment;
    private RActivation activation;

    @Where(clause = RObjectReference.REFERENCE_TYPE + "=" + RAccountRef.DISCRIMINATOR)
    @OneToMany(mappedBy = "owner", orphanRemoval = true)
    @ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<RObjectReference> getLinkRef() {
        if (linkRef == null) {
            linkRef = new HashSet<RObjectReference>();
        }
        return linkRef;
    }

    @Where(clause = RAssignment.F_ASSIGNMENT_OWNER + "=0")
    @OneToMany(mappedBy = RAssignment.F_OWNER, orphanRemoval = true)
    @ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<RAssignment> getAssignment() {
        if (assignment == null) {
            assignment = new HashSet<RAssignment>();
        }
        return assignment;
    }

    @Embedded
    public RActivation getActivation() {
        return activation;
    }

    public void setAssignment(Set<RAssignment> assignment) {
        this.assignment = assignment;
    }

    public void setLinkRef(Set<RObjectReference> linkRef) {
        this.linkRef = linkRef;
    }

    public void setActivation(RActivation activation) {
        this.activation = activation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RFocus other = (RFocus) o;

        if (assignment != null ? !assignment.equals(other.assignment) : other.assignment != null) return false;
        if (linkRef != null ? !linkRef.equals(other.linkRef) : other.linkRef != null) return false;
        if (activation != null ? !activation.equals(other.activation) : other.activation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (activation != null ? activation.hashCode() : 0);

        return result;
    }

    public static void copyFromJAXB(FocusType jaxb, RFocus repo, PrismContext prismContext) throws
            DtoTranslationException {
        RObject.copyFromJAXB(jaxb, repo, prismContext);

        repo.getLinkRef().addAll(
                RUtil.safeListReferenceToSet(jaxb.getLinkRef(), prismContext, repo, RReferenceOwner.USER_ACCOUNT));

        ContainerIdGenerator gen = new ContainerIdGenerator();
        for (AssignmentType assignment : jaxb.getAssignment()) {
            RAssignment rAssignment = new RAssignment(repo, RAssignmentOwner.FOCUS);
            RAssignment.copyFromJAXB(assignment, rAssignment, jaxb, prismContext);

            repo.getAssignment().add(rAssignment);
        }

        if (jaxb.getActivation() != null) {
            RActivation activation = new RActivation();
            RActivation.copyFromJAXB(jaxb.getActivation(), activation, prismContext);
            repo.setActivation(activation);

        }
    }

    public static void copyToJAXB(RFocus repo, FocusType jaxb, PrismContext prismContext) throws
            DtoTranslationException {
        RObject.copyToJAXB(repo, jaxb, prismContext);

        List linkRefs = RUtil.safeSetReferencesToList(repo.getLinkRef(), prismContext);
        if (!linkRefs.isEmpty()) {
            jaxb.getLinkRef().addAll(linkRefs);
        }

        for (RAssignment rAssignment : repo.getAssignment()) {
            jaxb.getAssignment().add(rAssignment.toJAXB(prismContext));
        }

        if (repo.getActivation() != null) {
            jaxb.setActivation(repo.getActivation().toJAXB(prismContext));
        }
    }
}
