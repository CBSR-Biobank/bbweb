/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../annotationTypeDataSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeDataSharedSpec) {
  'use strict';

  describe('CollectionEventType', function() {

    var httpBackend,
        CollectionEventType,
        cetFromServer,
        fakeEntities,
        study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _CollectionEventType_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend         = $httpBackend;
      CollectionEventType = _CollectionEventType_;
      fakeEntities        = fakeDomainEntities;

      study = fakeEntities.study();

      study.specimenGroups = _.map(_.range(2), function() {
        return fakeEntities.specimenGroup(study);
      });

      study.annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });

      study.specimenGroupsById = _.indexBy(study.specimenGroups, 'id');
      study.annotationTypesById = _.indexBy(study.annotationTypes, 'id');

      cetFromServer = fakeEntities.collectionEventType(study);
    }));

    it('constructor with no parameters has default values', function() {
      var ceventType = new CollectionEventType();

      expect(ceventType.isNew()).toBe(true);
      expect(ceventType.studyId).toBe(null);
      expect(ceventType.name).toBe('');
      expect(ceventType.recurring).toBe(false);
      expect(ceventType.specimenGroupData).toBeArrayOfSize(0);
      expect(ceventType.annotationTypeData).toBeArrayOfSize(0);
    });

    it('fails when creating from a non object', function() {
      expect(CollectionEventType.create(1))
        .toEqual(new Error('invalid object: has the correct keys'));
    });

    it('fails when creating from bad specimen group data', function() {
      cetFromServer = fakeEntities.collectionEventType(study);
      cetFromServer.specimenGroupData.push({ 1: 'abc' });

      expect(CollectionEventType.create(cetFromServer))
        .toEqual(new Error('invalid object from server: bad specimen group data'));
    });

    it('fails when creating from bad annotation type data', function() {
      cetFromServer = fakeEntities.collectionEventType(study);
      cetFromServer.annotationTypeData.push({ 1: 'abc' });

      expect(CollectionEventType.create(cetFromServer))
        .toEqual(new Error('invalid object from server: bad annotation type data'));
    });

    it('has valid values when creating from server response', function() {
      var ceventType;

      cetFromServer = fakeEntities.collectionEventType(study, {
        specimenGroups: study.specimenGroups,
        annotationTypes: study.annotationTypes
      });

      ceventType = CollectionEventType.create(cetFromServer);
      ceventType.compareToServerEntity(cetFromServer);
    });

    it('can retrieve a collection event type', function(done) {
      httpBackend.whenGET('/studies/' + study.id + '/cetypes?cetId=' + cetFromServer.id)
        .respond(serverReply(cetFromServer));

      CollectionEventType.get(study.id, cetFromServer.id).then(function(cet) {
        cet.compareToServerEntity(cetFromServer);
        done();
      });
      httpBackend.flush();
    });

    it('can list collection event types', function(done) {
      httpBackend.whenGET('/studies/' + study.id + '/cetypes')
        .respond(serverReply([ cetFromServer ]));
      CollectionEventType.list(study.id).then(function(list) {
        _.each(list, function (cet) {
          cet.compareToServerEntity(cetFromServer);
        });
        done();
      });
      httpBackend.flush();
    });

    it('can add a collection event type', function() {
      var ceventType = new CollectionEventType(_.omit(cetFromServer, 'id')),
          cmd = ceventTypeToAddCommand(ceventType);

      httpBackend.expectPOST('/studies/' + study.id + '/cetypes', cmd)
        .respond(201, serverReply(cetFromServer));

      ceventType.addOrUpdate().then(function(cet) {
        cet.compareToServerEntity(cetFromServer);
      });
      httpBackend.flush();
    });

    it('can update a collection event type', function() {
      var ceventType = new CollectionEventType(cetFromServer);
      updateCeventTypeSharedBehaviour(ceventType, cetFromServer, study.id);
    });

    it('can update a collection event type with specimen group data and annotation type data', function() {
      var ceventType;

      cetFromServer = fakeEntities.collectionEventType(study, {
        specimenGroups: study.specimenGroups,
        annotationTypes: study.annotationTypes
      });

      ceventType = new CollectionEventType(cetFromServer, {
        studySpecimenGroups: study.specimenGroups,
        studyAnnotationTypes: study.annotationTypes
      });
      updateCeventTypeSharedBehaviour(ceventType, cetFromServer, study.id);
    });

    function updateCeventTypeSharedBehaviour(ceventType, cetFromServer, studyId) {
      var cmd = ceventTypeToUpdateCommand(ceventType);
      httpBackend.expectPUT('/studies/' + studyId + '/cetypes/' + ceventType.id, cmd)
        .respond(201, serverReply(cetFromServer));

      ceventType.addOrUpdate().then(function(cet) {
        cet.compareToServerEntity(cetFromServer);
      });
      httpBackend.flush();
    }

    it('should remove a collection event type', function() {
      var ceventType;

      cetFromServer = fakeEntities.collectionEventType(study, {
        specimenGroups: study.specimenGroups,
        annotationTypes: study.annotationTypes
      });

      ceventType = new CollectionEventType(cetFromServer, {
        studySpecimenGroups: study.specimenGroups,
        studyAnnotationTypes: study.annotationTypes
      });

      httpBackend.expectDELETE('/studies/' + study.id + '/cetypes/' + ceventType.id + '/' + ceventType.version )
        .respond(201, serverReply(true));

      ceventType.remove();
      httpBackend.flush();
    });

    it('isNew should be true for a collection event type with no ID', function() {
      var cetNoId = _.omit(cetFromServer, 'id');
      var cet = new CollectionEventType(cetNoId);
      expect(cet.isNew()).toBe(true);
    });

    it('study ID matches the study', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(cet.studyId).toBe(study.id);
    });

    it('isNew should be false for a collection event type that has an ID', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(cet.isNew()).toBe(false);
    });

    it('should be initialized with specimen group and annotation type server objects', function() {
      var cetFromServer, cet;

      cetFromServer = fakeEntities.collectionEventType(study, {
        specimenGroups: study.specimenGroups,
        annotationTypes: study.annotationTypes
      });

      cet = new CollectionEventType(cetFromServer, {
        studySpecimenGroups: study.specimenGroups,
        studyAnnotationTypes: study.annotationTypes
      });

      _.each(study.specimenGroups, function(sg) {
        expect(cet.getSpecimenGroupDataById(sg.id).specimenGroup).toEqual(sg);
      });

      _.each(study.annotationTypes, function(at) {
        expect(cet.getAnnotationTypeDataById(at.id).annotationType).toEqual(at);
      });
    });

    it('should return the correct size for specimen group data', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });
      expect(cet.getSpecimenGroupData()).toBeArrayOfSize(cetFromServer.specimenGroupData.length);
    });

    it('should return the specimen group IDs', function() {
      var specimenGroupIds = _.pluck(study.specimenGroups, 'id');

      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });
      expect(cet.specimenGroupDataIds()).toBeArrayOfSize(cetFromServer.specimenGroupData.length);

      _.each(cet.specimenGroupDataIds(), function(id) {
        expect(specimenGroupIds).toContain(id);
      });
    });

    it('should return the specimen group IDs', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });

      _.each(cetFromServer.specimenGroupData, function(serverSgDataItem) {
        var sgDataItem = cet.getSpecimenGroupDataById(serverSgDataItem.specimenGroupId);
        expect(serverSgDataItem.specimenGroupId).toBe(sgDataItem.specimenGroupId);
        expect(serverSgDataItem.maxCount).toBe(sgDataItem.maxCount);
        expect(serverSgDataItem.amount).toBe(sgDataItem.amount);
      });
    });

    it('should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(function () { cet.getSpecimenGroupDataById(study.specimenGroups[0].id); })
        .toThrow(new Error('no data items'));
    });

    it('getSpecimenGroupDataById should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(function () { cet.getSpecimenGroupDataById(study.specimenGroups[0].id); })
        .toThrow(new Error('no data items'));
    });

    it('returns specimen group data as a string', function() {
      var cetFromServer = fakeEntities.collectionEventType(study, { specimenGroups: study.specimenGroups});
      var cet = new CollectionEventType(cetFromServer,
                                        { studySpecimenGroups: study.specimenGroups });
      expect(cet.getSpecimenGroupsAsString()).toEqual(
        getSpecimenGroupsAsStringFromServerData(cetFromServer, study.specimenGroups).join(', '));

      function getSpecimenGroupsAsStringFromServerData(cetFromServer, specimenGroups) {
        return _.map(cetFromServer.specimenGroupData, function (dataItem) {
          var sg = _.findWhere(specimenGroups, { id: dataItem.specimenGroupId });
          if (_.isUndefined(sg)) {
            throw new Error('specimen group with id not found: ' + dataItem.specimenGroupId);
          }
          return sg.name + ' (' + dataItem.maxCount + ', ' + dataItem.amount +
            ' ' + sg.units + ')';
        });
      }
    });

    it('getSpecimenGroupsAsString should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(function () { cet.getSpecimenGroupsAsString(); })
        .toThrow(new Error('no data items'));
    });

    it('should return the annotation type IDs', function() {
      var annotationTypeIds = _.pluck(study.annotationTypes, 'id');

      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { annotationTypes: study.annotationTypes });
      var cet = new CollectionEventType(
        cetFromServer,
        { studyAnnotationTypes: study.annotationTypes });
      expect(cet.annotationTypeDataIds()).toBeArrayOfSize(cetFromServer.annotationTypeData.length);

      _.each(cet.annotationTypeDataIds(), function(id) {
        expect(annotationTypeIds).toContain(id);
      });
    });

    it('should return the annotation type IDs', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { annotationTypes: study.annotationTypes });
      var cet = new CollectionEventType(
        cetFromServer,
        { studyAnnotationTypes: study.annotationTypes });

      _.each(cetFromServer.annotationTypeData, function(serverAtDataItem) {
        var atDataItem = cet.getAnnotationTypeDataById(serverAtDataItem.annotationTypeId);
        expect(serverAtDataItem.annotationTypeId).toBe(atDataItem.annotationTypeId);
        expect(serverAtDataItem.maxCount).toBe(atDataItem.maxCount);
        expect(serverAtDataItem.amount).toBe(atDataItem.amount);
      });
    });

    it('getAnnotationTypeData throws an error if there are no annotation type data items', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(function () { cet.getAnnotationTypeDataById(study.annotationTypes[0].id); })
        .toThrow(new Error('no data items'));
    });

    it('getAnnotationTypeAsString returns an empty string if there are no annotation type data items', function() {
      var cet = new CollectionEventType(cetFromServer);
      expect(cet.getAnnotationTypeDataAsString()).toBeEmptyString();
    });

    describe('uses annotation type set correctly', function () {
      var study, annotationTypes, cetFromServer, cet;
      var context = {};

      beforeEach(inject(function(CollectionEventType,
                                 fakeDomainEntities) {

        study = fakeDomainEntities.study();
        annotationTypes = _.map(_.range(2), function() {
          return fakeDomainEntities.annotationType(study);
        });

        cetFromServer = fakeEntities.collectionEventType(study, { annotationTypes: annotationTypes});
        cetFromServer.annotationTypeData[0].required = true;
        cetFromServer.annotationTypeData[0].required = false;

        cet = new CollectionEventType(cetFromServer,
                                      { studyAnnotationTypes: annotationTypes });
        context.parentObj = cet;
        context.annotationTypes = annotationTypes;
        context.fakeEntities = fakeEntities;
      }));

      annotationTypeDataSharedSpec(context);
    });

    function serverReply(obj) {
      return { status: 'success', data: obj };
    }

    function ceventTypeToAddCommand(ceventType) {
      return {
        studyId:            ceventType.studyId,
        name:               ceventType.name,
        description:        ceventType.description,
        recurring:          ceventType.recurring,
        specimenGroupData:  ceventType.getSpecimenGroupData(),
        annotationTypeData: ceventType.getAnnotationTypeData()
      };
    }

    function ceventTypeToUpdateCommand(ceventType) {
      return _.extend(ceventTypeToAddCommand(ceventType), {
        id: ceventType.id,
        expectedVersion: ceventType.version
      });
    }
  });

});
