DO $$
DECLARE
max_id bigint;
BEGIN

SELECT max(id) INTO max_id FROM users;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('users', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('users', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM drivers;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('drivers', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('drivers', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM rides;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('rides', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('rides', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM vehicles;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('vehicles', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('vehicles', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM ride_stops;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('ride_stops', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('ride_stops', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM ride_passengers;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('ride_passengers', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('ride_passengers', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM ride_reports;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('ride_reports', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('ride_reports', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM driver_profile_change_requests;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('driver_profile_change_requests', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('driver_profile_change_requests', 'id'), max_id, true);
END IF;

SELECT max(id) INTO max_id FROM driver_activation_tokens;
IF max_id IS NULL THEN
        PERFORM setval(pg_get_serial_sequence('driver_activation_tokens', 'id'), 1, false);
ELSE
        PERFORM setval(pg_get_serial_sequence('driver_activation_tokens', 'id'), max_id, true);
END IF;
END $$;
