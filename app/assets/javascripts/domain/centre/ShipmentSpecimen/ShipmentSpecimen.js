/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function ShipmentSpecimenFactory($q,
                                 $log,
                                 DomainEntity,
                                 ConcurrencySafeEntity,
                                 DomainError,
                                 ShipmentItemState,
                                 Specimen,
                                 biobankApi) {

  const SCHEMA = {
    'id': 'ShipmentSpecimen',
    'type': 'object',
    'properties': {
      'id':           { 'type': 'string' },
      'version':      { 'type': 'integer', 'minimum': 0 },
      'timeAdded':    { 'type': 'string' },
      'timeModified': { 'type': [ 'string', 'null' ] },
      'state':        { 'type': 'string' },
      'shipmentId':   { 'type': 'string' },
      'specimen':     { '$ref': 'Specimen' }
    },
    'required': [
      'id',
      'version',
      'state',
      'shipmentId',
      'specimen'
    ]
  };

  /**
   * Marks a specific {@link domain.participants.Specimen Specimen} as having been in a specific {@link
   * domain.centre.Shipment Shipment}.
   *
   * Use this contructor to create new Shipment Specimens to be persited on the server. Use [create()]{@link
   * domain.centres.ShipmentSpecimen.create} or [asyncCreate()]{@link
   * domain.centres.ShipmentSpecimen.asyncCreate} to create objects returned by the server.
   *
   * @see domain.centres.Shipment
   * @see domain.centres.ShipmentContainer
   *
   * @memberOf domain.centres
   * @extends domain.ConcurrencySafeEntity
   */
  class ShipmentSpecimen extends ConcurrencySafeEntity {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj, specimen) {

      /**
       * The state this shipment specimen is in.
       *
       * @name domain.centres.ShipmentSpecimen#state
       * @type {domain.centres.ShipmentItemState}
       * @protected
       */

      /**
       * The shipment this shipment specimen is in.
       *
       * @name domain.centres.ShipmentSpecimen#shipmentId
       * @type {string}
       * @protected
       */

      /**
       * The specimen this shipment specimen is linked to.
       *
       * @name domain.centres.ShipmentSpecimen#specimenId
       * @type {string}
       * @protected
       */

      /**
       * The shipment container this shipment specimen can be found in.
       *
       * @name domain.centres.ShipmentSpecimen#shipmentContainerId
       * @type {string}
       * @protected
       */

      super(Object.assign(
        {
          state: ShipmentItemState.PRESENT,
          shipmentId: null,
          specimen: null
        },
        obj));
      if (specimen) {
        this.specimen = specimen;
      }
    }

    /** @private */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [];
    }

    /**
     * Creates a Shipment Specimen, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {ShipmentSpecimen} A new shipment specimen.
     *
     * @see [asyncCreate()]{@link domain.centres.ShipmentSpecimen.asyncCreate} when you need to create
     * a shipment specimen within asynchronous code.
     */
    static create(obj) {
      var specimen, validation = ShipmentSpecimen.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }

      if (obj.specimen) {
        specimen = Specimen.create(obj.specimen);
      }

      return new ShipmentSpecimen(obj, specimen);
    }

    /**
     * Creates a shipment specimen from the specimen it represents.
     *
     * @param {domain.participants.Specimen} specimen - The specimen to base this shipment specimen on.
     *
     * @returns {ShipmentSpecimen} A shipment specimen.
     */
    static createFromSpecimen(specimen) {
      var obj = Object.assign(
        _.pick(specimen, 'amount', 'timeCreated'),
        {
          state:        ShipmentItemState.PRESENT,
          specimenId:   specimen.id,
          locationInfo: { locationId: specimen.locationId },
          status:       specimen.status
        });

      return new ShipmentSpecimen(obj);
    }

    /**
     * Creates a Shipment from a server reply but first validates that it has a valid schema.
     *
     * <p>Meant to be called from within promise code.</p>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise} A new shipment specimen wrapped in a promise.
     *
     * @see [create()]{@link domain.centres.ShipmentSpecimen.create} when not creating a shipment specimen
     * within asynchronous code.
     */
    static asyncCreate(obj) {
      var result;

      try {
        result = ShipmentSpecimen.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    static url(...paths) {
      const args = [ 'shipments/specimens' ].concat(paths);
      return super.url(...args);
    }

    /**
     * Retrieves a Shipment Specimen from the server.
     *
     * @param {string} id the ID of the shipment specimen to retrieve.
     *
     * @returns {Promise} The shipment specimen within a promise.
     */
    static get(id) {
      if (!id) {
        throw new DomainError('shipment specimen id not specified');
      }

      return biobankApi.get(ShipmentSpecimen.url(id)).then(ShipmentSpecimen.asyncCreate);
    }

    /**
     * Used to list the specimens in a shipment.
     *
     * <p>A paged API is used to list these specimen. See below for more details.</p>
     *
     * @param {string} shipmentId - The ID of the shipment these specimen shipments belong to.
     *
     * @param {object} options - The options to use to list shipments.
     *
     * @param {domain.centres.ShipmentItemState} options.stateFilter - The shipment item state to filter
     * specimens by.
     *
     * @param {string} options.sortField Shipments can be sorted by 'inventoryId' or by 'state'. Values other
     * than these two yield an error.
     *
     * @param {int} options.page If the total results are longer than limit, then page selects which
     * shipments should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.limit The total number of shipments to return per page. The maximum page size
     * is 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return {Promise} A promise. If the promise succeeds then a paged result is returned.
     */
    static list(shipmentId, options) {
      var url = ShipmentSpecimen.url(shipmentId),
          params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit',
            'order'
          ];

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys),
                        (value) => value === '');

      return biobankApi.get(url, params).then((reply) => {
        var deferred = $q.defer();
        try {
          // reply is a paged result
          reply.items = reply.items.map((obj) => ShipmentSpecimen.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid shipment specimens from server');
        }
        return deferred.promise;
      });
    }

    /**
     * Changes the state of this shipment to <code>Lost</code>.
     *
     * @see [ShipmentState]{@link domain.centres.ShipmentState}
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    remove() {
      var url = ShipmentSpecimen.url(this.shipmentId, this.id, this.version);
      return biobankApi.del(url);
    }
  }

  return ShipmentSpecimen;
}

export default ngModule => ngModule.factory('ShipmentSpecimen', ShipmentSpecimenFactory)
