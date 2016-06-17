/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore', 'tv4', 'sprintf'], function(_, tv4, sprintf) {
  'use strict';

  CollectionEventTypeFactory.$inject = [
    '$q',
    'funutils',
    'biobankApi',
    'ConcurrencySafeEntity',
    'CollectionSpecimenSpec',
    'DomainError',
    'AnnotationType',
    'hasCollectionSpecimenSpecs',
    'hasAnnotationTypes'
  ];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory($q,
                                      funutils,
                                      biobankApi,
                                      ConcurrencySafeEntity,
                                      CollectionSpecimenSpec,
                                      DomainError,
                                      AnnotationType,
                                      hasCollectionSpecimenSpecs,
                                      hasAnnotationTypes) {

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
     * Creates a collection event type object with helper methods.
     * @class
     * @memberOf domain.studies
     *
     * @param {Object} collectionEventType the collection event type JSON returned by the server.
     *
     * @param {Study} options.study the study this collection even type belongs to.
     */
    function CollectionEventType(obj, options) {
      var self = this,
          defaults = {
            studyId:         null,
            name:            '',
            description:     null,
            recurring:       false,
            specimenSpecs:   [],
            annotationTypes: []
          };

      obj = obj || {};
      options = options || {};
      ConcurrencySafeEntity.call(self, obj);
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)), _.pick(options, 'study'));

      this.specimenSpecs = _.map(this.specimenSpecs, function (specimenSpec) {
        return new CollectionSpecimenSpec(specimenSpec);
      });

      this.annotationTypes = _.map(this.annotationTypes, function (annotationType) {
        return new AnnotationType(annotationType);
      });
    }

    CollectionEventType.prototype = Object.create(ConcurrencySafeEntity.prototype);
    CollectionEventType.prototype.constructor = CollectionEventType;

    _.extend(CollectionEventType.prototype, hasCollectionSpecimenSpecs, hasAnnotationTypes);


    CollectionEventType.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid collection event types from server: ' + tv4.error);
        throw new DomainError('invalid collection event types from server: ' + tv4.error);
      }

      if (!hasCollectionSpecimenSpecs.validSpecimenSpecs(obj.specimenSpecs)) {
        console.error('invalid specimen specs from server: ' + tv4.error);
        throw new DomainError('invalid specimen specs from server: ' + tv4.error);
      }

      if (!hasAnnotationTypes.validAnnotationTypes(obj.annotationTypes)) {
        console.error('invalid annotation types from server: ' + tv4.error);
        throw new DomainError('invalid annotation types from server: ' + tv4.error);
      }

      return new CollectionEventType(obj);
    };

    CollectionEventType.get = function(studyId, id) {
      return biobankApi.get(uri(studyId) + '?cetId=' + id).then(function(reply) {
        return CollectionEventType.prototype.asyncCreate(reply);
      });
    };

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

    CollectionEventType.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        deferred.reject('invalid collection event types from server: ' + tv4.error);
      } else if (!hasCollectionSpecimenSpecs.validSpecimenSpecs(obj.specimenSpecs)) {
        deferred.reject('invalid specimen specs from server: ' + tv4.error);
      } else if (!hasAnnotationTypes.validAnnotationTypes(obj.annotationTypes)) {
        deferred.reject('invalid annotation types from server: ' + tv4.error);
      } else {
        deferred.resolve(new CollectionEventType(obj));
      }

      return deferred.promise;
    };

    CollectionEventType.prototype.add = function() {
      var self = this,
          json = _.extend(_.pick(self, 'studyId','name', 'recurring'),
                         funutils.pickOptional(self, 'description'));

      return biobankApi.post(sprintf.sprintf('%s/%s', uri(), self.studyId), json)
        .then(function(reply) {
          return self.asyncCreate(reply);
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
          found = _.findWhere(self.specimenSpecs,  { uniqueId: specimenSpec.uniqueId });

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

      return hasAnnotationTypes.removeAnnotationType.call(this, annotationType, url);
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
