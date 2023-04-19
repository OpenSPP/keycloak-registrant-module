<#import "template.ftl" as layout>
<#include "macros.ftl">
<@layout.registrationLayout displayInfo=true; section>
    <#if section="header">
        ${msg("benOIDCAuthTitle",realm.displayName)}
    <#elseif section="form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <form action="${url.loginAction}" method="post"
                    class="${properties.kcFormClass!}"
                    id="kc-u2f-login-form">
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="household_number" class="${properties.kcLabelClass!}">${msg("householdNumberField")}</label>
                        <div class="input-box" <@rtl/>>
                            <span class="counter"></span>
                            <input id="household_number" name="household_number"
                                required data-validate-msg=""
                                inputmode="numeric" minlength="10" maxlength="10" pattern="\d+"
                                type="text" class="${properties.kcInputClass!}" <@rtl/>
                                value="${(formDataX['household_number'])!''}" />
                            <i class="validate-status fa fa-check-circle"></i>
                        </div>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="uid" class="${properties.kcLabelClass!}">${msg("uidNumberField")}</label>
                        <div class="input-box" <@rtl/>>
                            <span class="counter"></span>
                            <input id="uid" name="uid"
                                required data-validate-msg=""
                                inputmode="numeric" minlength="12" maxlength="12" pattern="\d+"
                                type="text" class="${properties.kcInputClass!}" <@rtl/>
                                value="${(formDataX['uid'])!''}" />
                            <i class="validate-status fa fa-check-circle"></i>
                        </div>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="phone_number" class="${properties.kcLabelClass!}">${msg("phoneNumberField")}</label>
                        <div class="input-box <@ifrtl then='reverse'/>" <@rtl/>>
                            <span class="counter"></span>
                            <input id="_phone_number_prefix" tabindex="-1"
                                type="text" value="${(formDataX['intPhoneCode'])!'+964'}" readonly class="${properties.kcInputClass!}" />
                            <input id="phone_number" name="phone_number" type="text"
                                required data-validate-msg=""
                                pattern="[\d\s]+" inputmode="numeric" minlength="10" maxlength="12"
                                placeholder="${(formDataX['samplePhoneNumber'])!'712 345 6789'}" class="${properties.kcInputClass!}"
                                value="${(formDataX['phone_number'])!''}"/>
                            <i class="validate-status fa fa-check-circle"></i>
                        </div>
                    </div>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="password" class="${properties.kcLabelClass!}">${msg("passwordField")}</label>
                        <div class="input-box" <@rtl/>>
                            <span class="counter"></span>
                            <input id="password" name="password"
                                required data-validate-msg="" minlength="8"
                                type="password" class="${properties.kcInputClass!}" <@rtl/>
                            />
                            <i class="validate-status fa fa-check-circle"></i>
                            <div class="${properties.kcButtonClass!}">
                                <i class="toggle-password fa fa-eye"></i>
                            </div>
                        </div>
                    </div>

                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}" <@rtl/>>
                            <input name="submit" disabled
                                class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!} ${properties.kcButtonBlockClass!}"
                                type="submit" value="${msg('doLogIn')}" />
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <script>
            var MESSAGES = {
                'required': '${msg("required")}',
                'invalidInput': '${msg("invalidInput")}',
            };
        </script>
    </#if>
</@layout.registrationLayout>
