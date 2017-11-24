/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for Users.
 */
/* @ngInject */
function UserMembershipFactory($q,
                               $log,
                               MembershipBase,
                               ConcurrencySafeEntity,
                               EntityInfo,
                               EntitySet,
                               DomainError) {

  /**
   * Do not use this constructor. Use {@link domain.access.UserMembership.create|create()} or {@link
   * domain.access.UserMembership.asyncCreate|asyncCreate()} to create an object returned by the server.
   *
   * This class is only used by {@link domain.user.User|User} objects.
   *
   * @class
   * @memberOf domain.access
   * @extends domain.access.MembershipBase
   *
   * @classdesc User membership information.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function UserMembership(obj) {
    MembershipBase.call(this, obj);
  }

  UserMembership.prototype = Object.create(MembershipBase.prototype);
  UserMembership.prototype.constructor = UserMembership;

  UserMembership.SCHEMA = MembershipBase.SCHEMA;

  /**
   * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.access.Membership|Membership}.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
   */
  UserMembership.isValid = function (obj) {
    return ConcurrencySafeEntity.isValid(UserMembership.SCHEMA,
                                         [
                                           EntityInfo.SCHEMA,
                                           EntitySet.SCHEMA
                                         ],
                                         obj);
  };

  /**
   * Creates a UserMembership, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.access.Membership} A user created from the given object.
   *
   * @see {@link domain.access.UserMembership.asyncCreate|asyncCreate()} when you need to create
   * a user within asynchronous code.
   */
  UserMembership.create = function (obj) {
    var validation = UserMembership.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new UserMembership(obj);
  };

  /**
   * Creates a UserMembership from a server reply, but first validates that <tt>obj</tt> has a valid schema.
   * <i>Meant to be called from within promise code.</i>
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {Promise<domain.access.Membership>} A user wrapped in a promise.
   *
   * @see {@link domain.access.UserMembership.create|create()} when not creating a Membership within
   * asynchronous code.
   */
  UserMembership.asyncCreate = function (obj) {
    var result;
    try {
      result = UserMembership.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  return UserMembership;
}

export default ngModule => ngModule.factory('UserMembership', UserMembershipFactory)
