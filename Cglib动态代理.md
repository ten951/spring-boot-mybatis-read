# Cglib动态代理

## 名词解释
  
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

## Spring基于Cglib的动态代理

作为spring唯一的ProxyFactory实现.DefaultAopProxyFactory类 (工厂方法模式)
```java
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {
    //AdvisedSupport 这个类的作用类似于mybatis的Configuration类. 配置类. 拥有一切你想要的关于代理的所有信息.
	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
	}

}
```

ObjenesisCglibAopProxy类是Cglib的AopProxy实现类.  其父类CglibAopProxy实现的getProxy()方法.
```java
@Override
class CglibAopProxy {

	public Object getProxy(@Nullable ClassLoader classLoader) {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating CGLIB proxy: " + this.advised.getTargetSource());
		}

		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");

			Class<?> proxySuperClass = rootClass;
			if (ClassUtils.isCglibProxyClass(rootClass)) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}

			// Validate the class, writing log messages as necessary.
			validateClassIfNecessary(proxySuperClass, classLoader);

			// Configure CGLIB Enhancer...
            //使用cglib库的enhancer，配置之后生成代理对象实例            
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				if (classLoader instanceof SmartClassLoader &&
						((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
            //被代理的类
			enhancer.setSuperclass(proxySuperClass);
            //设置增强器提供者
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
		    // 命名策略是类名中加$$
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));
		    //获取所有的callback，此时callback是cglib 的，getCallbacks中会把advisors封装成callback传入
			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class<?>[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			// fixedInterceptorMap only populated at this point, after getCallbacks call above
            // 加入是否需要进行callback的过滤器，根据filter的返回的int值，cglib会执行不同的callback，索引分别对应上面的callback数组的索引：
		    // 0:AOP_PROXY、1:INVOKE_TARGET、2:NO_OVERRIDE、3:DISPATCH_TARGET、4:DISPATCH_ADVISED、5:INVOKE_EQUALS、6:INVOKE_HASHCODE
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
}
```