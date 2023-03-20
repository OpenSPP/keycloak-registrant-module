from typing import List, Optional

import pydantic
from .naive_orm_model import NaiveOrmModel


class PDSBaseIn(NaiveOrmModel):
    family_number: str
    pds_number: str
    uid_number: str


class PDSUpdatePhoneIn(PDSBaseIn):
    phone_number: str


class PDSUpdatePaswordIn(PDSBaseIn):
    phone_number: str
    password: str


class PDSBaseOut(NaiveOrmModel):
    group_id: int = 0
    individual_id: int = 0


class PDSUpdatePhoneOut(PDSBaseOut):
    phone_updated: bool = False


class PDSUpdatePasswordOut(PDSBaseOut):
    password_updated: bool = False
