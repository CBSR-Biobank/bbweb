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
   *
   * FIXME: translations are required
   */
  var StudyState = {
    DISABLED: 'disabled',
    ENABLED:  'enabled',
    RETIRED:  'retired'
  };

  return StudyState;
});
