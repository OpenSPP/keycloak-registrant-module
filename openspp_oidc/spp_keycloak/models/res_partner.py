# Part of OpenSPP. See LICENSE file for full copyright and licensing details.
import logging
import textwrap

from odoo import _, fields, models
from odoo.exceptions import UserError

_logger = logging.getLogger(__name__)


class ResPartnerOIDC(models.Model):
    _name = 'res.partner'
    _inherit = 'res.partner'

    oidc_password = fields.Char(string='OIDC password', required=False)
