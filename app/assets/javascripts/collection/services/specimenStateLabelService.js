/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  centreStateLabelService.$inject = [
    'labelService',
    'SpecimenState',
    'gettextCatalog'
  ];

  /**
   * An AngularJS service that converts a SpecimenState to a i18n string that can
   * be displayed to the centre.
   *
   * @param {object} SpecimenState - AngularJS constant that enumerates all the centre states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function centreStateLabelService(labelService, SpecimenState, gettextCatalog) {
    var labels = {};

    labels[SpecimenState.USABLE]   = function () { return gettextCatalog.getString('Unusable'); };
    labels[SpecimenState.UNUSABLE] = function () { return gettextCatalog.getString('Usable'); };

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
