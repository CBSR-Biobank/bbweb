/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for Users.
 */
/* @ngInject */
function UserNameFactory($q,
                         $log,
                         biobankApi,
                         EntityNameAndState,
                         DomainEntity,
                         DomainError,
                         UserState) {

  /**
   * Summary information for a {@link domain.users.User}.
   *
   * @memberOf domain.users
   * @extends domain.EntityNameAndState
   */
  class UserName extends EntityNameAndState {

    /**
     * Please do not use this constructor. It is meant for internal use.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj = { state: UserState.REGISTERED }) {
      /**
       * The state.
       *
       * @name domain.studies.UserName#state
       * @type {domain.users.UserState}
       */

      super(obj);
    }

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>registered</code> state.
     */
    isRegistered() {
      return (this.state === UserState.REGISTERED);
    }

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>active</code> state.
     */
    isActive() {
      return (this.state === UserState.ACTIVE);
    }

    /**
     * Used to query the User's current state.
     *
     * @returns {boolean} <code>true</code> if the User is in <code>locked</code> state.
     */
    isLocked() {
      return (this.state === UserState.LOCKED);
    }

    static url(...paths) {
      const allPaths = [ 'users' , 'names' ].concat(paths)
      return super.url(...allPaths);
    }

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
    static create(obj) {
      var validation = EntityNameAndState.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new UserName(obj);
    }

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
     * @param {Array<domain.EntityNameAndState>} omit - the list of names to filter out of the result returned
     *        from the server.
     *
     * @returns {Promise<common.controllers.PagedListController.PagedResult>} with items of type {@link
     * domain.users.User}.
     */
    static list(options, omit) {
      return super.list(UserName.url(), options, omit)
        .then(entities => entities.map(entity => UserName.create(entity)));
    }

    static from(user) {
      return UserName.create(
        {
          id:    user.id,
          slug:  user.slug,
          name:  user.name,
          state: user.state
        });
    }
  }

  return UserName;
}

export default ngModule => ngModule.factory('UserName', UserNameFactory)
