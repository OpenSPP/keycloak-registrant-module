# Part of OpenSPP. See LICENSE file for full copyright and licensing details.
import logging
import textwrap
import base64

from odoo import _, api, fields, models, tools, SUPERUSER_ID, _, Command
from odoo.exceptions import AccessDenied, AccessError, UserError, ValidationError

from passlib.context import CryptContext

_logger = logging.getLogger(__name__)


class ChangeOIDCPasswordUser(models.TransientModel):
    """ A model to configure users in the change OIDC password wizard. """
    _name = 'change.oidc.password.user'
    _description = 'User, Change OIDC Password Wizard'

    wizard_id = fields.Many2one('change.oidc.password.wizard', string='Wizard', required=True, ondelete='cascade')
    user_id = fields.Many2one('res.users', string='User', required=True, ondelete='cascade')
    user_login = fields.Char(string='User Login', readonly=True)
    new_passwd = fields.Char(string='New OIDC Password', default='')

    def change_oidc_password_button(self):
        crypt_context = CryptContext(schemes=['pbkdf2_sha512'])
        for line in self:
            if not line.new_passwd:
                raise UserError(_("Before clicking on 'Change OIDC Password', you have to write a new password."))
            password_hash = crypt_context.hash(line.new_passwd)
            line.user_id.partner_id.write({'oidc_password': password_hash})
        # don't keep temporary passwords in the database longer than necessary
        self.write({'new_passwd': False})


class ChangeOIDCPasswordWizard(models.TransientModel):
    """ A wizard to manage the change of users' passwords. """
    _name = "change.oidc.password.wizard"
    _description = "Change Password Wizard"

    def _default_user_ids(self):
        user_ids = self._context.get('active_model') == 'res.users' and self._context.get('active_ids') or []
        return [
            Command.create({'user_id': user.id, 'user_login': user.login})
            for user in self.env['res.users'].browse(user_ids)
        ]

    user_ids = fields.One2many('change.oidc.password.user', 'wizard_id', string='Users', default=_default_user_ids)

    def change_oidc_password_button(self):
        self.ensure_one()
        self.user_ids.change_oidc_password_button()
        if self.env.user in self.user_ids.user_id:
            return {'type': 'ir.actions.client', 'tag': 'reload'}
        return {'type': 'ir.actions.act_window_close'}
