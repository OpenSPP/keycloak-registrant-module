# Part of OpenG2P Registry. See LICENSE file for full copyright and licensing details.
from odoo.addons.base_rest.controllers import main


class KeycloakOIDCApiController(main.RestController):
    _root_path = "/api/v1/keycloak/oidc/"
    _collection_name = "base.rest.keycloak.oidc.services"
    _default_auth = "user"
