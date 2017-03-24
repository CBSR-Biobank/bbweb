/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  CollectionEventFactory.$inject = [
    '$q',
    '$log',
    'funutils',
    'ConcurrencySafeEntity',
    'DomainError',
    'CollectionEventType',
    'Annotation',
    'annotationFactory',
    'biobankApi',
    'HasAnnotations'
  ];

  /**
   * Factory for collection events.
   */
  function CollectionEventFactory($q,
                                  $log,
                                  funutils,
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
     * Creates a new CollectionEvent.
     * @class
     * @memberOf domain.participants
     *
     * @description To convert server side annotations to Annotation objects call
     * [setCollectionEventType]{@link domain.participant.CollectionEvent#setCollectionEventType}.
     *
     * @param {object} [obj = {}] - The JSON object to create this CollectionEvent from. This object usually
     * comes from the server.
     *
     * @param {domain.study.CollectionEventType} [collectionEventType] - The CollectionEventType that
     * describes this object.
     */
    function CollectionEvent(obj, collectionEventType, annotations) {
      // FIXME: jsdoc for this classes members is needed
      // var defaults = {
      //   participantId:         null,
      //   collectionEventTypeId: null,
      //   timeCompleted:         null,
      //   visitNumber:           null,
      //   annotations:           []
      // };

      /**
       * The number assigned to the collection event.
       *
       * @name domain.participants.CollectionEvent#visitNumber
       * @type {integer}
       */
      this.visitNumber = null;

      /**
       * The time this collection event was completed at.
       *
       * @name domain.participants.CollectionEvent#timeCompleted
       * @type {Date}
       */
      this.timeCompleted = null;

      /**
       * The annotations assigned to this collection event.
       *
       * @name domain.participants.CollectionEvent#annotations
       * @type {Array<domain.AnnotationType>}
       */
      this.annotations = [];

      ConcurrencySafeEntity.call(this, schema, obj);

      if (this.collectionEventTypeId &&
          collectionEventType &&
          (this.collectionEventTypeId !== collectionEventType.id)) {
        throw new DomainError('invalid collection event type');
      }

      annotations = annotations || [];
      _.extend(this, { annotations: annotations });

      if (collectionEventType) {
        this.setCollectionEventType(collectionEventType);
      }
    }

    CollectionEvent.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(CollectionEvent.prototype, HasAnnotations.prototype);
    CollectionEvent.prototype.constructor = CollectionEvent;

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a
     * {@link domain.studies.CollectionEvent|CollectionEvent}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    CollectionEvent.isValid = function(obj) {
      return ConcurrencySafeEntity.isValid(schema, null, obj);
    };

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
    CollectionEvent.create = function (obj) {
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
            var annotationType =
                _.find(collectionEventType.annotationTypes, { uniqueId: annotation.annotationTypeId });
            return annotationFactory.create(annotation, annotationType);
          });
        } else {
          annotations = obj.annotations.map(function (annotation) {
            return Annotation.create(annotation);
          });
        }
      }

      return new CollectionEvent(obj, collectionEventType, annotations);
    };

    CollectionEvent.asyncCreate = function (obj) {
      var result;

      try {
        result = CollectionEvent.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    /**
     * Retrieves a CollectionEvent from the server.
     *
     * @param {string} id - the collection event's ID.
     *
     * @param {biobank.domain.CollectionEventType} [collectionEventType] - The CollectionEventType that
     * describes this object.
     */
    CollectionEvent.get = function (id) {
      if (!id) {
        throw new DomainError('collection event id not specified');
      }

      return biobankApi.get(uri(id)).then(function (reply) {
        return CollectionEvent.asyncCreate(reply);
      });
    };

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
     * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
     *          domain.participants.CollectionEvent}.
     */
    CollectionEvent.list = function (participantId, options) {
      var url = uriWithPath('list', participantId),
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

    CollectionEvent.prototype.asyncCreate = function (obj) {
      return CollectionEvent.asyncCreate(obj);
    };

    /**
     * Assigns a CollectionEventType and converts annotations to Annotation objects.
     */
    CollectionEvent.prototype.setCollectionEventType = function (collectionEventType) {
      this.collectionEventTypeId = collectionEventType.id;
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
        if (!annotation.isValueValid()) {
          throw new DomainError('required annotation has no value: annotationId: ' +
                          annotation.annotationType.id);
        }
        return annotation.getServerAnnotation();
      });

      return biobankApi.post(uri(self.participantId), json).then(function(reply) {
        return CollectionEvent.asyncCreate(reply);
      });
    };

    CollectionEvent.prototype.remove = function () {
      return biobankApi.del(uri(this.participantId, this.id, this.version));
    };

    CollectionEvent.prototype.update = function (path, reqJson) {
      return ConcurrencySafeEntity.prototype.update.call(this, uriWithPath(path, this.id), reqJson)
        .then(function (updatedCevent) {
          return $q.when(CollectionEvent.create(updatedCevent));
        });
    };

    CollectionEvent.prototype.updateVisitNumber = function (visitNumber) {
      return this.update('visitNumber', { visitNumber: visitNumber });
    };

    CollectionEvent.prototype.updateTimeCompleted = function (timeCompleted) {
      return this.update('timeCompleted', { timeCompleted: timeCompleted });
    };

    CollectionEvent.prototype.addAnnotation = function (annotation) {
      return this.update('annot', annotation.getServerAnnotation());
    };

    CollectionEvent.prototype.removeAnnotation = function (annotation) {
      var url = sprintf('%s/%s/%d',
                        uri('annot', this.id),
                        annotation.annotationTypeId,
                        this.version);
      return HasAnnotations.prototype.removeAnnotation.call(this, annotation, url);
    };

    function uri(/* participantId, collectionEventId, version */) {
      var participantId,
          collectionEventId,
          version,
          result = '/participants/cevents',
          args = _.toArray(arguments);


      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }

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

    function uriWithPath(/* path, collectionEventId */) {
      var path,
          collectionEventId,
          result = uri(),
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        collectionEventId = args.shift();
        result += '/' + collectionEventId;
      }
      return result;
    }

    /** return constructor function */
    return CollectionEvent;
  }

  return CollectionEventFactory;
});
