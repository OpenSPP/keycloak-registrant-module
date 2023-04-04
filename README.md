# OpenSPP User Federation Provider (Keycloak User Storage Provider)

Providing user storage in a read-only mode, via an external PostgreSQL.

Features:
- Use JDBC driver to provide user data for keycloak via external database.
- Password hashing for authentication with hex hash digest algorithm.
- User's field in the database are automatically mapped as user's attribute in the client scope.
- Group and role mapping are using internal keycloak mapping.
- Beneficiary Authentication use multiple custom card numbers combined with phone number and password as credential.
- SMS OTP authentication with simulation mode and AWS SNS.
- Email OTP authentication.

## Usage

### Database setup

- Use sample.sql as sample data, import into existing database.

### Keycloak setup

#### User provider
1. Install OpenSPP user federation provider by copy file from `dist` folder to keycloak's `providers` folder.
2. Create or use existing realm.
3. Login as admin.
4. Go to User federation menu.
5. Select OpenSPP user provider.
6. Enter name, JDBC url, username, password.
7. Save and it is ready to use.
8. Verify: go to Users menu, show all users by using `*` as the search keyword.

#### Beneficiary Authentication
1. Create new Authentication flow
2. Add step
3. Search for Beneficiary Authentication and add to the flow
4. Select `Requirement` as `Require`
5. Create or update existing client
6. Go to Advanced tab and Select above authentication flow in the section `Authentication flow overrides`

#### SMS and Email OTP authentication
1. Add SMS or Email OTP as a new step in the authentication flow.
2. Select `Requirement` as `Require`
3. Click on the cog icon to open Settings dialog.
4. Enter alias.
5. SenderId: enter sender id to use as SMS sender.
5. Optional: turn on simulation mode to print the OTP code to console log instead of send SMS.
6. Optional: enter email in `Simulation email` to receive email with OTP code in simulation code.

Note: you need setup AWS SNS credential to send SMS. Check [this document](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-quickstart.html).

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
    --entrypoint=/bin/bash quay.io/keycloak/keycloak:21.0.1

# OS shell
# Copy OpenSPP user storage provider to keycloak's container
docker cp dist/. keycloak-dev:/opt/keycloak/providers/

# Keycloak container shell
/opt/keycloak/bin/kc.sh start-dev
```

To deploy new build you need to stop the keycloak server and copy new files from dist folder into `/opt/keycloak/providers/` and start the server again.
