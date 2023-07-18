import setuptools

with open('VERSION.txt', 'r') as f:
    version = f.read().strip()

setuptools.setup(
    name="odoo-addons-openspp-keycloak-registrant-module",
    description="Meta package for openspp-keycloak-registrant-module Odoo addons",
    version=version,
    install_requires=[
        'odoo-addon-spp_keycloak>=15.0dev,<15.1dev',
    ],
    classifiers=[
        'Programming Language :: Python',
        'Framework :: Odoo',
        'Framework :: Odoo :: 15.0',
    ]
)
