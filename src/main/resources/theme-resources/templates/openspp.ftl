<#macro if if then else=""><#if if>${then}<#else>${else}</#if></#macro>
<#macro rtl><#if locale.currentLanguageTag == 'ar' || locale.currentLanguageTag=='ku'>dir="rtl"</#if></#macro>