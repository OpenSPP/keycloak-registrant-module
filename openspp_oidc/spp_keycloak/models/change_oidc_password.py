# Part of OpenSPP. See LICENSE file for full copyright and licensing details.
import logging
import textwrap
import base64

from odoo import _, api, fields, models, Command
from odoo.exceptions import UserError

from passlib.context import CryptContext

_logger = logging.getLogger(__name__)


class ChangeOIDCPassword(models.TransientModel):
    """ A model to configure partner in the change OIDC password wizard. """
    _name = 'spp.change.oidc.password'
    _description = 'Change Partner OIDC Password'

    wizard_id = fields.Many2one('spp.change.oidc.password.wizard', string='Wizard', required=True, ondelete='cascade')
    partner_id = fields.Many2one('res.partner', string='Partner', required=True, readonly=True, ondelete='cascade')
    login = fields.Char(string='Name', readonly=True)
    new_passwd = fields.Char(string='New OIDC Password', default='')

    def change_oidc_password(self):
        crypt_context = CryptContext(schemes=['pbkdf2_sha512'])
        for line in self:
            if not line.new_passwd:
                raise UserError(_("Before clicking on 'Change Partner OIDC Password', you have to write a new password."))
            password_hash = crypt_context.hash(line.new_passwd)
            line.partner_id.write({'oidc_password': password_hash})
        # don't keep temporary passwords in the database longer than necessary
        self.write({'new_passwd': False})


class ChangeOIDCPasswordWizard(models.TransientModel):
    """ A wizard to manage the change of partner's OIDC passwords. """
    _name = "spp.change.oidc.password.wizard"
    _description = "Change Partner OIDC Password Wizard"

    def _default_partner_oidc_user_ids(self):
        partner_ids = self.env.context.get('default_partner_ids', []) or self.env.context.get('active_ids', [])
        return [
            Command.create({'partner_id': partner.id, 'login': partner.name})
            for partner in self.env['res.partner'].browse(partner_ids)
        ]

    partner_oidc_user_ids = fields.One2many('spp.change.oidc.password', 'wizard_id', string='Users', default=_default_partner_oidc_user_ids)

    def change_oidc_password(self):
        self.ensure_one()
        
        self.partner_oidc_user_ids.change_oidc_password()

        if hasattr(self.env, 'partner') and self.env.partner in self.partner_oidc_user_ids.partner_id:
            return {'type': 'ir.actions.client', 'tag': 'reload'}
        return {'type': 'ir.actions.act_window_close'}

    def open_wizard(self):
        # FIXME: active_ids cleared after error raised.
        _logger.info("Registrant IDs: %s" % self.env.context.get("active_ids"))
    #     return self.create({})._open_wizard()

    # def _open_wizard(self):
    #     self.refresh()
        return {
            'name': _('Change OIDC Password'),
            'type': 'ir.actions.act_window',
            'res_model': 'spp.change.oidc.password.wizard',
            'view_type': 'form',
            'view_mode': 'form',
            # 'res_id': self.id,
            "view_id": self.env.ref(
                "spp_keycloak.change_oidc_password_wizard_form_view"
            ).id,
            'target': 'new',
            'context': self._context,
        }
