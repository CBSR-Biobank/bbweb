/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CollectionEventFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'queryStringService',
    'biobankApi',
    'annotationFactory'
  ];

  /**
   * Factory for participants.
   */
  function CollectionEventFactory(funutils,
                                  validationService,
                                  ConcurrencySafeEntity,
                                  queryStringService,
                                  biobankApi,
                                  annotationFactory) {

    var requiredKeys = [
      'id',
      'participantId',
      'collectionEventTypeId',
      'timeCompleted',
      'visitNumber',
      'annotations',
      'version'
    ];

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

    var validateAnnotations = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('annotationTypeId', 'selectedValues'))),
      createObj);

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
    function CollectionEvent(obj, collectionEventType, annotationTypes) {
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

      if (annotationTypes) {
        this.setAnnotationTypes(annotationTypes);
      }
    }

    CollectionEvent.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    CollectionEvent.create = function (obj, collectionEventType, annotationTypes) {
      var annotValid,
          validation =
          validateObj(obj);

      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }

      annotValid =_.reduce(obj.annotations, function (memo, annotation) {
        var validation = validateAnnotations(annotation);
        return memo && _.isObject(validation);
      }, true);

      if (!annotValid) {
        return new Error('invalid annotation object from server');
      }
      return new CollectionEvent(obj, collectionEventType, annotationTypes);
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
    CollectionEvent.get = function (participantId, id, collectionEventType, annotationTypes) {
      if (!id) {
        throw new Error('collection event id not specified');
      }

      return biobankApi.get(uri(participantId) + '?ceventId=' + id)
        .then(function (reply) {
          return CollectionEvent.create(reply, collectionEventType, annotationTypes);
        });
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
        reply.items = _.map(reply.items, function(obj) {
          var result = CollectionEvent.create(obj);
          return result;
        });
        return reply;
      });
    };

    /**
     * @param collectionEventType can be undefined or null.
     *
     * @param annotationTypes can be undefined or null.
     */
    CollectionEvent.getByVisitNumber = function (participantId,
                                                 visitNumber,
                                                 collectionEventType,
                                                 annotationTypes) {
      return biobankApi.get(uri(participantId) + '/visitNumber/' + visitNumber)
        .then(function (reply) {
          return CollectionEvent.create(reply, collectionEventType, annotationTypes);
        });
    };

    CollectionEvent.prototype.setCollectionEventType = function (collectionEventType) {
      this.collectionEventType = collectionEventType;
    };

    /**
     * Converts the server side annotations to Annotation objects, which make it easier to manage them.
     *
     * @param {CollectionEventAnnotationType} annotationTypes - the annotation types allowed for this
     * participant.
     */
    CollectionEvent.prototype.setAnnotationTypes = function (annotationTypes) {
      var self = this,
          annotationTypeDataById,
          ceventTypeAnnotationTypeIds,
          serverAnnotationsById;

      if (_.isUndefined(self.collectionEventType)) {
        throw new Error('collection event type not defined');
      }

      annotationTypeDataById      = _.indexBy(self.collectionEventType.annotationTypeData,
                                              'annotationTypeId');
      ceventTypeAnnotationTypeIds = _.keys(annotationTypeDataById);
      serverAnnotationsById       = _.indexBy(self.annotations, 'annotationTypeId');

      checkValidIds(ceventTypeAnnotationTypeIds);
      self.annotationTypes = annotationTypes;
      self.annotations = _.map(annotationTypes, function (annotationType) {
        var serverAnnotation = serverAnnotationsById[annotationType.id];
        return annotationFactory.create(serverAnnotation,
                                        annotationType,
                                        annotationTypeDataById[annotationType.id].required);
      });

      function checkValidIds(ceventTypeAnnotationTypeIds) {
        // make sure the annotations ids match up with the corresponding annotation types
        var differentIds = _.difference(_.pluck(self.annotations, 'annotationTypeId'),
                                        ceventTypeAnnotationTypeIds);

        if (differentIds.length > 0) {
          throw new Error('annotations with invalid annotation type IDs found: ' +
                          differentIds);
        }

        differentIds = _.difference(_.pluck(annotationTypes, 'id'),
                                    ceventTypeAnnotationTypeIds);

        if (differentIds.length > 0) {
          throw new Error('annotation types not belonging to collection event type found: ' +
                          differentIds);
        }
        return true;
      }
    };

    CollectionEvent.prototype.addOrUpdate = function () {
      var self = this,
          cmd = _.pick(self,
                       'participantId',
                       'collectionEventTypeId',
                       'timeCompleted',
                       'visitNumber');

      // convert annotations to server side entities
      cmd.annotations = _.map(self.annotations, function (annotation) {
        // make sure required annotations have values
        if (!annotation.isValid()) {
          throw new Error('required annotation has no value: annotationId: ' +
                          annotation.annotationType.id);
        }
        return annotation.getServerAnnotation();
      });

      return addOrUpdateInternal(cmd).then(function(reply) {
        return CollectionEvent.create(reply);
      });

      // --

      function addOrUpdateInternal(cmd) {
        if (self.isNew()) {
          return biobankApi.post(uri(self.participantId), cmd);
        }
        _.extend(cmd, { id: self.id, expectedVersion: self.version });
        return biobankApi.put(uri(self.participantId, self.id), cmd);
      }
    };

    CollectionEvent.prototype.remove = function () {
      return biobankApi.del(uri(this.participantId, this.id, this.version));
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
