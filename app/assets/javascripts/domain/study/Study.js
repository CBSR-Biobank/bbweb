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
    'queryStringService',
    'ConcurrencySafeEntity',
    'StudyStatus',
    'AnnotationType'
  ];

  /**
   *
   */
  function StudyFactory($q,
                        funutils,
                        biobankApi,
                        queryStringService,
                        ConcurrencySafeEntity,
                        StudyStatus,
                        AnnotationType) {

    var schema = {
      'id': 'Study',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string'},
        'version':         { 'type': 'integer', 'minimum': 0},
        'timeAdded':       { 'type': 'string'},
        'timeModified':    { 'type': 'string'},
        'name':            { 'type': 'string'},
        'description':     { 'type': 'string'},
        'annotationTypes': { 'type': 'array'},
        'status':          { 'type': 'string'}
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
        studyIds:        [],
        annotationTypes: [],
        status:          StudyStatus.DISABLED()
      };

      obj = obj || {};
      ConcurrencySafeEntity.call(this, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
      this.statusLabel = StudyStatus.label(this.status);

      this.annotationTypes = _.map(this.annotationTypes, function (annotationType) {
        return new AnnotationType(annotationType);
      });
    }

    Study.prototype = Object.create(ConcurrencySafeEntity.prototype);

    Study.prototype.constructor = Study;

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    Study.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        throw new Error('invalid object from server: ' + tv4.error);
      }

      if (!validAnnotationTypes(obj.annotationTypes)) {
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
      var validKeys = [
        'filter',
        'status',
        'sort',
        'page',
        'pageSize',
        'order'
      ];
      var url = uri();
      var paramsStr = '';

      options = options || {};

      paramsStr = queryStringService.param(options, function (value, key) {
        return _.contains(validKeys, key);
      });

      if (paramsStr) {
        url += paramsStr;
      }

      return biobankApi.get(url).then(function(reply) {
        var deferred = $q.defer();
        try {
          // reply is a paged result
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
        return asyncCreate(reply);
      });
    };

    Study.prototype.add = function () {
      var self = this,
          json = { name: this.name };
      angular.extend(json, funutils.pickOptional(self, 'description'));
      return biobankApi.post(uri(), json).then(function(reply) {
        return asyncCreate(reply);
      });
    };

    Study.prototype.updateName = function (name) {
      var self = this,
          json = {
            name:            name,
            expectedVersion: self.version
          };

      return biobankApi.post(uri('name', self.id), json).then(function(reply) {
        return asyncCreate(reply);
      });
    };

    Study.prototype.updateDescription = function (description) {
      var self = this,
          json = {
            expectedVersion: self.version
          };

      if (description) {
        json.description = description;
      }

      return biobankApi.post(uri('description', self.id), json).then(function(reply) {
        return asyncCreate(reply);
      });
    };

    Study.prototype.addAnnotationType = function (annotationType) {
      var self = this,
          json = _.extend({ expectedVersion: self.version }, _.omit(annotationType, 'uniqueId')),
          found;

      found = _.findWhere(self.annotationTypes,  { uniqueId: annotationType.uniqueId });
      if (found) {
        throw new Error('annotation type with ID already present: ' + annotationType.uniqueId);
      }

      return biobankApi.post(uri('pannottype', self.id), json).then(function(reply) {
        return asyncCreate(reply);
      });
    };

    Study.prototype.removeAnnotationType = function (annotationType) {
      var self = this,
          url = sprintf.sprintf('%s/%d/%s',
                                uri('pannottype', self.id), self.version, annotationType.uniqueId),
          found;

      found = _.findWhere(self.annotationTypes,  { uniqueId: annotationType.uniqueId });
      if (!found) {
        throw new Error('annotation type with ID not present: ' + annotationType.uniqueId);
      }

      return biobankApi.del(url).then(function () {
        return asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            annotationTypes: _.filter(self.annotationTypes, function(at) {
              return at.uniqueId !== annotationType.uniqueId;
            })
          }));
      });
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
        throw new Error('already disabled');
      }
      return changeState.call(this, 'unretire');
    };

    Study.prototype.isDisabled = function () {
      return (this.status === StudyStatus.DISABLED());
    };

    Study.prototype.isEnabled = function () {
      return (this.status === StudyStatus.ENABLED());
    };

    Study.prototype.isRetired = function () {
      return (this.status === StudyStatus.RETIRED());
    };

    function asyncCreate(obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        deferred.reject('invalid object from server: ' + tv4.error);
      } else if (!validAnnotationTypes(obj.annotationTypes)) {
        deferred.reject('invalid annotation types from server: ' + tv4.error);
      } else {
        deferred.resolve(new Study(obj));
      }

      return deferred.promise;
    }

    function validAnnotationTypes(annotationTypes) {
      var result;

      if (annotationTypes.length <= 0) {
        // there are no annotation types, nothing to validate
        return true;
      }
      result = _.find(annotationTypes, function (annotType) {
        return !AnnotationType.valid(annotType);
      });

      return _.isUndefined(result);
    }

    function changeState(state) {
      /* jshint validthis:true */
      var self = this,
          json = { expectedVersion: self.version };

      return biobankApi.post(uri(state, self.id), json);
    }

    function uri(/* path, studyId */) {
      var args = _.toArray(arguments),
          studyId,
          path;

      var result = '/studies';

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
