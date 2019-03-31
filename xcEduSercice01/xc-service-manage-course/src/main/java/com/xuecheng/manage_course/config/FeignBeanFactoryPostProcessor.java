package com.xuecheng.manage_course.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 解
 * 决
 * 出
 * 现
 * 的
 * {Error creating bean with name 'eurekaAutoServiceRegistration'}
 * bug
 * <p>
 * org.springframework.beans.factory.BeanCreationNotAllowedException:
 * Error creating bean with name 'eurekaAutoServiceRegistration':
 * Singleton bean creation not allowed while singletons of this factory are in destruction
 * (Do not request a bean from a BeanFactory in a destroy method implementation!)
 */

@Component
public class FeignBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (containsBeanDefinition(beanFactory, "feignContext", "eurekaAutoServiceRegistration")) {
            BeanDefinition bd = beanFactory.getBeanDefinition("feignContext");
            bd.setDependsOn("eurekaAutoServiceRegistration");
        }
    }

    private boolean containsBeanDefinition(ConfigurableListableBeanFactory beanFactory, String... beans) {
        return Arrays.stream(beans).allMatch(b -> beanFactory.containsBeanDefinition(b));
    }
}
