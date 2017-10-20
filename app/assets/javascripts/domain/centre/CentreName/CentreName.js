/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/*
 * Angular factory for Centres.
 */
/* @ngInject */
function CentreNameFactory($q,
                           $log,
                           biobankApi,
                           EntityName,
                           DomainEntity,
                           DomainError,
                           CentreState) {

  /**
   * @classdesc A CentreName contains the ID, name and state for a Centre.
   *
   * Please do not use this constructor. It is meant for internal use.
   *
   * @class
   * @memberOf domain.centres
   * @extends domain.DomainEntity
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function CentreName(obj) {

    /**
     * The state can be one of: enabled, disabled, or retired.
     *
     * @name domain.centres.Centre#state
     * @type {domain.centres.CentreState}
     */
    this.state = CentreState.DISABLED;

    EntityName.call(this, obj);
  }

  CentreName.prototype = Object.create(EntityName.prototype);
  CentreName.prototype.constructor = CentreName;

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
  CentreName.create = function (obj) {
    var validation = EntityName.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new CentreName(obj);
  };

  CentreName.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
    const args = [ 'centres/names' ].concat(_.toArray(arguments));
    return DomainEntity.url.apply(null, args);
  };

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
   * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
   *        centres should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} [options.limit=10] The total number of centres to return per page. The maximum page size
   *        is 10. If a value larger than 10 is used then the response is an error.
   *
   * @param {Array<domain.EntityName>} omit - the list of names to filter out of the result returned
   *        from the server.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   *          domain.centres.Centre}.
   */
  CentreName.list = function (options, omit) {
    return EntityName.list(CentreName.url(), options, CentreName, omit);
  };

  /**
   * Used to query the Centre's current state.
   *
   * @returns {boolean} <code>true</code> if the Centre is in <code>disabled</code> state.
   */
  CentreName.prototype.isDisabled = function () {
    return (this.state === CentreState.DISABLED);
  };

  /**
   * Used to query the Centre's current state.
   *
   * @returns {boolean} <code>true</code> if the Centre is in <code>enabled</code> state.
   */
  CentreName.prototype.isEnabled = function () {
    return (this.state === CentreState.ENABLED);
  };

  return CentreName;
}

export default ngModule => ngModule.factory('CentreName', CentreNameFactory)
