/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  SpecimenFactory.$inject = [
    '$q',
    '$log',
    'DomainEntity',
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
                           DomainEntity,
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
     * @param {domain.studies.CollectionSpecimenDescription} [specimenDescription] - The specimen spec from
     *        the collection event type this specimen represents. An Undefined value is also valid.
     *
     */
    function Specimen(obj, specimenDescription) {

      /**
       * The unique inventory ID for this specimen.
       *
       * @name domain.participants.Specimen#inventoryId
       * @type {string}
       * @default null
       */
      this.inventoryId = null;

      /**
       * The ID corresponding to the the {@link domain.studies.CollectionSpecimenDescription SpecimenDescription}
       * for this specimen.
       *
       * @name domain.participants.Specimen#specimenDescriptionId
       * @type {string}
       * @default null
       */
      this.specimenDescriptionId = null;

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
       * domain.studies.CollectionSpecimenDescription#units CollectionSpecimenDescription#units}.
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

      ConcurrencySafeEntity.call(this, Specimen.SCHEMA, obj);

      if (specimenDescription) {
        this.setSpecimenDescription(specimenDescription);
      }
    }

    Specimen.prototype = Object.create(ConcurrencySafeEntity.prototype);
    Specimen.prototype.constructor = Specimen;

    Specimen.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
      const args = [ 'participants/cevents/spcs' ].concat(_.toArray(arguments));
      return DomainEntity.url.apply(null, args);
    };

    /**
     * Used for validation.
     */
    Specimen.SCHEMA = {
      'id': 'Specimen',
      'type': 'object',
      'properties': {
        'id':                       { 'type': 'string' },
        'inventoryId':              { 'type': 'string' },
        'specimenDescriptionId':    { 'type': 'string' },
        'specimenDescriptionName':  { 'type': [ 'string', 'null' ] },
        'specimenDescriptionUnits': { 'type': [ 'string', 'null' ] },
        'version':                  { 'type': 'integer', 'minimum': 0 },
        'timeAdded':                { 'type': 'string' },
        'timeModified':             { 'type': [ 'string', 'null' ] },
        'originLocationInfo':       {
          'type':  'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'locationInfo':             {
          'type':  'object',
          'items': { '$ref': 'CentreLocationInfo' }
        },
        'containerId':              { 'type': [ 'string', 'null' ] },
        'postitionId':              { 'type': [ 'string', 'null' ] },
        'timeCreated':              { 'type': 'string' },
        'amount':                   { 'type': 'number' },
        'isDefaultAmount':          { 'type': [ 'boolean', 'null' ] },
        'state':                    { 'type': 'string' }
      },
      'required': [
        'id',
        'inventoryId',
        'specimenDescriptionId',
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
      return ConcurrencySafeEntity.isValid(Specimen.SCHEMA,
                                           [ centreLocationInfoSchema, Specimen.schema ],
                                           obj);
    };

    /**
     * Creates a Specimen, but first it validates #obj to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {CollectionSpecimenDescription} [specimenDescription] - The specimen spec from the collection
     * event type this specimen represents.
     *
     * @returns {Specimen} A new specimen.
     *
     * @see [asyncCreate()]{@link domain.participants.Specimen.asyncCreate} when you need to create
     * a specimen within asynchronous code.
     */
    Specimen.create = function (obj, specimenDescription) {
      var validation = Specimen.isValid(obj);
      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }

      return new Specimen(obj, specimenDescription);
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
      var result;

      try {
        result = Specimen.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
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

      return biobankApi.get(Specimen.url(id)).then(function (reply) {
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

      return biobankApi.get(Specimen.url() + '/invid/' + inventoryId).then(function (reply) {
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
      return biobankApi.get(Specimen.url(ceventId), params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = reply.items.map((obj) => Specimen.create(obj));
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

      json.specimenData = specimens.map((specimen) => {
        var result = _.pick(specimen, 'inventoryId', 'specimenDescriptionId', 'timeCreated', 'amount');
        result.locationId = specimen.locationInfo.locationId;
        return result;
      });
      return biobankApi.post(Specimen.url(ceventId), json);
    };

    /**
     * Sets the specimen spec ID for this Specimen.
     *
     * @param {domain.studies.CollectionSpecimenDescription} specimenDescription The specimen specifications associated with
     * this specimen.
     *
     * @returns {undefined}
     */
    Specimen.prototype.setSpecimenDescription = function (specimenDescription) {
      this.specimenDescriptionId = specimenDescription.id;
      this.specimenDescription = specimenDescription;
    };

    /**
     * Returns the name for a specimen of this type.
     *
     * @returns {number} The amount used for this specimen.
     */
    Specimen.prototype.name = function () {
      if (_.isUndefined(this.specimenDescription)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenDescription.name;
    };

    /**
     * Returns the default amount that should be collected for a specimen of this type.
     *
     * @returns {number} The amount used for this specimen.
     */
    Specimen.prototype.defaultAmount = function () {
      if (_.isUndefined(this.specimenDescription)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenDescription.amount;
    };

    /**
     * Removes the specimen from the system.
     *
     * @return {Promise} Resolves to true if the specimen was removed successfully.
     */
    Specimen.prototype.remove = function (collectionEventId) {
      var url;
      if (!collectionEventId) {
        throw new DomainError('collection event id not specified');
      }
      url = Specimen.url(collectionEventId, this.id, this.version);
      return biobankApi.del(url);
    };

    /** return constructor function */
    return Specimen;
  }

  return SpecimenFactory;
});
