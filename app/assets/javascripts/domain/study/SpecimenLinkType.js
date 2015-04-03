/* global define */
define(['underscore'], function(_) {
  'use strict';

  SpecimenLinkTypeFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ConcurrencySafeEntity',
    'AnnotationTypeDataSet'
  ];

  /**
   * Factory for specimenLinkTypes.
   */
  function SpecimenLinkTypeFactory(funutils,
                                   validationService,
                                   biobankApi,
                                   ConcurrencySafeEntity,
                                   AnnotationTypeDataSet) {

    var requiredKeys = [
      'id',
      'processingTypeId',
      'version',
      'expectedInputChange',
      'expectedOutputChange',
      'inputCount',
      'outputCount',
      'inputGroupId',
      'outputGroupId',
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

    /**
     * Creates a specimen link type object with helper methods.
     *
     * @param specimenLinkType the specimen link type returned by the server.
     *
     * @param processing type processing type this specimenLinkType belongs. Returned by server.
     *
     * @param options.studySpecimenGroups all specimen groups for the study the processing type belongs to.
     *
     * @param options.studyAnnotationTypes all the specimen link annotation types for the study.
     */
    function SpecimenLinkType(specimenLinkType, options) {
      var self = this;

      specimenLinkType = specimenLinkType || {};
      ConcurrencySafeEntity.call(self, specimenLinkType);

      _.extend(self, _.defaults(specimenLinkType, {
        processingTypeId:     null,
        expectedInputChange:  null,
        expectedOutputChange: null,
        inputCount:           null,
        outputCount:          null,
        inputGroupId:         null,
        outputGroupId:        null,
        annotationTypeData:   []
      }));

      options = options || {};

      if (options.studySpecimenGroups) {
        self.studySpecimenGroups(options.studySpecimenGroups);
      }

      if (options.studyAnnotationTypes) {
        self.studyAnnotationTypes(options.studyAnnotationTypes);
      }
    }

    SpecimenLinkType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    SpecimenLinkType.create = function (obj) {
      var validation = validateObj(obj), atdValid;

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

      return new SpecimenLinkType(obj);
    };

    SpecimenLinkType.get = function(processingTypeId, id) {
      return biobankApi.get(uri(processingTypeId) + '?slTypeId=' + id)
        .then(function(reply) {
          return SpecimenLinkType.create(reply);
        });
    };

    SpecimenLinkType.list = function(processingTypeId) {
      return biobankApi.get(uri(processingTypeId)).then(function(reply) {
        return _.map(reply, function (cet) {
          return SpecimenLinkType.create(cet);
        });
      });
    };

    SpecimenLinkType.prototype.addOrUpdate = function (annotationTypes) {
      var self = this,
          cmd = _.extend(_.pick(this,
                                'processingTypeId',
                                'expectedInputChange',
                                'expectedOutputChange',
                                'inputCount',
                                'outputCount',
                                'inputGroupId',
                                'outputGroupId'),
                         funutils.pickOptional(this,
                                               'inputContainerTypeId',
                                               'outputContainerTypeId'));

      if (this.annotationTypeDataSet) {
        cmd.annotationTypeData = this.annotationTypeDataSet.getAnnotationTypeData();
      } else {
        cmd.annotationTypeData = this.annotationTypeData;
      }

      return addOrUpdateInternal().then(function(reply) {
        return SpecimenLinkType.create(reply);
      });

      function addOrUpdateInternal() {
        if (self.isNew()) {
          return biobankApi.post(uri(self.processingTypeId), cmd);
        } else {
          cmd.id = self.id;
          cmd.expectedVersion = self.version;
          return biobankApi.put(uri(self.processingTypeId, self.id), cmd);
        }
      }
    };

    SpecimenLinkType.prototype.remove = function () {
      return biobankApi.del(uri(this.processingTypeId, this.id, this.version));
    };

    SpecimenLinkType.prototype.studySpecimenGroups = function (studySpecimenGroups) {
      this.inputGroup = _.findWhere(studySpecimenGroups, { id: this.inputGroupId});
      this.outputGroup = _.findWhere(studySpecimenGroups, { id: this.outputGroupId});
    };

    SpecimenLinkType.prototype.studyAnnotationTypes = function (annotationTypes) {
      this.annotationTypeDataSet =
        new AnnotationTypeDataSet(this.annotationTypeData, {
          studyAnnotationTypes: annotationTypes
        });
    };

    SpecimenLinkType.prototype.allAnnotationTypeDataIds  = function () {
      if (this.annotationTypeDataSet) {
        return this.annotationTypeDataSet.allIds();
      }
      throw new Error('no data items');
    };

    SpecimenLinkType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      if (this.annotationTypeDataSet) {
        return this.annotationTypeDataSet.get(annotationTypeId);
      }
      throw new Error('no data items');
    };

    SpecimenLinkType.prototype.getAnnotationTypesAsString = function () {
      if (this.annotationTypeDataSet) {
        return this.annotationTypeDataSet.getAsString();
      }
      throw new Error('no data items');
    };

    function uri(processingTypeId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + processingTypeId + '/sltypes';

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
    return SpecimenLinkType;
  }

  return SpecimenLinkTypeFactory;
});
