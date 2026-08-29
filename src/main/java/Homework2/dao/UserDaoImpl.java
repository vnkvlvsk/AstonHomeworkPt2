package Homework2.dao;

import Homework2.entity.User;
import Homework2.exception.DaoException;
import org.hibernate.*;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User create(User user) throws DaoException{
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            log.info("Создан пользователь id={}, email={}", user.getId(), user.getEmail());
            return user;
        } catch (ConstraintViolationException e) {
            rollback(tx);
            log.warn("Нарушение ограничения БД при создании пользователя {}: {}", user.getEmail(), e.getMessage());
            throw new DaoException("Пользователь с email '" + user.getEmail() + "' уже существует", e);
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Не удалось создать пользователя {}", user.getEmail(), e);
            throw new DaoException("Не удалось создать пользователя: " + rootMessage(e), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DaoException {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.find(User.class, id));
        } catch (HibernateException e) {
            log.error("Не удалось загрузить пользователя id={}", id, e);
            throw new DaoException("Не удалось найти пользователя: " + rootMessage(e), e);
        }
    }

    @Override
    public List<User> findAll() throws DaoException {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User order by id", User.class).list();
        } catch (HibernateException e) {
            log.error("Не удалось загрузить список пользователей", e);
            throw new DaoException("Не удалось получить список пользователей: " + rootMessage(e), e);
        }
    }

    @Override
    public User update(User user) throws DaoException {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            User existing = session.find(User.class, user.getId());
            if (existing == null) {
                tx.rollback();
                throw new DaoException("Пользователь с id=" + user.getId() + " не найден");
            }
            existing.setName(user.getName());
            existing.setEmail(user.getEmail());
            existing.setAge(user.getAge());
            tx.commit();
            log.info("Обновлён пользователь id={}", existing.getId());
            return existing;
        } catch (ConstraintViolationException e) {
            rollback(tx);
            log.warn("Нарушение ограничения БД при обновлении пользователя id={}: {}", user.getId(), e.getMessage());
            throw new DaoException("Email '" + user.getEmail() + "' уже занят другим пользователем", e);
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Не удалось обновить пользователя id={}", user.getId(), e);
            throw new DaoException("Не удалось обновить пользователя: " + rootMessage(e), e);
        }
    }

    @Override
    public boolean delete(Long id) throws DaoException {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            User existing = session.find(User.class, id);
            if (existing == null) {
                tx.rollback();
                return false;
            }
            session.remove(existing);
            tx.commit();
            log.info("Удалён пользователь id={}", id);
            return true;
        } catch (HibernateException e) {
            rollback(tx);
            log.error("Не удалось удалить пользователя id={}", id, e);
            throw new DaoException("Не удалось удалить пользователя: " + rootMessage(e), e);
        }
    }

    private void rollback(Transaction tx) {
        if (tx == null || !tx.isActive()) {
            return;
        }
        try {
            tx.rollback();
        } catch (RuntimeException rollbackEx) {
            log.warn("Не удалось выполнить откат транзакции", rollbackEx);
        }
    }

    private String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}