# Part of OpenSPP. See LICENSE file for full copyright and licensing details.


{
    "name": "OpenSPP Keycloak OIDC",
    "category": "OpenSPP",
    "version": "15.0.1.0.1",
    "sequence": 1,
    "author": "OpenSPP.org",
    "website": "https://github.com/openspp/OpenSPP/keycloak-registrant-module",
    "license": "LGPL-3",
    "development_status": "Production/Stable",
    "maintainers": ["uocnb"],
    "depends": [
        "base",
        "mail",
        "g2p_registry_base",
        "g2p_registry_individual",
        "g2p_registry_group",
        "g2p_registry_membership",
        "base_rest_auth_user_service",
    ],
    "external_dependencies": {"python": ["extendable-pydantic", "pydantic"]},
    "data": [
        "security/ir.model.access.csv",
    ],
    "assets": {},
    "demo": [],
    "images": [],
    "application": True,
    "installable": True,
    "auto_install": False,
}
