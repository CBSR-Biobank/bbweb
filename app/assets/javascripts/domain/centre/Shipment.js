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
    'ShipmentState',
    'biobankApi',
    'centreLocationInfoSchema',
    'Specimen'
  ];

  function ShipmentFactory($q,
                           $log,
                           ConcurrencySafeEntity,
                           DomainError,
                           ShipmentState,
                           biobankApi,
                           centreLocationInfoSchema,
                           Specimen) {

    var schema = {
      'id': 'Shipment',
      'type': 'object',
      'properties': {
        'id':               { 'type': 'string' },
        'version':          { 'type': 'integer', 'minimum': 0 },
        'timeAdded':        { 'type': 'string' },
        'timeModified':     { 'type': [ 'string', 'null' ] },
        'state':            { 'type': 'string' },
        'courierName':      { 'type': 'string' },
        'trackingNumber':   { 'type': 'string' },
        'fromLocationInfo': {
          'type': 'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'toLocationInfo': {
          'type': 'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'timePacked':       { 'type': [ 'string', 'null' ] },
        'timeSent':         { 'type': [ 'string', 'null' ] },
        'timeReceived':     { 'type': [ 'string', 'null' ] },
        'timeUnpacked':     { 'type': [ 'string', 'null' ] }
      },
      'required': [
        'id',
        'version',
        'state',
        'courierName',
        'trackingNumber',
        'fromLocationInfo',
        'toLocationInfo'
      ]
    };

    /**
     * Use this contructor to create new Shipments to be persited on the server. Use
     * [create()]{@link domain.centres.Shipment.create} or [asyncCreate()]{@link
     * domain.centres.Shipment.asyncCreate} to create objects returned by the server.
     *
     * @classdesc Represents a transfer of {@link domain.participants.Specimen Specimens} and / or {@link
     * domain.containers.Container Containers} from one {@link domain.centres.Centre Centre} to another.
     *
     * @see domain.centres.ShipmentSpecimen
     * @see domain.centres.ShipmentContainer
     *
     * @class
     * @memberOf domain.centres
     * @extends domain.ConcurrencySafeEntity
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function Shipment(obj) {

      /**
       * The state this shipment is in.
       *
       * @name domain.centres.Shipment#state
       * @type {domain.centres.ShipmentState}
       */
      this.state = ShipmentState.CREATED;

      /**
       * The name of the courier company used to ship this package.
       *
       * @name domain.centres.Shipment#courierName
       * @type {string}
       */
      this.courierName = '';

      /**
       * The tracking number used by the courier company used to track the package.
       *
       * @name domain.centres.Shipment#trackingNumber
       * @type {string}
       */
      this.trackingNumber = '';

      /**
       * The information for the centre location which is sending the specimens.
       *
       * @name domain.centres.Shipment#fromLocationInfo
       * @type {domain.centres.CentreLocationInfo}
       */
      this.fromLocationInfo = undefined;

      /**
       * The information for the centre location which is receiving the specimens.
       *
       * @name domain.centres.Shipment#toLocationInfo
       * @type {domain.centres.CentreLocationInfo}
       */
      this.toLocationInfo = undefined;

      /**
       * The date and time when the shipment was packed.
       * @name domain.centres.Shipment#timePacked
       * @type {Date}
       */

      /**
       * The date and time when the shipment was sent.
       * @name domain.centres.Shipment#timeSent
       * @type {Date}
       */

      /**
       * The date and time when the shipment was received.
       * @name domain.centres.Shipment#timeReceived
       * @type {Date}
       */

      /**
       * The date and time when the shipment was unpacked.
       * @name domain.centres.Shipment#timeUpacked
       * @type {Date}
       */

      obj = obj || {};
      ConcurrencySafeEntity.call(this);
      _.extend(this, obj);
    }

    Shipment.prototype = Object.create(ConcurrencySafeEntity.prototype);
    Shipment.prototype.constructor = Shipment;

    /**
     * @private
     */
    Shipment.isValid = function(obj) {
      tv4.addSchema(centreLocationInfoSchema);
      tv4.addSchema(schema);
      return tv4.validate(obj, schema);
    };

    /**
     * Creates a Shipment, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Shipment} A new shipment.
     *
     * @see [asyncCreate()]{@link domain.centres.Shipment.asyncCreate} when you need to create
     * a shipment within asynchronous code.
     */
    Shipment.create = function (obj) {
      if (!Shipment.isValid(obj)) {
        $log.error('invalid object from server: ' + tv4.error);
        throw new DomainError('invalid object from server: ' + tv4.error);
      }

      return new Shipment(obj);
    };

    /**
     * Creates a Shipment from a server reply but first validates that it has a valid schema.
     *
     * <p>Meant to be called from within promise code.</p>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise} A new shipment wrapped in a promise.
     *
     * @see [create()]{@link domain.centres.Shipment.create} when not creating a shipment within
     * asynchronous code.
     */
    Shipment.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!Shipment.isValid(obj)) {
        $log.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else {
        deferred.resolve(new Shipment(obj));
      }

      return deferred.promise;
    };

    /**
     * Retrieves a Shipment from the server.
     *
     * @param {string} id the ID of the shipment to retrieve.
     *
     * @returns {Promise} The shipment within a promise.
     */
    Shipment.get = function (id) {
      if (!id) {
        throw new DomainError('shipment id not specified');
      }

      return biobankApi.get(uri(id)).then(function (reply) {
        return Shipment.asyncCreate(reply);
      });
    };

    /**
     * Used to list the shipments stored in the system.
     *
     * <p>A paged API is used to list shipments. See below for more details.</p>
     *
     * @param {String} centreId - The ID for the centre to list shipments for.
     *
     * @param {object} options - The options to use to list shipments.
     *
     * @param {string} options.courierFilter The filter to use on courier names. Default is empty string.
     *
     * @param {string} options.trackingNumberFilter The filter to use on tracking numbers. Default is empty
     * string.
     *
     * @param {string} options.stateFilter The filter to use on shipment state. Default is empty
     * string. See {@link domain.centres.ShipmentState ShipmentState} for valid values.
     *
     * @param {string} options.sortField Shipments can be sorted by 'courierName', 'trackingNumber' or by
     * 'state'. Values other than these two yield an error.
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
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    Shipment.list = function (centreId, options) {
      var url = uri() + 'list/' + centreId,
          params,
          validKeys = [
            'courierFilter',
            'trackingNumberFilter',
            'stateFilter',
            'sort',
            'page',
            'limit',
            'order'
          ];

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });

      return biobankApi.get(url, params).then(function(reply) {
        var deferred = $q.defer();
        try {
          // reply is a paged result
          reply.items = _.map(reply.items, function(obj){
            return Shipment.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid shipments from server');
        }
        return deferred.promise;
      });
    };

    /**
     * Creates a Shipment from a server reply but first validates that it has a valid schema.
     *
     * <p>A wrapper for {@link domian.centres.Shipment#asyncCreate}.</p>
     *
     * @see domain.ConcurrencySafeEntity.update
     */
    Shipment.prototype.asyncCreate = function (obj) {
      return Shipment.asyncCreate(obj);
    };

    /**
     * Adds a shipment to the system.
     *
     * @returns {Promise} The added shipment wrapped in a promise.
     */
    Shipment.prototype.add = function () {
      var json = { courierName:    this.courierName,
                   trackingNumber: this.trackingNumber,
                   fromLocationId: this.fromLocationInfo.locationId,
                   toLocationId:   this.toLocationInfo.locationId
                 };
      return biobankApi.post(uri(), json).then(function(reply) {
        return Shipment.asyncCreate(reply);
      });
    };

    /**
     * Removes this shipment from the system.
     *
     * @returns ???
     */
    Shipment.prototype.remove = function () {
      var url = sprintf('%s/%d', uri(this.id), this.version);
      return biobankApi.del(url);
    };

    /**
     * Updates the shipment's courier name.
     *
     * @param {string} courierName - The new courier name for this shipment.
     *
     * @returns {Promise} A copy of this shipment, but with the new courier name.
     */
    Shipment.prototype.updateCourierName = function (courierName) {
      return this.update.call(this, uri('courier', this.id), { courierName: courierName });
    };

    /**
     * Updates the shipment's tracking number.
     *
     * @param {string} trackingNumber - The new tracking number for this shipment.
     *
     * @returns {Promise} A copy of this shipment, but with the new tracking number.
     */
    Shipment.prototype.updateTrackingNumber = function (trackingNumber) {
      return this.update.call(this, uri('trackingnumber', this.id), { trackingNumber: trackingNumber });
    };

    /**
     * Updates the location this shipment is coming from.
     *
     * @param {string} fromLocation - The new location Id for where this shipment is coming from.
     *
     * @returns {Promise} A copy of this shipment, but with the new from location.
     */
    Shipment.prototype.updateFromLocation = function (fromLocation) {
      return this.update.call(this, uri('fromlocation', this.id), { locationId: fromLocation });
    };

    /**
     * Updates the location this shipment is going to.
     *
     * @param {string} toLocation - The new location Id for where this shipment is going to.
     *
     * @returns {Promise} A copy of this shipment, but with the new to location.
     */
    Shipment.prototype.updateToLocation = function (toLocation) {
      return this.update.call(this, uri('tolocation', this.id), { locationId: toLocation });
    };

    /**
     * Changes the state of this shipment.
     *
     * @param {ShipmentState|domain.centres.ShipmentState} the new state.
     *
     * @param {Date} [datetime] - the date and time this shipment took this state. Only required for the
     * following states: Packed, Sent, Received, Unpacked.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to Created.
     */
    Shipment.prototype.changeState = function (state, datetime) {
      var json = { newState: state };
      if (datetime) {
        _.extend(json, { datetime: datetime });
      }
      return this.update.call(this, uri('state', this.id), json);
    };

    /**
     * Changes the state of this shipment from CREATED to SENT.
     *
     * @param {Date} timePacked - the date and time this shipment was packed.
     *
     * @param {Date} timeSent - the date and time this shipment was sent.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to SENT.
     */
    Shipment.prototype.skipToStateSent = function (timePacked, timeSent) {
      var json = { timePacked: timePacked, timeSent: timeSent };
      return this.update.call(this, uri('state/skip-to-sent', this.id), json);
    };

    /**
     * Changes the state of this shipment from SENT to UNPACKED.
     *
     * @param {Date} timeReceived - the date and time this shipment was received.
     *
     * @param {Date} timeUnpacked - the date and time this shipment was unpacked.
     *
     * @returns {Promise} A copy of this shipment, but with the state set to UNPACKED.
     */
    Shipment.prototype.skipToStateUnpacked = function (timeReceived, timeUnpacked) {
      var json = { timeReceived: timeReceived, timeUnpacked: timeUnpacked };
      return this.update.call(this, uri('state/skip-to-unpacked', this.id), json);
    };

    /**
     * A predicate to test if the shipment's state is CREATED.
     *
     * @returns {boolean} TRUE if the state is CREATED.
     */
    Shipment.prototype.isCreated = function () {
      return this.state === ShipmentState.CREATED;
    };

    /**
     * A predicate to test if the shipment's state is PACKED.
     *
     * @returns {boolean} TRUE if the state is PACKED.
     */
    Shipment.prototype.isPacked = function () {
      return this.state === ShipmentState.PACKED;
    };

    /**
     * A predicate to test if the shipment's state is SENT.
     *
     * @returns {boolean} TRUE if the state is SENT.
     */
    Shipment.prototype.isSent = function () {
      return this.state === ShipmentState.SENT;
    };

    /**
     * A predicate to test if the shipment's state is NOT CREATED or UNPACKED.
     *
     * @returns {boolean} TRUE if the state is NOT CREATED or UNPACKED.
     */
    Shipment.prototype.isNotCreatedNorUnpacked = function () {
      return (this.state !== ShipmentState.CREATED) && (this.state !== ShipmentState.UNPACKED);
    };

    /**
     * Checks if a specimen inventory ID can be added to a shipment. It can be added if:
     *
     *  - it belongs to a valid specimen
     *  - the specimen is located at the same location that the shipment is coming from
     *  - the specimen is not already part of the shipment
     *
     *  @param {string} specimenInventoryId - the inventory ID of the specimen.
     *
     * @returns {Promise} The promise resolves to "true" if the specimen can be added to the shipment..
     */
    Shipment.prototype.canAddInventoryId = function (specimenInventoryId) {
      if (!specimenInventoryId) {
        throw new DomainError('specimen inventory id not specified');
      }

      return biobankApi.get(uri('specimens/canadd', this.id) + '/' + specimenInventoryId)
        .then(function (reply) {
          return Specimen.asyncCreate(reply);
        });
    };

    function uri(/* path, shipmentId */) {
      var shipmentId,
          result = '/shipments/',
          args = _.toArray(arguments),
          path;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        shipmentId = args.shift();
        result += '/' + shipmentId;
      }

      return result;
    }

    return Shipment;
  }

  return ShipmentFactory;
});
