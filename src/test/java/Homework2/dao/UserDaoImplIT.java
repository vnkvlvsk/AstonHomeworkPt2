package Homework2.dao;

import Homework2.entity.User;
import Homework2.exception.DaoException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserDaoImplIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static SessionFactory sessionFactory;

    UserDao userDao;

    @BeforeAll
    static void setUpSessionFactory() {
        sessionFactory = new Configuration()
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .setProperty("hibernate.connection.username", POSTGRES.getUsername())
                .setProperty("hibernate.connection.password", POSTGRES.getPassword())
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }

    @AfterAll
    static void tearDownSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createMutationQuery("delete from User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void create_persistsUserWithGeneratedIdAndTimestamp() {
        User created = userDao.create(new User("Ivan", "ivan@example.com", 30));

        assertNotNull(created.getId());
        assertEquals("Ivan", created.getName());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    void create_duplicateEmail_throwsDaoException() {
        userDao.create(new User("Ivan", "dup@example.com", 30));

        assertThrows(DaoException.class, () ->
                userDao.create(new User("Petr", "dup@example.com", 25)));
    }

    @Test
    void findById_existingUser_returnsUser() {
        User created = userDao.create(new User("Ivan", "ivan@example.com", 30));

        Optional<User> found = userDao.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals("Ivan", found.get().getName());
        assertEquals("ivan@example.com", found.get().getEmail());
    }

    @Test
    void findById_missingUser_returnsEmpty() {
        assertTrue(userDao.findById(999L).isEmpty());
    }

    @Test
    void findAll_noUsers_returnsEmptyList() {
        assertTrue(userDao.findAll().isEmpty());
    }

    @Test
    void findAll_returnsAllUsersOrderedById() {
        userDao.create(new User("A", "a@example.com", 20));
        userDao.create(new User("B", "b@example.com", 25));

        List<String> emails = userDao.findAll().stream().map(User::getEmail).toList();

        assertEquals(List.of("a@example.com", "b@example.com"), emails);
    }

    @Test
    void update_existingUser_persistsChanges() {
        User created = userDao.create(new User("Ivan", "ivan@example.com", 30));
        created.setName("Updated");
        created.setAge(31);

        User updated = userDao.update(created);

        assertEquals("Updated", updated.getName());
        assertEquals(31, updated.getAge());

        User reloaded = userDao.findById(created.getId()).orElseThrow();
        assertEquals("Updated", reloaded.getName());
        assertEquals(31, reloaded.getAge());
    }

    @Test
    void update_duplicateEmail_throwsDaoException() {
        userDao.create(new User("Ivan", "ivan@example.com", 30));
        User petr = userDao.create(new User("Petr", "petr@example.com", 25));

        petr.setEmail("ivan@example.com");

        assertThrows(DaoException.class, () -> userDao.update(petr));
    }

    @Test
    void delete_existingUser_removesItAndReturnsTrue() {
        User created = userDao.create(new User("Ivan", "ivan@example.com", 30));

        boolean deleted = userDao.delete(created.getId());

        assertTrue(deleted);
        assertTrue(userDao.findById(created.getId()).isEmpty());
    }

    @Test
    void delete_missingUser_returnsFalse() {
        assertFalse(userDao.delete(999L));
    }
}
