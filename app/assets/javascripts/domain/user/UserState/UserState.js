/**
 * Domain Entities related to {@link domain.users.User User}.
 *
 * @namespace domain.users.userStates
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * A {@link domain.users.User User} can be in one of these states.
 *
 * @enum {string}
 * @memberOf domain.users.userStates
 */
const UserState = {
  /**
   * A user that just registered with the system. This user does not yet have full access
   * the system.
   */
  REGISTERED: 'registered',

  /**
   * A user that has access to the system.
   */
  ACTIVE:     'active',

  /**
   * A user who no longer has access to the system.
   */
  LOCKED:     'locked'
};

export default ngModule => ngModule.constant('UserState', UserState)
