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
    'biobankApi',
    'annotationFactory'
  ];

  /**
   * Factory for participants.
   */
  function ParticipantFactory(funutils,
                              validationService,
                              ConcurrencySafeEntity,
                              biobankApi,
                              annotationFactory) {

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
     * To convert server side annotations to Annotation class call setAnnotationTypes().
     *
     * @param {object} obj.annotations - server response for annotation.
     *
     * @param {ParticiapantAnnotationType} annotationTypes. If both collectionEventType and
     * collectionEventTypes are passed to the constructor, the annotations will be converted to Annotation
     * objects.
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
        this.setAnnotationTypes(annotationTypes);
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
      return biobankApi.get(uri(studyId, id)).then(function (reply) {
        return Participant.create(reply);
      });
    };

    Participant.getByUniqueId = function (studyId, uniqueId) {
      return biobankApi.get(uri(studyId) + '/uniqueId/' + uniqueId)
        .then(function (reply) {
          return Participant.create(reply);
        });
    };

    Participant.prototype.setStudy = function (study) {
      this.study = study;
      this.studyId = study.id;
    };

    /**
     * Converts the server side annotations to Annotation objects, which make it easier to manage them.
     *
     * @param {ParticipantAnnotationType} annotationTypes - the annotation types allowed for this participant.
     */
    Participant.prototype.setAnnotationTypes = function (annotationTypes) {
      var self = this,
          differentIds;

      self.annotations = self.annotations || [];

      // make sure the annotations ids match up with the corresponding annotation types
      differentIds = _.difference(_.pluck(self.annotations, 'annotationTypeId'),
                                  _.pluck(annotationTypes, 'id'));

      if (differentIds.length > 0) {
        throw new Error('annotation types not found: ' + differentIds);
      }

      self.annotations = _.map(annotationTypes, function (annotationType) {
        var serverAnnotation = _.findWhere(self.annotations, { annotationTypeId: annotationType.id });

        // undefined is valid input
        return annotationFactory.create(serverAnnotation, annotationType);
      });
    };

    Participant.prototype.addOrUpdate = function () {
      var self = this,
          cmd = _.pick(self, 'studyId', 'uniqueId');

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
        return Participant.create(reply);
      });

      // --

      function addOrUpdateInternal(cmd) {
        if (self.isNew()) {
          return biobankApi.post(uri(self.studyId), cmd);
        }
        _.extend(cmd, { id: self.id, expectedVersion: self.version });
        return biobankApi.put(uri(self.studyId, self.id), cmd);
      }
    };

    /** return constructor function */
    return Participant;
  }

    function uri(/* studyId, participantId */) {
      var studyId,
          participantId,
          result = '/participants',
          args = _.toArray(arguments);

      if (args.length < 1) {
        throw new Error('study id not specified');
      }

      studyId = args.shift();
      result += '/' + studyId;


      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }

      return result;
    }
  return ParticipantFactory;
});
