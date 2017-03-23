/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash', 'tv4', 'sprintf-js'], function(_, tv4, sprintf) {
  'use strict';

  SpecimenFactory.$inject = [
    '$q',
    '$log',
    'ConcurrencySafeEntity',
    'DomainError',
    'biobankApi',
    'centreLocationInfoSchema'
  ];

  /**
   * Factory for Specimens.
   *
   * @param {$q} $q - AngularJS service for asynchronous functions.
   *
   * @param {ConcurrencySafeEntity} ConcurrencySafeEntity - Base class for domain objects.
   *
   * @param {biobankApi} biobankApi - service that communicates with the Biobank server.
   *
   * @returns {Factory} The AngularJS factory function.
   */
  function SpecimenFactory($q,
                           $log,
                           ConcurrencySafeEntity,
                           DomainError,
                           biobankApi,
                           centreLocationInfoSchema) {

    /**
     * Use this contructor to create new Specimens to be persited on the server. Use [create()]{@link
     * domain.participants.Specimen.create} or [asyncCreate()]{@link domain.participants.Specimen.asyncCreate}
     * to create objects returned by the server.
     *
     * @classdesc Represents something that was obtained from a {@link domain.participant.Participant} in a
     * {@link domain.study.Study}.
     *
     * @class
     * @memberOf domain.participants
     * @extends domain.ConcurrencySafeEntity
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     *        this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.studies.CollectionSpecimenSpec} [specimenSpec] - The specimen spec from the collection
     *        event type this specimen represents. An Undefined value is also valid.
     *
     */
    function Specimen(obj, specimenSpec) {

      /**
       * The unique inventory ID for this specimen.
       *
       * @name domain.participants.Specimen#inventoryId
       * @type {string}
       * @default null
       */
      this.inventoryId = null;

      /**
       * The ID corresponding to the the {@link domain.studies.CollectionSpecimenSpec SpecimenSpec}
       * for this specimen.
       *
       * @name domain.participants.Specimen#specimenSpecId
       * @type {string}
       * @default null
       */
      this.specimenSpecId = null;

      /**
       * The location of the {@link domain.participants.Centre} where this specimen was created.
       * @name domain.participants.Specimen#originLocationId
       * @type {string}
       */
      this.originLocationInfo = null;

      /**
       * The location of the {@link domain.participants.Centre} where this specimen is currently located.
       * @name domain.participants.Specimen#locationId
       * @type {string}
       */
      this.locationInfo = null;

      /**
       * The ID of the {@link domain.participants.Container} this specimen is stored in.
       * @name domain.participants.Specimen#containerId
       * @type {string}
       */

      /**
       * The {@link domain.centres.ContainerSchemaPosition} (i.e. position or label) this specimen has in its
       * container.
       * @name domain.participants.Specimen#positionId
       * @type {string}
       */

      /**
       * The date and time when the specimen was physically created. Not necessarily when this specimen was
       * added to the application.
       * @name domain.participants.Specimen#timeCreated
       * @type {Date}
       */
      this.timeCreated = null;

      /**
       * The amount this specimen contains, in units specified in {@link
       * domain.studies.CollectionSpecimenSpec#units CollectionSpecimenSpec#units}.
       *
       * @name domain.participants.Specimen#amount
       * @type {number}
       */
      this.amount = null;

      /**
       * The state for this specimen. One of: Usable or Unusable.
       * @name domain.participants.Specimen#state.
       * @type {string}
       */
      this.state = null;

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, obj);

      if (specimenSpec) {
        this.setSpecimenSpec(specimenSpec);
      }
    }

    Specimen.prototype = Object.create(ConcurrencySafeEntity.prototype);
    Specimen.prototype.constructor = Specimen;

    /**
     * Used for validation.
     */
    Specimen.schema = {
      'id': 'Specimen',
      'type': 'object',
      'properties': {
        'id':               { 'type': 'string' },
        'inventoryId':      { 'type': 'string' },
        'specimenSpecId':   { 'type': 'string' },
        'version':          { 'type': 'integer', 'minimum': 0 },
        'timeAdded':        { 'type': 'string' },
        'timeModified':     { 'type': [ 'string', 'null' ] },
        'originLocationInfo': {
          'type': 'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'locationInfo': {
          'type': 'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'containerId':      { 'type': [ 'string', 'null' ] },
        'postitionId':      { 'type': [ 'string', 'null' ] },
        'timeCreated':      { 'type': 'string' },
        'amount':           { 'type': 'number' },
        'state':            { 'type': 'string' }
      },
      'required': [
        'id',
        'inventoryId',
        'specimenSpecId',
        'version',
        'timeCreated',
        'state',
        'originLocationInfo',
        'locationInfo'
      ]
    };

    /**
     * @private
     */
    Specimen.isValid = function(obj) {
      tv4.addSchema(centreLocationInfoSchema);
      tv4.addSchema(Specimen.schema);
      return tv4.validate(obj, Specimen.schema);
    };

    /**
     * Creates a Specimen, but first it validates #obj to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {CollectionSpecimenSpec} [specimenSpec] - The specimen spec from the collection event type this
     * specimen represents.
     *
     * @returns {Specimen} A new specimen.
     *
     * @see [asyncCreate()]{@link domain.participants.Specimen.asyncCreate} when you need to create
     * a specimen within asynchronous code.
     */
    Specimen.create = function (obj, specimenSpec) {
      if (!Specimen.isValid(obj)) {
        $log.error('invalid object from server: ' + tv4.error);
        throw new DomainError('invalid object from server: ' + tv4.error);
      }

      return new Specimen(obj, specimenSpec);
    };

    /**
     * Creates a Specimen from a server reply but first validates that it has a valid schema.
     *
     * <p>Meant to be called from within promise code.</p>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise} A new specimen wrapped in a promise.
     *
     * @see [create()]{@link domain.participants.Specimen.create} when not creating a Specimen within
     * asynchronous code.
     */
    Specimen.asyncCreate = function (obj) {
      if (!Specimen.isValid(obj)) {
        $log.error('invalid object from server: ' + tv4.error);
        return $q.reject('invalid object from server: ' + tv4.error);
      }

      return  $q.when(new Specimen(obj));
    };

    /**
     * Retrieves a Specimen from the server.
     *
     * @param {string} id the ID of the specimen to retrieve.
     *
     * @returns {Promise} The specimen within a promise.
     */
    Specimen.get = function (id) {
      if (!id) {
        throw new DomainError('specimen id not specified');
      }

      return biobankApi.get(uri(id)).then(function (reply) {
        return Specimen.asyncCreate(reply);
      });
    };

    /**
     * Retrieves a Specimen with the given inventory ID from the server.
     *
     * @param {string} inventoryId the inventory ID for the specimen.
     *
     * @returns {Promise} The specimen within a promise.
     */
    Specimen.getByInventoryId = function (inventoryId) {
      if (!inventoryId) {
        throw new DomainError('specimen inventory id not specified');
      }

      return biobankApi.get(uri() + '/invid/' + inventoryId).then(function (reply) {
        return Specimen.asyncCreate(reply);
      });
    };

    /**
     * Used to list the specimens contained in a [CollectionEvent]{@link domain.CollectionEvent}.
     *
     * <p>A paged API is used to list specimens. See below for more details.</p>
     *
     * @param {string} ceventId - the CollectionEvent ID that all the specimens belong to.
     *
     * @param {object} options - The options to use to list specimens.
     *
     * @param {string} options.sort - The field to sort the specimens by: one of: <code>id</code>,
     *        <code>timeCreated</code>, <code>state</code>. Use a minus sign prefix to sort in descending
     *        order.
     *
     * @param {number} options.page - The page to retrieve when there are more than <code>options.limit</code>
     *        specimens.
     *
     * @param {number} options.limit - The maximum number of specimens returned per page.
     *
     * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type [Specimen]{@link
     *          domain.Specimen}.
     */
    Specimen.list = function (ceventId, options) {
      var params,
          validKeys = [
            'sort',
            'page',
            'limit'
          ];

      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });
      return biobankApi.get(uri(ceventId), params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return Specimen.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid specimens from server');
        }
        return deferred.promise;
      });
    };

    /**
     * Adds specimens to a collection event.
     *
     * <p>This method should only be used to add collected specimens to a collection event.</p>
     *
     * @param {string} ceventId - the CollectionEvent ID that the specimens will be added to.
     *
     * @param {Array.<Specimens>} - The specimens to add. Only new specimens can be added.
     *
     * @returns {undefined}
     */
    Specimen.add = function (ceventId, specimens) {
      var json = { collectionEventId: ceventId };

      json.specimenData = _.map(specimens, function (specimen) {
        return _.pick(specimen, 'inventoryId', 'specimenSpecId', 'timeCreated', 'locationId', 'amount');
      });
      return biobankApi.post(uri(ceventId), json);
    };

    /**
     * Sets the specimen spec ID for this Specimen.
     *
     * @param {domain.studies.CollectionSpecimenSpec} specimenSpec The specimen specifications associated with
     * this specimen.
     *
     * @returns {undefined}
     */
    Specimen.prototype.setSpecimenSpec = function (specimenSpec) {
      this.specimenSpecId = specimenSpec.uniqueId;
      this.specimenSpec = specimenSpec;
    };

    /**
     * Returns the name for a specimen of this type.
     *
     * @returns {number} The amount used for this specimen.
     */
    Specimen.prototype.name = function () {
      if (_.isUndefined(this.specimenSpec)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenSpec.name;
    };

    /**
     * Returns the default amount that should be collected for a specimen of this type.
     *
     * @returns {number} The amount used for this specimen.
     */
    Specimen.prototype.defaultAmount = function () {
      if (_.isUndefined(this.specimenSpec)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenSpec.amount;
    };

    /**
     * Whether or not the amount for this specimen is the default amount.
     *
     * @returns {boolean} True if the amount is the default.
     */
    Specimen.prototype.isDefaultAmount = function () {
      if (_.isUndefined(this.specimenSpec)) {
        throw new DomainError('specimen spec not assigned');
      }
      return (this.amount === this.specimenSpec.amount);
    };

    /**
     * Returns the units a specimen of this type.
     *
     * @returns {string} The units used for this specimen.
     */
    Specimen.prototype.units = function () {
      if (_.isUndefined(this.specimenSpec)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenSpec.units;
    };

    /**
     * Removes the specimen from the system.
     *
     * @return {Promise} Resolves to true if the specimen was removed successfully.
     */
    Specimen.prototype.remove = function () {
      var url = sprintf.sprintf('%s/%s/%d', uri(this.collectionEventId), this.id, this.version);
      return biobankApi.del(url);
    };

    function uri(/* specimenId */) {
      var specimenId,
          result = '/participants/cevents/spcs',
          args = _.toArray(arguments);

      if (args.length > 0) {
        specimenId = args.shift();
        result += '/' + specimenId;
      }

      return result;
    }

    /** return constructor function */
    return Specimen;
  }

  return SpecimenFactory;
});
