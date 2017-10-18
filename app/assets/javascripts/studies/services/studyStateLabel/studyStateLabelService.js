/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

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
/* @ngInject */
function StudyStateLabelService(labelService, StudyState, gettextCatalog) {
  var labels = {};

  labels[StudyState.DISABLED] = () => gettextCatalog.getString('Disabled');
  labels[StudyState.ENABLED]  = () => gettextCatalog.getString('Enabled');
  labels[StudyState.RETIRED]  = () => gettextCatalog.getString('Retired');

  var service = {
    stateToLabelFunc
  };
  return service;

  //-------

  function stateToLabelFunc(state) {
    return labelService.getLabel(labels, state);
  }

}

export default ngModule => ngModule.service('studyStateLabelService', StudyStateLabelService)
