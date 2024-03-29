# Part of OpenSPP. See LICENSE file for full copyright and licensing details.
import logging
import textwrap

from odoo import _, api, fields, models, tools

_logger = logging.getLogger(__name__)


class ResPartnerOIDC(models.Model):
    _name = "res.partner"
    _inherit = "res.partner"

    oidc_password = fields.Char(string="OIDC password", required=False)


class ResPartnerOIDC(models.Model):
    _name = "spp.partner.oidc"
    _auto = False

    id = fields.Many2one(comodel_name='res.partner', string='Partner')
    username = fields.Char(string="Username", required=False)
    full_name = fields.Char(string="Full Name", required=False)
    parent_id = fields.Integer(string="Parent ID", required=False)
    kind_id = fields.Many2one(comodel_name="g2p.group.kind", string="Kind ID")
    kind_name = fields.Char(string="Kind Name", required=False)
    group_membership_kind_id = fields.Char(comodel_name="g2p.group.membership.kind", string="Group Membership Kind ID")
    group_membership_kind_name = fields.Char(comodel_name="g2p.group.membership.kind", string="Group Membership Kind Name", required=False)
    group_membership_is_ended = fields.Boolean(string="Group Membership Is Ended", required=False)
    id_type_id = fields.Many2one(comodel_name="g2p.id.type", string="ID Type ID")
    id_type_name = fields.Char(string="ID Type Name", required=False,)
    id_type_value = fields.Char(string="ID Type Value", required=False,)
    company_name = fields.Integer(string="Company Name", required=False)
    is_group = fields.Boolean(string="Is Group", required=False)
    active_group = fields.Many2one(comodel_name="res_partner", required=False)
    title = fields.Char(string="Title", required=False)
    lang = fields.Char(string="Lang", required=False)
    tz = fields.Char(string="Time Zone", required=False)
    active = fields.Char(string="Active", required=False)
    email = fields.Char(string="Email", required=False)
    phone = fields.Char(string="Phone", required=False)
    password = fields.Char(string="OIDC Password", required=False)

    def init(self):
        """Initialize the sql view  """
        _logger.debug("v" * 80)
        _logger.debug("Creating OIDC view")
        _logger.debug("^" * 80)
        tools.drop_view_if_exists(self._cr, self._table)
        query = """
            CREATE VIEW %s AS
            SELECT
                p.id,
                p.name AS username,
                p.display_name AS full_name,
                '' AS first_name,
                '' AS last_name,
                p.is_group,
                p.active_group,
                p.parent_id,
                g2p_group_kind.id AS kind_id,
                g2p_group_kind.name AS kind_name,
                g2p_group_membership_kind.id AS group_membership_kind_id,
                g2p_group_membership_kind.name AS group_membership_kind_name,
                g2p_group_membership.is_ended AS group_membership_is_ended,
                g2p_id_type.id AS id_type_id,
                g2p_id_type.name AS id_type_name,
                g2p_reg_id.value AS id_type_value,
                p.company_name,
                p.title,
                p.lang,
                p.tz,
                p.active,
                p.email,
                g2p_phone_number.phone_no as phone,
                g2p_phone_number.phone_sanitized,
                p.oidc_password AS password
            FROM
                res_partner p
                LEFT JOIN g2p_group_kind ON
                    g2p_group_kind.id = p.kind
                LEFT JOIN g2p_reg_id ON
                    g2p_reg_id.partner_id = p.id
                LEFT JOIN g2p_id_type ON
                    g2p_id_type.id = g2p_reg_id.id_type
                LEFT JOIN g2p_phone_number ON
                    g2p_phone_number.partner_id = p.id
                LEFT JOIN g2p_group_membership ON
                    g2p_group_membership.individual = p.id
                LEFT JOIN g2p_group_membership_g2p_group_membership_kind_rel ON
                    g2p_group_membership_g2p_group_membership_kind_rel.g2p_group_membership_id = g2p_group_membership.id
                LEFT JOIN g2p_group_membership_kind ON
                    g2p_group_membership_kind.id = g2p_group_membership_g2p_group_membership_kind_rel.g2p_group_membership_kind_id
            WHERE
                p.is_company = false AND
                p.is_registrant = true
            ORDER BY id
        """ % self._table
        self._cr.execute(query)
