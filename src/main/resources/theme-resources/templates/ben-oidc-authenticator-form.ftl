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
                        <input id="household_number" name="household_number" required
                            type="text" class="${properties.kcInputClass!}" <@rtl/>
                            value="${(formDataX['household_number'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="uid" class="${properties.kcLabelClass!}">${msg("uidNumberField")}</label>
                        <input id="uid" name="uid" required
                            type="text" class="${properties.kcInputClass!}" <@rtl/>
                            value="${(formDataX['uid'])!''}" />
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="phone_number" class="${properties.kcLabelClass!}">${msg("phoneNumberField")}</label>
                        <div class="input-box" <@rtl/>>
                            <input id="_phone_number_prefix" name="phone_number_prefix"
                            type="text" value="+964" readonly class="${properties.kcInputClass!}" />
                            <input id="phone_number" name="phone_number" type="text" required
                                pattern="7\d{2,2}\s*\d{3,3}\s*\d{4,4}" inputmode="numeric" maxlength="12"
                                placeholder="712 345 6789" class="${properties.kcInputClass!}"
                                value="${(formDataX['phone_number'])!''}"/>
                            <i class="validate-status fa fa-check-circle"></i>
                        </div>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="password" class="${properties.kcLabelClass!}">${msg("passwordField")}</label>
                        <input id="password" name="password" required
                            type="password" class="${properties.kcInputClass!}" <@rtl/>
                        />
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}" <@rtl/>>
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonBlockClass!}"
                                type="submit" value="${msg('doLogIn')}" />
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <script>
            function onPhoneNumberUpdate(e) {
                // Allow paste and copy
                if ((e.ctrlKey || e.shiftKey || e.altKey)) {
                    return;
                }
                let validChars = /[\d\s]/;
                let key = e.key;
                let val = this.value;
                if (key != null && key.length === 1) {
                    if (key.search(validChars) === -1) {
                        e.preventDefault();
                        e.stopPropagation();
                        return;
                    }
                    val = val.substr(0, this.selectionStart) + key + val.substr(this.selectionEnd);
                }
                if (val.replace(/\s+/g, '').length == 10) {
                    this.value = formatPhoneNumber(val);
                }
            }
            function onPhoneNumberUpdated(e) {
                this.value = formatPhoneNumber(this.value);
            }

            function formatPhoneNumber(val) {
                if (val.replace(/\s+/g, '').length == 10) {
                    val = val.replace(/\s+/g, '');
                    val = val.substr(0, 3) + ' ' + val.substr(3, 3) + ' ' + val.substr(6);
                }
                return val;
            }

            let phoneNumber = document.querySelector('#phone_number');
            phoneNumber.addEventListener('keydown', onPhoneNumberUpdate);
            phoneNumber.addEventListener('change', onPhoneNumberUpdated);
        </script>
    <#--  <#elseif section="info">
        ${msg("benOIDCAuthInstruction")}  -->
    </#if>
</@layout.registrationLayout>
