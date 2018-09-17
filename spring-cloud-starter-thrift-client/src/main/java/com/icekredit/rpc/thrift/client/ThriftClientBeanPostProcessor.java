package com.icekredit.rpc.thrift.client;

import com.icekredit.rpc.thrift.client.annotation.ThriftReferer;
import com.icekredit.rpc.thrift.client.common.ThriftClientAware;
import com.icekredit.rpc.thrift.client.exception.ThriftClientInstantiateException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThriftClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Object target = bean;
        if (AopUtils.isJdkDynamicProxy(target)) {
            TargetSource targetSource = ((Advised) target).getTargetSource();
            if (log.isDebugEnabled()) {
                log.debug("Target object {} uses jdk dynamic proxy");
            }

            try {
                target = targetSource.getTarget();
            } catch (Exception e) {
                throw new ThriftClientInstantiateException("Failed to get target bean from " + target, e);
            }
        }

        if (AopUtils.isCglibProxy(target)) {
            TargetSource targetSource = ((Advised) target).getTargetSource();
            if (log.isDebugEnabled()) {
                log.debug("Target object {} uses cglib proxy");
            }

            try {
                target = targetSource.getTarget();
            } catch (Exception e) {
                throw new ThriftClientInstantiateException("Failed to get target bean from " + target, e);
            }
        }

        Class<?> targetClass = target.getClass();
        final Object targetBean = target;

        ReflectionUtils.doWithFields(targetClass, field -> {
            ThriftReferer thriftReferer = AnnotationUtils.findAnnotation(field, ThriftReferer.class);
            String referName = StringUtils.isNotBlank(thriftReferer.value()) ? thriftReferer.value() : thriftReferer.name();

            Class<?> fieldType = field.getType();
            Object injectedBean;

            if (StringUtils.isNotBlank(referName)) {
                injectedBean = applicationContext.getBean(fieldType, referName);

                injectedBean = Optional.ofNullable(injectedBean)
                        .orElseThrow(() -> new ThriftClientInstantiateException("Detected non-qualified bean with name {}" + referName));

                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, targetBean, injectedBean);
            } else {
                Map<String, ?> injectedBeanMap = applicationContext.getBeansOfType(field.getType());
                if (MapUtils.isEmpty(injectedBeanMap)) {
                    throw new ThriftClientInstantiateException("Detected non-qualified bean of {}" + fieldType.getSimpleName());
                }

                if (injectedBeanMap.size() > 1) {
                    throw new ThriftClientInstantiateException("Detected ambiguous beans of {}" + fieldType.getSimpleName());
                }

                injectedBean = injectedBeanMap.entrySet().stream()
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(() -> new ThriftClientInstantiateException(
                                "Detected non-qualified bean of {}" + fieldType.getSimpleName()));

                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, targetBean, injectedBean);
            }

            if (log.isDebugEnabled()) {
                log.debug("Bean {} is injected into target bean {}, field {}", injectedBean, targetBean, field.getName());
            }

        }, field -> (AnnotationUtils.getAnnotation(field, ThriftReferer.class) != null));

        ReflectionUtils.MethodFilter methodFilter = method -> {
            boolean basicCondition = AnnotationUtils.getAnnotation(method, ThriftReferer.class) != null
                    && method.getParameterCount() > 0
                    && method.getReturnType() == Void.TYPE;

            if (!basicCondition) {
                return false;
            }

            return Arrays.stream(method.getParameters())
                    .map(Parameter::getType)
                    .map(ThriftClientAware.class::isAssignableFrom)
                    .reduce((param1, param2) -> param1 && param2)
                    .get();
        };

        ReflectionUtils.doWithMethods(targetClass, method -> {
            Parameter[] parameters = method.getParameters();
            Object objectArray = Arrays.stream(parameters).map(parameter -> {
                Class<?> parameterType = parameter.getType();
                Map<String, ?> injectedBeanMap = applicationContext.getBeansOfType(parameterType);
                if (MapUtils.isEmpty(injectedBeanMap)) {
                    throw new ThriftClientInstantiateException("Detected non-qualified bean of {}" + parameterType.getSimpleName());
                }

                if (injectedBeanMap.size() > 1) {
                    throw new ThriftClientInstantiateException("Detected ambiguous beans of {}" + parameterType.getSimpleName());
                }

                try {
                    return injectedBeanMap.entrySet().stream()
                            .findFirst()
                            .map(Map.Entry::getValue);
                } catch (ThriftClientInstantiateException e) {
                    throw e;
                }
            }).collect(Collectors.toList()).toArray();

            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, targetBean, objectArray);
        }, methodFilter);

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
