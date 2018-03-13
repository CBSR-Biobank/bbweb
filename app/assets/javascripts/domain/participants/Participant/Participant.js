/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Factory for participants.
 */
/* @ngInject */
function ParticipantFactory($q,
                            $log,
                            DomainEntity,
                            ConcurrencySafeEntity,
                            Study,
                            Annotation,
                            annotationFactory,
                            DomainError,
                            biobankApi,
                            HasAnnotations) {

  /**
   * Use this contructor to create a new Participant to be persited on the server. Use {@link
   * domain.studies.Participant.create|create()} or {@link
   * domain.studies.Particiapnt.asyncCreate|asyncCreate()} to create objects returned by the server.
   *
   * @classdesc The subject for which a set of specimens were collected from. The subject can be human or
   * non human. A participant belongs to a single [Study]{@link domain.studies.Study}.
   *
   * <i>To convert server side annotations to Annotation class call setAnnotationTypes().<i>
   *
   * @class
   * @memberOf domain.participants
   * @extends domain.ConcurrencySafeEntity
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

    ConcurrencySafeEntity.call(this, Participant.SCHEMA, obj);
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

  Participant.REST_API_URL_SUFFIX = 'participants';

  Participant.SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'Participant',
    properties: {
      'slug':        { 'type': 'string' },
      'uniqueId':    { 'type': 'string' },
      'studyId':     { 'type': 'string' },
      'annotations': { 'type': 'array', 'items':{ '$ref': 'Annotation' } }
    },
    required: [ 'slug', 'uniqueId', 'annotations' ]
  });

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
    return ConcurrencySafeEntity.isValid(Participant.SCHEMA, [ Annotation.SCHEMA ], obj);
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

  Participant.url = function (...paths) {
    const args = [ Participant.REST_API_URL_SUFFIX ].concat(paths);
    return DomainEntity.url.apply(null, args);
  };

  /**
   * Retrieves a Participant from the server.
   *
   * @param {string} slug the slug of the study to retrieve.
   *
   * @returns {Promise<domain.studies.Participant>} The participant within a promise.
   */
  Participant.get = function (slug) {
    return biobankApi.get(Participant.url(slug))
      .then(Participant.asyncCreate);
  };

  Participant.prototype.setStudy = function (study) {
    this.study = study;
    this.studyId = study.id;
    this.setAnnotationTypes(study.annotationTypes);
  };

  /**
   * Returns a promise. If annotations are found to be invalid, then the promise is rejected. If the
   * annotations are valid, then a request is made to the server to add the participant.
   */
  Participant.prototype.add = function () {
    var deferred = $q.defer(),
        invalidAnnotationErrMsg = null,
        cmd = _.pick(this, 'studyId', 'uniqueId');

    // convert annotations to server side entities
    if (this.annotations) {
      cmd.annotations = this.annotations.map((annotation) => {
        // make sure required annotations have values
        if (!annotation.isValueValid()) {
          invalidAnnotationErrMsg = 'required annotation has no value: annotationId: ' +
            annotation.annotationTypeId;
        }
        return annotation.getServerAnnotation();
      });
    } else {
      cmd.annotations = [];
    }

    if (invalidAnnotationErrMsg) {
      deferred.reject(invalidAnnotationErrMsg);
    } else {
      biobankApi.post(Participant.url(this.studyId), cmd)
        .then(Participant.asyncCreate)
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
    return ConcurrencySafeEntity.prototype.update.call(this, Participant.url(path, this.id), reqJson)
      .then(Participant.asyncCreate)
      .then(updatedParticipant => {
        if (this.study) {
          updatedParticipant.setStudy(this.study);
        }
        return $q.when(updatedParticipant);
      });
  };

  Participant.prototype.updateUniqueId = function (uniqueId) {
    return this.update('uniqueId', { uniqueId: uniqueId });
  };

  Participant.prototype.addAnnotation = function (annotation) {
    return this.update('annot', annotation.getServerAnnotation());
  };

  Participant.prototype.removeAnnotation = function (annotation) {
    var url = Participant.url('annot', this.id, this.version, annotation.annotationTypeId);
    return HasAnnotations.prototype.removeAnnotation.call(this, annotation, url)
      .then(Participant.asyncCreate);
  };

  /** return constructor function */
  return Participant;
}

export default ngModule => ngModule.factory('Participant', ParticipantFactory)
