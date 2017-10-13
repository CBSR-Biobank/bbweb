/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

xdescribe('SpecimenLinkType', function() {

  var httpBackend,
      SpecimenLinkType,
      factory;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ServerReplyMixin, TestUtils) {
      _.extend(this, ServerReplyMixin);
      httpBackend      = this.$injector.get('$httpBackend');
      SpecimenLinkType = this.$injector.get('SpecimenLinkType');
      factory          = this.$injector.get('Factory');
      TestUtils.addCustomMatchers();
    });
  });

  function createEntities() {
    var study,
        processingType,
        specimenGroups,
        annotationTypes,
        sltFromServer;

    study          = factory.study();
    processingType = factory.processingType(study);
    sltFromServer  = factory.specimenLinkType(processingType);

    specimenGroups  = _.range(2).map(() => factory.specimenGroup(study));
    annotationTypes = _.range(2).map(() => factory.annotationType(study));

    return {
      study:           study,
      processingType:  processingType,
      sltFromServer:   sltFromServer,
      specimenGroups:  specimenGroups,
      annotationTypes: annotationTypes
    };
  }

  /*
   * Creates a {@link SpecimenLinkType} linked to specimen groups and annotation types.
   */
  function entitiesWithLinkedSpecimenLinkType() {
    var entities = createEntities(), slt;

    entities.sltFromServer = factory.specimenLinkType(
      entities.processingType, {
        inputGroup: entities.specimenGroups[0],
        outputGroup: entities.specimenGroups[1],
        annotationTypes: entities.annotationTypes
      });
    slt = new SpecimenLinkType(entities.sltFromServer, {
      studySpecimenGroups: entities.specimenGroups,
      studyAnnotationTypes: entities.annotationTypes
    });
    return _.extend(entities, { slt: slt });
  }

  it('constructor with no parameters has default values', function() {
    var slt = new SpecimenLinkType();

    expect(slt.isNew()).toBe(true);
    expect(slt.processingTypeId).toBe(null);
    expect(slt.expectedInputChange).toBe(null);
    expect(slt.expectedOutputChange).toBe(null);
    expect(slt.inputCount).toBe(null);
    expect(slt.outputCount).toBe(null);
    expect(slt.inputGroupId).toBe(null);
    expect(slt.outputGroupId).toBe(null);
    expect(slt.annotationTypeData).toBeArrayOfSize(0);
  });

  it('fails when creating from a non object', function() {
    expect(SpecimenLinkType.create(1))
      .toEqual(new Error('invalid object: has the correct keys'));
  });

  it('fails when creating from bad annotation type data', function() {
    var entities = createEntities();

    entities.sltFromServer = factory.specimenLinkType(
      entities.processingType, {
        inputGroup: entities.specimenGroups[0],
        outputGroup: entities.specimenGroups[1]
      });
    entities.sltFromServer.annotationTypeData = [{ 1: 'abc' }];

    expect(SpecimenLinkType.create(entities.sltFromServer))
      .toEqual(new Error('invalid object from server: bad annotation type data'));
  });

  it('can retrieve a specimen link type', function() {
    fail('needs implementation');
  });

  it('can list specimen link types', function() {
    fail('needs implementation');
  });

  it('can add a specimen link type', function() {
    fail('needs implementation');
  });

  it('can update a specimen link type without annotation types', function() {
    var entities = createEntities(), slt;

    entities.sltFromServer = factory.specimenLinkType(
      entities.processingType, {
        inputGroup: entities.specimenGroups[0],
        outputGroup: entities.specimenGroups[1]
      });

    slt = new SpecimenLinkType(entities.sltFromServer);

    updateSltSharedBehaviour(slt, entities.sltFromServer, entities.processingType.id);
  });

  it('can update a specimen link type with annotation type data', function() {
    var entities = entitiesWithLinkedSpecimenLinkType();
    updateSltSharedBehaviour(entities.slt, entities.sltFromServer, entities.processingType.id);
  });

  it('should remove a specimen link type', function() {
    var entities = entitiesWithLinkedSpecimenLinkType();

    httpBackend.expectDELETE('/studies/' +
                             entities.slt.processingTypeId + '/sltypes/' +
                             entities.slt.id + '/' +
                             entities.slt.version)
      .respond(this.reply(true));

    entities.slt.remove();
    httpBackend.flush();
  });

  it('isNew should be true for a specimen link type with no ID', function() {
    var entities = createEntities(),
        slt = new SpecimenLinkType(_.omit(entities.sltFromServer, 'id'));
    expect(slt.isNew()).toBe(true);
  });

  it('isNew should be false for a specimen link type that has an ID', function() {
    var entities = createEntities(),
        slt = new SpecimenLinkType(entities.sltFromServer);
    expect(slt.isNew()).toBe(false);
  });

  it('processing type ID matches', function() {
    var entities = createEntities(),
        slt = new SpecimenLinkType(entities.sltFromServer);
    expect(slt.processingTypeId).toBe(entities.processingType.id);
  });

  it('should be initialized with annotation type objects', function() {
    var entities = entitiesWithLinkedSpecimenLinkType();

    expect(entities.slt.inputGroup).toEqual(entities.specimenGroups[0]);
    expect(entities.slt.outputGroup).toEqual(entities.specimenGroups[1]);

    entities.annotationTypes.forEach((at) => {
      expect(entities.slt.getAnnotationTypeDataById(at.id).annotationType).toEqual(at);
    });
  });

  it('allAnnotationTypeDataIds returns valid results', function() {
    var entities = createEntities(), slt, ids;

    entities.sltFromServer = factory.specimenLinkType(
      entities.processingType, {
        inputGroup: entities.specimenGroups[0],
        outputGroup: entities.specimenGroups[1],
        annotationTypes: entities.annotationTypes
      });

    slt = new SpecimenLinkType(entities.sltFromServer, {
      studySpecimenGroups: entities.specimenGroups,
      studyAnnotationTypes: entities.annotationTypes
    });

    ids = slt.annotationTypeDataIds();
    expect(ids).toBeArrayOfSize(entities.annotationTypes.length);
    expect(ids).toContainAll(_.map(entities.annotationTypes, 'id'));
  });

  it('getAnnotationTypeDataById throws an error if there are no annotation type data items', function() {
    var entities = createEntities(),
        slt = new SpecimenLinkType(entities.sltFromServer);
    expect(function () { slt.getAnnotationTypeDataById(entities.annotationTypes[0].id); })
      .toThrow(new Error('no data items'));
  });

  it('getAnnotationTypeDataAsString should return an empty string if there are no annotation type data items',
     function() {
       var entities = createEntities(),
           slt = new SpecimenLinkType(entities.sltFromServer);
       expect(slt.getAnnotationTypeDataAsString()).toBeEmptyString();
     });

  // function sltToAddCommand(slt) {
  //   var cmd =  _.extend(_.pick(slt,
  //                              'processingTypeId',
  //                              'expectedInputChange',
  //                              'expectedOutputChange',
  //                              'inputCount',
  //                              'outputCount',
  //                              'inputGroupId',
  //                              'outputGroupId'),
  //                       funutils.pickOptional(slt,
  //                                             'inputContainerTypeId',
  //                                             'outputContainerTypeId'));
  //   cmd.annotationTypeData = slt.getAnnotationTypeData();
  //   return cmd;
  // }

  // function sltToUpdateCommand(ceventType) {
  //   return _.extend(sltToAddCommand(ceventType), {
  //     id: ceventType.id,
  //     expectedVersion: ceventType.version
  //   });
  // }

  function updateSltSharedBehaviour(/* slt, sltFromServer, processingTypeId */) {
    fail('needs implementation');
  }

});
