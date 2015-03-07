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

    var checkObject = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys('id', 'uniqueId', 'annotations'));

    var addedEventKeys = ['studyId', 'participantId', 'uniqueId', 'annotations'];

    var updatedEventKeys = addedEventKeys.concat('version');

    var checkAddedEvent = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys.apply(null, addedEventKeys));

    var checkUpdatedEvent = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys.apply(null, updatedEventKeys));

    var checkAnnotations = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys('annotationTypeId', 'selectedValues'));

    function checkAddOrUpdateEvent(checker, event) {
      var checks = checker();

      if (checks.length) {
        return checks;
      }

      // now check annotations
      checks = _.reduce(event.annotations, function (annotation) {
        return checkAnnotations(annotation);
      });

      return checks;
    }

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
      var checks = checkObject(obj);
      if (checks.length) {
        throw new Error('invalid object from server: ' + checks.join(', '));
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
        var checker = this.isNew() ? checkAddedEvent : checkUpdatedEvent;
        var checks = checkAddOrUpdateEvent(checker, event);

        if (checks.length) {
          throw new Error('invalid event from server: ' + checks.join(', '));
        }

        return Participant.create(_.extend(funutils.renameKeys(event, { participantId: 'id' }),
                                           { version: 0 }));
      });
    };

    Participant.prototype.createAnnotationHelpers = function (annotationTypes) {
      var self = this;
      self.annotationHelpers = _.map(annotationTypes, function(annotType) {
        var helper =  new AnnotationHelper(annotType);
        var annotation = _.findWhere(self.annotations, {id: annotation.annotationTypeId});
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
