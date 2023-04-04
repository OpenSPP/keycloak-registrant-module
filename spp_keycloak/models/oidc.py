import logging
import re
from pydantic import validator
from typing import List, Optional
from odoo import _, api
from odoo.addons.phone_validation.tools import phone_validation
from .naive_orm_model import NaiveOrmModel


_logger = logging.getLogger(__name__)


class OIDCBaseIn(NaiveOrmModel):
    household_number: str
    uid_number: str
    phone_number: str

    @validator("phone_number")
    def phone_validation(cls, v):
        if v:
            v = phone_validation.phone_format(
                re.sub('^0', '+', v),
                None,
                None,
                force_format="E164",
                raise_exception=False,
            )
            _logger.debug(f"phone_number: {v}")
        else:
            raise ValueError("Phone number must not empty")
        return v

class OIDCUpdatePhoneIn(OIDCBaseIn):
    pass

class OIDCUpdatePaswordIn(OIDCBaseIn):
    password: str


class OIDCBaseOut(NaiveOrmModel):
    group_id: int = 0
    individual_id: int = 0


class OIDCUpdatePhoneOut(OIDCBaseOut):
    phone_updated: bool = False


class OIDCUpdatePasswordOut(OIDCBaseOut):
    password_updated: bool = False
