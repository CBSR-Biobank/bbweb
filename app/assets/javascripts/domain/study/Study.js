/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash', 'sprintf', 'tv4'], function(angular, _, sprintf, tv4) {
  'use strict';

  StudyFactory.$inject = [
    '$q',
    'funutils',
    'biobankApi',
    'ConcurrencySafeEntity',
    'DomainError',
    'StudyStatus',
    'studyStatusLabel',
    'AnnotationType',
    'hasAnnotationTypes'
  ];

  /**
   * Angular factory for Studies.
   */
  function StudyFactory($q,
                        funutils,
                        biobankApi,
                        ConcurrencySafeEntity,
                        DomainError,
                        StudyStatus,
                        studyStatusLabel,
                        AnnotationType,
                        hasAnnotationTypes) {

    /**
     * Used for validating plain objects.
     */
    var schema = {
      'id': 'Study',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string' },
        'version':         { 'type': 'integer', 'minimum': 0 },
        'timeAdded':       { 'type': 'string' },
        'timeModified':    { 'type': [ 'string', 'null' ] },
        'name':            { 'type': 'string' },
        'description':     { 'type': [ 'string', 'null' ] },
        'annotationTypes': { 'type': 'array' },
        'status':          { 'type': 'string' }
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'status' ]
    };

    /**
     * Use this contructor to create new Study to be persited on the server. Use
     * [create()]{@link domain.studies.Study.create} or [asyncCreate()]{@link
     * domain.studies.Study.asyncCreate} to create objects returned by the server.
     *
     * @class
     * @memberOf domain.studies
     * @extends domain.ConcurrencySafeEntity
     *
     * @classdesc A Study represents a collection of participants and specimens collected for a particular research
     * study.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function Study(obj) {

      /**
       * A short identifying name that is unique.
       *
       * @name domain.studies.Study#name
       * @type {string}
       */
      this.name = '';

      /**
       * A description that can provide additional details on the name.
       *
       * @name domain.studies.Study#description
       * @type {string}
       * @default null
       */
      this.description = null;

      /**
       * The annotation types associated with participants of this study.
       *
       * @name domain.studies.Study#annotationTypes
       * @type {Array<domain.AnnotationType>}
       */
      this.annotationTypes = [];

      /**
       * The status can be one of: enabled, disabled, or retired.
       *
       * @name domain.studies.Study#status
       * @type {domain.studies.StudyStatus}
       */
      this.status = StudyStatus.DISABLED;

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, obj);
      this.statusLabel = studyStatusLabel.statusToLabel(this.status);

      this.annotationTypes = _.map(this.annotationTypes, function (annotationType) {
        return new AnnotationType(annotationType);
      });
    }

    Study.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(Study.prototype, hasAnnotationTypes);

    Study.prototype.constructor = Study;

    /**
     * Creates a Study, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Study} A study created from the given object.
     *
     * @see [asyncCreate()]{@link domain.studies.Study.asyncCreate} when you need to create
     * a study within asynchronous code.
     */
    Study.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new DomainError('invalid object from server: ' + tv4.error);
      }

      obj.annotationTypes = obj.annotationTypes || {};

      if (!hasAnnotationTypes.validAnnotationTypes(obj.annotationTypes)) {
        console.error('invalid object from server: bad annotation type');
        throw new DomainError('invalid object from server: bad annotation type');
      }

      return new Study(obj);
    };

    /**
     * Creates a Study from a server reply but first validates that it has a valid schema.
     *
     * <p>Meant to be called from within promise code.</p>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.studies.Study>} A study wrapped in a promise.
     *
     * @see [create()]{@link domain.studies.Study.create} when not creating a Study within asynchronous code.
     */
    Study.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else if (!hasAnnotationTypes.validAnnotationTypes(obj.annotationTypes)) {
        console.error('invalid annotation types from server: ' + tv4.error);
        deferred.reject('invalid annotation types from server: ' + tv4.error);
      } else {
        deferred.resolve(new Study(obj));
      }

      return deferred.promise;
    };

    /**
     * Used to list studies.
     *
     * <p>A paged API is used to list studies. See below for more details.</p>
     *
     * @param {object} options - The options to use to list studies.
     *
     * @param {string} [options.filter] The filter to use on study names.
     *
     * @param {string} [options.status=all] Returns studies filtered by status. The following are valid:
     * <code>all</code> to return all studies, <code>disabled</code> to only return disabled studies,
     * <code>enabled</code> to only return enabled studies, and <code>retired</code> to only return retired
     * studies. For any other values the response is an error.
     *
     * @param {string} [options.sortField=name] Studies can be sorted by <code>name</code> or by
     * <code>status</code>. Values other than these two yield an error.
     *
     * @param {int} [options.page=1] If the total results are longer than pageSize, then page selects which
     * studies should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} [options.pageSize=10] The total number of studies to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} [options.order=asc] - The order to list studies by. One of: <code>asc</code> for
     * ascending order, or <code>desc</code> for descending order.
     *
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    Study.list = function (options) {
      var url = uri(),
          params,
          validKeys = [
            'filter',
            'status',
            'sort',
            'page',
            'pageSize',
            'order'
          ];

      options = options || {};
      params = _.extend({}, _.pick(options, validKeys));

      return biobankApi.get(url, params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return Study.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid studies from server');
        }
        return deferred.promise;
      });
    };

    /**
     * @typedef domain.studies.StudyNameDto
     * @type object
     * @property {String} id - the study's identity
     * @property {String} name - the study's name
     * @property {String} status - the study's status
     */

    /**
     * Returns the names of all the studies in the system.
     *
     * @returns {Promise<Array<domain.studies.StudyNameDto>>} The names of the studies in the system.
     */
    Study.names = function () {
      return biobankApi.get('/studies/names');
    };

    /**
     * Retrieves a Study from the server.
     *
     * @param {string} id the ID of the study to retrieve.
     *
     * @returns {Promise<domain.studies.Study>} The study within a promise.
     */
    Study.get = function (id) {
      return biobankApi.get(uri(id)).then(function(reply) {
        return Study.asyncCreate(reply);
      });
    };

    /**
     * Adds a study.
     *
     * @return {Promise<domain.studies.Study>} A promise containing the study that was created.
     */
    Study.prototype.add = function () {
      var self = this,
          json = { name: this.name };
      angular.extend(json, funutils.pickOptional(self, 'description'));
      return biobankApi.post(uri(), json).then(function(reply) {
        return Study.asyncCreate(reply);
      });
    };

    /**
     * Creates a Study from a server reply but first validates that it has a valid schema.
     *
     * <p>A wrapper for {@link domian.studies.Study#asyncCreate}.</p>
     *
     * @see domain.ConcurrencySafeEntity.update
     */
    Study.prototype.asyncCreate = function (obj) {
      return Study.asyncCreate(obj);
    };


    /**
     * Updates the Study's name.
     *
     * @param {String} name - The new name to give this study.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new name.
     */
    Study.prototype.updateName = function (name) {
      return this.update.call(this, uri('name', this.id), { name: name });
    };

    /**
     * Updates the Study's description.
     *
     * @param {String} description - The new description to give this study. When description is
     * <code>falsy</code>, the description will be cleared.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new description.
     */
    Study.prototype.updateDescription = function (description) {
      return this.update.call(this,
                              uri('description', this.id),
                              description ? { description: description } : {});
    };

    /**
     * Adds an annotation type to be used on participants of this study.
     *
     * @param {domain.AnnotationType} annotationType - the annotation type to add to this study.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new annotation type.
     */
    Study.prototype.addAnnotationType = function (annotationType) {
      return this.update.call(this,
                              uri('pannottype', this.id),
                              _.omit(annotationType, 'uniqueId'));
    };

    /**
     * Updates an existing annotation type for a participant.
     *
     * @param {domain.AnnotationType} annotationType - the annotation type with the new values.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the updated annotation type.
     */
    Study.prototype.updateAnnotationType = function (annotationType) {
      return this.update.call(this,
                              uri('pannottype', this.id) + '/' + annotationType.uniqueId,
                              annotationType);
    };

    /**
     * Removes an existing annotation type for a participant.
     *
     * @param {domain.AnnotationType} annotationType - the annotation type to remove.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the removed annotation type.
     */
    Study.prototype.removeAnnotationType = function (annotationType) {
      var url = sprintf.sprintf('%s/%d/%s',
                                uri('pannottype', this.id), this.version, annotationType.uniqueId);
      return hasAnnotationTypes.removeAnnotationType.call(this, annotationType, url);
    };

    /**
     * Disables a study.
     *
     * @throws An error if the study is already disabled.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new status.
     */
    Study.prototype.disable = function () {
      if (this.isDisabled()) {
        throw new DomainError('already disabled');
      }
      return changeState.call(this, 'disable');
    };

    /**
     * Enables a study.
     *
     * @throws An error if the study is already enabled.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new status.
     */
    Study.prototype.enable = function () {
      if (this.isEnabled()) {
        throw new DomainError('already enabled');
      }
      return changeState.call(this, 'enable');
    };

    /**
     * Retires a study.
     *
     * @throws An error if the study is already retired.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new status.
     */
    Study.prototype.retire = function () {
      if (this.isRetired()) {
        throw new DomainError('already retired');
      }
      return changeState.call(this, 'retire');
    };

    /**
     * Unretires a study. The study will be in <code>disabled</code> state after it is unretired.
     *
     * @throws An error if the study is not retired.
     *
     * @returns {Promise<domain.studies.Study>} A promise containing the study with the new status.
     */
    Study.prototype.unretire = function () {
      if (!this.isRetired()) {
        throw new DomainError('not retired');
      }
      return changeState.call(this, 'unretire');
    };

    /**
     * Used to query the study's current status.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>disabled</code> state.
     */
    Study.prototype.isDisabled = function () {
      return (this.status === StudyStatus.DISABLED);
    };

    /**
     * Used to query the study's current status.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>enabled</code> state.
     */
    Study.prototype.isEnabled = function () {
      return (this.status === StudyStatus.ENABLED);
    };

    /**
     * Used to query the study's current status.
     *
     * @returns {boolean} <code>true</code> if the study is in <code>retired</code> state.
     */
    Study.prototype.isRetired = function () {
      return (this.status === StudyStatus.RETIRED);
    };

    /**
     * Returns all locations for all the centres associated with this study.
     *
     * @returns {Promise<Array<domain.centres.CentreLocationDto>>} A promise.
     */
    Study.prototype.allLocations = function () {
      return biobankApi.get('/studies/centres/' + this.id);
    };

    function changeState(state) {
      /* jshint validthis:true */
      var self = this,
          json = { expectedVersion: self.version };

      return biobankApi.post(uri(state, self.id), json).then(function (reply) {
        return Study.asyncCreate(reply);
      });
    }

    function uri(/* path, studyId */) {
      var args = _.toArray(arguments),
          studyId,
          path,
          result = '/studies';

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        studyId = args.shift();
        result += '/' + studyId;
      }

      return result;
    }

    return Study;
  }

  return StudyFactory;
});
