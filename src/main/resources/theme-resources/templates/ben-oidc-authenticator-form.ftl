<#import "template.ftl" as layout>
<#include "openspp.ftl">
<@layout.registrationLayout displayInfo=true; section>
    <#if section="header">
        ${msg("benOIDCAuthTitle",realm.displayName)}
    <#elseif section="form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form"
                    method="post">
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="household_number" class="${properties.kcLabelClass!}">${msg("householdNumberField")}</label>
                        <input id="household_number" name="household_number"
                            type="text" class="${properties.kcInputClass!}" <@rtl/>
                            value="${(login.formData['household_number'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="uid" class="${properties.kcLabelClass!}">${msg("uidNumberField")}</label>
                        <input id="uid" name="uid"
                            type="text" class="${properties.kcInputClass!}" <@rtl/>
                            value="${(login.formData['uid'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="phone_number" class="${properties.kcLabelClass!}">${msg("phoneNumberField")}</label>
                        <input id="phone_number" name="phone_number"
                            type="text" class="${properties.kcInputClass!}" <@rtl/>
                            value="${(login.formData['phone_number'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="password" class="${properties.kcLabelClass!}">${msg("passwordField")}</label>
                        <input id="password" name="password"
                            type="password" class="${properties.kcInputClass!}" <@rtl/>
                        />
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}" <@rtl/>>
                            <input name="cancel"
                                class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonLargeClass!}"
                                type="submit" value="${msg('doCancel')}" />

                            <input
                                class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                                type="submit" value="${msg('doSubmit')}" />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    <#elseif section="info">
        ${msg("benOIDCAuthInstruction")}
    </#if>
</@layout.registrationLayout>