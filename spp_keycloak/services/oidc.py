import logging

import json
from odoo.addons.base_rest import restapi
from odoo.addons.base_rest_pydantic.restapi import PydanticModel
from odoo.addons.component.core import Component
from odoo.addons.phone_validation.tools import phone_validation
from passlib.context import CryptContext

from ..models.oidc import (
    OIDCBaseIn,
    OIDCUpdatePhoneIn,
    OIDCUpdatePhoneOut,
    OIDCUpdatePasswordIn,
    OIDCUpdatePasswordOut,
    OIDCVerifyPasswordIn,
    OIDCVerifyPasswordOut,
)


_logger = logging.getLogger(__name__)
crypt_context = CryptContext(schemes=['pbkdf2_sha512'])


class OIDCApiService(Component):
    _inherit = "base.rest.service"
    _name = "oidc.rest.service"
    _usage = "oidc"
    _collection = "base.rest.keycloak.services"
    _description = """
        OIDC API Services
    """

    def _find_indvidual(self, info: OIDCBaseIn, updating_phone=False):
        domain = [
            ("group_membership_kind_name", "=", "Head"),
            ("id_type_name", "=", "Unified ID"),
            ("id_type_value", "=", info.uid_number),
        ]
        if not updating_phone and info.phone_number:
            domain.append(("phone", "=", info.phone_number))

        individual = self.env["spp.partner.oidc"].search(domain)

        if len(individual) > 0:
            return individual[0]

        return individual

    def _find_group(self, info: OIDCBaseIn):
        group = self.env["spp.partner.oidc"].search([
            ("username", "=", info.household_number),
        ])

        if len(group) > 0:
            return group[0]

        return group

    @restapi.method(
        [(["/phone"], "POST")],
        input_param=PydanticModel(OIDCUpdatePhoneIn),
        output_param=PydanticModel(OIDCUpdatePhoneOut),
        auth="jwt_oidc",
    )
    def changePhoneNumber(self, info: OIDCUpdatePhoneIn):
        """
        Change phone number in res.partner
        :param info: An instance of the OIDC info
        :return: An instance of OIDC info
        """
        logging.info("v" * 80)
        logging.info("Change Phone number")
        logging.info(
            f"Household={info.household_number} Unified ID={info.uid_number} Phone={info.phone_number}")

        group = self._find_group(info)
        individual = self._find_indvidual(info, updating_phone=True)

        phone_updated = False
        group_id = group.id if group else 0
        individual_id = individual.id if individual else 0

        if individual_id > 0 and group_id > 0:
            logging.info(f"Found group={group_id} individual={individual_id}")
            g2p_phone_number = self.env["g2p.phone.number"].search([
                ("partner_id", "=", individual_id)
            ])

            if not g2p_phone_number:
                g2p_phone_number = self.env["g2p.phone.number"].create({
                    "partner_id": individual_id,
                    "phone_no": info.phone_number,
                })
                phone_updated = True
            else:
                # TODO: allow update?
                phone_updated = g2p_phone_number[0].write(
                    {'phone_no': info.phone_number})
        else:
            logging.info("OIDC parameter is invalid.")

        res = OIDCUpdatePhoneOut(
            phone_updated=phone_updated,
            group_id=group_id,
            individual_id=individual_id,
        )
        logging.info("^" * 80)
        return res

    @restapi.method(
        [(["/password"], "POST")],
        input_param=PydanticModel(OIDCUpdatePasswordIn),
        output_param=PydanticModel(OIDCUpdatePasswordOut),
        auth="jwt_oidc",
    )
    def changePassword(self, info: OIDCUpdatePasswordIn):
        """
        Change OIDC password in res.partner
        :param info: An instance of the OIDC info
        :return: An instance of OIDC info
        """
        logging.info("v" * 80)
        logging.info("Change OIDC password")
        logging.info(
            f"Household={info.household_number} Unified ID={info.uid_number} Phone={info.phone_number} Password=***")

        group = self._find_group(info)
        individual = self._find_indvidual(info)

        password_updated = False
        group_id = group.id if group else 0
        individual_id = individual.id if individual else 0

        if individual_id > 0 and group_id > 0:
            logging.info(f"Found group={group_id} individual={individual_id}")
            partner = self.env["res.partner"].browse(individual_id)

            password_hash = crypt_context.hash(info.password)
            password_updated = partner.write({'oidc_password': password_hash})
        else:
            logging.info("OIDC parameter is invalid.")

        res = OIDCUpdatePasswordOut(
            password_updated=password_updated,
            group_id=group_id,
            individual_id=individual_id,
        )
        logging.info("^" * 80)
        return res

    @restapi.method(
        [(["/password/verify"], "POST")],
        input_param=PydanticModel(OIDCVerifyPasswordIn),
        output_param=PydanticModel(OIDCVerifyPasswordOut),
        auth="jwt_oidc",
    )
    def verifyPassword(self, info: OIDCVerifyPasswordIn):
        """
        Verify existing OIDC password in res.partner
        :param info: An instance of the OIDC info
        :return: An instance of OIDC info
        """
        logging.info("v" * 80)
        logging.info("Verify existing OIDC password")
        logging.info(
            f"Household={info.household_number} Unified ID={info.uid_number} Phone={info.phone_number} Password=***")

        group = self._find_group(info)
        individual = self._find_indvidual(info)

        password_hash_verified = False
        group_id = group.id if group else 0
        individual_id = individual.id if individual else 0

        if individual_id > 0 and group_id > 0 and info.password:
            logging.info(f"Found group={group_id} individual={individual_id}")

            password_hash_verified = crypt_context.verify(info.password, individual.password)
        else:
            logging.info("OIDC parameter is invalid.")

        res = OIDCVerifyPasswordOut(
            password_verified=password_hash_verified,
            group_id=group_id,
            individual_id=individual_id,
        )
        logging.info("^" * 80)
        return res
