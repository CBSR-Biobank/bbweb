/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for Centres.
 */
/* @ngInject */
function CentreNameFactory($q,
                           $log,
                           biobankApi,
                           EntityNameAndState,
                           DomainEntity,
                           DomainError,
                           CentreState) {

  /**
   * Summary information for a {@link domain.centres.Centre}.
   *
   * Please do not use this constructor. It is meant for internal use.
   *
   * @memberOf domain.centres
   * @extends domain.EntityNameAndState
   */
  class CentreName extends EntityNameAndState {

    /**
     * Please do not use this constructor. It is meant for internal use.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj = { state: CentreState.DISABLED }) {
      /**
       * The state can be one of: enabled, disabled, or retired.
       *
       * @name domain.centres.CentreName#state
       * @type {domain.centres.CentreState}
       */

      super(obj);
    }

    /**
     * Used to query the Centre's current state.
     *
     * @returns {boolean} <code>true</code> if the Centre is in <code>disabled</code> state.
     */
    isDisabled() {
      return (this.state === CentreState.DISABLED);
    }

    /**
     * Used to query the Centre's current state.
     *
     * @returns {boolean} <code>true</code> if the Centre is in <code>enabled</code> state.
     */
    isEnabled() {
      return (this.state === CentreState.ENABLED);
    }

    static url(...paths) {
      const allPaths = [ 'centres' , 'names' ].concat(paths)
      return super.url(...allPaths);
    }

    /**
     * Creates a CentreName, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.centres.Centre} A Centre created from the given object.
     *
     * @see {@link domain.centres.CentreName.asyncCreate|asyncCreate()} when you need to create
     * a Centre within asynchronous code.
     */
    static create(obj) {
      var validation = EntityNameAndState.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new CentreName(obj);
    }

    /**
     * Used to list CentreNames.
     *
     * <p>A paged API is used to list centres. See below for more details.</p>
     *
     * @param {object} options - The options to use to list centres.
     *
     * @param {string} [options.filter] The filter to use on Centre names. Default is empty string.
     *
     * @param {string} [options.sort=name] Centres can be sorted by <code>name</code> or by
     *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
     *        in descending order.
     *
     * @param {Array<domain.EntityNameAndState>} omit - the list of names to filter out of the result returned
     *        from the server.
     *
     * @returns {Promise<common.controllers.PagedListController.PagedResult>} with items of type {@link
     * domain.centres.Centre}.
     */
    static list(options, omit) {
      return EntityNameAndState.list(CentreName.url(), options, omit)
         .then(entities => entities.map(entity => CentreName.create(entity)));
   }
  }

  return CentreName;
}

export default ngModule => ngModule.factory('CentreName', CentreNameFactory)
