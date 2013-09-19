//
// Loads content when user selects the different tags in the "Show Study" page.
//
//
var tab2JsRoutes = {};
tab2JsRoutes["#tab-specimens"] = [
    jsRoutes.controllers.study.SpecimenGroupController.showAll
];

tab2JsRoutes["#tab-collection-events"] = [
    jsRoutes.controllers.study.CeventTypeController.showAll,
    jsRoutes.controllers.study.CeventAnnotTypeController.showAll
];

function getTabContent(tab, studyId, studyName) {
    if (tab in tab2JsRoutes) {
        var ajaxCalls = [];
        $.each(tab2JsRoutes[tab], function(index, route) {
            ajaxCalls.push(route(studyId, studyName).ajax());
        });

        $.when.apply($, ajaxCalls).done(function(){
            if (ajaxCalls.length == 1) {
                $(tab).html(arguments[0]);
            } else {
                // response has variable number of arguments
                var responses = "";
                $.each(arguments, function(index, responseData){
                    responses += responseData[0];
                });
                $(tab).html(responses);
            }
        });
    }
}

$(function() {
    var activeTab = $("div.tabbable").data("activeTab");
    var studyId = $("div.tabbable").data("studyId");
    var studyName= $("div.tabbable").data("studyName");

    getTabContent(activeTab, studyId, studyName);

    $('#study-tabs').bind('show', function(e) {
        var pattern = /#.+/gi; //use regex to get anchor(==selector)
        var tabId = e.target.toString().match(pattern)[0]; //get anchor
        getTabContent(tabId, studyId, studyName);

        // hide the alert box when a new tab is selected
        $("div.alert").hide();
    });
});

