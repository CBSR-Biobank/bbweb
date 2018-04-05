/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

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
/* @ngInject */
function SpecimenFactory($q,
                         $log,
                         DomainEntity,
                         ConcurrencySafeEntity,
                         DomainError,
                         biobankApi,
                         centreLocationInfoSchema) {


  /*
   * Used for validation.
   */
  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'Specimen',
    properties: {
      'slug':                     { 'type': 'string' },
      'inventoryId':              { 'type': 'string' },
      'specimenDescriptionId':    { 'type': 'string' },
      'specimenDescriptionName':  { 'type': [ 'string', 'null' ] },
      'specimenDescriptionUnits': { 'type': [ 'string', 'null' ] },
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
      'state':                    { 'type': 'string' },
      'eventTypeName':            { 'type': 'string' }
    },
    required: [
      'slug',
      'inventoryId',
      'specimenDescriptionId',
      'state',
      'originLocationInfo',
      'locationInfo',
      'eventTypeName'
    ]
  });

  /**
   * Represents something that was obtained from a {@link domain.participant.Participant Participant} from a
   * particular {@link domain.studies.Study Study}.
   *
   * Use this contructor to create new Specimens to be persited on the server. Use [create()]{@link
   * domain.participants.Specimen.create} or [asyncCreate()]{@link domain.participants.Specimen.asyncCreate}
   * to create objects returned by the server.
   *
   * @memberOf domain.participants
   * @extends domain.ConcurrencySafeEntity
   */
  class Specimen extends ConcurrencySafeEntity {

    /**
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     *        this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.studies.CollectionSpecimenDescription} [specimenDescription] - The specimen spec from
     *        the collection event type this specimen represents. An Undefined value is also valid.
     */
    constructor(obj, specimenDescription) {
      /**
       * The unique inventory ID for this specimen.
       *
       * @name domain.participants.Specimen#inventoryId
       * @type {string}
       * @default null
       */

      /**
       * The ID corresponding to the the {@link domain.studies.CollectionSpecimenDescription
       * SpecimenDescription} for this specimen.
       *
       * @name domain.participants.Specimen#specimenDescriptionId
       * @type {string}
       * @default null
       */

      /**
       * The location of the {@link domain.participants.Centre} where this specimen was created.
       * @name domain.participants.Specimen#originLocationId
       * @type {string}
       */

      /**
       * The location of the {@link domain.participants.Centre} where this specimen is currently located.
       * @name domain.participants.Specimen#locationId
       * @type {string}
       */

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

      /**
       * The amount this specimen contains, in units specified in {@link
       * domain.studies.CollectionSpecimenDescription#units CollectionSpecimenDescription#units}.
       *
       * @name domain.participants.Specimen#amount
       * @type {number}
       */

      /**
       * The state for this specimen. One of: Usable or Unusable.
       * @name domain.participants.Specimen#state.
       * @type {string}
       */

      super(Object.assign(
              {
                inventoryId:           null,
                specimenDescriptionId: null,
                originLocationInfo:    null,
                locationInfo:          null,
                timeCreated:           null,
                amount:                null,
                state:                 null
              },
              obj));

      if (specimenDescription) {
        this.setSpecimenDescription(specimenDescription);
      }
    }

    static url(...paths) {
      const args = [ 'participants/cevents/spcs' ].concat(paths);
      return super.url(...args);
    }

    /**
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    static additionalSchemas() {
      return [ centreLocationInfoSchema ];
    }

    /**
     * Creates a Specimen, but first it validates #obj to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {CollectionSpecimenDescription} [specimenDescription] - The specimen spec from the collection
     * event type this specimen represents.
     *
     * @returns {domain.participants.Specimen} A new specimen.
     *
     * @see {@link domain.participants.Specimen.asyncCreate asyncCreate()} when you need to create
     * a specimen within asynchronous code.
     */
    static create(obj, specimenDescription) {
      var validation = Specimen.isValid(obj);
      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }

      return new Specimen(obj, specimenDescription);
    }

    /**
     * Creates a Specimen from a server reply but first validates that it has a valid schema.
     *
     * Meant to be called from within promise code.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.participants.Specimen>}
     *
     * @see {@link domain.participants.Specimen.create create()} when not creating a Specimen within
     * asynchronous code.
     */
    static asyncCreate(obj) {
      var result;

      try {
        result = Specimen.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a Specimen from the server.
     *
     * @param {string} slug the slug of the study to retrieve.
     *
     * @returns {Promise<domain.participants.Specimen>}
     */
    static get(slug) {
      if (!slug) {
        throw new DomainError('slug not specified');
      }

      return biobankApi.get(Specimen.url('get', slug)).then((reply) => Specimen.asyncCreate(reply));
    }

    /**
     * Used to list the specimens contained in a [CollectionEvent]{@link domain.CollectionEvent}.
     *
     * A paged API is used to list specimens. See below for more details.
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
     * @returns {Promise<common.controllers.PagedListController.PagedResult<domain.participants.Specimen>>}
     */
    static list(ceventSlug, options) {
      var params,
          validKeys = [
            'sort',
            'page',
            'limit'
          ];

      params = _.omitBy(_.pick(options, validKeys), (value) => value === '');
      return biobankApi.get(Specimen.url(ceventSlug), params).then((reply) => {
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
    }

    /**
     * Adds specimens to a collection event.
     *
     * This method should only be used to add collected specimens to a collection event.
     *
     * @param {string} ceventId - the CollectionEvent ID that the specimens will be added to.
     *
     * @param {Array<domain.participants.Specimens>} - The specimens to add. Only new specimens can be added.
     *
     * @returns {undefined}
     */
    static add(ceventId, specimens) {
      var json = { collectionEventId: ceventId };

      json.specimenData = specimens.map((specimen) => {
        var result = _.pick(specimen, 'inventoryId', 'specimenDescriptionId', 'timeCreated', 'amount');
        result.locationId = specimen.locationInfo.locationId;
        return result;
      });
      return biobankApi.post(Specimen.url(ceventId), json);
    }

    /**
     * Sets the specimen spec ID for this Specimen.
     *
     * @param {domain.studies.CollectionSpecimenDescription} specimenDescription The specimen specifications
     * associated with this specimen.
     *
     * @returns {undefined}
     */
    setSpecimenDescription(specimenDescription) {
      this.specimenDescriptionId = specimenDescription.id;
      this.specimenDescription = specimenDescription;
    }

    /**
     * Returns the name for a specimen of this type.
     *
     * @returns {string}
     */
    name() {
      if (_.isUndefined(this.specimenDescription)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenDescription.name;
    }

    /**
     * Returns the default amount that should be collected for a specimen of this type.
     *
     * @returns {number}
     */
    defaultAmount() {
      if (_.isUndefined(this.specimenDescription)) {
        throw new DomainError('specimen spec not assigned');
      }
      return this.specimenDescription.amount;
    }

    /**
     * Removes the specimen from the system.
     *
     * @return {Promise<boolean>} Resolves to true if the specimen was removed successfully.
     */
    remove(collectionEventId) {
      var url;
      if (!collectionEventId) {
        throw new DomainError('collection event id not specified');
      }
      url = Specimen.url(collectionEventId, this.id, this.version);
      return biobankApi.del(url);
    }
  }

  /** return constructor function */
  return Specimen;
}

export default ngModule => ngModule.factory('Specimen', SpecimenFactory)
