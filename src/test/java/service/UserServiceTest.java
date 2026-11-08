package service;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Role;
import model.StandardRoom;
import model.User;

class UserServiceTest {

    private UserService users;
    private HotelService hotel;

    @BeforeEach
    void setUp(@TempDir Path dir) {
        Database.setUrl("jdbc:sqlite:" + dir.resolve("test.db"));
        Database.initialize();
        users = new UserService();
        hotel = new HotelService();
    }

    private int count(String table) throws Exception {
        try (Connection c = Database.connect();
                ResultSet rs = c.createStatement()
                        .executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void duplicateUsernameCannotRegister() {
        assertTrue(users.register("alice", "pw1"));
        assertFalse(users.register("alice", "pw2"));
    }

    @Test
    void loginWithCorrectPasswordReturnsUser() {
        users.register("alice", "pw1");
        User user = users.login("alice", "pw1");
        assertNotNull(user);
        assertEquals("alice", user.getUsername());
    }

    @Test
    void loginWithWrongPasswordReturnsNull() {
        users.register("alice", "pw1");
        assertNull(users.login("alice", "wrong"));
    }

    @Test
    void loginUnknownUserReturnsNull() {
        assertNull(users.login("nobody", "pw"));
    }

    @Test
    void defaultAdminCanLogin() {
        User admin = users.login("admin", "admin123");
        assertNotNull(admin);
        assertEquals(Role.ADMIN, admin.getRole());
    }

    @Test
    void passwordIsStoredAsHash() throws Exception {
        users.register("alice", "secret");
        try (Connection c = Database.connect();
                ResultSet rs = c.createStatement().executeQuery(
                        "SELECT password FROM users WHERE username = 'alice'")) {
            assertTrue(rs.next());
            String stored = rs.getString(1);
            assertNotEquals("secret", stored);
            assertTrue(stored.startsWith("$2"));
        }
    }

    @Test
    void deletingAccountAlsoRemovesItsBookings() throws Exception {
        users.register("alice", "pw1");
        User alice = users.login("alice", "pw1");
        hotel.addRoom(new StandardRoom(101, 50.0));
        hotel.bookRoom(101, alice,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));

        assertTrue(users.deleteAccount(alice));

        assertNull(users.login("alice", "pw1"));
        assertEquals(0, count("bookings"));
    }
}