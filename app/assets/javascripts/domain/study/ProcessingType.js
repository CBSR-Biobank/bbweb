define(['underscore'], function(_) {
  'use strict';

  ProcessingTypeFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ConcurrencySafeEntity'
  ];

  function ProcessingTypeFactory(funutils,
                                 validationService,
                                 biobankApi,
                                 ConcurrencySafeEntity) {

    var requiredKeys = ['id', 'studyId', 'version', 'timeAdded', 'name', 'enabled'];

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('must be a map', _.isObject),
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      _.identity);

    /**
     *
     */
    function ProcessingType(obj) {
      var self = this,
          defaults = {
            studyId:     null,
            name:        '',
            description: null,
            enabled:     false
          };

      obj = obj || {};
      ConcurrencySafeEntity.call(self, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    ProcessingType.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    ProcessingType.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }
      return new ProcessingType(obj);
    };

    ProcessingType.get = function(studyId, id) {
      return biobankApi.get(uri(studyId) + '?procTypeId=' + id)
        .then(function(reply) {
          return ProcessingType.create(reply);
        });
    };

    ProcessingType.list = function(studyId) {
      return biobankApi.get(uri(studyId)).then(function(reply) {
        return _.map(reply, function (cet) {
          return ProcessingType.create(cet);
        });
      });
    };

    ProcessingType.prototype.addOrUpdate = function (annotationTypes) {
      var self = this,
          cmd = _.extend(_.pick(self,
                                'studyId',
                                'name',
                                'enabled'),
                         funutils.pickOptional(self, 'description'));

      return addOrUpdateInternal().then(function(reply) {
        return ProcessingType.create(reply);
      });

      // --

      function addOrUpdateInternal() {
        if (self.isNew()) {
          return biobankApi.post(uri(self.studyId), cmd);
        }
        _.extend(cmd, { id: self.id, expectedVersion: self.version });
        return biobankApi.put(uri(self.studyId, self.id), cmd);
      }
    };

    ProcessingType.prototype.remove = function () {
      return biobankApi.del(uri(this.studyId, this.id, this.version));
    };

    function uri(studyId, ceventTypeId, version) {
      var result = '/studies';
      if (arguments.length <= 0) {
        throw new Error('study id not specified');
      } else {
        result += '/' + studyId + '/proctypes';

        if (arguments.length > 1) {
          result += '/' + ceventTypeId;
        }

        if (arguments.length > 2) {
          result += '/' + version;
        }
      }
      return result;
    }

    return ProcessingType;
  }

  return ProcessingTypeFactory;

});

