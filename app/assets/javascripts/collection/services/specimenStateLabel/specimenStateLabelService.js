/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */


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
function SpecimenStateLabelService(labelService, SpecimenState, gettextCatalog) {
  var labels = {};

  labels[SpecimenState.USABLE]   = () => gettextCatalog.getString('Usable');
  labels[SpecimenState.UNUSABLE] = () => gettextCatalog.getString('Unusable');

  var service = {
    stateToLabelFunc: stateToLabelFunc
  };
  return service;

  //-------

  function stateToLabelFunc(state) {
    return labelService.getLabel(labels, state);
  }

}

export default ngModule => ngModule.service('specimenStateLabelService', SpecimenStateLabelService)
