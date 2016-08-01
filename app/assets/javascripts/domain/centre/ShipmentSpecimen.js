/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _       = require('lodash'),
      tv4     = require('tv4'),
      sprintf = require('sprintf').sprintf;

  ShipmentFactory.$inject = [
    '$q',
    '$log',
    'ConcurrencySafeEntity',
    'DomainError',
    'ShipmentItemState',
    'biobankApi'
  ];

  function ShipmentFactory($q,
                           $log,
                           ConcurrencySafeEntity,
                           DomainError,
                           ShipmentItemState,
                           biobankApi) {

    var schema = {
      'id': 'Shipment',
      'type': 'object',
      'properties': {
        'id':                  { 'type': 'string' },
        'version':             { 'type': 'integer', 'minimum': 0 },
        'timeAdded':           { 'type': 'string' },
        'timeModified':        { 'type': [ 'string', 'null' ] },
        'state':               { 'type': 'string' },
        'shipmentId':          { 'type': 'string' },
        'specimenId':          { 'type': 'string' },
        'shipmentContainerId': { 'type': [ 'string', 'null' ] },
        'locationId':          { 'type': 'string' },
        'locationName':        { 'type': 'string' },
        'timeCreated':         { 'type': 'string' },
        'amount':              { 'type': 'number' },
        'units':               { 'type': 'string' },
        'status':              { 'type': 'string' }
      },
      'required': [
        'id',
        'version',
        'state',
        'shipmentId',
        'specimenId',
        'locationId',
        'locationName',
        'timeCreated',
        'amount',
        'units',
        'status'
      ]
    };

    /**
     * Use this contructor to create new Shipment Specimens to be persited on the server. Use [create()]{@link
     * domain.centres.ShipmentSpecimen.create} or [asyncCreate()]{@link
     * domain.centres.ShipmentSpecimen.asyncCreate} to create objects returned by the server.
     *
     * @classdesc Marks a specific {@link domain.participants.Specimen Specimen} as having been in a specific
     * {@link domain.centre.Shipment Shipment}.
     *
     * @see domain.centres.Shipment
     * @see domain.centres.ShipmentContainer
     *
     * @class
     * @memberOf domain.centres
     * @extends domain.ConcurrencySafeEntity
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function ShipmentSpecimen(obj, shipment) {

      /**
       * The state this shipment specimen is in.
       *
       * @name domain.centres.ShipmentSpecimen#state
       * @type {domain.centres.ShipmentItemState}
       * @protected
       */
      this.state = ShipmentItemState.PRESENT;

      /**
       * The shipment this shipment specimen is in.
       *
       * @name domain.centres.ShipmentSpecimen#shipmentId
       * @type {string}
       * @protected
       */
      this.shipmentId = null;

      /**
       * The specimen this shipment specimen is linked to.
       *
       * @name domain.centres.ShipmentSpecimen#specimenId
       * @type {string}
       * @protected
       */
      this.specimenId = null;

      /**
       * The shipment container this shipment specimen can be found in.
       *
       * @name domain.centres.ShipmentSpecimen#shipmentContainerId
       * @type {string}
       * @protected
       */

      /**
       * The location ID where this specimen is currently at.
       *
       * @name domain.centres.ShipmentSpecimen#locationId
       * @type {string}
       * @protected
       */
      this.locationId = null;

      /**
       * The name of the location (contains the centre name also) where this specimen is currently
       * at.
       *
       * @name domain.centres.ShipmentSpecimen#locationName
       * @type {string}
       * @protected
       */
      this.locationName = null;

      /**
       * The date and time this specimen was created.
       *
       * @name domain.centres.ShipmentSpecimen#timeCreated
       * @type {Date}
       * @protected
       *
       * @see [Specimen.timeCreated]{@link domain.participants.Specimen#timeCreated}
       */
      this.timeCreated = null;

      /**
       * The amount this specimen contains.
       *
       * @name domain.centres.ShipmentSpecimen#amount
       * @type {number}
       * @protected
       *
       * @see [Specimen.amount]{@link domain.participants.Specimen#amount}
       */
      this.amount = null;

      /**
       * The units this specimen is measured in.
       *
       * @name domain.centres.ShipmentSpecimen#units
       * @type {number}
       * @protected
       *
       * @see [CollectionSpecimenSpec.units]{@link domain.studies.CollectionSpecimenSpec#units}
       */
      this.units = null;

      /**
       * The status for this specimen
       *
       * @name domain.centres.ShipmentSpecimen#status
       * @type {number}
       * @protected
       *
       * @see [Specimen.status]{@link domain.participants.Specimen#status}
       */
      this.status = null;

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, obj);
    }

    ShipmentSpecimen.prototype = Object.create(ConcurrencySafeEntity.prototype);
    ShipmentSpecimen.prototype.constructor = ShipmentSpecimen;

    /**
     * @private
     */
    ShipmentSpecimen.isValid = function(obj) {
      return tv4.validate(obj, schema);
    };

    /**
     * Creates a Shipment Specimen, but first it validates <code>obj</code> to ensure that it has a valid
     * schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {ShipmentSpecimen} A new shipment specimen.
     *
     * @see [asyncCreate()]{@link domain.centres.ShipmentSpecimen.asyncCreate} when you need to create
     * a shipment specimen within asynchronous code.
     */
    ShipmentSpecimen.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        $log.error('invalid object from server: ' + tv4.error);
        throw new DomainError('invalid object from server: ' + tv4.error);
      }

      return new ShipmentSpecimen(obj);
    };

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
    ShipmentSpecimen.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        $log.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else {
        deferred.resolve(new ShipmentSpecimen(obj));
      }

      return deferred.promise;
    };

    /**
     * Retrieves a Shipment Specimen from the server.
     *
     * @param {string} id the ID of the shipment specimen to retrieve.
     *
     * @returns {Promise} The shipment specimen within a promise.
     */
    ShipmentSpecimen.get = function (id) {
      if (!id) {
        throw new DomainError('shipment specimen id not specified');
      }

      return biobankApi.get(uri(id)).then(function (reply) {
        return ShipmentSpecimen.asyncCreate(reply);
      });
    };

    /**
     * Used to list the specimens in a shipment.
     *
     * <p>A paged API is used to list these specimen. See below for more details.</p>
     *
     * @param {object} options - The options to use to list shipments.
     *
     * @param {string} options.sortField Shipments can be sorted by 'inventoryId' or by 'state'. Values other
     * than these two yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * shipments should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of shipments to return per page. The maximum page size
     * is 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    ShipmentSpecimen.list = function (shipmentId, options) {
      var url = uri(shipmentId),
          params,
          validKeys = [
            'sort',
            'page',
            'pageSize',
            'order'
          ];

      options = options || {};
      params = _.pick(options, validKeys);

      return biobankApi.get(url, params).then(function(reply) {
        var deferred = $q.defer();
        try {
          // reply is a paged result
          reply.items = _.map(reply.items, function(obj){
            return ShipmentSpecimen.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid shipment specimens from server');
        }
        return deferred.promise;
      });
    };

    /**
     * Creates a Shipment Specimen from a server reply but first validates that it has a valid schema.
     *
     * <p>A wrapper for {@link domian.centres.Shipment#asyncCreate}.</p>
     *
     * @see domain.ConcurrencySafeEntity.update
     */
    ShipmentSpecimen.prototype.asyncCreate = function (obj) {
      return ShipmentSpecimen.asyncCreate(obj);
    };

    /**
     * Adds a shipment specimen to the system.
     *
     * @returns {Promise} The added shipment wrapped in a promise.
     */
    ShipmentSpecimen.prototype.add = function (shipmentId, specimenId, shipmentContainerId) {
      var json = { shipmentId: shipmentId, specimenId: specimenId };
      if (shipmentContainerId) {
        _.extend(json, { shipmentContainerId: shipmentContainerId });
      }
      return biobankApi.post(uri(shipmentId), json).then(function(reply) {
        return ShipmentSpecimen.asyncCreate(reply);
      });
    };

    /**
     * Changes the state of this shipment to <code>Lost</code>.
     *
     * @see [ShipmentState]{@link domain.centres.ShipmentState}
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    ShipmentSpecimen.prototype.remove = function () {
      var url = sprintf('%s/%s/%d', uri(this.shipmentId), this.id, this.version);
      return biobankApi.del(url);
    };

    /**
     * Updates the shipment container that holds this specimen.
     *
     * @param {String} shipmentContainerId - the ID of the shipment container that holds this
     * specimen.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    ShipmentSpecimen.prototype.updateShipmentContainer = function (shipmentContainerId) {
      return this.update.call(this, uri('container', this.shipmentId, this.id), {});
    };

    /**
     * Updates the state of this shipment specimen to be RECEIVED.
     *
     * <p>Note that only specimens in unpacked shipments can have the state updated.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    ShipmentSpecimen.prototype.received = function () {
      return this.update.call(this, uri('received', this.shipmentId, this.id), {});
    };

    /**
     * Updates the state of this shipment specimen to be MISSING.
     *
     * <p>Note that only specimens in unpacked shipments can have the state updated.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    ShipmentSpecimen.prototype.missing = function () {
      return this.update.call(this, uri('missing', this.shipmentId, this.id), {});
    };

    /**
     * Updates the state of this shipment specimen to be EXTRA.
     *
     * <p>Note that only specimens in unpacked shipments can have the state updated.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Lost.
     */
    ShipmentSpecimen.prototype.extra = function () {
      return this.update.call(this, uri('extra', this.shipmentId, this.id), {});
    };

    function uri(/* path, shipmentId, shipmentSpecimenId */) {
      var path,
          shipmentId,
          shipmentSpecimenId,
          result = '/shipments/specimens',
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        shipmentId = args.shift();
        result += '/' + shipmentId;
      }

      if (args.length > 0) {
        shipmentSpecimenId = args.shift();
        result += '/' + shipmentSpecimenId;
      }

      return result;
    }

    return ShipmentSpecimen;
  }

  return ShipmentFactory;
});
