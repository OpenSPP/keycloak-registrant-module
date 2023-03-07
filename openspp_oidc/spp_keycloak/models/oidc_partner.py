# Part of OpenSPP. See LICENSE file for full copyright and licensing details.
import logging
import textwrap

from odoo import _, api, fields, models

_logger = logging.getLogger(__name__)


class OIDCPartner(models.Model):
    _name = 'res.partner'
    _inherit = 'res.partner'
    _description = 'OIDC Partner'

    oidc_password = fields.Char(string='OIDC password', required=False)
