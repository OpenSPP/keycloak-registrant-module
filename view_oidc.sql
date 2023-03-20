DROP VIEW IF EXISTS view_oidc;
CREATE VIEW view_oidc AS
  SELECT
    p.id,
    p.name AS username,
    p.display_name AS full_name,
    '' AS first_name,
    '' AS last_name,
    p.parent_id,
    ggk.id AS kind_id,
    ggk.name AS kind_name,
    git.name AS type_name,
    gri.value AS type_value,
    p.company_id,
    p.is_group,
    p.title,
    p.lang,
    p.tz,
    p.active,
    p.email,
    p.phone,
    p.oidc_password AS password
  FROM
    res_partner p
    LEFT JOIN g2p_group_kind ggk ON ggk.id = p.kind
    LEFT JOIN g2p_reg_id gri ON gri.partner_id = p.id
    LEFT JOIN g2p_id_type git ON git.id = gri.id_type
  WHERE
    p.is_company = false
    AND p.is_registrant = true
  ORDER BY id
;