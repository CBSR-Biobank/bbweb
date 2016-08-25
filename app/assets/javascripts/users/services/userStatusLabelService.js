/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  userStatusLabelService.$inject = [
    'UserStatus',
    'gettext'
  ];

  /**
   * Description
   */
  function userStatusLabelService(UserStatus,
                                  gettext) {
    var labels = {};

    /// user status
    labels[UserStatus.REGISTERED] = gettext('Registered');

    /// user status
    labels[UserStatus.ACTIVE]     = gettext('Active');

    /// user status
    labels[UserStatus.LOCKED]     = gettext('Locked');

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
