/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  userStatusLabelService.$inject = [
    'UserStatus',
    'gettextCatalog'
  ];

  /**
   * Description
   */
  function userStatusLabelService(UserStatus,
                                  gettextCatalog) {
    var labels = {};

    /// user status
    labels[UserStatus.REGISTERED] = gettextCatalog.getString('Registered');

    /// user status
    labels[UserStatus.ACTIVE]     = gettextCatalog.getString('Active');

    /// user status
    labels[UserStatus.LOCKED]     = gettextCatalog.getString('Locked');

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
