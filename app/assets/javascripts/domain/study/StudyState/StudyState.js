/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * The statuses a {@link domain.studies.Study Study} can have.
 *
 * See also {@ling StudyStateLabelService}.
 *
 * @enum {string}
 * @memberOf domain.studies
 *
 */
const StudyState = {
  DISABLED: 'disabled',
  ENABLED:  'enabled',
  RETIRED:  'retired'
};

export default ngModule => ngModule.constant('StudyState', StudyState)
