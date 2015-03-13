/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('Participant', ParticipantFactory);

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

    var requiredKeys = ['studyId', 'uniqueId', 'annotations'];

    var objRequiredKeys = requiredKeys.concat('id');

    var addedEventRequiredKeys = requiredKeys.concat('participantId');

    var updatedEventRequiredKeys = addedEventRequiredKeys.concat('version');

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, objRequiredKeys))),
      createObj);

    var validateAddedEvent = validateObj;

    var validateUpdatedEvent = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, updatedEventRequiredKeys))),
      createObj);

    var validateAnnotations = validationService.condition1(
      validationService.validator('has the correct keys',
                                  validationService.hasKeys(null, 'annotationTypeId', 'selectedValues')),
      createObj);

    function Participant(obj, study, annotationTypes) {
      var self = this;

      obj = obj || {};

      ConcurrencySafeEntity.call(this, obj);

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
        self.annotationHelpers = this.createAnnotationHelpers(annotationTypes);
      }
    }

    Participant.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        throw new Error('invalid object from server: ' + validation);
      }
      return new Participant(obj);
    };

    Participant.get = function (id) {
      var self = this;

      if (self.studyId === null) {
        throw new Error('study ID is null');
      }
      return participantsService.get(self.studyId, id).then(function (reply) {
        return Participant.create(reply);
      });
    };

    Participant.getByUniqueId = function (uniqueId) {
      var self = this;

      if (self.studyId === null) {
        throw new Error('study ID is null');
      }
      return participantsService.getByUniqueId(self.studyId, uniqueId).then(function (reply) {
        return Participant.create(reply);
      });
    };

    Participant.prototype.addOrUpdate = function () {
      var self = this;
      return participantsService.addOrUpdate(self).then(function(event) {
        var validation = validateAddOrUpdateEvent(event);

        if (!_.isObject(validation)) {
          throw validation;
        }

        return Participant.create(_.extend(funutils.renameKeys(event, { participantId: 'id' }),
                                           { version: 0 }));
      });

      function validateAddOrUpdateEvent(event) {
        var annotValid;
        var validator = self.isNew() ? validateAddedEvent : validateUpdatedEvent;
        var validation = validator(event);

        if (!_.isObject(validation)) {
          return new Error('invalid event from server: ' + validation);
        }

        // now check annotations
        annotValid =_.reduce(event.annotations, function (memo, annotation) {
          var validation = validateAnnotations(annotation);
          if (!_.isObject(validation)) {
            return false;
          }
          return memo;
        }, true);

        if (!annotValid) {
          return new Error('invalid event from server: ' + validation);
        }

        return event;
      }
    };

    Participant.prototype.createAnnotationHelpers = function (annotationTypes) {
      var self = this;
      self.annotationHelpers = _.map(annotationTypes, function(annotType) {
        var helper =  new AnnotationHelper(annotType);
        var annotation = _.findWhere(self.annotations, {id: annotType.id});
        if (annotation) {
          helper.setValue(annotation);
        }
        return helper;
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

    /** return constructor function */
    return Participant;
  }

});
