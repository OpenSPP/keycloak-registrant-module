DROP VIEW IF EXISTS view_oidc;
CREATE VIEW view_oidc AS
  SELECT
    u.id,
    login,
    password,
    p.oidc_password,
    p.name,
    p.display_name,
    p.id AS partner_id,
    p.company_id,
    p.title,
    p.lang,
    p.tz,
    p.active,
    p.email,
    p.phone
  FROM
  res_users u
  INNER JOIN res_partner p ON u.partner_id = p.id;