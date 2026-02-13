package org.example.e2e.fixture;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FavoriteRouteOrderE2eFixture {

    private static final long PASSENGER_USER_ID = 920201L;
    private static final long DRIVER_USER_ID = 920202L;
    private static final long DRIVER_ID = 920210L;

    private static final long HAPPY_FAVORITE_ROUTE_ID = 920301L;
    private static final long MISSING_FAVORITE_ROUTE_ID = 920399L;

    private static final String PASSENGER_EMAIL = "e2e-passenger-student1@taxi.app";
    private static final String DRIVER_EMAIL = "e2e-driver-student1@taxi.app";
    private static final String PASSENGER_PASSWORD = "e2e-pass-123";

    private static final String VEHICLE_LICENSE_PLATE = "NS-E2E-243";

    private static final String HAPPY_START_ADDRESS = "E2E 2.4.3 Start 1, Novi Sad";
    private static final String HAPPY_STOP_1_ADDRESS = "E2E 2.4.3 Stop 1, Novi Sad";
    private static final String HAPPY_STOP_2_ADDRESS = "E2E 2.4.3 Stop 2, Novi Sad";
    private static final String HAPPY_DESTINATION_ADDRESS = "E2E 2.4.3 End 9, Novi Sad";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private final String passengerPasswordHash;
    private final String driverPasswordHash;

    public static FavoriteRouteOrderE2eFixture fromSystemProperties() {
        String url = System.getProperty("e2e.db.url", "jdbc:postgresql://localhost:5432/ddn");
        String user = System.getProperty("e2e.db.user", "ddn");
        String password = System.getProperty("e2e.db.password", "ddn");
        return new FavoriteRouteOrderE2eFixture(url, user, password);
    }

    public FavoriteRouteOrderE2eFixture(String dbUrl, String dbUser, String dbPassword) {
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

    public String happyStartAddress() {
        return HAPPY_START_ADDRESS;
    }

    public String happyDestinationAddress() {
        return HAPPY_DESTINATION_ADDRESS;
    }

    public int happyCheckpointCount() {
        return 2;
    }

    public long prepareHappyPathScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            upsertDriverUser(connection);
            upsertDriver(connection);
            upsertVehicle(connection);
            upsertHappyFavoriteRoute(connection);
            return null;
        });
        return HAPPY_FAVORITE_ROUTE_ID;
    }

    public long prepareMissingFavoriteScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            return null;
        });
        return MISSING_FAVORITE_ROUTE_ID;
    }

    public void prepareEmptyFavoritesScenario() {
        cleanup();
        withConnection(connection -> {
            upsertPassengerUser(connection);
            return null;
        });
    }

    public int countFavoritesForPassenger() {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select count(1) from favorite_routes where user_id = ?")) {
                ps.setLong(1, PASSENGER_USER_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        });
    }

    public int countRidesForPassengerOnHappyRoute() {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("""
                    select count(distinct r.id)
                    from rides r
                    join ride_passengers rp on rp.ride_id = r.id
                    where lower(rp.email) = lower(?)
                      and r.start_address = ?
                      and r.destination_address = ?
                    """)) {
                ps.setString(1, PASSENGER_EMAIL);
                ps.setString(2, HAPPY_START_ADDRESS);
                ps.setString(3, HAPPY_DESTINATION_ADDRESS);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        });
    }

    public long latestRideIdForPassengerOnHappyRoute() {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("""
                    select coalesce(max(r.id), -1)
                    from rides r
                    join ride_passengers rp on rp.ride_id = r.id
                    where lower(rp.email) = lower(?)
                      and r.start_address = ?
                      and r.destination_address = ?
                    """)) {
                ps.setString(1, PASSENGER_EMAIL);
                ps.setString(2, HAPPY_START_ADDRESS);
                ps.setString(3, HAPPY_DESTINATION_ADDRESS);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        });
    }

    public String rideStatus(long rideId) {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select status from rides where id = ?")) {
                ps.setLong(1, rideId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "";
                    }
                    return rs.getString(1);
                }
            }
        });
    }

    public int countRideStops(long rideId) {
        return withConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "select count(1) from ride_stops where ride_id = ?")) {
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
            deleteTestRides(connection);
            deleteFavoriteRoutes(connection);
            deleteDriverAndVehicle(connection);
            deleteUsers(connection);
            return null;
        });
    }

    private void deleteTestRides(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                delete from rides
                where id in (
                    select distinct r.id
                    from rides r
                    left join ride_passengers rp on rp.ride_id = r.id
                    where lower(coalesce(rp.email, '')) = lower(?)
                       or (
                           r.start_address = ?
                           and r.destination_address = ?
                       )
                )
                """)) {
            ps.setString(1, PASSENGER_EMAIL);
            ps.setString(2, HAPPY_START_ADDRESS);
            ps.setString(3, HAPPY_DESTINATION_ADDRESS);
            ps.executeUpdate();
        }
    }

    private void deleteFavoriteRoutes(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "delete from favorite_routes where user_id = ? or id between 920300 and 920399")) {
            ps.setLong(1, PASSENGER_USER_ID);
            ps.executeUpdate();
        }
    }

    private void deleteDriverAndVehicle(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "delete from vehicles where driver_id = ? or license_plate = ?")) {
            ps.setLong(1, DRIVER_ID);
            ps.setString(2, VEHICLE_LICENSE_PLATE);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "delete from drivers where id = ? or user_id = ?")) {
            ps.setLong(1, DRIVER_ID);
            ps.setLong(2, DRIVER_USER_ID);
            ps.executeUpdate();
        }
    }

    private void deleteUsers(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "delete from users where id in (?, ?) or email in (?, ?)")) {
            ps.setLong(1, PASSENGER_USER_ID);
            ps.setLong(2, DRIVER_USER_ID);
            ps.setString(3, PASSENGER_EMAIL);
            ps.setString(4, DRIVER_EMAIL);
            ps.executeUpdate();
        }
    }

    private void upsertPassengerUser(Connection connection) throws SQLException {
        String sql = """
                insert into users (
                    id, role, email, password_hash, first_name, last_name, address, phone, is_active, blocked
                ) values (?, 'PASSENGER', ?, ?, 'E2E', 'Passenger1', 'Novi Sad', '+381600092201', true, false)
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
                    block_reason = null,
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
                ) values (?, 'DRIVER', ?, ?, 'E2E', 'Driver1', 'Novi Sad', '+381600092202', true, false)
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
                    block_reason = null,
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
                values (?, ?, true, 4.80, now())
                on conflict (id) do update
                set user_id = excluded.user_id,
                    available = true,
                    rating = excluded.rating
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, DRIVER_ID);
            ps.setLong(2, DRIVER_USER_ID);
            ps.executeUpdate();
        }
    }

    private void upsertVehicle(Connection connection) throws SQLException {
        String sql = """
                insert into vehicles (
                    driver_id, latitude, longitude, model, type, license_plate, seats, baby_transport, pet_transport
                ) values (?, ?, ?, 'Skoda Octavia', 'standard', ?, 4, true, true)
                on conflict (driver_id) do update
                set latitude = excluded.latitude,
                    longitude = excluded.longitude,
                    model = excluded.model,
                    type = excluded.type,
                    license_plate = excluded.license_plate,
                    seats = excluded.seats,
                    baby_transport = excluded.baby_transport,
                    pet_transport = excluded.pet_transport,
                    updated_at = now()
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, DRIVER_ID);
            ps.setDouble(2, 45.2672);
            ps.setDouble(3, 19.8338);
            ps.setString(4, VEHICLE_LICENSE_PLATE);
            ps.executeUpdate();
        }
    }

    private void upsertHappyFavoriteRoute(Connection connection) throws SQLException {
        String routeSql = """
                insert into favorite_routes (
                    id, user_id, name,
                    start_address, start_lat, start_lng,
                    destination_address, dest_lat, dest_lng
                ) values (?, ?, 'E2E 2.4.3 Happy Route', ?, ?, ?, ?, ?, ?)
                on conflict (id) do update
                set user_id = excluded.user_id,
                    name = excluded.name,
                    start_address = excluded.start_address,
                    start_lat = excluded.start_lat,
                    start_lng = excluded.start_lng,
                    destination_address = excluded.destination_address,
                    dest_lat = excluded.dest_lat,
                    dest_lng = excluded.dest_lng
                """;
        try (PreparedStatement ps = connection.prepareStatement(routeSql)) {
            ps.setLong(1, HAPPY_FAVORITE_ROUTE_ID);
            ps.setLong(2, PASSENGER_USER_ID);
            ps.setString(3, HAPPY_START_ADDRESS);
            ps.setDouble(4, 45.2671);
            ps.setDouble(5, 19.8335);
            ps.setString(6, HAPPY_DESTINATION_ADDRESS);
            ps.setDouble(7, 45.2712);
            ps.setDouble(8, 19.8464);
            ps.executeUpdate();
        }

        String stopSql = """
                insert into favorite_route_stops (favorite_route_id, stop_order, address, lat, lng)
                values (?, ?, ?, ?, ?)
                on conflict (favorite_route_id, stop_order) do update
                set address = excluded.address,
                    lat = excluded.lat,
                    lng = excluded.lng
                """;
        try (PreparedStatement ps = connection.prepareStatement(stopSql)) {
            ps.setLong(1, HAPPY_FAVORITE_ROUTE_ID);
            ps.setInt(2, 1);
            ps.setString(3, HAPPY_STOP_1_ADDRESS);
            ps.setDouble(4, 45.2684);
            ps.setDouble(5, 19.8361);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(stopSql)) {
            ps.setLong(1, HAPPY_FAVORITE_ROUTE_ID);
            ps.setInt(2, 2);
            ps.setString(3, HAPPY_STOP_2_ADDRESS);
            ps.setDouble(4, 45.2695);
            ps.setDouble(5, 19.8402);
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
