/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  studyStatusLabelService.$inject = ['StudyStatus'];

  /**
   * Description
   */
  function studyStatusLabelService(StudyStatus) {
    var labels = {};

    labels[StudyStatus.DISABLED] = 'Disabled';
    labels[StudyStatus.ENABLED]  = 'Enabled';
    labels[StudyStatus.RETIRED]  = 'Retired';

    var service = {
      statusToLabel: statusToLabel
    };
    return service;

    //-------

    function statusToLabel(status) {
      var result = labels[status];
      if (_.isUndefined(result)) {
        throw new Error('invalid status for study: ' + status);
      }
      return result;
    }

  }

  return studyStatusLabelService;
});
