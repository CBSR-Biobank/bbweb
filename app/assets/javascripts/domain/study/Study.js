/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore', 'sprintf', 'tv4'], function(angular, _, sprintf, tv4) {
  'use strict';

  StudyFactory.$inject = [
    '$q',
    'funutils',
    'biobankApi',
    'ConcurrencySafeEntity',
    'StudyStatus',
    'studyStatusLabel',
    'AnnotationType',
    'hasAnnotationTypes'
  ];

  /**
   *
   */
  function StudyFactory($q,
                        funutils,
                        biobankApi,
                        ConcurrencySafeEntity,
                        StudyStatus,
                        studyStatusLabel,
                        AnnotationType,
                        hasAnnotationTypes) {

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
     *
     */
    function Study(obj) {
      var defaults = {
        name:            '',
        description:     null,
        annotationTypes: [],
        status:          StudyStatus.DISABLED
      };

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
      this.statusLabel = studyStatusLabel.statusToLabel(this.status);

      this.annotationTypes = _.map(this.annotationTypes, function (annotationType) {
        return new AnnotationType(annotationType);
      });
    }

    Study.prototype = Object.create(ConcurrencySafeEntity.prototype);
    _.extend(Study.prototype, hasAnnotationTypes);

    Study.prototype.constructor = Study;

    /**
     * Use Study.prototype.asyncCreate to create a study from async code.
     */
    Study.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new Error('invalid object from server: ' + tv4.error);
      }

      obj.annotationTypes = obj.annotationTypes || {};

      if (!hasAnnotationTypes.validAnnotationTypes(obj.annotationTypes)) {
        console.error('invalid object from server: bad annotation type');
        throw new Error('invalid object from server: bad annotation type');
      }

      return new Study(obj);
    };

    /**
     * @param {string} options.filter The filter to use on study names. Default is empty string.
     *
     * @param {string} options.status Returns studies filtered by status. The following are valid: 'all' to
     * return all studies, 'disabled' to return only disabled studies, 'enabled' to reutrn only enable
     * studies, and 'retired' to return only retired studies. For any other values the response is an error.
     *
     * @param {string} options.sortField Studies can be sorted by 'name' or by 'status'. Values other than
     * these two yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * studies should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of studies to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
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

    Study.get = function (id) {
      return biobankApi.get(uri(id)).then(function(reply) {
        return Study.prototype.asyncCreate(reply);
      });
    };

    Study.prototype.asyncCreate = function (obj) {
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

    Study.prototype.add = function () {
      var self = this,
          json = { name: this.name };
      angular.extend(json, funutils.pickOptional(self, 'description'));
      return biobankApi.post(uri(), json).then(function(reply) {
        return self.asyncCreate(reply);
      });
    };

    Study.prototype.updateName = function (name) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, uri('name', this.id), { name: name });
    };

    Study.prototype.updateDescription = function (description) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('description', this.id),
        description ? { description: description } : {});
    };

    Study.prototype.addAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('pannottype', this.id),
        _.omit(annotationType, 'uniqueId'));
    };

    Study.prototype.updateAnnotationType = function (annotationType) {
      return ConcurrencySafeEntity.prototype.update.call(
        this,
        uri('pannottype', this.id) + '/' + annotationType.uniqueId,
        annotationType);
    };

    Study.prototype.removeAnnotationType = function (annotationType) {
      var url = sprintf.sprintf('%s/%d/%s',
                                uri('pannottype', this.id), this.version, annotationType.uniqueId);
      return hasAnnotationTypes.removeAnnotationType.call(this, annotationType, url);
    };

    Study.prototype.disable = function () {
      if (this.isDisabled()) {
        throw new Error('already disabled');
      }
      return changeState.call(this, 'disable');
    };

    Study.prototype.enable = function () {
      if (this.isEnabled()) {
        throw new Error('already enabled');
      }
      return changeState.call(this, 'enable');
    };

    Study.prototype.retire = function () {
      if (this.isRetired()) {
        throw new Error('already retired');
      }
      return changeState.call(this, 'retire');
    };

    Study.prototype.unretire = function () {
      if (!this.isRetired()) {
        throw new Error('not retired');
      }
      return changeState.call(this, 'unretire');
    };

    Study.prototype.isDisabled = function () {
      return (this.status === StudyStatus.DISABLED);
    };

    Study.prototype.isEnabled = function () {
      return (this.status === StudyStatus.ENABLED);
    };

    Study.prototype.isRetired = function () {
      return (this.status === StudyStatus.RETIRED);
    };

    function changeState(state) {
      /* jshint validthis:true */
      var self = this,
          json = { expectedVersion: self.version };

      return biobankApi.post(uri(state, self.id), json).then(function (reply) {
        return self.asyncCreate(reply);
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

/* Local Variables:  */
/* mode: js          */
/* End:              */
