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
        $.each(tab2JsRoutes[tab], function(index, route) {
            route(studyId, studyName).ajax({
                success : function(data) {
                    if (index == 0) {
                        $(tab).html(data);
                    } else {
                        $(tab).append(data);
                    }
                },
                error : function(err) {
                    alert("Ajax Call error: " + err);
                }
            });
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

