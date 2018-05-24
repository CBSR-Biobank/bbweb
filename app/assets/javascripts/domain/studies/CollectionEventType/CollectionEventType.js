
/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { HasAnnotationTypesMixin } from '../../annotations/HasAnnotationTypes';
import _ from 'lodash'

/**
 * Angular factory for collectionEventTypes.
 */
/* @ngInject */
function CollectionEventTypeFactory($q,
                                    $log,
                                    biobankApi,
                                    DomainEntity,
                                    ConcurrencySafeEntity,
                                    CollectionSpecimenDefinition,
                                    DomainError,
                                    AnnotationType) {

  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    'id': 'CollectionEventType',
    'properties': {
      'studyId':             { 'type': 'string' },
      'slug':                { 'type': 'string' },
      'name':                { 'type': 'string' },
      'description':         { 'type': [ 'string', 'null' ] },
      'recurring':           { 'type': 'boolean' },
      'specimenDefinitions': { 'type': 'array', 'items': { '$ref': 'CollectionSpecimenDefinition' } },
      'annotationTypes':     { 'type': 'array', 'items': { '$ref': 'AnnotationType' } }
    },
    'required': [ 'studyId', 'slug', 'name', 'recurring' ]
  });

  /**
   * @classdesc A CollectionEventType defines a classification name, unique to the {@link
   * domain.studies.Study|Study}, to a {@link domain.studies.Participant|Participant} visit. A participant
   * visit is a record of when specimens were collected from a participant at a collection centre.
   *
   * Use this contructor to create a new CollectionEventType to be persited on the server. Use {@link
   * domain.studies.CollectionEventType.create|create()} or {@link
   * domain.studies.CollectionEventType.asyncCreate|asyncCreate()} to create objects returned by the server.
   *
   * @class
   * @memberOf domain.studies
   *
   * @param {Object} collectionEventType the collection event type JSON returned by the server.
   *
   * @param {Study} options.study the study this collection even type belongs to.
   *
   * @param {Array<domain.studies.CollectionSpecimenDefinition>} options.specimenDefinitions the specimen
   * definitions defined for this collection event type.
   *
   * @param {Array<domain.annotations.AnnotationType>} options.annotationTypes the annotation types defined
   * for this collection event type.
   */
  class CollectionEventType extends HasAnnotationTypesMixin(ConcurrencySafeEntity) {

    constructor(obj = {}, options = { specimenDefinitions: [], annotationTypes: [] }) {
      /**
       * The ID of the {@link domain.studies.Study|Study} this collection event type belongs to.
       *
       * @name domain.studies.CollectionEventType#studyId
       * @type {string}
       */

      /**
       * A short identifying name that is unique.
       *
       * @name domain.studies.CollectionEventType#name
       * @type {string}
       */

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.studies.CollectionEventType#description
       * @type {string}
       * @default null
       */

      /**
       * True if collection events of this type occur more than once for the duration of the study.
       *
       * @name domain.studies.CollectionEventType#recurring
       * @type {boolean}
       */

      /**
       * The specifications for the specimens that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#specimenDefinitions
       * @type {Array<domain.studies.CollectionSpecimenDefinition>}
       */

      /**
       * The annotation types that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#annotationTypes
       * @type {Array<domain.annotations.AnnotationType>}
       */

      super(Object.assign(
        {
          recurring: false
        },
        // for mixin
        {
          $q,
          biobankApi
        },
        obj));

      Object.assign(this,
                    // for mixin
                    {
                      $q,
                      biobankApi,
                      DomainError
                    },
                    _.pick(options, 'study', 'specimenDefinitions', 'annotationTypes'));

      if (options.study) {
        this.studyId = options.study.id;
      }
    }

    static url(...paths) {
      const allPaths = [ 'studies/cetypes' ].concat(paths);
      return super.url(...allPaths);
    }

    /**
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [
        CollectionSpecimenDefinition.schema(),
        AnnotationType.schema()
      ];
    }

    /**
     * Creates a CollectionEventType, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.CollectionEventType} A collection event type created from the given object.
     *
     * @see {@link domain.studies.CollectionEventType.asyncCreate|asyncCreate()} when you need to create a
     * collection event type within asynchronous code.
     */
    static create(obj) {
      const options = {},
            validation = CollectionEventType.isValid(obj);
      if (!validation.valid) {
        $log.error('invalid collection event type from server: ' + validation.message);
        throw new DomainError('invalid collection event type from server: ' + validation.message);
      }

      if (obj.annotationTypes) {
        try {
          options.annotationTypes = obj.annotationTypes
            .map(annotationType => AnnotationType.create(annotationType));
        } catch (e) {
          throw new DomainError('invalid annotation types from server: ' + e.message);
        }
      }

      if (obj.specimenDefinition) {
        try {
          options.specimenDefinitions = obj.specimenDefinitions
            .map((specimenDefinition) => CollectionSpecimenDefinition.create(specimenDefinition));
        } catch (e) {
          throw new DomainError('invalid specimen definitions from server: ' + e.message);
        }
      }

      return new CollectionEventType(obj, options);
    }

    /**
     * Creates a CollectionEventType from a server reply but first validates that `obj` has a valid
     * schema. *Meant to be called from within promise code*.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.studies.CollectionEventType>} A collection event type wrapped in a promise.
     *
     * @see {@link domain.studies.CollectionEventType.create|create()} when not creating a collection event
     * type within asynchronous code.
     */
    static asyncCreate(obj) {
      try {
        const result = CollectionEventType.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a CollectionEventType from the server.
     *
     * @param {string} studySlug the slug of the study this collection event type belongs to.
     *
     * @param {string} slug the sllug of the collection event type to retrieve.
     *
     * @returns {Promise<domain.studies.CollectionEventType>} The collection event type within a promise.
     */
    static get(studySlug, slug) {
      return biobankApi.get(CollectionEventType.url(studySlug, slug))
        .then(reply => CollectionEventType.asyncCreate(reply));
    }

    /**
     * Fetches collection event types for a {@link domain.studies.Study|Study}.
     *
     * @param {string} studySlug the slug of the study the collection event types belongs to.
     *
     * @returns {Promise<Array<domain.studies.CollectionEventType>>} An array of collection event types within
     * a promise.
     */
    static list(studySlug, options = {}) {
      const url = CollectionEventType.url(studySlug),
            validKeys = [
              'filter',
              'sort',
              'page',
              'limit'
            ];

      const params = _.omitBy(_.pick(options, validKeys),
                              (value) => value === '');

      return biobankApi.get(url, params).then((reply) => {
        const deferred = $q.defer();

        try {
          reply.items = reply.items.map((obj) => CollectionEventType.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject(e);
        }
        return deferred.promise;
      });
    }

    /**
     * Sorts an array of Collection Event Types by name.
     *
     * @param {Array<CollectionEventType>} collectionEventTypes The array to sort.
     *
     * @return {Array<CollectionEventType>} A new array sorted by name.
     */
    static sortByName(collectionEventTypes) {
      return _.sortBy(collectionEventTypes,
                      (collectionEventType) => collectionEventType.name);
    }

    add() {
      const json = _.pick(this, 'studyId','name', 'recurring', 'description');
      return biobankApi.post(CollectionEventType.url(this.studyId), json)
        .then(CollectionEventType.asyncCreate);
    }

    remove() {
      const url = CollectionEventType.url(this.studyId, this.id, this.version);
      return biobankApi.del(url);
    }

    update(path, reqJson) {
      return super.update(path, reqJson).then(CollectionEventType.asyncCreate);
    }

    updateName(name) {
      return this.update(CollectionEventType.url('name', this.id),
                         { studyId: this.studyId, name: name });
    }

    updateDescription(description) {
      const json = { studyId: this.studyId };
      if (description) {
        json.description = description;
      }
      return this.update(CollectionEventType.url('description', this.id), json);
    }

    updateRecurring(recurring) {
      return this.update(CollectionEventType.url('recurring', this.id),
                         { studyId: this.studyId, recurring: recurring });
    }

    addSpecimenDefinition(specimenDefinition) {
      return this.update(CollectionEventType.url('spcdesc', this.id),
                         Object.assign({ studyId: this.studyId }, _.omit(specimenDefinition, 'id')));
    }

    updateSpecimenDefinition(specimenDefinition) {
      return this.update(CollectionEventType.url('spcdesc', this.id, specimenDefinition.id),
                         Object.assign({ studyId: this.studyId }, specimenDefinition));
    }

    removeSpecimenDefinition(specimenDefinition) {
      const found = _.find(this.specimenDefinitions,  { id: specimenDefinition.id });

      if (!found) {
        throw new DomainError('specimen description with ID not present: ' + specimenDefinition.id);
      }

      const url = CollectionEventType.url('spcdesc',
                                          this.studyId,
                                          this.id,
                                          this.version,
                                          specimenDefinition.id);
      return biobankApi.del(url).then(CollectionEventType.asyncCreate);
    }

    addAnnotationType(annotationType) {
      return this.update(CollectionEventType.url('annottype', this.id),
                         Object.assign({ studyId: this.studyId }, _.omit(annotationType, 'uniqueId')));
    }

    updateAnnotationType(annotationType) {
      return this.update(CollectionEventType.url('annottype', this.id, annotationType.id),
                         Object.assign({ studyId: this.studyId }, annotationType));
    }

    removeAnnotationType(annotationType) {
      const url = CollectionEventType.url('annottype',
                                          this.studyId,
                                          this.id,
                                          this.version,
                                          annotationType.id);
      return super.removeAnnotationType(annotationType, url)
        .then(CollectionEventType.asyncCreate);
    }

    inUse() {
      return biobankApi.get(CollectionEventType.url('inuse', this.id));
    }
  }

  return CollectionEventType;
}

export default ngModule => ngModule.factory('CollectionEventType', CollectionEventTypeFactory)
