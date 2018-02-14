/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * A {@link domain.users.User User} can be in one of these states.
 *
 * @enum {string}
 * @memberOf domain.users
 */
const UserState = {
  REGISTERED: 'registered',
  ACTIVE:     'active',
  LOCKED:     'locked'
};

export default ngModule => ngModule.constant('UserState', UserState)
