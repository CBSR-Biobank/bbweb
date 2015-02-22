/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  var module = angular.module('biobank.test.extendDomainEntities', []);

  module.service('extendDomainEntities', extendDomainEntities);

  extendDomainEntities.$inject = [
    'ConcurrencySafeEntity',
    'AnnotationType',
    'StudyAnnotationType',
    'ParticipantAnnotationType',
    'Location'
  ];

  function extendDomainEntities(ConcurrencySafeEntity,
                                AnnotationType,
                                StudyAnnotationType,
                                ParticipantAnnotationType,
                                Location) {

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attr* the attributes to compare agains. May be more than one.
     */
    function validateAttrs(obj, entity, attr) {
      var args = Array.prototype.slice.call(arguments, 2);
      _.each(args, function(arg) {
        expect(obj[arg]).toEqual(entity[arg]);
      });
    };

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attr* the attributes to compare agains. May be more than one.
     */
    function validateOptional(obj, entity, attr) {
      var args = Array.prototype.slice.call(arguments, 2);
      _.each(args, function(arg) {
        if (obj[arg] !== null) {
          expect(obj[arg]).toEqual(entity[arg]);
        } else {
          expect(entity[arg]).toBeUndefined();
        }
      });
    };

    ConcurrencySafeEntity.prototype.compareToServerEntity = function (entity) {
      validateAttrs(this, entity, 'version');
    };

    AnnotationType.prototype.compareToServerEntity = function (annotType) {
      var self = this;
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, annotType);
      validateAttrs(this, annotType, 'name', 'valueType', 'options');
      validateOptional(this, annotType, 'description', 'maxValueCount');
    };

    StudyAnnotationType.prototype.compareToServerEntity = function (annotType) {
      AnnotationType.prototype.compareToServerEntity.call(this, annotType);
      validateAttrs(this, annotType, 'studyId');
    };

    ParticipantAnnotationType.prototype.compareToServerEntity = function (annotType) {
      StudyAnnotationType.prototype.compareToServerEntity.call(this, annotType);
      validateAttrs(this, annotType, 'required');
    };

    Location.prototype.compareToServerEntity = function (location) {
      validateAttrs(this,
                    location,
                    'name',
                    'street',
                    'city',
                    'province',
                    'postalCode',
                    'countryIsoCode');
      validateOptional(this, location, 'id', 'poBoxNumber');
    };
  }

});
