/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['underscore'], function(_) {
  'use strict';

  ParticipantFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'participantsService',
    'Annotation'
  ];

  /**
   * Factory for participants.
   */
  function ParticipantFactory(funutils,
                              validationService,
                              ConcurrencySafeEntity,
                              participantsService,
                              Annotation) {

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

    /**
     * @param {object} obj.annotations - server response for annotation.
     */
    function Participant(obj, study, annotationTypes) {
      var defaults = {
        study:       null,
        studyId:     null,
        uniqueId:    '',
        annotations: []
      };

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));

      if (study) {
        this.setStudy(study);
      }

      if (annotationTypes) {
        this.setAnnotationTypes(obj.annotations, annotationTypes);
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
        return new Error('invalid annotation object from server');
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

    Participant.prototype.setAnnotationTypes = function (serverAnnotations, annotationTypes) {
      // make sure the annotations ids match up with the corresponding annotation types
      var differentIds = _.difference(_.pluck(serverAnnotations, 'annotationTypeId'),
                                      _.pluck(annotationTypes, 'id'));

      if (differentIds.length > 0) {
        throw new Error('annotation types not found: ' + differentIds);
      }

      serverAnnotations = serverAnnotations || [];
      this.annotations = _.map(annotationTypes, function (annotationType) {
        var serverAnnotation = _.findWhere(serverAnnotations, { annotationTypeId: annotationType.id }) || {};
        return new Annotation(serverAnnotation, annotationType);
      });
    };

    Participant.prototype.addOrUpdate = function () {
      var self = this;

      // convert annotations to server side entities
      self.annotations = _.map(self.annotations, function (annotation) {
        // make sure required annotations have values
        if (!annotation.isValid()) {
          throw new Error('required annotation has no value: annotationId: ' +
                          annotation.annotationType.id);
        }
        return annotation.getServerAnnotation();
      });

      return participantsService.addOrUpdate(self).then(function(reply) {
        return Participant.create(reply);
      });
    };

    /** return constructor function */
    return Participant;
  }

  return ParticipantFactory;
});
