/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['underscore'], function(_) {
  'use strict';

  CollectionEventTypeFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ConcurrencySafeEntity',
    'SpecimenGroupData',
    'AnnotationTypeData'
  ];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory(funutils,
                                      validationService,
                                      biobankApi,
                                      ConcurrencySafeEntity,
                                      SpecimenGroupData,
                                      AnnotationTypeData) {
    var requiredKeys = [
      'id',
      'studyId',
      'version',
      'name',
      'recurring',
      'specimenGroupData',
      'annotationTypeData'
    ];

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

    var validateAnnotationTypeData = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('annotationTypeId', 'required'))),
      createObj);

    var validateSpecimenGroupData = funutils.partial(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys('specimenGroupId',
                                                              'maxCount',
                                                              'amount'))),
      createObj);

    /**
     * Creates a collection event type object with helper methods.
     *
     * @param {Object} collectionEventType the collection event type returned by the server.
     *
     * @param {Study} options.study the study this collection even type belongs to.
     *
     * @param {SpecimenGroup array} options.studySpecimenGroups all specimen groups for the study. Should be a
     * list returned by the server.
     *
     * @param {AnnotationType array} options.studyAnnotationTypes all the collection event annotation types
     * for the study. Should be a list returned by the server.
     *
     * @param {Array} options.studySpecimenGroups all specimen groups for the study.
     *
     * @param {Array} options.studyAnnotationTypes all the collection event annotation types for the
     * study.
     */
    function CollectionEventType(obj, options) {
      var self = this,
          defaults = {
            studyId:            null,
            name:               '',
            description:        null,
            recurring:          false,
            specimenGroupData:  [],
            annotationTypeData: []
          };

      obj = obj || {};
      ConcurrencySafeEntity.call(self, obj);
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));

      options = options || {};
      if (options.studySpecimenGroups) {
        self.studySpecimenGroups(options.studySpecimenGroups);
      }

      if (options.studyAnnotationTypes) {
        self.studyAnnotationTypes(options.studyAnnotationTypes);
      }
    }

    CollectionEventType.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(CollectionEventType.prototype, SpecimenGroupData);
    _.extend(CollectionEventType.prototype, AnnotationTypeData);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    CollectionEventType.create = function (obj) {
      var validation = validateObj(obj), atdValid, sgdValid;

      if (!_.isObject(validation)) {
        return new Error('invalid object: ' + validation);
      }

      atdValid = _.reduce(obj.annotationTypeData, function(memo, atd) {
        var validation = validateAnnotationTypeData(atd);
        return memo && _.isObject(validation);
      }, true);

      if (!atdValid) {
        return new Error('invalid object from server: bad annotation type data');
      }

      sgdValid = _.reduce(obj.specimenGroupData, function(memo, sgd) {
        var validation = validateSpecimenGroupData(sgd);
        return memo && _.isObject(validation);
      }, true);

      if (!sgdValid) {
        return new Error('invalid object from server: bad specimen group data');
      }

      return new CollectionEventType(obj);
    };

    CollectionEventType.get = function(studyId, id) {
      return biobankApi.get(uri(studyId) + '?cetId=' + id)
        .then(function(reply) {
          return CollectionEventType.create(reply);
        });
    };

    CollectionEventType.list = function(studyId) {
      return biobankApi.get(uri(studyId)).then(function(reply) {
        return _.map(reply, function (cet) {
          return CollectionEventType.create(cet);
        });
      });
    };

    CollectionEventType.prototype.addOrUpdate = function (annotationTypes) {
      var self = this,
          cmd = _.extend(_.pick(self,
                                'studyId',
                                'name',
                                'recurring'),
                         funutils.pickOptional(self, 'description'));

      cmd.specimenGroupData = self.getSpecimenGroupData();
      cmd.annotationTypeData = self.getAnnotationTypeData();

      return addOrUpdateInternal().then(function(reply) {
        return CollectionEventType.create(reply);
      });

      // --

      function addOrUpdateInternal() {
        if (self.isNew()) {
          return biobankApi.post(uri(self.studyId), cmd);
        }
        _.extend(cmd, { id: self.id, expectedVersion: self.version });
        return biobankApi.put(uri(self.studyId, self.id), cmd);
      }
    };

    CollectionEventType.prototype.remove = function () {
      return biobankApi.del(uri(this.studyId, this.id, this.version));
    };

    function uri(studyId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/cetypes';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    /** return constructor function */
    return CollectionEventType;
  }

  return CollectionEventTypeFactory;
});
