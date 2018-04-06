/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Factory for collection events.
 */
/* @ngInject */
function CollectionEventFactory($q,
                                $log,
                                DomainEntity,
                                ConcurrencySafeEntity,
                                DomainError,
                                CollectionEventType,
                                Annotation,
                                annotationFactory,
                                biobankApi,
                                HasAnnotations) {

  /**
   * Used to validate the fields of a plain object that contains default fields.
   *
   * @see  CollectionEvent.create() and CollectionEvent.asyncCreate.
   */
  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'CollectionEvent',
    properties: {
      'participantId':           { 'type': 'string' },
      'participantSlug':         { 'type': 'string' },
      'collectionEventTypeId':   { 'type': 'string' },
      'collectionEventTypeSlug': { 'type': 'string' },
      'timeCompleted':           { 'type': 'string' },
      'slug':                    { 'type': 'string' },
      'visitNumber':             { 'type': 'integer' },
      'annotations':             { 'type': 'array' }
    },
    'required': [
      'participantId',
      'participantSlug',
      'collectionEventTypeId',
      'collectionEventTypeSlug',
      'timeCompleted',
      'slug',
      'visitNumber',
      'annotations'
    ]
  });

  /**
   * Creates a new CollectionEvent.
   *
   * To convert server side annotations to Annotation objects call
   * {@link domain.participant.CollectionEvent.setCollectionEventType setCollectionEventType()}.
   *
   * @memberOf domain.participants
   *
   * @param {object} [obj = {}] - The JSON object to create this CollectionEvent from. This object usually
   * comes from the server.
   *
   * @param {domain.study.CollectionEventType} [collectionEventType] - The CollectionEventType that
   * describes this object.
   */
  class CollectionEvent extends HasAnnotations {

    constructor(obj, collectionEventType, annotations) {
      /**
       * The ID of the {@link domain.participants.Participant Participant} this collection event belongs to.
       *
       * @name domain.participants.CollectionEvent#visitNumber
       * @type {string}
       */

      /**
       * The ID of the {@link domain.studies.CollectionEventType CollectionEventType} this collection event
       * is associated with.
       *
       * @name domain.participants.CollectionEvent#visitNumber
       * @type {string}
       */

      /**
       * The number assigned to the collection event.
       *
       * @name domain.participants.CollectionEvent#visitNumber
       * @type {integer}
       */

      /**
       * The time this collection event was completed at.
       *
       * @name domain.participants.CollectionEvent#timeCompleted
       * @type {Date}
       */

      /**
       * The annotations assigned to this collection event.
       *
       * @name domain.participants.CollectionEvent#annotations
       * @type {Array<domain.annotations.AnnotationType>}
       */

      super(Object.assign(
        {
          id: null,
          version: 0,
          annotations: []
        },
        obj));

      if (this.collectionEventTypeId &&
          collectionEventType &&
          (this.collectionEventTypeId !== collectionEventType.id)) {
        throw new DomainError('invalid collection event type');
      }

      annotations = annotations || [];
      Object.assign(this, { annotations: annotations });

      if (collectionEventType) {
        this.setCollectionEventType(collectionEventType);
      }
    }

    /**
     * Assigns a CollectionEventType and converts annotations to Annotation objects.
     */
    setCollectionEventType(collectionEventType) {
      this.collectionEventTypeId = collectionEventType.id;
      this.collectionEventType = collectionEventType;
      this.setAnnotationTypes(collectionEventType.annotationTypes);
    }

    add() {
      const json = _.pick(this,
                          'participantId',
                          'collectionEventTypeId',
                          'timeCompleted',
                          'visitNumber');

      // convert annotations to server side entities
      json.annotations = this.annotations.map((annotation) => {
        // make sure required annotations have values
        if (!annotation.isValueValid()) {
          throw new DomainError('required annotation has no value: annotationId: ' +
                                annotation.annotationType.id);
        }
        return annotation.getServerAnnotation();
      });

      return biobankApi.post(CollectionEvent.url(this.participantId), json).then(function(reply) {
        return CollectionEvent.asyncCreate(reply);
      });
    }

    remove() {
      return biobankApi.del(CollectionEvent.url(this.participantId, this.id, this.version));
    }

    update(path, reqJson) {
      return super.update(CollectionEvent.url(path, this.id), reqJson)
        .then(CollectionEvent.asyncCreate);
    }

    updateVisitNumber(visitNumber) {
      return this.update('visitNumber', { visitNumber: visitNumber });
    }

    updateTimeCompleted(timeCompleted) {
      return this.update('timeCompleted', { timeCompleted: timeCompleted });
    }

    addAnnotation(annotation) {
      return this.update('annot', annotation.getServerAnnotation());
    }

    removeAnnotation(annotation) {
      var url = CollectionEvent.url('annot', this.id, this.version, annotation.annotationTypeId);
      return super.removeAnnotation(annotation, url)
        .then(CollectionEvent.asyncCreate);
    }

    static url(...paths) {
      const args = [ 'participants/cevents' ].concat(paths);
      return super.url(...args);
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
     * Creates a CollectionEvent, but first it validates <code>obj</code> to ensure that it has a valid
     * schema.
     *
     * @param {object} [obj = {}] - The JSON object to create this CollectionEvent from. This object usually
     * comes from the server.
     *
     * @param {biobank.domain.CollectionEventType} [collectionEventType] - The CollectionEventType that
     * describes this object.
     */
    static create(obj) {
      var collectionEventType,
          annotations,
          validation = CollectionEvent.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }

      if (obj.collectionEventType) {
        collectionEventType =  CollectionEventType.create(obj.collectionEventType);
      }

      if (obj.annotations) {
        if (collectionEventType) {
          // match annotations with annotationTypes
          annotations = obj.annotations.map(function (annotation) {
            var annotationType = _.find(collectionEventType.annotationTypes,
                                        { id: annotation.annotationTypeId });
            if (_.isUndefined(annotationType)) {
              throw new DomainError('annotation type not found: ' + annotation.annotationTypeId);
            }
            return annotationFactory.create(annotation, annotationType);
          });
        } else {
          // at this point the AnnotationType's that belong to each Annotation are not known
          //
          // just copy the raw annotation objects
          annotations = obj.annotations;
        }
      }

      return new CollectionEvent(obj, collectionEventType, annotations);
    }

    static asyncCreate(obj) {
      var result;

      try {
        result = CollectionEvent.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a CollectionEvent from the server.
     *
     * @param {string} id - the collection event's ID.
     *
     * @param {biobank.domain.CollectionEventType} [collectionEventType] - The CollectionEventType that
     * describes this object.
     */
    static get(slug) {
      if (!slug) {
        throw new DomainError('collection event id not specified');
      }

      return biobankApi.get(this.url(slug)).then((reply) => CollectionEvent.asyncCreate(reply));
    }

    /**
     * Used to list Collection Events.
     *
     * <p>A paged API is used to list Collection Events. See below for more details.</p>
     *
     * @param {object} options - The options to use to list studies.
     *
     * @param {string} [options.sort=visitNumber] Collection Events can be sorted by <code>visitNumber</code>
     *        or by <code>timeCompleted</code>. Values other than these two yield an error. Use a minus sign
     *        prefix to sort in descending order.
     *
     * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
     *        Collection Events should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} [options.limit=10] The total number of Collection Events to return per page. The maximum
     *        page size is 10. If a value larger than 10 is used then the response is an error.
     *
     * @returns {Promise<common.controllers.PagedListController.PagedResult>} with items of type {@link
     * domain.participants.CollectionEvent}.
     */
    static list(participantId, options = {}) {
      var url = this.url('list', participantId),
          params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit'
          ];

      params = _.omitBy(_.pick(options, validKeys),
                        (value) =>  value === '');

      return biobankApi.get(url, params).then((reply) => {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = reply.items.map((obj) => CollectionEvent.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid studies from server');
        }
        return deferred.promise;
      });
    }

    /**
     * @param collectionEventType can be undefined or null.
     *
     * @param annotationTypes can be undefined or null.
     */
    static getByVisitNumber(participantId, visitNumber) {
      return biobankApi.get(this.url('visitNumber', participantId, visitNumber))
        .then((reply) => this.create(reply));
    }

    /**
     * Sorts an array of Collection Events by visit number.
     *
     * @param {Array<CollectionEvent>} collectionEvents The array to sort.
     *
     * @return {Array<CollectionEvent>} A new array sorted by visit number.
     */
    static sortByVisitNumber(collectionEvents) {
      return _.sortBy(collectionEvents,
                      (collectionEvent) => collectionEvent.visitNumber);
    }
  }

  /** return constructor function */
  return CollectionEvent;
}

export default ngModule => ngModule.factory('CollectionEvent', CollectionEventFactory)
