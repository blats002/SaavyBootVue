DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'saavy_db') THEN
      CREATE DATABASE springboot_db;
END IF;

   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db') THEN
      CREATE DATABASE keycloak_db;
END IF;
END
