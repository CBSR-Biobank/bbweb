/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash', 'tv4', 'sprintf-js'], function(_, tv4, sprintf) {
  'use strict';

  ParticipantFactory.$inject = [
    '$q',
    '$log',
    'funutils',
    'ConcurrencySafeEntity',
    'Study',
    'Annotation',
    'annotationFactory',
    'DomainError',
    'biobankApi',
    'HasAnnotations'
  ];

  /**
   * Factory for participants.
   */
  function ParticipantFactory($q,
                              $log,
                              funutils,
                              ConcurrencySafeEntity,
                              Study,
                              Annotation,
                              annotationFactory,
                              DomainError,
                              biobankApi,
                              HasAnnotations) {

    var schema = {
      'id': 'Participant',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string' },
        'version':         { 'type': 'integer', 'minimum': 0 },
        'timeAdded':       { 'type': 'string' },
        'timeModified':    { 'type': [ 'string', 'null' ] },
        'uniqueId':        { 'type': 'string' },
        'studyId':         { 'type': 'string' },
        'annotations':     { 'type': 'array', 'items':{ '$ref': 'Annotation' } }
      },
      'required': [ 'id', 'studyId', 'uniqueId', 'annotations', 'version' ]
    };

    /**
     * Use this contructor to create a new Participant to be persited on the server. Use {@link
     * domain.studies.Participant.create|create()} or {@link
     * domain.studies.Particiapnt.asyncCreate|asyncCreate()} to create objects returned by the server.
     *
     * @classdesc The subject for which a set of specimens were collected from. The subject can be human or
     * non human. A participant belongs to a single [Study]{@link domain.studies.Study}.
     *
     * @class
     * @memberOf domain.studies
     * @extends domain.ConcurrencySafeEntity
     *
     * <i>To convert server side annotations to Annotation class call setAnnotationTypes().<i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     */
    function Participant(obj, study, annotations) {
      /**
       * A participant has a unique identifier that is used to identify the participant in the system. This
       * identifier is not the same as the <code>id</code> value object used by the domain model.
       *
       * @name domain.studies.Participant#uniqueId
       * @type {string}
       */
      this.uniqueId = '';

      /**
       * The study identifier for the {@link domain.studies.Study|Study} this participant belongs to.
       *
       * @name domain.studies.Participant#studyId
       * @type {string}
       */
      this.studyId = null;

      /**
       * The values of the {@link domain.Annotation|Annotations} collected for this participant.
       *
       * @name domain.studies.Participant#annotations
       * @type {Array<domain.Annotation>}
       */
      this.annotations = [];

      ConcurrencySafeEntity.call(this, schema, obj);
      _.extend(this, {
        study:       study,
        annotations: annotations
      });

      if (this.study) {
        this.setStudy(this.study);
      }
    }

    Participant.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(Participant.prototype, HasAnnotations.prototype);
    Participant.prototype.constructor = Participant;

    /**
     * Checks if <tt>obj</tt> has valid properties to construct a {@link
     * domain.studies.Participant|Participant}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    Participant.isValid = function (obj) {
      return ConcurrencySafeEntity.isValid(schema, [Annotation.schema ], obj);
    };

    /**
     * Creates a Participant, but first it validates <tt>obj</tt> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.Participant} A participant created from the given object.
     *
     * @see {@link domain.studies.Participant.asyncCreate|asyncCreate()} when you need to create a participant
     * within asynchronous code.
     */
    Participant.create = function (obj) {
      var study, annotations, validation = Participant.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      if (obj.annotations) {
        try {
          annotations = obj.annotations.map(function (annotation) {
            return Annotation.create(annotation);
          });
        } catch (e) {
          throw new DomainError('bad annotation type');
        }
      }

      return new Participant(obj, study, annotations);
    };

    /**
     * Creates a Participant from a server reply but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.studies.Participant>} A participant wrapped in a promise.
     *
     * @see {@link domain.studies.Participant.create|create()} when not creating a Participant within
     * asynchronous code.
     */
    Participant.asyncCreate = function (obj) {
      var result;

      try {
        result = Participant.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    /**
     * Retrieves a Participant from the server.
     *
     * @param {string} id the ID of the participant to retrieve.
     *
     * @returns {Promise<domain.studies.Participant>} The participant within a promise.
     */
    Participant.get = function (studyId, id) {
      return biobankApi.get(sprintf.sprintf('/participants/%s/%s', studyId, id))
        .then(Participant.prototype.asyncCreate);
    };

    /**
     * Retrieves a Participant, using the uniqueId, from the server.
     *
     * @param {string} uniqueId the uniqeue ID assigned to the participant.
     *
     * @returns {Promise<domain.studies.Participant>} The participant within a promise.
     */
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
      return Participant.asyncCreate(obj);
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

    /*
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
      return HasAnnotations.prototype.removeAnnotation.call(this, annotation, url);
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
