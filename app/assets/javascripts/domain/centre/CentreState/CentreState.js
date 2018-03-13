/**
 * Domain Entities related to {@link domain.centres.Centre Centres}.
 *
 * @namespace domain.centres.centreStates
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The states a {@link domain.centres.Centre Centre} can have.
 *
 * @enum {string}
 * @memberOf domain.centres.centreStates
 */
const CentreState = {
  /**
   * The {@link domain.centres.Centre Centre} cannot participate in {@link domain.participants.Specimen
   * Specimen} collection and cannot store any new specimens.
   */
  DISABLED: 'disabled',

  /**
   * The {@link domain.centres.Centre Centre} is available participate in either {@link
   * domain.participants.Specimen Specimen} collection or in storing specimens.
   */
  ENABLED:  'enabled'
};

export default ngModule => ngModule.constant('CentreState', CentreState)
