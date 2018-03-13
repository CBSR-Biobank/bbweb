/**
 * Domain Entities used in {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace domain.participants.specimenStates
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The states a {@link domain.participants.Specimen Specimen} can have.
 *
 * @enum {string}
 * @memberOf domain.participants.specimenStates
 */
const SpecimenState = {
  /** the {@link domain.participants.Specimen Specimen} is available for processing */
  USABLE:   'usable',

  /** the {@link domain.participants.Specimen Specimen} is no longer available for processing */
  UNUSABLE: 'unusable'
};

export default ngModule => ngModule.constant('SpecimenState', SpecimenState)
