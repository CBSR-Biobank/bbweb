/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash', 'tv4', 'sprintf-js'], function(_, tv4, sprintf) {
  'use strict';

  CollectionEventTypeFactory.$inject = [
    '$q',
    '$log',
    'biobankApi',
    'ConcurrencySafeEntity',
    'CollectionSpecimenSpec',
    'DomainError',
    'AnnotationType',
    'HasCollectionSpecimenSpecs',
    'HasAnnotationTypes'
  ];

  /**
   * Angular factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory($q,
                                      $log,
                                      biobankApi,
                                      ConcurrencySafeEntity,
                                      CollectionSpecimenSpec,
                                      DomainError,
                                      AnnotationType,
                                      HasCollectionSpecimenSpecs,
                                      HasAnnotationTypes) {

    var schema = {
      'id': 'CollectionEventType',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string' },
        'version':         { 'type': 'integer', 'minimum': 0 },
        'timeAdded':       { 'type': 'string' },
        'timeModified':    { 'type': 'string' },
        'name':            { 'type': 'string' },
        'description':     { 'type': [ 'string', 'null' ] },
        'recurring':       { 'type': 'boolean' },
        'specimenSpecs':   { 'type': 'array' },
        'annotationTypes': { 'type': 'array' }
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'recurring' ]
    };

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
      var self = this;

      /**
       * The ID of the {@link domain.studies.Study|Study} this collection event type belongs to.
       *
       * @name domain.studies.CollectionEventType#studyId
       * @type {string}
       */
      self.studyId = null;

      /**
       * A short identifying name that is unique.
       *
       * @name domain.studies.CollectionEventType#name
       * @type {string}
       */
      self.name = '';

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
      self.recurring = false;

      /**
       * The specifications for the specimens that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#specimenSpecs
       * @type {Array<domain.studies.CollectionSpecimenSpec>}
       */
      self.annotationTypes = [];

      /**
       * The annotation types that are collected for this collection event type.
       *
       * @name domain.studies.CollectionEventType#annotationTypes
       * @type {Array<domain.AnnotationType>}
       */
      self.annotationTypes = [];

      obj = obj || {};
      options = options || {};
      ConcurrencySafeEntity.call(self);
      _.extend(self, obj, _.pick(options, 'study'));


      self.specimenSpecs = _.map(self.specimenSpecs, function (specimenSpec) {
        return new CollectionSpecimenSpec(specimenSpec);
      });

      this.annotationTypes = _.map(this.annotationTypes, function (annotationType) {
        return new AnnotationType(annotationType);
      });
    }

    CollectionEventType.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(CollectionEventType.prototype,
             HasAnnotationTypes.prototype,
             HasCollectionSpecimenSpecs.prototype);
    CollectionEventType.prototype.constructor = CollectionEventType;

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a {@link
     * domain.studies.CollectionEventType|CollectionEventType}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    CollectionEventType.validate = function (obj) {
      if (!tv4.validate(obj, schema)) {
        return { valid: false, message: 'invalid collection event types from server: ' + tv4.error };
      }

      if (!HasCollectionSpecimenSpecs.prototype.validSpecimenSpecs(obj.specimenSpecs)) {
        return { valid: false, message: 'invalid specimen specs from server: ' + tv4.error };
      }

      if (!HasAnnotationTypes.prototype.validAnnotationTypes(obj.annotationTypes)) {
        return { valid: false, message: 'invalid annotation types from server: ' + tv4.error };
      }

      return { valid: true, message: null };
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
      var validation = CollectionEventType.validate(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new CollectionEventType(obj);
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
      var deferred = $q.defer(),
          validation = CollectionEventType.validate(obj);

      if (!validation.valid) {
        $log.error(validation.message);
        deferred.reject(validation.message);
      } else {
        deferred.resolve(new CollectionEventType(obj));
      }
      return deferred.promise;
    };

    /**
     * Retrieves a CollectionEventType from the server.
     *
     * @param {string} id the ID of the collection event type to retrieve.
     *
     * @returns {Promise<domain.studies.CollectionEventType>} The collection event type within a promise.
     */
    CollectionEventType.get = function(studyId, id) {
      return biobankApi.get(uri(studyId) + '?cetId=' + id).then(function(reply) {
        return CollectionEventType.prototype.asyncCreate(reply);
      });
    };

    /**
     * Fetches all collection event types for a {@link domain.studies.Study|Study}.
     *
     * @returns {Promise<Array<domain.studies.CollectionEventType>>} An array of collection event types within
     * a promise.
     */
    CollectionEventType.list = function(studyId) {
      return biobankApi.get(uri(studyId)).then(function(reply) {
        var deferred = $q.defer(),
            result;

        try {
          result = _.map(reply, function (cet) {
            return CollectionEventType.create(cet);
          });
          deferred.resolve(result);
        } catch (e) {
          deferred.reject(e);
        }
        return deferred.promise;
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
      return biobankApi.post(sprintf.sprintf('%s/%s', uri(), this.studyId), json)
        .then(function(reply) {
          return CollectionEventType.asyncCreate(reply);
        });
    };

    CollectionEventType.prototype.remove = function () {
      var url = sprintf.sprintf('%s/%s/%s/%d', uri(), this.studyId, this.id, this.version);
      return biobankApi.del(url);
    };

    CollectionEventType.prototype.updateName = function (name) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, uri('name', this.id), { studyId: this.studyId, name: name });
    };

    CollectionEventType.prototype.updateDescription = function (description) {
      var json = { studyId: this.studyId };
      if (description) {
        json.description = description;
      }
      return ConcurrencySafeEntity.prototype.update.call(this, uri('description', this.id), json);
    };

    CollectionEventType.prototype.updateRecurring = function (recurring) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('recurring', this.id),
        { studyId: this.studyId, recurring: recurring });
    };

    CollectionEventType.prototype.addSpecimenSpec = function (specimenSpec) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('spcspec', this.id),
        _.extend({ studyId: this.studyId }, _.omit(specimenSpec, 'uniqueId')));
    };

    CollectionEventType.prototype.updateSpecimenSpec = function (specimenSpec) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('spcspec', this.id) + '/' + specimenSpec.uniqueId,
        _.extend({ studyId: this.studyId }, specimenSpec));
    };

    CollectionEventType.prototype.removeSpecimenSpec = function (specimenSpec) {
      var self = this,
          url,
          found = _.find(self.specimenSpecs,  { uniqueId: specimenSpec.uniqueId });

      if (!found) {
        throw new DomainError('specimen spec with ID not present: ' + specimenSpec.uniqueId);
      }

      url = sprintf.sprintf('%s/%s/%d/%s',
                            uri('spcspec', this.studyId),
                            this.id,
                            this.version,
                            specimenSpec.uniqueId);

      return biobankApi.del(url).then(function () {
        return self.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            specimenSpecs: _.filter(self.specimenSpecs, function(ss) {
              return ss.uniqueId !== specimenSpec.uniqueId;
            })
          }));
      });
    };

    CollectionEventType.prototype.addAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('annottype', this.id),
        _.extend({ studyId: this.studyId }, _.omit(annotationType, 'uniqueId')));
    };

    CollectionEventType.prototype.updateAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('annottype', this.id) + '/' + annotationType.uniqueId,
        _.extend({ studyId: this.studyId }, annotationType));
    };

    CollectionEventType.prototype.removeAnnotationType = function (annotationType) {
      var url = sprintf.sprintf('%s/%s/%d/%s',
                                uri('annottype', this.studyId),
                                this.id,
                                this.version,
                                annotationType.uniqueId);

      return HasAnnotationTypes.prototype.removeAnnotationType.call(this, annotationType, url);
    };

    CollectionEventType.prototype.inUse = function () {
      return biobankApi.get(uri() + '/inuse/' + this.id);
    };

    function uri(/* path, ceventTypeId */) {
      var args = _.toArray(arguments),
          result = '/studies/cetypes',
          path,
          ceventTypeId;

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        ceventTypeId = args.shift();
        result += '/' + ceventTypeId;
      }
      return result;
    }

    /** return constructor function */
    return CollectionEventType;
  }

  return CollectionEventTypeFactory;
});
