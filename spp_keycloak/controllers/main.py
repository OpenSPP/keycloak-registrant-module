# Part of OpenG2P Registry. See LICENSE file for full copyright and licensing details.
from odoo.addons.base_rest.controllers import main


class KeycloakOIDCApiController(main.RestController):
    _root_path = "/api/v1/keycloak/"
    _collection_name = "base.rest.keycloak.services"
    _default_auth = "user"
