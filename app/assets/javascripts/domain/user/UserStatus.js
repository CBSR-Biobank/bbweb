/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * The statuses a {@link domain.users.User User} can have.
   *
   * @enum {string}
   * @memberOf domain.users
   */
  var UserStatus = {
    REGISTERED: 'RegisteredUser',
    ACTIVE:     'ActiveUser',
    LOCKED:     'LockedUser'
  };

  return UserStatus;
});
