/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for Studies.
 */
/* @ngInject */
function StudyFactory($q,
                      $log,
                      biobankApi,
                      DomainEntity,
                      ConcurrencySafeEntity,
                      DomainError,
                      StudyState,
                      AnnotationType,
                      HasAnnotationTypes) {

  /**
   * Use this contructor to create a new Study to be persited on the server. Use {@link
   * domain.studies.Study.create|create()} or {@link domain.studies.Study.asyncCreate|asyncCreate()} to
   * create objects returned by the server.
   *
   * @class
   * @memberOf domain.studies
   * @extends domain.ConcurrencySafeEntity
   *
   * @classdesc A Study represents a collection of participants and specimens collected for a particular
   * research study.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function Study(obj, annotationTypes) {

    /**
     * A short identifying name that is unique.
     *
     * @name domain.studies.Study#name
     * @type {string}
     */
    this.name = '';

    /**
     * An optional description that can provide additional details on the name.
     *
     * @name domain.studies.Study#description
     * @type {string}
     * @default null
     */

    /**
     * The annotation types associated with participants of this study.
     *
     * @name domain.studies.Study#annotationTypes
     * @type {Array<domain.AnnotationType>}
     */
    this.annotationTypes = [];

    /**
     * The state can be one of: enabled, disabled, or retired.
     *
     * @name domain.studies.Study#state
     * @type {domain.studies.StudyState}
     */
    this.state = StudyState.DISABLED;

    ConcurrencySafeEntity.call(this, Study.SCHEMA, obj);
    _.extend(this, { annotationTypes: annotationTypes });
  }

  Study.prototype = Object.create(ConcurrencySafeEntity.prototype);
  _.extend(Study.prototype, HasAnnotationTypes.prototype);
  Study.prototype.constructor = Study;

  Study.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
    const args = [ 'studies' ].concat(_.toArray(arguments));
    return DomainEntity.url.apply(null, args);
  };

  /**
   * Used for validating plain objects.
   */
  Study.SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'Study',
    properties: {
      'slug':            { 'type': 'string' },
      'name':            { 'type': 'string' },
      'description':     { 'type': [ 'string', 'null' ] },
      'annotationTypes': { 'type': 'array', 'items':{ '$ref': 'AnnotationType' }  },
      'state':           { 'type': 'string' }
    },
    required: [ 'slug', 'name', 'annotationTypes', 'state' ]
  });

  /**
   * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.studies.Study|Study}.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
   */
  Study.isValid = function (obj) {
    return ConcurrencySafeEntity.isValid(Study.SCHEMA, [ AnnotationType.SCHEMA ], obj);
  };

  /**
   * Creates a Study, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.studies.Study} A study created from the given object.
   *
   * @see {@link domain.studies.Study.asyncCreate|asyncCreate()} when you need to create
   * a study within asynchronous code.
   */
  Study.create = function (obj) {
    var annotationTypes, validation = Study.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    if (obj.annotationTypes) {
      annotationTypes = obj.annotationTypes.map((annotationType) => AnnotationType.create(annotationType));
    }
    return new Study(obj, annotationTypes);
  };

  /**
   * Creates a Study from a server reply, but first validates that <tt>obj</tt> has a valid schema.
   * <i>Meant to be called from within promise code.</i>
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {Promise<domain.studies.Study>} A study wrapped in a promise.
   *
   * @see {@link domain.studies.Study.create|create()} when not creating a Study within asynchronous code.
   */
  Study.asyncCreate = function (obj) {
    var result;

    try {
      result = Study.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  /**
   * Retrieves a Study from the server.
   *
   * @param {string} slug the slug of the study to retrieve.
   *
   * @returns {Promise<domain.studies.Study>} The study within a promise.
   */
  Study.get = function (slug) {
    return biobankApi.get(Study.url(slug)).then(function(reply) {
      return Study.asyncCreate(reply);
    });
  };

  /**
   * Used to list studies.
   *
   * <p>A paged API is used to list studies. See below for more details.</p>
   *
   * @param {object} options - The options to use to list studies.
   *
   * @param {string} [options.filter] The filter to use on study names. Default is empty string.
   *
   * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
   *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
   *        in descending order.
   *
   * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
   *        studies should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} [options.limit=10] The total number of studies to return per page. The maximum page size
   *        is 10. If a value larger than 10 is used then the response is an error.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   *          domain.studies.Study}.
   */
  Study.list = function (options) {
    var params,
        validKeys = [
          'filter',
          'sort',
          'page',
          'limit'
        ];

    options = options || {};
    params = _.omitBy(_.pick(options, validKeys), function (value) {
      return value === '';
    });

    return biobankApi.get(Study.url('search'), params).then(createStudiesFromPagedResult);
  };

  /**
   * Used to list studies the user can collect specimens for.
   *
   * <p>A paged API is used to list studies. See below for more details.</p>
   *
   * @param {object} options - The options to use to list studies.
   *
   * @param {string} [options.filter] The filter to use on study names. Default is empty string.
   *
   * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
   *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
   *        in descending order.
   *
   * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
   *        studies should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} [options.limit=10] The total number of studies to return per page. The maximum page size
   *        is 10. If a value larger than 10 is used then the response is an error.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   *          domain.studies.Study}.
   */
  Study.collectionStudies = function (options) {
    var params,
        validKeys = [
          'filter',
          'sort',
          'page',
          'limit'
        ];

    options = options || {};
    params = _.omitBy(_.pick(options, validKeys), function (value) {
      return value === '';
    });

    return biobankApi.get(Study.url('collectionStudies'), params).then(createStudiesFromPagedResult);
  };

  /**
   * Adds a study.
   *
   * @return {Promise<domain.studies.Study>} A promise containing the study that was created.
   */
  Study.prototype.add = function () {
    var json = _.pick(this, 'name', 'description');
    // calling function Study.url with ean empty string appends a trailing slash
    return biobankApi.post(Study.url(''), json).then(function(reply) {
      return Study.asyncCreate(reply);
    });
  };

  /**
   * Creates a Study from a server reply but first validates that it has a valid schema.
   *
   * <p>A wrapper for {@link domian.studies.Study#asyncCreate}.</p>
   *
   * @param {object} obj - the object to take initial values from.
   *
   * @returns {domain.studies.Study} A new Study object.
   *
   * @see {@link domain.ConcurrencySafeEntity#update}
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
    return this.update.call(this, Study.url('name', this.id), { name: name });
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
                            Study.url('description', this.id),
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
                            Study.url('pannottype', this.id),
                            _.omit(annotationType, 'id'));
  };

  /**
   * Updates an existing annotation type for a participant.
   *
   * @param {domain.AnnotationType} annotationType - the annotation type with the new values.
   *
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the updated annotation
   * type.
   */
  Study.prototype.updateAnnotationType = function (annotationType) {
    return this.update.call(this,
                            Study.url('pannottype', this.id) + '/' + annotationType.id,
                            annotationType);
  };

  /**
   * Removes an existing annotation type for a participant.
   *
   * @param {domain.AnnotationType} annotationType - the annotation type to remove.
   *
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the removed annotation
   * type.
   */
  Study.prototype.removeAnnotationType = function (annotationType) {
    var url = Study.url('pannottype', this.id, this.version, annotationType.id);
    return HasAnnotationTypes.prototype.removeAnnotationType.call(this, annotationType, url);
  };

  /**
   * Disables a study.
   *
   * @throws An error if the study is already disabled.
   *
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the new state.
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
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the new state.
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
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the new state.
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
   * @returns {Promise<domain.studies.Study>} A promise containing the study with the new state.
   */
  Study.prototype.unretire = function () {
    if (!this.isRetired()) {
      throw new DomainError('not retired');
    }
    return changeState.call(this, 'unretire');
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>disabled</code> state.
   */
  Study.prototype.isDisabled = function () {
    return (this.state === StudyState.DISABLED);
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>enabled</code> state.
   */
  Study.prototype.isEnabled = function () {
    return (this.state === StudyState.ENABLED);
  };

  /**
   * Used to query the study's current state.
   *
   * @returns {boolean} <code>true</code> if the study is in <code>retired</code> state.
   */
  Study.prototype.isRetired = function () {
    return (this.state === StudyState.RETIRED);
  };

  /**
   * Returns all locations for all the centres associated with this study.
   *
   * @returns {Promise<Array<domain.centres.CentreLocationDto>>} A promise.
   *
   * @see [Centre.centreLocationToNames()]{@link domain.centres.Centre.centreLocationToNames}
   */
  Study.prototype.allLocations = function () {
    return biobankApi.get(Study.url('centres', this.id));
  };

  /**
   * Weather the study can be enabled or not.
   *
   * @return {Promise<boolean>} If the promise is successful and it's value is TRUE, then the Study can be
   * enabled.
   */
  Study.prototype.isEnableAllowed = function () {
    return biobankApi.get(Study.url('enableAllowed', this.id));
  };

  function createStudiesFromPagedResult(reply) {
    var deferred = $q.defer();
    try {
      reply.items = reply.items.map((obj) => Study.create(obj));
      deferred.resolve(reply);
    } catch (e) {
      deferred.reject('invalid studies from server');
    }
    return deferred.promise;
  }

  function changeState(state) {
    /* jshint validthis:true */
    var self = this,
        json = { expectedVersion: self.version };

    return biobankApi.post(Study.url(state, self.id), json)
      .then((reply) => Study.asyncCreate(reply));
  }

  return Study;
}

export default ngModule => ngModule.factory('Study', StudyFactory)
