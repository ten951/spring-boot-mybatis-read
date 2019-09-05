# Spring MVC

## MVC的自动加载
```java
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
// WebMvcConfigurationSupport这个Bean不存在才会启动 这个类的作用是如果@EnableWebMvc注解的自动装配 两个是互斥的
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
//优先级
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
//这些自动配置加载之后加载
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {
    
}
```
功能是
1. 启动spring.mvc和spring.resources开头的配置项
2. 加载InternalResourceViewResolver 内部试图资源解析器 我理解指定JSP位置的
3. 加载BeanNameViewResolver 支持通过BeanName匹配的师徒解析器
4. RequestMappingHandlerAdapter  处理controller和view的对应关系
5. RequestMappingHandlerMapping 处理请求路径和controller对应关系的
6. WelcomePageHandlerMapping
7. FormattingConversionService
8. Validator
9. ContentNegotiationManager


## HandlerInterceptor接口

* preHandle是在找到处理handler对象的HandlerMapping之后，HandlerAdapter调度handler之前执行。

* postHandle是在HandlerAdapter调度handler之后，DispatcherServlet渲染视图之前执行，可以通过ModelAndView来向视图中添加一些信息等。

* afterCompletion是在渲染视图结束后执行，主要可以用来进行事后的资源清理。

## HandlerExecutionChain类

这个类由一个handler和若干的HandlerInterceptor构成。那么这个类的作用就显而易见了，就是将拦截器和handle组合起来执行。就是对handle进行了包装。

```java
public class HandlerExecutionChain {


	boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
            // 顺序执行
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
                // 直到遇到一个false的
				if (!interceptor.preHandle(request, response, this.handler)) {
                    // false 就执行triggerAfterCompletion
					triggerAfterCompletion(request, response, null);
					return false;
				}
                // 记录false的位置
				this.interceptorIndex = i;
			}
		}
		return true;
	}

	/**
	 * Apply postHandle methods of registered interceptors.
	 */
	void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.postHandle(request, response, this.handler, mv);
			}
		}
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle invocation
	 * has successfully completed and returned true.
	 */
	void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
		    // 从false的位置开始. 向前遍历顺序执行. 
			for (int i = this.interceptorIndex; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				try {
					interceptor.afterCompletion(request, response, this.handler, ex);
				}
				catch (Throwable ex2) {
					logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
				}
			}
		}
	}

}
```

## MappedInterceptor接口

```java
public final class MappedInterceptor implements HandlerInterceptor {
	@Nullable
	private final String[] includePatterns;

	@Nullable
	private final String[] excludePatterns;

	private final HandlerInterceptor interceptor;

	@Nullable
	private PathMatcher pathMatcher;

public boolean matches(String lookupPath, PathMatcher pathMatcher) {
		PathMatcher pathMatcherToUse = (this.pathMatcher != null ? this.pathMatcher : pathMatcher);
		if (!ObjectUtils.isEmpty(this.excludePatterns)) {
			for (String pattern : this.excludePatterns) {
				if (pathMatcherToUse.match(pattern, lookupPath)) {
					return false;
				}
			}
		}
		if (ObjectUtils.isEmpty(this.includePatterns)) {
			return true;
		}
		for (String pattern : this.includePatterns) {
			if (pathMatcherToUse.match(pattern, lookupPath)) {
				return true;
			}
		}
		return false;
	}

}
```
true 拦截 false 不拦截

如果路径在excludePatterns中，则不拦截。如果不在，那么若includePatterns为空，则拦截，否则在includePatterns中才拦截。

比较关键的两个地方是：

1. 优先判excludePatterns
2. 若includePatterns列表为空且请求不在excludePatterns的情况下全部拦截，否则只拦截includePatterns中的内容

## HandlerMapping接口

HandlerMapping是用来处理请求与handler对象的对应关系的。

HandlerMapping主要包含getHandler这个方法。

HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
DispatcherServlet就是通过调用HandlerMapping的getHandler来进行找到request对应的handler的。

### RequestMappingHandlerMapping#afterPropertiesSet()
这个方法是在项目启动创建Bean的时候调用的. 因为他实现了InitializingBean接口.
AbstractHandlerMethodMapping#afterPropertiesSet()->initHandlerMethods()
```java

	protected void initHandlerMethods() {
		for (String beanName : getCandidateBeanNames()) {
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
				processCandidateBean(beanName);
			}
		}
		handlerMethodsInitialized(getHandlerMethods());
	}

    protected String[] getCandidateBeanNames() {
		return (this.detectHandlerMethodsInAncestorContexts ?
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
				obtainApplicationContext().getBeanNamesForType(Object.class));
	}
    protected void processCandidateBean(String beanName) {
		Class<?> beanType = null;
		try {

			beanType = obtainApplicationContext().getType(beanName);
		}
		catch (Throwable ex) {
			// An unresolvable bean type, probably from a lazy bean - let's ignore it.
			if (logger.isTraceEnabled()) {
				logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
			}
		}
        //判断是否Handler 标注了@Controller或者@RequestMapping
		if (beanType != null && isHandler(beanType)) {
            //解析controller的方法并注册
			detectHandlerMethods(beanName);
		}
	}
    
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
				AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
	}

    protected void detectHandlerMethods(Object handler) {
        //获取到controller的class
		Class<?> handlerType = (handler instanceof String ?
				obtainApplicationContext().getType((String) handler) : handler.getClass());

		if (handlerType != null) {
			Class<?> userType = ClassUtils.getUserClass(handlerType);
            // 完成了controller中方法和RequestMappingInfo的绑定. 例如TestController类中的 tet方法 请求路径信息的绑定
            //RequestMappingInfo 代表了请求的一些信息 如:路径信息(/test/get)  method(GET)
			Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
					(MethodIntrospector.MetadataLookup<T>) method -> {
						try {
							return getMappingForMethod(method, userType);
						}
						catch (Throwable ex) {
							throw new IllegalStateException("Invalid mapping on handler class [" +
									userType.getName() + "]: " + method, ex);
						}
					});
			if (logger.isTraceEnabled()) {
				logger.trace(formatMappings(userType, methods));
			}
			methods.forEach((method, mapping) -> {
				Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                //通过MappingRegistry#registry()方法注册. 
				registerHandlerMethod(handler, invocableMethod, mapping);
			});
		}
	}

```
obtainApplicationContext().getBeanNamesForType(Object.class); 这个方法. 调用链是
AbstractApplicationContext#getBeanNamesForType(Object.class) -> DefaultListableBeanFactory#getBeanNamesForType(type, true(包含非单例的), true(包含提早加载的)); ->
DefaultListableBeanFactory#doGetBeanNamesForType() 从DefaultListableBeanFactory中定义的注册表中获取 type为Object的类名字. 当然包含Controller.


经过这些所有的controller就都解析完成了. 

## HandlerAdapter接口
在springMVC的执行流行流程中，当执行完handlerMapping获取到request对应的HandlerExecutionChain之后，下一步就是调用HandlerAdapter执行对应的Handler。
```java
public interface HandlerAdapter {

   boolean supports(Object handler);

   @Nullable
   ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

   long getLastModified(HttpServletRequest request, Object handler);

}

```
supports()是用来判断一个handler是否属于该HandlerAdapter的，一个典型的实现方式是判断该handler的类型，通常来说一个HandlerAdapter只支持一种类型的handler。

handle()的作用是使用给定的handler去处理请求。

getLastModified()的作用和HttpServlet中的getLastModified一致，若是handler不支持getLastModified则直接返回-1。



