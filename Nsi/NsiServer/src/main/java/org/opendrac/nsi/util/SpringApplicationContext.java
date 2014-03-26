/**
 * Copyright (c) 2011, SURFnet bv, The Netherlands
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of the SURFnet bv, The Netherlands nor the names of
 *     its contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL SURFnet bv, The Netherlands BE LIABLE FOR
 * AND DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.opendrac.nsi.util;

/**
 * Obtain a reference to the Spring bean that has been configured and built
 * declaratively by the Spring container from the legacy code.
 *
 * Shamelessly liberated from the web.
 *
 * @author Sujit Pal
 * http://sujitpal.blogspot.com/2007/03/accessing-spring-beans-from-legacy-code.html
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Wrapper to always return a reference to the Spring Application Context from
 * within non-Spring enabled beans. Unlike Spring MVC's WebApplicationContextUtils
 * we do not need a reference to the Servlet context for this. All we need is
 * for this bean to be initialized during application startup.
 */
@Component("springApplicationContext")
public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    /**
    * This method is called from within the ApplicationContext once it is
    * done starting up, it will stick a reference to itself into this bean.
    * @param context a reference to the ApplicationContext.
    */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        CONTEXT = context;
    }

    /**
    * This is about the same as context.getBean("beanName"), except it has its
    * own static handle to the Spring context, so calling this method statically
    * will give access to the beans by name in the Spring application context.
    * As in the context.getBean("beanName") call, the caller must cast to the
    * appropriate target class. If the bean does not exist, then a Runtime error
    * will be thrown.
    * @param beanName the name of the bean to get.
    * @param requiredType the type to be returned
    * @return a reference to the named bean.
    */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return CONTEXT.getBean(name, requiredType);
    }
}