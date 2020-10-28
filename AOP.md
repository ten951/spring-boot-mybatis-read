# Spring Aop的实现原理

## aop体系结构及其和spring aop整合

1. 标准体系架构:

* Advice 增强器标记接口

* Interceptor 拦截器，Advice的子接口，标记拦截器。拦截器是增强器的一种。

* MethodInterceptor 方法拦截器，Interceptor的子接口，拦截方法并处理。

* ConstructorInterceptor 构造器拦截器，Interceptor的子接口，拦截构造器并处理。

* Joinpoint 连接点。在拦截器中使用，封装了原方法调用的相关信息，如参数、原对象信息，以及直接调用原方法的proceed方法。

* Invocation Joinpoint的子类，添加了获取调用参数方法。

* MethodInvocation Invocation的子类，包含了获取调用方法的方法。

* ConstructorInvocation Invocation的子类，包含了获取构造器的方法。


2. Spring AOP框架的整合

* AopProxyFactory接口

AopProxy代理工厂类，用于生成代理对象AopProxy。
```java
public interface AopProxyFactory {
    AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;
}
```
* AopProxy 代表一个AopProxy代理对象，可以通过这个对象构造代理对象实例。spring aop一共的接口. 用来屏蔽各种代理的实现, 如jdk或者cglib, 只要实现AopProxy接口就可以了. spring框架高层抽象只要依赖这个接口就可以, 充分体现了高层避免依赖底层实现的设计原则.

```java
public interface AopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
```
* Advised接口 代表被Advice增强的对象，包括添加advisor的方法、添加advice等的方法。

* ProxyConfig类 一个代理对象的配置信息，包括代理的各种属性，如基于接口还是基于类构造代理。

* AdvisedSupport类 对Advised的构建提供支持，Advised的实现类以及ProxyConfig的子类。

* ProxyCreatorSupport类 AdvisedSupport的子类，创建代理对象的支持类，内部包含AopProxyFactory工厂成员，可直接使用工厂成员创建Proxy。

* ProxyFactory类 ProxyCreatorSupport的子类，用于生成代理对象实例的工厂类，生成代码参考下面。

* Advisor接口 代表一个增强器提供者的对象，内部包含getAdvice方法获取增强器。

* AdvisorChainFactory接口 获取增强器链的工厂接口。提供方法返回所有增强器，以数组返回。

* Pointcut接口 切入点，用于匹配类与方法，满足切入点的条件是才插入advice

## 代理方式

代理方式分为两种 JDK和Cglib.  默认情况下是cglib. 除非你特殊指定. aop的功能和代理类是cglib还是jdk都是自动配置的 奥秘在``AopAutoConfiguration``类

AopAutoConfiguration这类是本身就是EnableAutoConfiguration注解扫描的一部分. 自动配置的工作原理这里就不说了.

```java

@Configuration
//要求上下文依赖中必须存在的类
@ConditionalOnClass({ EnableAspectJAutoProxy.class, Aspect.class, Advice.class, AnnotatedElement.class })
//当spring.aop.auto = true或者不指定这个属性
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = false)
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false",
			matchIfMissing = false)
	public static class JdkDynamicAutoProxyConfiguration {

	}

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
    //如果spring.aop.proxy-target-class = true或者不配置这个项目. 这是默认行为. 
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	public static class CglibAutoProxyConfiguration {

	}

}
```
ConditionalOnClass和ConditionalOnProperty 满足这些条件就会启动这个自动装载. 

JdkDynamicAutoProxyConfiguration是使用JDK动态代理的方式配置  CglibAutoProxyConfiguration是启用cglib的方式(默认行为). 

### @EnableAspectJAutoProxy注解
```java
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //将AnnotationAwareAspectJAutoProxyCreator类封装成RootBeanDefinition并注册
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}
```

### AnnotationAwareAspectJAutoProxyCreator类
![截屏2019-10-1612.28.20](media/15711998192950/%E6%88%AA%E5%B1%8F2019-10-1612.28.20.png)
AnnotationAwareAspectJAutoProxyCreator本身是ProxyConfig也是InstantiationAwareBeanPostProcessor.

#### InstantiationAwareBeanPostProcessor简介
```java
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {  
    /**
     * 是最先执行的方法，它在目标对象实例化之前调用，该方法的返回值类型是Object，我们可以返回任何类型的值。
     * 由于这个时候目标对象还未实例化，所以这个返回值可以用来代替原本该生成的目标对象的实例(比如代理对象)。
     * 如果该方法的返回值代替原本该生成的目标对象，
     */
    Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

    /**
     * 在目标对象实例化之后调用，这个时候对象已经被实例化，但是该实例的属性还未被设置都是null
     */
    boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

    /**
     * 对属性值进行修改(这个时候属性值还未被设置，但是我们可以修改原本该设置进去的属性值)。
     */
    PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException;
}
```

#### AnnotationAwareAspectJAutoProxyCreator#postProcessBeforeInstantiation方法的实现


AnnotationAwareAspectJAutoProxyCreator  支持注释的AspectJ自动代理创建器.
ReflectiveAspectJAdvisorFactory 反射AspectJ 增强器提供者工厂  可以获得所有切面注解实现的advice.
BeanFactoryAspectJAdvisorsBuilderAdapter  AspectJ构建者适配器

在他的父类AbstractAutoProxyCreator中找到了实现方法
```java
@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        //确定缓存KEY
		Object cacheKey = getCacheKey(beanClass, beanName);

		if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
			if (this.advisedBeans.containsKey(cacheKey)) {
				return null;
			}
			if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return null;
			}
		}

		// Create proxy here if we have a custom TargetSource.
		// Suppresses unnecessary default instantiation of the target bean:
		// The TargetSource will handle target instances in a custom fashion.
		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
		if (targetSource != null) {
			if (StringUtils.hasLength(beanName)) {
				this.targetSourcedBeans.add(beanName);
			}
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		return null;
	}


    @Override
	public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (this.earlyProxyReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}

    @Override
	public Object getEarlyBeanReference(Object bean, String beanName) {
		Object cacheKey = getCacheKey(bean.getClass(), beanName);
		this.earlyProxyReferences.put(cacheKey, bean);
		return wrapIfNecessary(bean, beanName, cacheKey);
	}
```

上面的三个方法都是创建代理里的入口. 其中getAdvicesAndAdvisorsForBean()方法无疑是很关键的. 
```java


	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
		//发现符合条件的advisors(在aop框架中的语义是 增强器的提供者.)
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * @param beanClass the clazz to find advisors for
	 * @param beanName the name of the currently proxied bean
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		//获取候选增强器提供者 
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		//验证有效性. 也就是canApply 可以应用的.
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// 钩子方法方便拓展
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			//排序
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}
	//AnnotationAwareAspectJAutoProxyCreator类findCandidateAdvisors()方法
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules. AbstractAdvisorAutoProxyCreator#findCandidateAdvisors
		// 根据超类的方式(Advisor) 获取Spring的增强器
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		// 为bean工厂中的所有AspectJ方面 构建增强器
		if (this.aspectJAdvisorsBuilder != null) {
			// 这里获取
			advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		}
		return advisors;
	}

	/**
	 *AbstractAdvisorAutoProxyCreator类findCandidateAdvisors
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}


	//AbstractAdvisorAutoProxyCreator类的findAdvisorBeans()
	public List<Advisor> findAdvisorBeans() {
		// Determine list of advisor bean names, if not cached already.
		String[] advisorNames = this.cachedAdvisorBeanNames;
		if (advisorNames == null) {
			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the auto-proxy creator apply to them!
			//在beanFactory中 找到类型为Advisor的类. 也就是实现了Advisor的类.  这里拿到的都是advisor类的名字
			advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
					this.beanFactory, Advisor.class, true, false);
			this.cachedAdvisorBeanNames = advisorNames;
		}
		if (advisorNames.length == 0) {
			return new ArrayList<>();
		}

		List<Advisor> advisors = new ArrayList<>();
		for (String name : advisorNames) {
			if (isEligibleBean(name)) {
				// 如果这个bean 正在被创建. 就跳过了. 
				if (this.beanFactory.isCurrentlyInCreation(name)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipping currently created advisor '" + name + "'");
					}
				}
				else {
					try {
						//将这些类初始化.
						advisors.add(this.beanFactory.getBean(name, Advisor.class));
					}
					catch (BeanCreationException ex) {
						Throwable rootCause = ex.getMostSpecificCause();
						if (rootCause instanceof BeanCurrentlyInCreationException) {
							BeanCreationException bce = (BeanCreationException) rootCause;
							String bceBeanName = bce.getBeanName();
							if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
								if (logger.isTraceEnabled()) {
									logger.trace("Skipping advisor '" + name +
											"' with dependency on currently created bean: " + ex.getMessage());
								}
								// Ignore: indicates a reference back to the bean we're trying to advise.
								// We want to find advisors other than the currently created bean itself.
								continue;
							}
						}
						throw ex;
					}
				}
			}
		}
		return advisors;
	}

	/**
	 * Determine whether the aspect bean with the given name is eligible.
	 * <p>The default implementation always returns {@code true}.
	 * @param beanName the name of the aspect bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

```

上述方法的执行完成. 会得到Object[]数组, 内容是所有的advisor. 通过advisor.getAdvice()方法可以获得增强器. 接下来就是构建代理.

#### createProxy构建代理

```java
//为指定的bean创建代理 beanClass(bean的Class) beanName(bean名称) specificInterceptors(拦截器也就是所有的增强器提供者) targetSource 被代理的类
protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
			@Nullable Object[] specificInterceptors, TargetSource targetSource) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
			//记录代理类的原始类. 
			AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);
		// 代理行为控制. 是生成代理类替换的形式.还是在方法前后增强的形式
		if (!proxyFactory.isProxyTargetClass()) {
			// 使用代理类的形式
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}
		//
		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}
		//生成代理类  委托给ProxyFactory
		return proxyFactory.getProxy(getProxyClassLoader());
	}
```


到这里我们基本上以上看到了AOP是如何做代理的. 如何兼容AspectJ的. 使用了AspectJ包的注解. 但是不需要额外的编译以及AspectJ的织入器. 还是通过CGLIB做的动态代理.


### CGLIB动态代理

CGLIB[Code Generation LIB] 是一个强大的高性能的代码生成包。它广泛应用于许多 AOP 框架。

#### 代理增强的结果

![截屏2019-10-1813.01.30.png](https://ten951-img.oss-cn-shanghai.aliyuncs.com/截屏2019-10-1813.01.30.png)

明显做到了在不更改业务代码的情况下对类的行为进行了增强


#### 代理创建

DefaultAopProxyFactory类和ObjenesisCglibAopProxy类. 属于工厂方法模式.


#### CglibAopProxy类是ObjenesisCglibAopProxy的父类
validateClassIfNecessary
```java
@Override
	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating CGLIB proxy: " + this.advised.getTargetSource());
		}

		try {
			// advised是AProxyCreatorSupport类.  获取目标类 需要被代理的类 也是上图中 TestServiceImpl
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");

			Class<?> proxySuperClass = rootClass;
			//判断是不是已经是被CGLIB代理过了呀. 名字中带$$表示已经被代理过了
			if (ClassUtils.isCglibProxyClass(rootClass)) {
				// 获得代理的父类
				proxySuperClass = rootClass.getSuperclass();
				// 获得所有候选接口
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				//添加新的代理接口
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}

			// Validate the class, writing log messages as necessary.
			//必要的验证 比如不能是final的
			validateClassIfNecessary(proxySuperClass, classLoader);

			// Configure CGLIB Enhancer...
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				if (classLoader instanceof SmartClassLoader &&
						((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
			//设置被代理类
			enhancer.setSuperclass(proxySuperClass);
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
			// 代理类名称生成策略
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));
			// 获取所有回调
			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class<?>[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			// fixedInterceptorMap only populated at this point, after getCallbacks call above
			enhancer.setCallbackFilter(new ProxyCallbackFilter(
					this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset));
			enhancer.setCallbackTypes(types);

			// Generate the proxy class and create a proxy instance.
			return createProxyClassAndInstance(enhancer, callbacks);
		}
		catch (CodeGenerationException | IllegalArgumentException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of " + this.advised.getTargetClass() +
					": Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (Throwable ex) {
			// TargetSource.getTarget() failed
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}


private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
		// Parameters used for optimization choices...
		boolean exposeProxy = this.advised.isExposeProxy();
		boolean isFrozen = this.advised.isFrozen();
		boolean isStatic = this.advised.getTargetSource().isStatic();

		// Choose an "aop" interceptor (used for AOP calls).
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

		// Choose a "straight to target" interceptor. (used for calls that are
		// unadvised but can return this). May be required to expose the proxy.
		Callback targetInterceptor;
		if (exposeProxy) {
			targetInterceptor = (isStatic ?
					new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource()));
		}
		else {
			targetInterceptor = (isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource()));
		}

		// Choose a "direct to target" dispatcher (used for
		// unadvised calls to static targets that cannot return this).
		Callback targetDispatcher = (isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp());
		// 这里设置拦截器的顺序和上边图片上的顺序是一致的.
		Callback[] mainCallbacks = new Callback[] {
				aopInterceptor,  // for normal advice
				targetInterceptor,  // invoke target without considering advice, if optimized
				new SerializableNoOp(),  // no override for methods mapped to this
				targetDispatcher, this.advisedDispatcher,
				new EqualsInterceptor(this.advised),
				new HashCodeInterceptor(this.advised)
		};

		Callback[] callbacks;

		// If the target is a static one and the advice chain is frozen,
		// then we can make some optimizations by sending the AOP calls
		// direct to the target using the fixed chain for that method.
		if (isStatic && isFrozen) {
			Method[] methods = rootClass.getMethods();
			Callback[] fixedCallbacks = new Callback[methods.length];
			this.fixedInterceptorMap = new HashMap<>(methods.length);

			// TODO: small memory optimization here (can skip creation for methods with no advice)
			for (int x = 0; x < methods.length; x++) {
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
				fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
						chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
				this.fixedInterceptorMap.put(methods[x].toString(), x);
			}

			// Now copy both the callbacks from mainCallbacks
			// and fixedCallbacks into the callbacks array.
			callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
			System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
			System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
			this.fixedInterceptorOffset = mainCallbacks.length;
		}
		else {
			callbacks = mainCallbacks;
		}
		return callbacks;
	}



```

代理类已经生成完毕. 

#### DynamicAdvisedInterceptor类

