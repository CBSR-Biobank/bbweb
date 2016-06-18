/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  userStatusLabelService.$inject = ['UserStatus'];

  /**
   * Description
   */
  function userStatusLabelService(UserStatus) {
    var labels = {};

    labels[UserStatus.REGISTERED] = 'Registered';
    labels[UserStatus.ACTIVE]     = 'Active';
    labels[UserStatus.LOCKED]     = 'Locked';

    var service = {
      statusToLabel: statusToLabel
    };
    return service;

    //-------

    function statusToLabel(status) {
      var result = labels[status];
      if (_.isUndefined(result)) {
        throw new Error('invalid status for user: ' + status);
      }
      return result;
    }

  }

  return userStatusLabelService;
});
