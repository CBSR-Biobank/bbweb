/* global define */
define(['underscore'], function(_) {
  'use strict';

  ParticipantFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'participantsService',
    'AnnotationHelper'
  ];

  /**
   * Factory for participants.
   */
  function ParticipantFactory(funutils,
                              validationService,
                              ConcurrencySafeEntity,
                              participantsService,
                              AnnotationHelper) {

    var requiredKeys = ['id', 'studyId', 'uniqueId', 'annotations', 'version'];

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

    function Participant(obj, study, annotationTypes) {
      var self = this;

      obj = obj || {};

      _.extend(this, _.defaults(obj, {
        study:             null,
        studyId:           null,
        uniqueId:          '',
        annotations:       [],
        annotationHelpers: []
      }));

      if (study) {
        self.study   = study;
        self.studyId = study.id;
      }

      if (annotationTypes) {
        self.annotationHelpers = createAnnotationHelpers.call(self, annotationTypes);
      }
    }

    Participant.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Participant.create = function (obj) {
      var annotValid, validation = validateObj(obj);

      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }

      annotValid =_.reduce(obj.annotations, function (memo, annotation) {
        var validation = validateAnnotations(annotation);
        return memo && _.isObject(validation);
      }, true);

      if (!annotValid) {
        return new Error('invalid object from server: ' + annotValid);
      }
      return new Participant(obj);
    };

    Participant.get = function (studyId, id) {
      return participantsService.get(studyId, id).then(function (reply) {
        return Participant.create(reply);
      });
    };

    Participant.getByUniqueId = function (studyId, uniqueId) {
      return participantsService.getByUniqueId(studyId, uniqueId).then(function (reply) {
        return Participant.create(reply);
      });
    };

    Participant.prototype.setStudy = function (study) {
      this.study = study;
      this.studyId = study.id;
    };

    Participant.prototype.setAnnotationTypes = function (annotationTypes) {
      this.annotationHelpers = createAnnotationHelpers.call(this, annotationTypes);
    };

    Participant.prototype.addOrUpdate = function () {
      var self = this;
      return participantsService.addOrUpdate(self).then(function(reply) {
        return Participant.create(reply);
      });
    };

    /**
     * Updated the annotations array with the values assigned to the annotation helpers.
     */
    Participant.prototype.updateAnnotations = function () {
      var self = this;
      self.annotations = [];
      return _.each(self.annotationHelpers, function (annotationHelper) {
        self.annotations.push(annotationHelper.getAnnotation());
      });
    };

    function createAnnotationHelpers(annotationTypes) {
      /*jshint validthis:true */
      var self = this;
      return _.map(annotationTypes, function(annotationType) {
        var helper =  new AnnotationHelper(annotationType);
        var annotation = _.findWhere(self.annotations, {annotationTypeId: annotationType.id});
        if (annotation) {
          helper.setValue(annotation);
        }
        return helper;
      });
    }

    /** return constructor function */
    return Participant;
  }

  return ParticipantFactory;
});
