/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * @enum {string}
 * @memberOf domain.centres
 */
const SpecimenState = {
  USABLE:   'usable',
  UNUSABLE: 'unusable'
};

export default ngModule => ngModule.constant('SpecimenState', SpecimenState)
