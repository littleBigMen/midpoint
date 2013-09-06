/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.wf.activiti;

import com.evolveum.midpoint.audit.api.AuditService;
import com.evolveum.midpoint.model.api.expr.MidpointFunctions;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.wf.ProcessInstanceController;
import com.evolveum.midpoint.wf.util.MiscDataUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationContextHolder implements ApplicationContextAware {

	private static ApplicationContext context;

	public void setApplicationContext(ApplicationContext ctx) throws BeansException { 
		context = ctx;
    }  

	public static ApplicationContext getApplicationContext() {
        if (context == null) {
            throw new IllegalStateException("Spring application context could not be determined.");
        }
		return context;
	}

    public static ActivitiInterface getActivitiInterface() {
        return getBean("activitiInterface", ActivitiInterface.class);
    }

    private static<T> T getBean(String name, Class<T> aClass) {
        T bean = getApplicationContext().getBean(name, aClass);
        if (bean == null) {
            throw new IllegalStateException("Could not find " + name + " bean");
        }
        return bean;
    }

    public static MiscDataUtil getMiscDataUtil() {
        return getBean("miscDataUtil", MiscDataUtil.class);
    }

    public static RepositoryService getRepositoryService() {
        return getBean("repositoryService", RepositoryService.class);
    }

    public static PrismContext getPrismContext() {
        return getBean("prismContext", PrismContext.class);
    }

    public static ProcessInstanceController getProcessInstanceController() {
        return getBean("processInstanceController", ProcessInstanceController.class);
    }

    public static AuditService getAuditService() {
        return getBean("auditService", AuditService.class);
    }

    public static MidpointFunctions getMidpointFunctions() {
        return getBean("midpointFunctionsImpl", MidpointFunctions.class);
    }
}

  
