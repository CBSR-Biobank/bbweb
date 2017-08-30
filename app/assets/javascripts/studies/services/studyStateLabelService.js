/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  studyStateLabelService.$inject = [
    'labelService',
    'StudyState',
    'gettextCatalog'
  ];

  /**
   * An AngularJS service that converts a StudyState to a i18n string that can
   * be displayed to the study.
   *
   * @param {object} StudyState - AngularJS constant that enumerates all the study states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function studyStateLabelService(labelService, StudyState, gettextCatalog) {
    var labels = {};

    labels[StudyState.DISABLED] = function () { return gettextCatalog.getString('Disabled'); };
    labels[StudyState.ENABLED]  = function () { return gettextCatalog.getString('Enabled'); };
    labels[StudyState.RETIRED]  = function () { return gettextCatalog.getString('Retired'); };

    var service = {
      stateToLabelFunc: stateToLabelFunc
    };
    return service;

    //-------

    function stateToLabelFunc(state) {
      return labelService.getLabel(labels, state);
    }

  }

  return studyStateLabelService;
});
