# OpenSPP User Federation Provider (Keycloak User Storage Provider)

Providing user storage in a read-only mode, via an external PostgreSQL.

Features:
- Use JDBC driver to provide user data for keycloak via external database.
- Password hashing for authentication with hex hash digest algorithm.
- User's field in the database are automatically mapped as user's attribute in the client scope.
- Group and role mapping are using internal keycloak mapping.

## Usage

### Database setup

- Use sample.sql as sample data, import into existing database.

### Keycloak setup

- Install OpenSPP user federation provider by copy file from `dist` folder to keycloak's `providers` folder.
- Create or use existing realm.
- Login as admin.
- Go to User federation menu.
- Select OpenSPP user provider.
- Enter name, JDBC url, username, password, password hash algorithm.
- Save and it is ready to use.

## Development

```shell
# Compile and package
mvn compile package

# Or clean compile
mvn clean compile package
```

## Deploy

Copy all files from `dist` folder to keycloak's providers folder and run following command to build.

```shell
/opt/keycloak/bin/kc.sh build
```

## Docker development

```shell
# Create a new interactive keycloak container, replace PASSWORD with your
docker run -it --rm --name keycloak-dev -p 8080:8080 \
    -e KEYCLOAK_ADMIN=admin \
    -e KEYCLOAK_ADMIN_PASSWORD=PASSWORD \
    --entrypoint=/bin/bash quay.io/keycloak/keycloak:latest

# OS shell
# Copy OpenSPP user storage provider to keycloak's container
docker cp dist/. keycloak-dev:/opt/keycloak/providers/

# Keycloak container shell
/opt/keycloak/bin/kc.sh start-dev
```

To deploy new build you need to stop the keycloak server and copy new files from dist folder into `/opt/keycloak/providers/` and start the server again.
