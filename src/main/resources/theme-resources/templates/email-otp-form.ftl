<#import "template.ftl" as layout>
<#include "macros.ftl">
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
		${msg("emailAuthTitle",realm.displayName)}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form"
                    method="post">
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="code" class="${properties.kcLabelClass!}">${msg("otpAuthLabel")}</label>
                        <input type="text" id="code" name="code"
                            class="${properties.kcInputClass!}" <@rtl/> autofocus/>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}" <@rtl/>>
                            <input name="submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonBlockClass!}"
                                type="submit" value="${msg('doSubmit')}" disabled/>

                            <input name="resend" ${(formDataX['resendOTPStatus'])!''}
                                class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonBlockClass!}"
                                data-ttl="${(formDataX['ttl'])!''}" data-resend-time="${(formDataX['resendTime'])!''}"
                                type="submit" value="${msg('resendOTP')}"/>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>