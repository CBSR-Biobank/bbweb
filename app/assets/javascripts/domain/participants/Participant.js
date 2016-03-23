/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore', 'tv4', 'sprintf'], function(_, tv4, sprintf) {
  'use strict';

  ParticipantFactory.$inject = [
    '$q',
    'funutils',
    'ConcurrencySafeEntity',
    'biobankApi',
    'hasAnnotations',
    'annotationFactory'
  ];

  /**
   * Factory for participants.
   */
  function ParticipantFactory($q,
                              funutils,
                              ConcurrencySafeEntity,
                              biobankApi,
                              hasAnnotations,
                              annotationFactory) {

    var schema = {
      'id': 'Participant',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string' },
        'version':         { 'type': 'integer', 'minimum': 0 },
        'timeAdded':       { 'type': 'string' },
        'timeModified':    { 'type': [ 'string', 'null' ] },
        'uniqueId':        { 'type': 'string' },
        'annotations':     { 'type': 'array' }
      },
      'required': [ 'id', 'studyId', 'uniqueId', 'annotations', 'version' ]
    };

    /**
     * To convert server side annotations to Annotation class call setAnnotationTypes().
     *
     * @param {object} obj.annotations - server response for annotation.
     *
     * @param {study} study The study this participant is a member of.
     */
    function Participant(obj, study) {
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
    }

    Participant.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(Participant.prototype, hasAnnotations);
    Participant.prototype.constructor = Participant;

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Participant.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new Error('invalid object from server: ' + tv4.error);
      }

      if (!hasAnnotations.validAnnotations(obj.annotations)) {
        console.error('invalid object from server: bad annotation type');
        throw new Error('invalid object from server: bad annotation type');
      }
      return new Participant(obj);
    };

    Participant.get = function (studyId, id) {
      return biobankApi.get(sprintf.sprintf('/participants/%s/%s', studyId, id))
        .then(Participant.prototype.asyncCreate);
    };

    Participant.getByUniqueId = function (studyId, uniqueId) {
      return biobankApi.get(sprintf.sprintf('/participants/uniqueId/%s/%s', studyId, uniqueId))
        .then(function (reply) {
          return Participant.create(reply);
        });
    };

    Participant.prototype.setStudy = function (study) {
      this.study = study;
      this.studyId = study.id;
      this.setAnnotationTypes(study.annotationTypes);
    };

    Participant.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();

      obj = obj || {};
      obj.annotations = obj.annotations || {};

      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else if (!hasAnnotations.validAnnotations(obj.annotations)) {
        console.error('invalid object from server: bad annotation type');
        deferred.reject('invalid object from server: bad annotation type');
      } else {
        deferred.resolve(new Participant(obj));
      }
      return deferred.promise;
    };

    /**
     * Returns a promise. If annotations are found to be invalid, then the promise is rejected. If the
     * annotations are valid, then a request is made to the server to add the participant.
     */
    Participant.prototype.add = function () {
      var self = this,
          deferred = $q.defer(),
          invalidAnnotationErrMsg = null,
          cmd = _.pick(self, 'studyId', 'uniqueId');

      // convert annotations to server side entities
      cmd.annotations = _.map(self.annotations, function (annotation) {
        // make sure required annotations have values
        if (!annotation.isValueValid()) {
          invalidAnnotationErrMsg = 'required annotation has no value: annotationId: ' +
            annotation.annotationTypeId;
        }
        return annotation.getServerAnnotation();
      });

      if (invalidAnnotationErrMsg) {
        deferred.reject(invalidAnnotationErrMsg);
      } else {
        biobankApi.post(uri(self.studyId), cmd)
          .then(self.asyncCreate)
          .then(function (participant) {
            deferred.resolve(participant);
          });
      }

      return deferred.promise;
    };

    /**
     * Sets the collection event type after an update.
     */
    Participant.prototype.update = function (path, reqJson) {
      var self = this;

      return ConcurrencySafeEntity.prototype.update.call(this, uri(path, self.id), reqJson)
        .then(postUpdate);

      function postUpdate(updatedParticipant) {
        if (self.study) {
          updatedParticipant.setStudy(self.study);
        }
        return $q.when(updatedParticipant);
      }
    };

    Participant.prototype.updateUniqueId = function (uniqueId) {
      return this.update('uniqueId', { uniqueId: uniqueId });
    };

    Participant.prototype.addAnnotation = function (annotation) {
      return this.update('annot', annotation.getServerAnnotation());
    };

    Participant.prototype.removeAnnotation = function (annotation) {
      var url = sprintf.sprintf('%s/%d/%s',
                                uri('annot', this.id),
                                this.version,
                                annotation.annotationTypeId);
      return hasAnnotations.removeAnnotation.call(this, annotation, url);
    };

    function uri(/* path, participantId */) {
      var path,
          participantId,
          result = '/participants',
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }

      return result;
    }

    /** return constructor function */
    return Participant;
  }

  return ParticipantFactory;
});
