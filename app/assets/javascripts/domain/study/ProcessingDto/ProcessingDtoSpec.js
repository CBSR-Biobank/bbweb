/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

xdescribe('ProcessingDto', function() {
  var httpBackend,
      ProcessingDto,
      createEntities,
      factory;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ServerReplyMixin, TestUtils) {
      _.extend(this, ServerReplyMixin.prototype);

      httpBackend   = this.$injector.get('$httpBackend');
      ProcessingDto = this.$injector.get('ProcessingDto');
      factory  = this.$injector.get('Factory');
      TestUtils.addCustomMatchers();

      createEntities = setupEntities(this.$injector);
    });
  });

  function setupEntities(injector) {
    var AnnotationValueType = injector.get('AnnotationValueType');

    return create;

    //--
    function create() {
      var entities = {};
      entities.study = factory.study();
      entities.processingTypes = _.range(2).map(() => factory.processingType(entities.study));
      entities.specimenGroups  = _.range(2).map(() => factory.specimenGroup(entities.study));
      entities.specimenLinkAnnotationTypes = _.values(AnnotationValueType)
        .map((valueType) => factory.studyAnnotationType(entities.study, {
          valueType: valueType
        }));
      entities.specimenLinkAnnotationTypeIdsInUse = [ entities.specimenLinkAnnotationTypes[0] ];
      entities.specimenLinkTypes = _.range(2)
        .map((id) => factory.specimenLinkType(entities.processingTypes[id], {
          inputGroup: entities.specimenGroups[0],
          outputGorup: entities.specimenGroups[1],
          annotationTypes: entities.specimenLinkAnnotationTypes
        }));
      return entities;
    }
  }

  it('constructor with no parameters has default values', function() {
    var dto = new ProcessingDto();

    expect(dto.processingTypes).toBeArrayOfSize(0);
    expect(dto.processingTypes).toBeArrayOfSize(0);
    expect(dto.specimenLinkAnnotationTypes).toBeArrayOfSize(0);
    expect(dto.specimenGroups).toBeArrayOfSize(0);
  });

  it('fails when creating from a non object', function() {
    expect(ProcessingDto.create(1))
      .toEqual(new Error('invalid object from server: must be a map, has the correct keys'));
  });

  it('can retrieve a processing dto', function(done) {
    var ProcessingType             = this.$injector.get('ProcessingType'),
        SpecimenLinkType           = this.$injector.get('SpecimenLinkType'),
        SpecimenLinkAnnotationType = this.$injector.get('SpecimenLinkAnnotationType'),
        SpecimenGroup              = this.$injector.get('SpecimenGroup'),
        entities                   = createEntities();

    httpBackend.whenGET('/studies/' + entities.study.id + '/dto/processing')
      .respond(this.reply(entities));

    ProcessingDto.get(entities.study.id).then(function(dto) {
      compareToServerObj(dto, entities);

      dto.processingTypes.forEach((pt) => {
        expect(pt).toEqual(jasmine.any(ProcessingType));
      });

      dto.specimenLinkTypes.forEach((slt) => {
        expect(slt).toEqual(jasmine.any(SpecimenLinkType));
      });

      dto.specimenLinkAnnotationTypes.forEach((slat) => {
        expect(slat).toEqual(jasmine.any(SpecimenLinkAnnotationType));
      });

      dto.specimenGroups.forEach((sg) => {
        expect(sg).toEqual(jasmine.any(SpecimenGroup));
      });

      done();
    });
    httpBackend.flush();
  });

  function compareToServerObj(dto, serverObj) {
    expect(dto.processingTypes).toBeArrayOfSize(serverObj.processingTypes.length);
    expect(dto.specimenLinkTypes).toBeArrayOfSize(serverObj.specimenLinkTypes.length);
    expect(dto.specimenLinkAnnotationTypes).toBeArrayOfSize(serverObj.specimenLinkAnnotationTypes.length);
    expect(dto.specimenGroups).toBeArrayOfSize(serverObj.specimenGroups.length);

    expect(dto.processingTypes).toContainAll(serverObj.processingTypes);
    expect(dto.specimenLinkTypes).toContainAll(serverObj.specimenLinkTypes);
    expect(dto.specimenLinkAnnotationTypes).toContainAll(serverObj.specimenLinkAnnotationTypes);
    expect(dto.specimenGroups).toContainAll(serverObj.specimenGroups);

    dto.specimenLinkTypes.forEach((slt) => {
      expect(slt.annotationTypeDataIds()).toBeArrayOfSize(serverObj.specimenLinkAnnotationTypes.length);
      expect(slt.annotationTypeDataIds()).toContainAll(_.map(serverObj.specimenLinkAnnotationTypes, 'id'));
    });
  }

});
