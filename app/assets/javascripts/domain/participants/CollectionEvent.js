/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore', 'tv4'], function(_, tv4) {
  'use strict';

  CollectionEventFactory.$inject = [
    '$q',
    'funutils',
    'ConcurrencySafeEntity',
    'Annotation',
    'queryStringService',
    'biobankApi',
    'hasAnnotations',
    'annotationFactory'
  ];

  /**
   * Factory for participants.
   */
  function CollectionEventFactory($q,
                                  funutils,
                                  ConcurrencySafeEntity,
                                  Annotation,
                                  queryStringService,
                                  biobankApi,
                                  hasAnnotations,
                                  annotationFactory) {

    var schema = {
      'id': 'CollectionEvent',
      'type': 'object',
      'properties': {
        'id':                    { 'type': 'string' },
        'participantId':         { 'type': 'string' },
        'collectionEventTypeId': { 'type': 'string' },
        'version':               { 'type': 'integer', 'minimum': 0 },
        'timeAdded':             { 'type': 'string' },
        'timeModified':          { 'type': [ 'string', 'null' ] },
        'timeCompleted':         { 'type': 'string' },
        'visitNumber':           { 'type': 'integer' },
        'annotations':           { 'type': 'array' }
      },
      'required': [
        'id',
        'participantId',
        'collectionEventTypeId',
        'timeCompleted',
        'visitNumber',
        'annotations',
        'version'
      ]
    };

    /**
     * To convert server side annotations to Annotation class call setAnnotationTypes().
     *
     * @param {object} obj.annotations - server response for annotation.
     *
     * @param {CollectionEventType} collectionEventType
     *
     * @param {CollectionEventAnnotationType} annotationTypes. If both collectionEventType and annotationTypes
     * are passed to the constructor, the annotations will be converted to Annotation objects.
     */
    function CollectionEvent(obj, collectionEventType) {
      var defaults = {
        participantId:         null,
        collectionEventTypeId: null,
        timeCompleted:         null,
        visitNumber:           null,
        annotations:           []
      };

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));

      if (this.collectionEventTypeId &&
          collectionEventType &&
          (this.collectionEventTypeId !== collectionEventType.id)) {
        throw new Error('invalid collection event type');
      }

      this.setCollectionEventType(collectionEventType);
    }

    CollectionEvent.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(CollectionEvent.prototype, hasAnnotations);
    CollectionEvent.prototype.constructor = CollectionEvent;

    CollectionEvent.isValid = function(obj) {
      return tv4.validate(obj, schema);
    };

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    CollectionEvent.create = function (obj, collectionEventType) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new Error('invalid object from server: ' + tv4.error);
      }

      if (!Annotation.validAnnotations(obj.annotations)) {
        console.error('invalid object from server: bad annotations');
        throw new Error('invalid object from server: bad annotations');
      }

      return new CollectionEvent(obj, collectionEventType);
    };

    /**
     * @param participantId the ID of the participant this collection event belongs to.
     *
     * @param id the collection event's ID.
     *
     * @param collectionEventType can be undefined or null.
     *
     * @param annotationTypes can be undefined or null.
     */
    CollectionEvent.get = function (participantId, id, collectionEventType) {
      if (!id) {
        throw new Error('collection event id not specified');
      }

      return biobankApi.get(uri(participantId) + '?ceventId=' + id)
        .then(function (reply) {
          return CollectionEvent.prototype.asyncCreate(reply);
        });
    };

    CollectionEvent.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else if (!Annotation.validAnnotations(obj.annotationTypes)) {
        console.error('invalid annotation types from server: ' + tv4.error);
        deferred.reject('invalid annotation types from server: ' + tv4.error);
      } else {
        deferred.resolve(new CollectionEvent(obj));
      }

      return deferred.promise;
    };

    /**
     * @eturn a paged result.
     */
    CollectionEvent.list = function (participantId, options) {
      var validKeys = [
        'sort',
        'page',
        'pageSize',
        'order'
      ];
      var url = uri(participantId) + '/list';
      var paramsStr = '';

      if (arguments.length > 0) {
        paramsStr = queryStringService.param(options, function (value, key) {
          return _.contains(validKeys, key);
        });
      }

      if (paramsStr) {
        url += paramsStr;
      }

      return biobankApi.get(url).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return CollectionEvent.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid studies from server');
        }
        return deferred.promise;
      });
    };

    /**
     * @param collectionEventType can be undefined or null.
     *
     * @param annotationTypes can be undefined or null.
     */
    CollectionEvent.getByVisitNumber = function (participantId,
                                                 visitNumber,
                                                 collectionEventType) {
      return biobankApi.get(uri(participantId) + '/visitNumber/' + visitNumber)
        .then(function (reply) {
          return CollectionEvent.create(reply, collectionEventType);
        });
    };

    CollectionEvent.prototype.setCollectionEventType = function (collectionEventType) {
      if (_.isUndefined(collectionEventType)) {
        throw new Error('collection event type not defined');
      }
      this.collectionEventType = collectionEventType;
      this.setAnnotationTypes(collectionEventType.annotationTypes);
    };

    CollectionEvent.prototype.add = function () {
      var self = this,
          json = _.pick(self,
                       'participantId',
                       'collectionEventTypeId',
                       'timeCompleted',
                       'visitNumber');

      // convert annotations to server side entities
      json.annotations = _.map(self.annotations, function (annotation) {
        // make sure required annotations have values
        if (!annotation.isValid()) {
          throw new Error('required annotation has no value: annotationId: ' +
                          annotation.annotationType.id);
        }
        return annotation.getServerAnnotation();
      });

      return biobankApi.post(uri(self.participantId), json).then(function(reply) {
        return self.asyncCreate(reply);
      });
    };

    CollectionEvent.prototype.remove = function () {
      return biobankApi.del(uri(this.participantId, this.id, this.version));
    };

    CollectionEvent.prototype.updateVisitNumber = function (visitNumber) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, uri('visitNumber', this.id), { visitNumber: visitNumber });
    };

    CollectionEvent.prototype.updateTimeCompleted = function (timeCompleted) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, uri('timeCompleted', this.id), { timeCompleted: timeCompleted });
    };

    function uri(/* participantId, collectionEventId, version */) {
      var participantId,
          collectionEventId,
          version,
          result = '/participants',
          args = _.toArray(arguments);

      if (args.length < 1) {
        throw new Error('participant id not specified');
      }

      participantId = args.shift();
      result += '/cevents/' + participantId;

      if (args.length > 0) {
        collectionEventId = args.shift();
        result += '/' + collectionEventId;
      }

      if (args.length > 0) {
        version = args.shift();
        result += '/' + version;
      }

      return result;
    }

    /** return constructor function */
    return CollectionEvent;
  }

  return CollectionEventFactory;
});
