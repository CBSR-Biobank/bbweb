/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  extendedDomainEntities.$inject = [
    'ConcurrencySafeEntity',
    'AnnotationType',
    'StudyAnnotationType',
    'ParticipantAnnotationType',
    'Centre',
    'Location'
  ];

  function extendedDomainEntities(ConcurrencySafeEntity,
                                  AnnotationType,
                                  StudyAnnotationType,
                                  ParticipantAnnotationType,
                                  Centre,
                                  Location) {

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attrs* the attributes to compare agains. May be more than one.
     */
    function validateAttrs(/* obj, entity, attrs */) {
      var args = _.toArray(arguments);
      var obj = args.shift();
      var entity = args.shift();

      _.each(args, function(arg) {
        expect(obj[arg]).toEqual(entity[arg]);
      });
    };

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attrs* the attributes to compare agains. May be more than one.
     */
    function validateOptional(/* obj, entity, attrs */) {
      var args = _.toArray(arguments);
      var obj = args.shift();
      var entity = args.shift();

      _.each(args, function(attr) {
        if (obj[attr] !== null) {
          expect(obj[attr]).toEqual(entity[attr]);
        } else {
          expect(entity[attr]).toBeUndefined();
        }
      });
    };

    ConcurrencySafeEntity.prototype.compareToServerEntity = function (entity) {
      validateAttrs(this, entity, 'version');
    };

    AnnotationType.prototype.compareToServerEntity = function (annotType) {
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

    Centre.prototype.compareToServerEntity = function (centre) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, centre);
      validateAttrs(this, centre, 'name', 'Status');
      validateOptional(this, centre, 'description');
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

  return extendedDomainEntities;
});
