<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Email OTP Form
    <#elseif section = "header">
        Email OTP Form
    <#elseif section = "form">
        <p>Enter email OTP code</p>
        <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-u2f-login-form" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="emailOTP">OTP</label>
                <input id="emailOTP" name="emailOTP" type="text" inputmode="numeric" pattern="[0-9]*"/>
            </div>

            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doSubmit")}"/>

            <input name="resend"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("resendOTP")}"/>

            <input name="cancel"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                   type="submit" value="${msg("doCancel")}"/>
        </form>
    </#if>
</@layout.registrationLayout>