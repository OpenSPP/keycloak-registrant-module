import logging

import json
from odoo.addons.base_rest import restapi
from odoo.addons.base_rest_pydantic.restapi import PydanticModel
from odoo.addons.component.core import Component
from passlib.context import CryptContext

from ..models.pds import (
    PDSBaseIn,
    PDSUpdatePhoneIn,
    PDSUpdatePhoneOut,
    PDSUpdatePaswordIn,
    PDSUpdatePasswordOut,
)


class PDSApiService(Component):
    _inherit = "base.rest.service"
    _name = "pds.rest.service"
    _usage = "pds"
    _collection = "base.rest.keycloak.oidc.services"
    _description = """
        PDS OIDC API Services
    """

    @restapi.method(
        [(["/phone"], "POST")],
        input_param=PydanticModel(PDSUpdatePhoneIn),
        output_param=PydanticModel(PDSUpdatePhoneOut),
        auth="jwt",
    )
    def changePhoneNumber(self, info: PDSUpdatePhoneIn):
        """
        Change phone number in res.partner
        :param info: An instance of the pds info
        :return: An instance of pds.info
        """
        logging.info("v" * 80)
        logging.info("Change Phone number")
        logging.info(
            f"Family={info.family_number} PDS={info.pds_number} Unified ID={info.uid_number} Phone={info.phone_number}")

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
            logging.info("PDS parameter is invalid.")

        res = PDSUpdatePhoneOut(
            phone_updated=phone_updated,
            group_id=group_id,
            individual_id=individual_id,
        )
        logging.info("^" * 80)
        return res

    @restapi.method(
        [(["/password"], "POST")],
        input_param=PydanticModel(PDSUpdatePaswordIn),
        output_param=PydanticModel(PDSUpdatePasswordOut),
        auth="jwt",
    )
    def changePassword(self, info: PDSUpdatePaswordIn):
        """
        Change OIDC password in res.partner
        :param info: An instance of the pds info
        :return: An instance of pds.info
        """
        logging.info("v" * 80)
        logging.info("Change OIDC password")
        logging.info(
            f"Family={info.family_number} PDS={info.pds_number} Unified ID={info.uid_number} Phone={info.phone_number} Password=***")

        group = self._find_group(info)
        individual = self._find_indvidual(info)

        password_updated = False
        group_id = group.id if group else 0
        individual_id = individual.id if individual else 0

        if individual_id > 0 and group_id > 0:
            logging.info(f"Found group={group_id} individual={individual_id}")
            partner = self.env["res.partner"].browse(individual_id)

            crypt_context = CryptContext(schemes=['pbkdf2_sha512'])
            password_hash = crypt_context.hash(info.password)
            password_updated = partner.write({'oidc_password': password_hash})
        else:
            logging.info("PDS parameter is invalid.")

        res = PDSUpdatePasswordOut(
            password_updated=password_updated,
            group_id=group_id,
            individual_id=individual_id,
        )
        logging.info("^" * 80)
        return res

    def _find_indvidual(self, info: PDSBaseIn, updating_phone=False):
        domain = [
            ("id_type_name", "=", "Unified ID"),
            ("id_type_value", "=", info.uid_number),
        ]
        if not updating_phone and info.phone_number:
            domain.append(("phone", "=", info.phone_number))

        individual = self.env["spp.partner.oidc"].search(domain)

        if len(individual) > 0:
            return individual[0]

        return individual

    def _find_group(self, info: PDSBaseIn):
        group = self.env["spp.partner.oidc"].search([
            ("id_type_name", "=", "PDS"),
            ("id_type_value", "=", info.pds_number),
            ("username", "=", info.family_number),
        ])

        if len(group) > 0:
            return group[0]

        return group
