/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * The statuses a {@link domain.centres.Centre Centre} can have.
 *
 * @enum {string}
 * @memberOf domain.centres
 */
const CentreState = {
  DISABLED: 'disabled',
  ENABLED:  'enabled'
};

export default ngModule => ngModule.constant('CentreState', CentreState)
