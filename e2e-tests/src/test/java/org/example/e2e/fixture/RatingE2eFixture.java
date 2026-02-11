package org.example.e2e.fixture;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class RatingE2eFixture {

    private static final long PASSENGER_USER_ID = 920001L;
    private static final long DRIVER_USER_ID = 920002L;
    private static final long DRIVER_ID = 920010L;

    private static final long HAPPY_RIDE_ID = 920101L;
    private static final long ALREADY_RATED_RIDE_ID = 920102L;
    private static final long EXPIRED_RIDE_ID = 920103L;

    private static final String PASSENGER_EMAIL = "e2e-passenger-student2@taxi.app";
    private static final String DRIVER_EMAIL = "e2e-driver-student2@taxi.app";
    private static final String PASSENGER_PASSWORD = "e2e-pass-123";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final String passengerPasswordHash;
    private final String driverPasswordHash;

    public static RatingE2eFixture fromSystemProperties() {
        String url = System.getProperty("e2e.db.url", "jdbc:postgresql://localhost:5432/ddn");
        String user = System.getProperty("e2e.db.user", "ddn");
        String password = System.getProperty("e2e.db.password", "ddn");
        return new RatingE2eFixture(url, user, password);
    }

    public RatingE2eFixture(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.passengerPasswordHash = "{bcrypt}" + encoder.encode(PASSENGER_PASSWORD);
        this.driverPasswordHash = "{bcrypt}" + encoder.encode(PASSENGER_PASSWORD);
    }

    public String passengerEmail() {
        return PASSENGER_EMAIL;
    }

    public String passengerPassword() {
        return PASSENGER_PASSWORD;
    }

    public long prepareHappyPathScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            upsertDriverUser(connection);
            upsertDriver(connection);

            OffsetDateTime endedAt = OffsetDateTime.now().minusHours(2);
            insertCompletedRide(connection, HAPPY_RIDE_ID, endedAt);
            insertRidePassenger(connection, HAPPY_RIDE_ID, PASSENGER_EMAIL, "E2E Passenger");
            return null;
        });
        return HAPPY_RIDE_ID;
    }

    public long prepareAlreadyRatedScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            upsertDriverUser(connection);
            upsertDriver(connection);

            OffsetDateTime endedAt = OffsetDateTime.now().minusHours(3);
            insertCompletedRide(connection, ALREADY_RATED_RIDE_ID, endedAt);
            insertRidePassenger(connection, ALREADY_RATED_RIDE_ID, PASSENGER_EMAIL, "E2E Passenger");
            insertRating(connection, ALREADY_RATED_RIDE_ID, 3, 4, "Existing fixture rating", endedAt.plusMinutes(10));
            return null;
        });
        return ALREADY_RATED_RIDE_ID;
    }

    public long prepareExpiredWindowScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            upsertDriverUser(connection);
            upsertDriver(connection);

            OffsetDateTime endedAt = OffsetDateTime.now().minusDays(4).minusHours(1);
            insertCompletedRide(connection, EXPIRED_RIDE_ID, endedAt);
            insertRidePassenger(connection, EXPIRED_RIDE_ID, PASSENGER_EMAIL, "E2E Passenger");
            return null;
        });
        return EXPIRED_RIDE_ID;
    }

    public int countRatingsForRide(long rideId) {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select count(1) from ride_ratings where ride_id = ?")) {
                ps.setLong(1, rideId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        });
    }

    public void cleanup() {
        withConnection(connection -> {
            deleteByRideRange(connection, "ride_ratings");
            deleteByRideRange(connection, "ride_reports");
            deleteByRideRange(connection, "ride_stops");
            deleteByRideRange(connection, "ride_passengers");
            deleteByRideRange(connection, "rides");

            try (PreparedStatement ps = connection.prepareStatement(
                    "delete from vehicles where driver_id = ?")) {
                ps.setLong(1, DRIVER_ID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "delete from drivers where id = ? or user_id = ?")) {
                ps.setLong(1, DRIVER_ID);
                ps.setLong(2, DRIVER_USER_ID);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "delete from users where id in (?, ?) or email in (?, ?)")) {
                ps.setLong(1, PASSENGER_USER_ID);
                ps.setLong(2, DRIVER_USER_ID);
                ps.setString(3, PASSENGER_EMAIL);
                ps.setString(4, DRIVER_EMAIL);
                ps.executeUpdate();
            }
            return null;
        });
    }

    private void deleteByRideRange(Connection connection, String tableName) throws SQLException {
        String sql = "delete from " + tableName + " where ride_id between 920100 and 920199";
        if ("rides".equals(tableName)) {
            sql = "delete from rides where id between 920100 and 920199";
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private void upsertPassengerUser(Connection connection) throws SQLException {
        String sql = """
                insert into users (
                    id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked
                ) values (?, 'PASSENGER', ?, ?, 'E2E', 'Passenger', 'Novi Sad', '+381600092001', true, false)
                on conflict (id) do update
                set role = excluded.role,
                    email = excluded.email,
                    password_hash = excluded.password_hash,
                    first_name = excluded.first_name,
                    last_name = excluded.last_name,
                    address = excluded.address,
                    phone = excluded.phone,
                    is_active = true,
                    blocked = false,
                    updated_at = now()
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, PASSENGER_USER_ID);
            ps.setString(2, PASSENGER_EMAIL);
            ps.setString(3, passengerPasswordHash);
            ps.executeUpdate();
        }
    }

    private void upsertDriverUser(Connection connection) throws SQLException {
        String sql = """
                insert into users (
                    id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked
                ) values (?, 'DRIVER', ?, ?, 'E2E', 'Driver', 'Novi Sad', '+381600092002', true, false)
                on conflict (id) do update
                set role = excluded.role,
                    email = excluded.email,
                    password_hash = excluded.password_hash,
                    first_name = excluded.first_name,
                    last_name = excluded.last_name,
                    address = excluded.address,
                    phone = excluded.phone,
                    is_active = true,
                    blocked = false,
                    updated_at = now()
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, DRIVER_USER_ID);
            ps.setString(2, DRIVER_EMAIL);
            ps.setString(3, driverPasswordHash);
            ps.executeUpdate();
        }
    }

    private void upsertDriver(Connection connection) throws SQLException {
        String sql = """
                insert into drivers (id, user_id, available, rating, created_at)
                values (?, ?, true, 5.00, now())
                on conflict (id) do update
                set user_id = excluded.user_id,
                    available = excluded.available,
                    rating = excluded.rating
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, DRIVER_ID);
            ps.setLong(2, DRIVER_USER_ID);
            ps.executeUpdate();
        }
    }

    private void insertCompletedRide(Connection connection, long rideId, OffsetDateTime endedAt) throws SQLException {
        String sql = """
                insert into rides (
                    id, driver_id, started_at, ended_at, start_address, destination_address, canceled, canceled_by,
                    status, price, panic_triggered, picked_up, start_lat, start_lng, dest_lat, dest_lng, car_lat,
                    car_lng, next_stop_index
                ) values (
                    ?, ?, ?, ?, 'Bulevar Evrope 1', 'Futoska 2', false, null, 'COMPLETED', 1180.00, false, true,
                    45.2520, 19.8330, 45.2600, 19.8450, 45.2600, 19.8450, 0
                )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, rideId);
            ps.setLong(2, DRIVER_ID);
            ps.setObject(3, endedAt.minusMinutes(20));
            ps.setObject(4, endedAt);
            ps.executeUpdate();
        }
    }

    private void insertRidePassenger(
            Connection connection,
            long rideId,
            String email,
            String name
    ) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into ride_passengers (ride_id, name, email) values (?, ?, ?)")) {
            ps.setLong(1, rideId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.executeUpdate();
        }
    }

    private void insertRating(
            Connection connection,
            long rideId,
            int driverRating,
            int vehicleRating,
            String comment,
            OffsetDateTime createdAt
    ) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                insert into ride_ratings (ride_id, driver_rating, vehicle_rating, comment, created_at)
                values (?, ?, ?, ?, ?)
                """)) {
            ps.setLong(1, rideId);
            ps.setInt(2, driverRating);
            ps.setInt(3, vehicleRating);
            ps.setString(4, comment);
            ps.setObject(5, createdAt);
            ps.executeUpdate();
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private <T> T withConnection(SqlFunction<T> fn) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(true);
            return fn.apply(connection);
        } catch (SQLException ex) {
            throw new RuntimeException("E2E fixture DB error", ex);
        }
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection connection) throws SQLException;
    }
}
