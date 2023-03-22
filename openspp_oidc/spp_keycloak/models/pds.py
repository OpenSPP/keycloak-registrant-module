import logging
from pydantic import validator
from typing import List, Optional
from odoo import _, api
from odoo.addons.phone_validation.tools import phone_validation
from .naive_orm_model import NaiveOrmModel


_logger = logging.getLogger(__name__)


class PDSBaseIn(NaiveOrmModel):
    family_number: str
    pds_number: str
    uid_number: str
    phone_number: str

    @validator("phone_number")
    def phone_validation(cls, v):
        if v:
            v = phone_validation.phone_format(
                v,
                None,
                None,
                force_format="INTERNATIONAL",
                raise_exception=False,
            )
            _logger.debug(f"phone_number: {v}")
        else:
            raise ValueError("Phone number must not empty")
        return v

class PDSUpdatePhoneIn(PDSBaseIn):
    pass

class PDSUpdatePaswordIn(PDSBaseIn):
    password: str


class PDSBaseOut(NaiveOrmModel):
    group_id: int = 0
    individual_id: int = 0


class PDSUpdatePhoneOut(PDSBaseOut):
    phone_updated: bool = False


class PDSUpdatePasswordOut(PDSBaseOut):
    password_updated: bool = False
