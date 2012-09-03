package br.gov.framework.demoiselle.persistence;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import br.gov.framework.demoiselle.persistence.hibernate.PersistenceHibernateException;
import br.gov.framework.demoiselle.persistence.transaction.DefaultTransactionContextHandler;
import br.gov.framework.demoiselle.persistence.transaction.ITransactionContextHandler;
import br.gov.framework.demoiselle.util.config.ConfigurationException;
import br.gov.framework.demoiselle.util.config.ConfigurationLoader;

/**
 * A class based on Proxy design pattern intended to capture calls made to an EntityManager. The latter will be
 * encapsulated into the proxy instance.
 * 
 * @author CETEC/CTJEE
 * @see EntityManager
 * 
 *      * Observação: Esta classe foi redifinida para:
 * 
 *      1. Ser aderente a especificação JPA2 (demais métodos da interface EntityManager).
 * 
 *      2. Join da transação do EntityManager com JTA. (TODO: Avaliar a realização de proxy em Query e TypedQuery).
 * 
 *      4. Não foi utilizado a anotação @Override para os métodos do EntityManager, afim de ser utilizada tanto com JPA1 como JPA2.
 */
public class EntityManagerProxy implements EntityManager {


    private EntityManagerFactory emf;
    private String persistenceUnit;

    /**
     * Class constructor.
     * 
     * @param emf
     *            Entity Manager Factory
     * @param persistenceUnit
     *            Persistence Unit Name
     */
    public EntityManagerProxy(EntityManagerFactory emf, String persistenceUnit) {
        this.emf = emf;
        this.persistenceUnit = persistenceUnit;
    }

    /**
     * Retrieves the instance of EntityManager assigned to the current transaction context. If still not allocated, an
     * instance will be asked to the respective EntityManagerFactory.
     * 
     * @return an EntityManager
     */
    private EntityManager getEntityManager() {

        EntityManagerProxyConfig config = ConfigurationLoader.load(EntityManagerProxyConfig.class);
        String transactionContextHandlerClassName = config.getTransactionContextHandlerClass();

        ITransactionContextHandler transactionContextHandler;
        if (transactionContextHandlerClassName == null) {
            transactionContextHandler = new DefaultTransactionContextHandler();
        } else {
            Class<?> entityManagerResolverClass;
            try {
                entityManagerResolverClass = Class.forName(transactionContextHandlerClassName);
                transactionContextHandler = (ITransactionContextHandler) entityManagerResolverClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new PersistenceException("Error: Entity Manager Resolver class \""
                        + transactionContextHandlerClassName + "\" not found!", e);
            } catch (Exception e) {
                throw new PersistenceHibernateException("Error: Could not instantiate Entity Manager Resolver class \""
                        + transactionContextHandlerClassName + "\"", e);
            }
        }

        EntityManager em = transactionContextHandler.handler(persistenceUnit, emf);

        fillConfiguration(em);

        return em;
    }

    /**
     * Sets the attributes of Session with values loaded from @ EntityManagerConfig} .
     */
    private void fillConfiguration(EntityManager em) {
        EntityManagerProxyConfig configuration = null;
        try {
            configuration = ConfigurationLoader.load(EntityManagerProxyConfig.class);
        } catch (ConfigurationException ce) {
        }
        if (configuration != null) {
            if (!isEmpty(configuration.getFlushMode())) {
                em.setFlushMode(FlushModeType.valueOf(configuration.getFlushMode()));
            }
        }
    }

    /**
     * A string is null if its value is equal to null or its length is equal to zero.
     * 
     * @param string
     * @return
     */
    private boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }

    /**
     * Opens an user transaction if still not open.
     */
    protected void startTransaction() {

        EntityTransaction tx = getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
    }

    /**
     * Attemp to join transaction, if the active transaction is not managed by current EntityManager.
     */
    protected final void joinTransactionIfNecessary() {
        try {
            getEntityManager().getTransaction();
        } catch (IllegalStateException cause) {
            // IllegalStateException is launched if we are on a JTA entity manager, so
            // we assume we need to join transaction instead of creating one.

            try {
                getEntityManager().joinTransaction();
            } catch (TransactionRequiredException te) {
                // It get's launched if there is no JTA transaction opened. It usually means we are
                // being launched inside a method not marked with @Transactional so we ignore the exception.
            }
        }
    }

    private void startOrJoinTransactionIfNecessary() {
        try {
            getEntityManager().getTransaction();
            startTransaction();
        } catch (IllegalStateException cause) {
            // IllegalStateException is launched if we are on a JTA entity manager, so
            // we assume we need to join transaction instead of creating one.

            try {
                getEntityManager().joinTransaction();
            } catch (TransactionRequiredException te) {
                // It get's launched if there is no JTA transaction opened. It usually means we are
                // being launched inside a method not marked with @Transactional so we ignore the exception.
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#persist(java.lang.Object)
     */
    public void persist(Object entity) {
        startOrJoinTransactionIfNecessary();
        getEntityManager().persist(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#merge(java.lang.Object)
     */
    public <T> T merge(T entity) {
        startOrJoinTransactionIfNecessary();
        return getEntityManager().merge(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#remove(java.lang.Object)
     */
    public void remove(Object entity) {
        startOrJoinTransactionIfNecessary();
        getEntityManager().remove(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        joinTransactionIfNecessary();
        return getEntityManager().find(entityClass, primaryKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        joinTransactionIfNecessary();
        return getEntityManager().find(entityClass, primaryKey, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        joinTransactionIfNecessary();
        return getEntityManager().find(entityClass, primaryKey, lockMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType,
     * java.util.Map)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        joinTransactionIfNecessary();
        return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getReference(java.lang.Class, java.lang.Object)
     */
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        joinTransactionIfNecessary();
        return getEntityManager().getReference(entityClass, primaryKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#flush()
     */
    public void flush() {
        getEntityManager().flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#setFlushMode(javax.persistence.FlushModeType)
     */
    public void setFlushMode(FlushModeType flushMode) {
        getEntityManager().setFlushMode(flushMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getFlushMode()
     */
    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType)
     */
    public void lock(Object entity, LockModeType lockMode) {
        joinTransactionIfNecessary();
        getEntityManager().lock(entity, lockMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        joinTransactionIfNecessary();
        getEntityManager().lock(entity, lockMode, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#refresh(java.lang.Object)
     */
    public void refresh(Object entity) {
        joinTransactionIfNecessary();
        getEntityManager().refresh(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
     */
    public void refresh(Object entity, Map<String, Object> properties) {
        joinTransactionIfNecessary();
        getEntityManager().refresh(entity, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType)
     */
    public void refresh(Object entity, LockModeType lockMode) {
        joinTransactionIfNecessary();
        getEntityManager().refresh(entity, lockMode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        joinTransactionIfNecessary();
        getEntityManager().refresh(entity, lockMode, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#clear()
     */
    public void clear() {
        getEntityManager().clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#detach(java.lang.Object)
     */
    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#contains(java.lang.Object)
     */
    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getLockMode(java.lang.Object)
     */
    public LockModeType getLockMode(Object entity) {
        joinTransactionIfNecessary();
        return getEntityManager().getLockMode(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String propertyName, Object value) {
        getEntityManager().setProperty(propertyName, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getProperties()
     */
    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createQuery(java.lang.String)
     */
    public Query createQuery(String qlString) {
        return getEntityManager().createQuery(qlString);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createQuery(javax.persistence.criteria.CriteriaQuery)
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getEntityManager().createQuery(criteriaQuery);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return getEntityManager().createQuery(qlString, resultClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createNamedQuery(java.lang.String)
     */
    public Query createNamedQuery(String name) {
        return getEntityManager().createNamedQuery(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createNamedQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return getEntityManager().createNamedQuery(name, resultClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String)
     */
    public Query createNativeQuery(String sqlString) {
        return getEntityManager().createNativeQuery(sqlString);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
	public Query createNativeQuery(String sqlString, Class resultClass) {
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.String)
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#joinTransaction()
     */
    public void joinTransaction() {
        getEntityManager().joinTransaction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getDelegate()
     */
    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#close()
     */
    public void close() {
        getEntityManager().close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#isOpen()
     */
    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getTransaction()
     */
    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getEntityManagerFactory()
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getCriteriaBuilder()
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.persistence.EntityManager#getMetamodel()
     */
    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        return getEntityManager().equals(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getEntityManager().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getEntityManager().toString();
    }

}