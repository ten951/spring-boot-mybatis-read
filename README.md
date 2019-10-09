# spring-boot-mybatis-read

[Spring MVC](MVC.md)

[Spring 事务](事务.md)

## 自动加载

![20190823100135.png](https://ten951-img.oss-cn-shanghai.aliyuncs.com/20190823100135.png)

在spring.factories文件中:
```yaml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration,\
org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
```
这些类是在项目启动时. spring的注解驱动帮我们自动加载的.
### MybatisAutoConfiguration(Mybatis自动加载类)
```java
@org.springframework.context.annotation.Configuration
//SqlSessionFactoryBean是mybatis-spring包的类. SqlSessionFactory是mybatis包的类. 也就是说必须依赖和两个包
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
//必须存在一个首选的DataSource Bean
@ConditionalOnSingleCandidate(DataSource.class)
//开启或者读取mybatis开头的配置
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter({ DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class })
public class MybatisAutoConfiguration implements InitializingBean {
}
```
这个自动装配类. 在我自己看来干了这么几件事:
#### @EnableConfigurationProperties(MybatisProperties.class) 读取mybatis开头的配置项
#### SqlSessionFactory的初始化
```java
public class MybatisAutoConfiguration implements InitializingBean {

  @Bean
//当缺少SqlSessionFactory的时候初始化
  @ConditionalOnMissingBean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    factory.setVfs(SpringBootVFS.class);
    // 设置mybatis-config.xml的
    if (StringUtils.hasText(this.properties.getConfigLocation())) {
      factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
    }
    //初始化Configuration类.
    applyConfiguration(factory);
    if (this.properties.getConfigurationProperties() != null) {
      factory.setConfigurationProperties(this.properties.getConfigurationProperties());
    }
    if (!ObjectUtils.isEmpty(this.interceptors)) {
      factory.setPlugins(this.interceptors);
    }
    if (this.databaseIdProvider != null) {
      factory.setDatabaseIdProvider(this.databaseIdProvider);
    }
    if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
      factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
    }
    if (this.properties.getTypeAliasesSuperType() != null) {
      factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
    }
    if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
      factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
    }
    if (!ObjectUtils.isEmpty(this.typeHandlers)) {
      factory.setTypeHandlers(this.typeHandlers);
    }
// 设置mapper.xml的
    if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
      factory.setMapperLocations(this.properties.resolveMapperLocations());
    }
    Set<String> factoryPropertyNames = Stream
        .of(new BeanWrapperImpl(SqlSessionFactoryBean.class).getPropertyDescriptors()).map(PropertyDescriptor::getName)
        .collect(Collectors.toSet());
    Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
    if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !ObjectUtils.isEmpty(this.languageDrivers)) {
      // Need to mybatis-spring 2.0.2+
      factory.setScriptingLanguageDrivers(this.languageDrivers);
      if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {
        defaultLanguageDriver = this.languageDrivers[0].getClass();
      }
    }
    if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
      // Need to mybatis-spring 2.0.2+
      factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
    }

    return factory.getObject();
  }
}
```
SqlSessionFactoryBean->getObject()->afterPropertiesSet()->buildSqlSessionFactory()->new DefaultSqlSessionFactory(config);
SqlSessionFactoryBean->buildSqlSessionFactory()方法是构建SqlSessionFactory的核心方法.
这个方法主要干两件事情:
1. XMLConfigBuilder构建和解析(parse)
2. XMLMapperBuilder构建和解析(parse)

##### XMLConfigBuilder构建和解析(parse方法)

```java
public class XMLConfigBuilder extends BaseBuilder {
  public Configuration parse() {
    if (parsed) {
      throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    parsed = true;
    parseConfiguration(parser.evalNode("/configuration"));
    return configuration;
  }

  private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      propertiesElement(root.evalNode("properties"));
      Properties settings = settingsAsProperties(root.evalNode("settings"));
      loadCustomVfs(settings);
      loadCustomLogImpl(settings);
      typeAliasesElement(root.evalNode("typeAliases"));
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectorFactoryElement(root.evalNode("reflectorFactory"));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }
}
```
这些元素都很熟悉. 背后是在设置Configuration类.TypeAliasRegistry类和TypeHandlerRegistry类
##### XMLMapperBuilder(Mapper文件解析器)
```java
public class XMLMapperBuilder extends BaseBuilder {

  private final XPathParser parser;
//解析mapper的协助类
  private final MapperBuilderAssistant builderAssistant;
  private final Map<String, XNode> sqlFragments;
  private final String resource;

public void parse() {
    if (!configuration.isResourceLoaded(resource)) {
//解析mapper.xml的内容
      configurationElement(parser.evalNode("/mapper"));
      configuration.addLoadedResource(resource);
//通过mapper.xml的namespace属性.拿到mapper接口. 并通过Configuration类提供的MapperRegistry注册器将这个mapper接口注册knownMappers集合中,
//在加入集合之前. 解析mapper接口中所使用的注解. 另外knownMappers是个HashMap. key为mapper接口的全限定名.value 是new MapperProxyFactory<>(type)
//是mapper接口的代理生成工厂.
//下面这段代码就是spring ioc在注入时会容器帮我们生成mapper代理时最终调用的代码
    /*  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
          throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
          return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
          throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
      }*/
      bindMapperForNamespace();
    }
    //补偿机制 resultMap
    parsePendingResultMaps();
    //补偿机制 cache-ref
    parsePendingCacheRefs();
    //补偿机制 select|insert|update|delete
    parsePendingStatements();
  }
private void configurationElement(XNode context) {
    try {
      //获取命名空间
      String namespace = context.getStringAttribute("namespace");
      if (namespace == null || namespace.equals("")) {
        throw new BuilderException("Mapper's namespace cannot be empty");
      }
      //
      builderAssistant.setCurrentNamespace(namespace);
//二级缓存相关的
      cacheRefElement(context.evalNode("cache-ref"));
      cacheElement(context.evalNode("cache"));
//解析parameterMap
      parameterMapElement(context.evalNodes("/mapper/parameterMap"));
//解析resultMap
      resultMapElements(context.evalNodes("/mapper/resultMap"));
//解析sql片段
      sqlElement(context.evalNodes("/mapper/sql"));
//解析sql语句
      buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
    }
  }
}

```
SqlSessionFactoryBean.getObject(); 执行的时候,mybatis的config解析完成, mapper.xml解析完成, mapper接口的解析, mapper接口代理工厂的设置.都已经完成了

#### AutoConfiguredMapperScannerRegistrar

扫描Mapper类的扫描器. (这个类只会Spring boot相同的基本包) 如果使用完整的功能要使用(@MapperScan)

这个类两个主要的点:

>builder.addPropertyValue("annotationClass", Mapper.class);  扫描了标记Mapper注解(这个注解是没有@Component元注解的. 也就是spring在自动装载阶段不管的.只能自己mybatis自己来)
>registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition()); MapperScannerConfigurer Mapper扫描器
#### MapperScannerConfigurer
这个类实现了BeanDefinitionRegistryPostProcessor接口. 所以在spring ioc启动的时候会执行postProcessBeanDefinitionRegistry()方法:
```java
public class MapperScannerConfigurer 
                implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
 @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    if (this.processPropertyPlaceHolders) {
      processPropertyPlaceHolders();
    }

    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    scanner.setAddToConfig(this.addToConfig);
    scanner.setAnnotationClass(this.annotationClass);
    scanner.setMarkerInterface(this.markerInterface);
    scanner.setSqlSessionFactory(this.sqlSessionFactory);
    scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
    scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
    scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
    scanner.setResourceLoader(this.applicationContext);
    scanner.setBeanNameGenerator(this.nameGenerator);
    scanner.setMapperFactoryBeanClass(this.mapperFactoryBeanClass);
    if (StringUtils.hasText(lazyInitialization)) {
      scanner.setLazyInitialization(Boolean.valueOf(lazyInitialization));
    }
    scanner.registerFilters();
    scanner.scan(
        StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
  }
}
```
ClassPathMapperScanner#scan() 方法就是扫描@MapperScan(basePackages = "com.ten951.boot.mybatis.read.mapper")这里指定的包下的mapper接口的. 并注册.

#### SqlSessionTemplate初始化 
初始化目前发现2中方式. 
第一种是在自动装配阶段通过@Bean加载的. 这个是先执行的 因为本质是BeanFactoryPostProcessor
第二种是BeanPostProcessor执行的时候,AutowiredAnnotationBeanPostProcessor注入的时候创建依赖Bean,在populateBean()的时候. 填充属性new的.
但不管那种方式 构造方法是必须执行的.
```java
public class SqlSessionTemplate implements SqlSession, DisposableBean {
  public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
      PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
    notNull(executorType, "Property 'executorType' is required");

    this.sqlSessionFactory = sqlSessionFactory;
    this.executorType = executorType;
    this.exceptionTranslator = exceptionTranslator;
//创建sqlSession的代理.
    this.sqlSessionProxy = (SqlSession) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
        new Class[] { SqlSession.class }, new SqlSessionInterceptor());
  }
}

```


### Mapper执行的时候


生成代理的方式有两种. 1是JDK的 2是Cglib. mybatis的sqlSessionProxy和mapperProxy都是通过JDK实现的.
mapper接口的是代理类, 并且是MapperProxyFactory类型. 当spring启动生成bean时. 通过调用MapperFactoryBean.getObject()方法. 最后必然会
执行
```text
 public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
```
当执行mapper接口的方法时. MapperProxy.invoke()方法就会执行.

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (method.isDefault()) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
//这里的sqlSession其实是子类SqlSessionTemplate, SqlSessionTemplate持有SqlSession的代理类.通过代理去执行.
    return mapperMethod.execute(sqlSession, args);
  }

  private MapperMethod cachedMapperMethod(Method method) {
    return methodCache.computeIfAbsent(method, k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
  }

  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
      throws Throwable {
    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
        .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    return constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
  }
}

// sqlSession代理类执行逻辑. 
private class SqlSessionInterceptor implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      //并且通过SqlSessionFactory openSession, 完成了执行器 事务管理器 和 sqlSession的绑定.
      SqlSession sqlSession = getSqlSession(SqlSessionTemplate.this.sqlSessionFactory,
          SqlSessionTemplate.this.executorType, SqlSessionTemplate.this.exceptionTranslator);
      try {
        Object result = method.invoke(sqlSession, args);
        //是否是spring管理的事务. 不是话就用sqlSession绑定执行器绑定的事务.
        if (!isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
          // force commit even on non-dirty sessions because some databases require
          // a commit/rollback before calling close()
          sqlSession.commit(true);
        }
        return result;
      } catch (Throwable t) {
        Throwable unwrapped = unwrapThrowable(t);
        if (SqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
          // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
            //关闭sqlSession
          closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
          sqlSession = null;
          Throwable translated = SqlSessionTemplate.this.exceptionTranslator
              .translateExceptionIfPossible((PersistenceException) unwrapped);
          if (translated != null) {
            unwrapped = translated;
          }
        }
        throw unwrapped;
      } finally {
        if (sqlSession != null) {
            //关闭sqlSession
          closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
        }
      }
    }
  }

```
```textmate

  public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType,
      PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
    notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);

    SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

    SqlSession session = sessionHolder(executorType, holder);
    if (session != null) {
      return session;
    }

    LOGGER.debug(() -> "Creating a new SqlSession");
    //打开SqlSession. 完成执行器 事务 SqlSession的绑定
    session = sessionFactory.openSession(executorType);
    // 注册spring的事务.
    registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);

    return session;
  }

  private static void registerSessionHolder(SqlSessionFactory sessionFactory, ExecutorType executorType,
      PersistenceExceptionTranslator exceptionTranslator, SqlSession session) {
    SqlSessionHolder holder;
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      Environment environment = sessionFactory.getConfiguration().getEnvironment();

      if (environment.getTransactionFactory() instanceof SpringManagedTransactionFactory) {
        LOGGER.debug(() -> "Registering transaction synchronization for SqlSession [" + session + "]");
        //资源持有者. 资源指的是SQLSession
        holder = new SqlSessionHolder(session, executorType, exceptionTranslator);
        //绑定
        TransactionSynchronizationManager.bindResource(sessionFactory, holder);
        //注册SqlSessionSynchronization会话管理器. 线程私有的. 所以线程安全
        TransactionSynchronizationManager
            .registerSynchronization(new SqlSessionSynchronization(holder, sessionFactory));
        holder.setSynchronizedWithTransaction(true);
        holder.requested();
      } else {
        if (TransactionSynchronizationManager.getResource(environment.getDataSource()) == null) {
          LOGGER.debug(() -> "SqlSession [" + session
              + "] was not registered for synchronization because DataSource is not transactional");
        } else {
          throw new TransientDataAccessResourceException(
              "SqlSessionFactory must be using a SpringManagedTransactionFactory in order to use Spring transaction synchronization");
        }
      }
    } else {
      LOGGER.debug(() -> "SqlSession [" + session
          + "] was not registered for synchronization because synchronization is not active");
    }

  }

```
最终在DefaultSqlSessionFactory类中 找打了下面代码, 完成了执行器 事务管理器 和 sqlSession的绑定.
```text
  private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
      final Environment environment = configuration.getEnvironment();
//获得事务工厂
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
//开启事务
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
//生成执行器
      final Executor executor = configuration.newExecutor(tx, execType);
//生成DefaultSqlSession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeTransaction(tx); // may have fetched a connection so lets call close()
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }
```


[事务.md]: 事务.md