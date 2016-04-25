/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * The statuses a {@link domain.studies.Study Study} can have.
   *
   * @enum {string}
   * @memberOf domain.studies
   */
  var StudyStatus = {
    DISABLED: 'DisabledStudy',
    ENABLED:  'EnabledStudy',
    RETIRED:  'RetiredStudy'
  };

  return StudyStatus;
});
