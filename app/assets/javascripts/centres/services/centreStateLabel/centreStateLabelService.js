/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  centreStateLabelService.$inject = [
    'labelService',
    'CentreState',
    'gettextCatalog'
  ];

  /**
   * An AngularJS service that converts a CentreState to a i18n string that can
   * be displayed to the centre.
   *
   * @param {object} CentreState - AngularJS constant that enumerates all the centre states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function centreStateLabelService(labelService, CentreState, gettextCatalog) {
    var labels = {};

    labels[CentreState.DISABLED] = function () { return gettextCatalog.getString('Disabled'); };
    labels[CentreState.ENABLED]  = function () { return gettextCatalog.getString('Enabled'); };

    var service = {
      stateToLabelFunc: stateToLabelFunc
    };
    return service;

    //-------

    function stateToLabelFunc(state) {
      return labelService.getLabel(labels, state);
    }

  }

  return centreStateLabelService;
});
