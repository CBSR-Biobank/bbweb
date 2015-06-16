/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  extendedDomainEntities.$inject = [
    'ConcurrencySafeEntity',
    'AnnotationType',
    'Study',
    'StudyAnnotationType',
    'ParticipantAnnotationType',
    'Participant',
    'Centre',
    'Location'
  ];

  function extendedDomainEntities(ConcurrencySafeEntity,
                                  AnnotationType,
                                  Study,
                                  StudyAnnotationType,
                                  ParticipantAnnotationType,
                                  Participant,
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

    AnnotationType.prototype.compareToServerEntity = function (annotationType) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, annotationType);
      validateAttrs(this, annotationType, 'name', 'valueType', 'options');
      validateOptional(this, annotationType, 'description', 'maxValueCount');
    };

    StudyAnnotationType.prototype.compareToServerEntity = function (annotationType) {
      AnnotationType.prototype.compareToServerEntity.call(this, annotationType);
      validateAttrs(this, annotationType, 'studyId');
    };

    ParticipantAnnotationType.prototype.compareToServerEntity = function (annotationType) {
      StudyAnnotationType.prototype.compareToServerEntity.call(this, annotationType);
      validateAttrs(this, annotationType, 'required');
    };

    Study.prototype.compareToServerEntity = function (study) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, study);
      validateAttrs(this, study, 'name', 'Status');
      validateOptional(this, study, 'description');
    };

    Participant.prototype.compareToServerEntity = function (participant) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, participant);
      validateAttrs(this, participant, 'studyId', 'uniqueId', 'annotations');
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
