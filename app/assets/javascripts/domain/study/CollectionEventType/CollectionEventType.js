/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _ = require('lodash');

  CollectionEventTypeFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'DomainEntity',
    'ConcurrencySafeEntity',
    'CollectionSpecimenDescription',
    'DomainError',
    'AnnotationType',
    'HasCollectionSpecimenDescriptions',
    'HasAnnotationTypes'
  ];

  /**
   * Angular factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory($q,
                                      $log,
                                      biobankApi,
                                      DomainEntity,
                                      ConcurrencySafeEntity,
                                      CollectionSpecimenDescription,
                                      DomainError,
                                      AnnotationType,
                                      HasCollectionSpecimenDescriptions,
                                      HasAnnotationTypes) {

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
     */
    function CollectionEventType(obj, options) {
      /**
       * The ID of the {@link domain.studies.Study|Study} this collection event type belongs to.
       *
       * @name domain.studies.CollectionEventType#studyId
       * @type {string}
       */
      this.studyId = _.get(obj, 'studyId', null);

      /**
       * A short identifying name that is unique.
       *
       * @name domain.studies.CollectionEventType#name
       * @type {string}
       */
      this.name = '';

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.studies.CollectionEventType#description
       * @type {string}
       * @default null
       */

      /**
       * True if this collection events of this type occur more than once for the duration of the study.
       *
       * @name domain.studies.CollectionEventType#recurring
       * @type {boolean}
       */
      this.recurring = false;

      /**
       * The specifications for the specimens that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#specimenDescriptions
       * @type {Array<domain.studies.CollectionSpecimenDescription>}
       */
      this.specimenDescriptions = [];

      /**
       * The annotation types that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#annotationTypes
       * @type {Array<domain.AnnotationType>}
       */
      this.annotationTypes = [];

      ConcurrencySafeEntity.call(this, CollectionEventType.SCHEMA, obj);

      options                      = options || {};
      options.study                = _.get(options, 'study', undefined);
      options.specimenDescriptions = _.get(options, 'specimenDescriptions', []);
      options.annotationTypes      = _.get(options, 'annotationTypes', []);

      _.extend(this, _.pick(options, 'study', 'specimenDescriptions', 'annotationTypes'));

      if (options.study) {
        this.studyId = options.study.id;
      }
    }

    CollectionEventType.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(CollectionEventType.prototype,
             HasAnnotationTypes.prototype,
             HasCollectionSpecimenDescriptions.prototype);
    CollectionEventType.prototype.constructor = CollectionEventType;

    CollectionEventType.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
      const args = [ 'studies/cetypes' ].concat(_.toArray(arguments));
      return DomainEntity.url.apply(null, args);
    };

    CollectionEventType.SCHEMA = {
      'id': 'CollectionEventType',
      'type': 'object',
      'properties': {
        'id':                   { 'type': 'string' },
        'version':              { 'type': 'integer', 'minimum': 0 },
        'timeAdded':            { 'type': 'string' },
        'timeModified':         { 'type': 'string' },
        'name':                 { 'type': 'string' },
        'description':          { 'type': [ 'string', 'null' ] },
        'recurring':            { 'type': 'boolean' },
        'specimenDescriptions': { 'type': 'array', 'items': { '$ref': 'CollectionSpecimenDescription' } },
        'annotationTypes':      { 'type': 'array', 'items': { '$ref': 'AnnotationType' } }
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'recurring' ]
    };

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a
     * {@link domain.studies.CollectionEventType|CollectionEventType}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    CollectionEventType.isValid = function(obj) {
      return ConcurrencySafeEntity.isValid(CollectionEventType.SCHEMA,
                                           [
                                             CollectionSpecimenDescription.SCHEMA,
                                             AnnotationType.SCHEMA
                                           ],
                                           obj);
    };

    /**
     * Creates a CollectionEventType, but first it validates <tt>obj</tt> to ensure that it has a valid
     * schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.CollectionEventType} A collection event type created from the given object.
     *
     * @see {@link domain.studies.CollectionEventType.asyncCreate|asyncCreate()} when you need to create a
     * collection event type within asynchronous code.
     */
    CollectionEventType.create = function (obj) {
      var options = {},
          validation = CollectionEventType.isValid(obj);
      if (!validation.valid) {
        $log.error('invalid collection event type from server: ' + validation.message);
        throw new DomainError('invalid collection event type from server: ' + validation.message);
      }

      if (obj.annotationTypes) {
        try {
          options.annotationTypes =
            obj.annotationTypes.map((annotationType) => AnnotationType.create(annotationType));
        } catch (e) {
          throw new DomainError('invalid annotation types from server: ' + validation.message);
        }
      }

      if (obj.specimenDescriptions) {
        try {
          options.specimenDescriptions = obj.specimenDescriptions
            .map((specimenDescription) => CollectionSpecimenDescription.create(specimenDescription));
        } catch (e) {
          throw new DomainError('invalid specimen specs from server: ' + validation.message);
        }
      }

      return new CollectionEventType(obj, options);
    };

    /**
     * Creates a CollectionEventType from a server reply but first validates that <tt>obj</tt> has a valid
     * schema. <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.studies.CollectionEventType>} A collection event type wrapped in a promise.
     *
     * @see {@link domain.studies.CollectionEventType.create|create()} when not creating a collection event
     * type within asynchronous code.
     */
    CollectionEventType.asyncCreate = function (obj) {
      var result;

      try {
        result = CollectionEventType.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    /**
     * Retrieves a CollectionEventType from the server.
     *
     * @param {string} id the ID of the collection event type to retrieve.
     *
     * @returns {Promise<domain.studies.CollectionEventType>} The collection event type within a promise.
     */
    CollectionEventType.get = function(studyId, id) {
      return biobankApi.get(CollectionEventType.url(studyId, id))
        .then((reply) => CollectionEventType.prototype.asyncCreate(reply));
    };

    /**
     * Fetches all collection event types for a {@link domain.studies.Study|Study}.
     *
     * @returns {Promise<Array<domain.studies.CollectionEventType>>} An array of collection event types within
     * a promise.
     */
    CollectionEventType.list = function(studyId, options) {
      var url = CollectionEventType.url(studyId),
          params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit'
          ];

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });

      return biobankApi.get(url, params).then(function(reply) {
        var deferred = $q.defer();

        try {
          reply.items = reply.items.map((obj) => CollectionEventType.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject(e);
        }
        return deferred.promise;
      });
    };

    /**
     * Sorts an array of Collection Event Types by name.
     *
     * @param {Array<CollectionEventType>} collectionEventTypes The array to sort.
     *
     * @return {Array<CollectionEventType>} A new array sorted by name.
     */
    CollectionEventType.sortByName = function(collectionEventTypes) {
      return _.sortBy(collectionEventTypes, function (collectionEventType) {
        return collectionEventType.name;
      });
    };

    /**
     * Creates a CollectionEventType from a server reply but first validates that it has a valid schema.
     *
     * <p>A wrapper for {@link domian.studies.CollectionEventType#asyncCreate}.</p>
     *
     * @see {@link domain.ConcurrencySafeEntity#update}
     */
    CollectionEventType.prototype.asyncCreate = function (obj) {
      return CollectionEventType.asyncCreate(obj);
    };

    CollectionEventType.prototype.add = function() {
      var json = _.pick(this, 'studyId','name', 'recurring', 'description');
      return biobankApi.post(CollectionEventType.url(this.studyId), json)
        .then(function(reply) {
          return CollectionEventType.asyncCreate(reply);
        });
    };

    CollectionEventType.prototype.remove = function () {
      var url = CollectionEventType.url(this.studyId, this.id, this.version);
      return biobankApi.del(url);
    };

    CollectionEventType.prototype.updateName = function (name) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, CollectionEventType.url('name', this.id), { studyId: this.studyId, name: name });
    };

    CollectionEventType.prototype.updateDescription = function (description) {
      var json = { studyId: this.studyId };
      if (description) {
        json.description = description;
      }
      return ConcurrencySafeEntity.prototype.update.call(this, CollectionEventType.url('description', this.id), json);
    };

    CollectionEventType.prototype.updateRecurring = function (recurring) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        CollectionEventType.url('recurring', this.id),
        { studyId: this.studyId, recurring: recurring });
    };

    CollectionEventType.prototype.addSpecimenDescription = function (specimenDescription) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        CollectionEventType.url('spcdesc', this.id),
        _.extend({ studyId: this.studyId }, _.omit(specimenDescription, 'id')));
    };

    CollectionEventType.prototype.updateSpecimenDescription = function (specimenDescription) {
      var url = CollectionEventType.url('spcdesc', this.id, specimenDescription.id);
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        url,
        _.extend({ studyId: this.studyId }, specimenDescription));
    };

    CollectionEventType.prototype.removeSpecimenDescription = function (specimenDescription) {
      var url,
          found = _.find(this.specimenDescriptions,  { id: specimenDescription.id });

      if (!found) {
        throw new DomainError('specimen description with ID not present: ' + specimenDescription.id);
      }

      url = CollectionEventType.url('spcdesc', this.studyId, this.id, this.version, specimenDescription.id);
      return biobankApi.del(url).then(CollectionEventType.asyncCreate);
    };

    CollectionEventType.prototype.addAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        CollectionEventType.url('annottype', this.id),
        _.extend({ studyId: this.studyId }, _.omit(annotationType, 'uniqueId')));
    };

    CollectionEventType.prototype.updateAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        CollectionEventType.url('annottype', this.id, annotationType.id),
        _.extend({ studyId: this.studyId }, annotationType));
    };

    CollectionEventType.prototype.removeAnnotationType = function (annotationType) {
      var url = CollectionEventType.url('annottype', this.studyId, this.id, this.version, annotationType.id);
      return HasAnnotationTypes.prototype.removeAnnotationType.call(this, annotationType, url);
    };

    CollectionEventType.prototype.inUse = function () {
      return biobankApi.get(CollectionEventType.url('inuse', this.id));
    };

    /** return constructor function */
    return CollectionEventType;
  }

  return CollectionEventTypeFactory;
});