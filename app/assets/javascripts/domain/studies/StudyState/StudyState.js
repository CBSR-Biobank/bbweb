/**
 * Enumeration related to {@link domain.studies.Study Studies}.
 *
 * @namespace domain.studies.StudyState
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The statuses a {@link domain.studies.Study Study} can have.
 *
 *
 * @enum {string}
 * @memberOf domain.studies.StudyState
 * @see studies.service.StudyStateLabelService
 */
const StudyState = {
  /**
   * This is the initial state for a study.  In this state, only configuration changes are allowed.
   * Collection and processing of specimens cannot be recorded.
   */
  DISABLED: 'disabled',

  /**
   * When a study is in this state, collection and processing of specimens can be recorded.
    */
  ENABLED:  'enabled',

  /**
   *  In this state, the study cannot be modified and collection and processing of specimens is not allowed.
    */
  RETIRED:  'retired'
};

export default ngModule => ngModule.constant('StudyState', StudyState)
