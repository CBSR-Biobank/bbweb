/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  UserNameFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'EntityName',
    'DomainError',
    'UserState'
  ];

  /*
   * Angular factory for Users.
   */
  function UserNameFactory($q,
                            $log,
                            biobankApi,
                            EntityName,
                            DomainError,
                            UserState) {

    /**
     * @classdesc A UserName contains the ID, name and state for a User.
     *
     * Please do not use this constructor. It is meant for internal use.
     *
     * @class
     * @memberOf domain.users
     * @extends domain.EntityName
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function UserName(obj) {

      /**
       * The state can be one of: enabled, disabled, or retired.
       *
       * @name domain.users.User#state
       * @type {domain.users.UserState}
       */
      this.state = UserState.DISABLED;

      EntityName.call(this, obj);
    }

    UserName.prototype = Object.create(EntityName.prototype);
    UserName.prototype.constructor = UserName;

    UserName.REST_API_URL = '/users/names';

    /**
     * Creates a UserName, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.users.User} A User created from the given object.
     *
     * @see {@link domain.users.UserName.asyncCreate|asyncCreate()} when you need to create
     * a User within asynchronous code.
     */
    UserName.create = function (obj) {
      return EntityName.create(UserName, obj);
    };

    /**
     * Used to list UserNames.
     *
     * <p>A paged API is used to list users. See below for more details.</p>
     *
     * @param {object} options - The options to use to list users.
     *
     * @param {string} [options.filter] The filter to use on User names. Default is empty string.
     *
     * @param {string} [options.sort=name] Users can be sorted by <code>name</code> or by
     *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
     *        in descending order.
     *
     * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
     *        users should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} [options.limit=10] The total number of users to return per page. The maximum page size
     *        is 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {Array<domain.EntityName>} omit - the list of names to filter out of the result returned
     *        from the server.
     *
     * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
     *          domain.users.User}.
     */
    UserName.list = function (options, omit) {
      return EntityName.list(UserName.REST_API_URL, options, UserName.create, omit);
    };

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>registered</code> state.
     */
    UserName.prototype.isRegistered = function () {
      return (this.state === UserState.REGISTERED);
    };

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>active</code> state.
     */
    UserName.prototype.isActive = function () {
      return (this.state === UserState.ACTIVE);
    };

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>locked</code> state.
     */
    UserName.prototype.isLocked = function () {
      return (this.state === UserState.LOCKED);
    };

    return UserName;
  }

  return UserNameFactory;
});
