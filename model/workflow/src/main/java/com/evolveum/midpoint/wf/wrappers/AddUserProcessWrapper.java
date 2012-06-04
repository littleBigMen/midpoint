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

package com.evolveum.midpoint.wf.wrappers;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ChangeType;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.wf.WfHook;
import com.evolveum.midpoint.wf.WfProcessStartCommand;
import com.evolveum.midpoint.wf.WfTaskUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ModelOperationStageType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.UserType;
import com.evolveum.midpoint.xml.ns._public.communication.workflow_1.WfProcessInstanceEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mederly
 * Date: 11.5.2012
 * Time: 15:06
 * To change this template use File | Settings | File Templates.
 */
@Component
public class AddUserProcessWrapper implements WfProcessWrapper {

    @Autowired(required = true)
    private WfHook wfHook;

    @Autowired(required = true)
    private WfTaskUtil wfTaskUtil;

    @PostConstruct
    public void register() {
        wfHook.registerWfProcessWrapper(this);
    }

    @Override
    public WfProcessStartCommand startProcessIfNeeded(ModelOperationStageType stage, Collection<ObjectDelta<? extends ObjectType>> changes, Task task) {

        if (stage == ModelOperationStageType.PRIMARY) {
            if (changes.size() == 1) {
                ObjectDelta<? extends ObjectType> change = changes.iterator().next();

                if (change.getChangeType() == ChangeType.ADD) {

                    // this causes problems in deltas -- probably it changes their internal state(!)
                    // todo: investigate further
                    //ObjectType objectToAdd = change.getObjectToAdd().getValue().getValue();

                    PrismObject<?> prismToAdd = change.getObjectToAdd();
                    boolean isUser = prismToAdd.getCompileTimeClass().isAssignableFrom(UserType.class);

                    if (isUser) {

                        String user = prismToAdd.asObjectable().getName();
                        if (user.startsWith("testwf")) {
                            WfProcessStartCommand startCommand = new WfProcessStartCommand();
                            startCommand.setProcessName("AddUser");
                            startCommand.addProcessVariable("user", user);
                            startCommand.setSimple(true);
                            return startCommand;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void finishProcess(WfProcessInstanceEventType event, Task task, OperationResult result) throws Exception {

        Map<String,String> variables = wfTaskUtil.unwrapWfVariables(event);
        if ("true".equals(variables.get("approved"))) {
            wfTaskUtil.markAcceptation(task, result);
        } else {
            wfTaskUtil.markRejection(task, result);
        }

    }
}
