//
// Loads content when user selects the different tags in the 'Show Study' page.
//
//
var tab2JsRoutes = {};
tab2JsRoutes['#tab-summary'] = jsRoutes.org.biobank.controllers.study.StudyController.summaryTab;
tab2JsRoutes['#tab-participants'] = jsRoutes.org.biobank.controllers.study.StudyController.participantsTab;
tab2JsRoutes['#tab-specimens'] = jsRoutes.org.biobank.controllers.study.StudyController.specimensTab;
tab2JsRoutes['#tab-collection-events'] = jsRoutes.org.biobank.controllers.study.StudyController.ceventsTab;
tab2JsRoutes['#tab-processing-events'] = jsRoutes.org.biobank.controllers.study.StudyController.peventsTab;

function getTabContent(tab, studyId, studyName) {
    if (tab in tab2JsRoutes) {
        tab2JsRoutes[tab](studyId, studyName).ajax({
            success : function(data) {
                console.log(data);
                $(tab).html(data);
            },
            error : function(err) {
                alert('An error occurred. Please reload this page.');
            }
        });
    }
}

$(function() {
    var activeTab = $('div.tabbable').data('activeTab');
    var studyId = $('div.tabbable').data('studyId');
    var studyName= $('div.tabbable').data('studyName');

    getTabContent(activeTab, studyId, studyName);

    $('#study-tabs').bind('show', function(e) {
        var pattern = /#.+/gi; //use regex to get anchor(==selector)
        var tabId = e.target.toString().match(pattern)[0]; //get anchor
        getTabContent(tabId, studyId, studyName);

        // hide the alert box when a new tab is selected
        $('div.alert').hide();
    });
});

