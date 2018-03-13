/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for Studies.
 */
/* @ngInject */
function StudyNameFactory($q,
                          $log,
                          biobankApi,
                          EntityNameAndState,
                          DomainEntity,
                          DomainError,
                          StudyState) {

  /**
   * Summary information for a {@link domain.studies.Study}.
   *
   * @memberOf domain.studies
   * @extends domain.EntityNameAndState
   */
  class StudyName extends EntityNameAndState {

    /**
     * Please do not use this constructor. It is meant for internal use.
     *
     * @param {object} [obj] - An initialization object whose properties are the same as the members from this
     * class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj = { state: StudyState.DISABLED }) {
      /**
       * The state can be one of: enabled, disabled, or retired.
       *
       * @name domain.studies.StudyName#state
       * @type {domain.studies.StudyState}
       */

      super(obj);
    }

    /**
     * Used to query the study's current state.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>disabled</code> state.
     */
    isDisabled() {
      return (this.state === StudyState.DISABLED);
    }

    /**
     * Used to query the study's current state.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>enabled</code> state.
     */
    isEnabled() {
      return (this.state === StudyState.ENABLED);
    }

    /**
     * Used to query the study's current state.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>retired</code> state.
     */
    isRetired() {
      return (this.state === StudyState.RETIRED);
    }

    static url(...paths) {
      const allPaths = [ 'studies' , 'names' ].concat(paths);
      return super.url(...allPaths);
    }

    /**
     * Creates a StudyName, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.Study} A study created from the given object.
     *
     * @see {@link domain.studies.StudyName.asyncCreate|asyncCreate()} when you need to create
     * a study within asynchronous code.
     */
    static create(obj) {
      var validation = EntityNameAndState.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new StudyName(obj);
    }

    /**
     * Used to list StudyNames.
     *
     * <p>A paged API is used to list studies. See below for more details.</p>
     *
     * @param {object} options={} - The options to use to list studies.
     *
     * @param {string} options.filter='' The filter to use on study names. Default is empty string.
     *
     * @param {string} options.sort='name' Studies can be sorted by `name` or by `state`. Values other than
     * these two yield an error. Use a minus sign prefix to sort in descending order.
     *
     * @param {Array<domain.studies.StudyName>} omit=[] - the list of names to filter out of the result
     * returned from the server.
     *
     * @returns {Promise<Array<domain.studies.StudyName>>}
     */
    static list(options={}, omit=[]) {
      return super.list(StudyName.url(), options, omit)
        .then(entities => entities.map(entity => StudyName.create(entity)));
    }
  }

  StudyName.SCHEMA = Object.assign(EntityNameAndState.SCHEMA, { id: 'StudyName'});

  return StudyName;
}

export default ngModule => ngModule.factory('StudyName', StudyNameFactory)
