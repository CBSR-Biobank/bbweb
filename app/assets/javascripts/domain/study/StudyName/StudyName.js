/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for Studies.
 */
/* @ngInject */
function StudyNameFactory($q,
                          $log,
                          biobankApi,
                          EntityName,
                          DomainEntity,
                          DomainError,
                          StudyState) {

  /**
   * @classdesc A StudyName contains the ID, name and state for a study.
   *
   * Please do not use this constructor. It is meant for internal use.
   *
   * @class
   * @memberOf domain.studies
   * @extends domain.DomainEntity
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function StudyName(obj) {

    /**
     * The state can be one of: enabled, disabled, or retired.
     *
     * @name domain.studies.Study#state
     * @type {domain.studies.StudyState}
     */
    this.state = StudyState.DISABLED;

    EntityName.call(this, obj);
  }

  StudyName.prototype = Object.create(EntityName.prototype);
  StudyName.prototype.constructor = StudyName;

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
  StudyName.create = function (obj) {
    var validation = EntityName.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new StudyName(obj);
  };

  StudyName.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
    const args = [ 'studies/names' ].concat(_.toArray(arguments));
    return DomainEntity.url.apply(null, args);
  };

  /**
   * Used to list StudyNames.
   *
   * <p>A paged API is used to list studies. See below for more details.</p>
   *
   * @param {object} options - The options to use to list studies.
   *
   * @param {string} [options.filter] The filter to use on study names. Default is empty string.
   *
   * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
   *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
   *        in descending order.
   *
   * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
   *        studies should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} [options.limit=10] The total number of studies to return per page. The maximum page size
   *        is 10. If a value larger than 10 is used then the response is an error.
   *
   * @param {Array<domain.EntityName>} omit - the list of names to filter out of the result returned
   *        from the server.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   *          domain.studies.Study}.
   */
  StudyName.list = function (options, omit) {
    return EntityName.list(StudyName.url(), options, StudyName, omit);
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>disabled</code> state.
   */
  StudyName.prototype.isDisabled = function () {
    return (this.state === StudyState.DISABLED);
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>enabled</code> state.
   */
  StudyName.prototype.isEnabled = function () {
    return (this.state === StudyState.ENABLED);
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>retired</code> state.
   */
  StudyName.prototype.isRetired = function () {
    return (this.state === StudyState.RETIRED);
  };

  return StudyName;
}

export default ngModule => ngModule.factory('StudyName', StudyNameFactory)
