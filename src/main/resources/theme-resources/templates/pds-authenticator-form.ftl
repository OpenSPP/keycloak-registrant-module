<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section="header">
        ${msg("pdsAuthTitle",realm.displayName)}
    <#elseif section="form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form"
                    method="post">
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="pds" class="${properties.kcLabelClass!}">ePDS Card Number</label>
                        <input id="pds" name="pds" type="text" class="${properties.kcInputClass!}"
                            value="${(pds.formData['pds'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="uid" class="${properties.kcLabelClass!}">Unified ID Number</label>
                        <input id="uid" name="uid" type="text" class="${properties.kcInputClass!}"
                            value="${(pds.formData['uid'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="family_number" class="${properties.kcLabelClass!}">Family Number</label>
                        <input id="family_number" name="family_number" type="text" class="${properties.kcInputClass!}"
                            value="${(pds.formData['family_number'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="phone_number" class="${properties.kcLabelClass!}">Phone Number</label>
                        <input id="phone_number" name="phone_number" type="text" class="${properties.kcInputClass!}"
                            value="${(pds.formData['phone_number'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="password" class="${properties.kcLabelClass!}">Password</label>
                        <input id="password" name="password" type="password" class="${properties.kcInputClass!}"
                            value="${(pds.formData['password'])!''}" />
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                            <input
                                class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                                type="submit" value="${msg("doSubmit")}" />

                            <input name="cancel"
                                class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                                type="submit" value="${msg("doCancel")}" />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    <#elseif section="info">
        ${msg("pdsAuthInstruction")}
    </#if>
</@layout.registrationLayout>